#include <stdlib.h>
#include <stdio.h>
#include <time.h>

#define CL_TARGET_OPENCL_VERSION 120
#include "ocl_boiler.h"

size_t gws_align_init;
size_t gws_align_transpose;

cl_event matinit(cl_kernel matinit_k, cl_command_queue que,
	cl_mem d_A, cl_int nrows, cl_int ncols)
{
	const size_t gws[] = { round_mul_up(ncols, gws_align_init), nrows };
	cl_event matinit_evt;
	cl_int err;

	cl_uint i = 0;
	err = clSetKernelArg(matinit_k, i++, sizeof(d_A), &d_A);
	ocl_check(err, "set matinit arg", i-1);
	err = clSetKernelArg(matinit_k, i++, sizeof(nrows), &nrows);
	ocl_check(err, "set matinit arg", i-1);
	err = clSetKernelArg(matinit_k, i++, sizeof(ncols), &ncols);
	ocl_check(err, "set matinit arg", i-1);

	err = clEnqueueNDRangeKernel(que, matinit_k, 2,
		NULL, gws, NULL,
		0, NULL, &matinit_evt);

	ocl_check(err, "enqueue matinit");

	return matinit_evt;
}

cl_event transpose(cl_kernel transpose_k,int _lws, int vec, cl_command_queue que,
	cl_mem d_T, cl_mem d_A, cl_int nrows_T, cl_int ncols_T, cl_event init_evt)
{
	const size_t lws[] = { _lws/4, _lws/4 };
	const size_t gws[] = { round_mul_up(ncols_T/vec, _lws/4),round_mul_up( nrows_T/vec,_lws/4) };
	cl_event trans_evt;
	cl_int err;
	cl_uint ncols_quarts=ncols_T/4;
	cl_uint nrows_quarts=nrows_T/4;
	cl_uint i = 0;
	err = clSetKernelArg(transpose_k, i++, sizeof(d_T), &d_T);
	err |= clSetKernelArg(transpose_k, i++, sizeof(d_A), &d_A);
	err |= clSetKernelArg(transpose_k, i++, sizeof(nrows_quarts), &nrows_quarts);
	err |= clSetKernelArg(transpose_k, i++, sizeof(ncols_quarts), &ncols_quarts);
	err |= clSetKernelArg(transpose_k, i++, (_lws*_lws/4)*sizeof(cl_int),NULL );
	
	ocl_check(err, "set transpose arg", i-1);

	err = clEnqueueNDRangeKernel(que, transpose_k, 2,
		NULL, gws, NULL,
		1, &init_evt, &trans_evt);

	ocl_check(err, "enqueue tranpose");

	return trans_evt;
}

void verify(const int *h_T, int nrows_T, int ncols_T)
{
	for (int r = 0; r < nrows_T; ++r) {
		for (int c = 0; c < ncols_T; ++c) {
			if (h_T[r*ncols_T+c] != c-r) {
				fprintf(stderr, "mismatch @ (%d, %d) : %d != %d\n",
					r, c, h_T[r*ncols_T+c], c-r);
				exit(3);
			}
		}
	}
}


int main(int argc, char *argv[])
{
	if (argc <= 2) {
		fprintf(stderr, "specify number of rows and columns\n");
		exit(1);
	}

	const int nrows_A = atoi(argv[1]);
	const int ncols_A = atoi(argv[2]);
	const int vec = argc >= 4 ? atoi(argv[3]) : 1;
	const int lws = argc == 5 ? atoi(argv[4]) : gws_align_init;

	const char *trans_kernel_name = NULL;

	if (vec > 1) {
		if (nrows_A & (vec-1)) {
			fprintf(stderr, "nrows %d non è multiplo di %d\n", nrows_A, vec);
			exit(1);
		}
		if (ncols_A & (vec-1)) {
			fprintf(stderr, "ncols %d non è multiplo di %d\n", ncols_A, vec);
			exit(1);
		}

	}

	const size_t memsize = nrows_A*ncols_A*sizeof(cl_int);
	const int nrows_T = ncols_A;
	const int ncols_T = nrows_A;

	cl_platform_id p = select_platform();
	cl_device_id d = select_device(p);
	cl_context ctx = create_context(p, d);
	cl_command_queue que = create_queue(ctx, d);
	cl_program prog = create_program("transpose.ocl", ctx, d);
	cl_int err;

	cl_kernel matinit_k = clCreateKernel(prog, "matinit", &err);
	ocl_check(err, "create kernel matinit");

	cl_kernel transpose_k = clCreateKernel(prog, trans_kernel_name, &err);
	ocl_check(err, "create kernel transpose");

	/* get information about the preferred work-group size multiple */
	err = clGetKernelWorkGroupInfo(matinit_k, d,
		CL_KERNEL_PREFERRED_WORK_GROUP_SIZE_MULTIPLE,
		sizeof(gws_align_init), &gws_align_init, NULL);
	ocl_check(err, "preferred wg multiple for init");
	err = clGetKernelWorkGroupInfo(transpose_k, d,
		CL_KERNEL_PREFERRED_WORK_GROUP_SIZE_MULTIPLE,
		sizeof(gws_align_transpose), &gws_align_transpose, NULL);
	ocl_check(err, "preferred wg multiple for transpose");


	cl_mem d_A = NULL, d_T = NULL;

	d_A = clCreateBuffer(ctx,
		CL_MEM_READ_WRITE | CL_MEM_HOST_NO_ACCESS,
		memsize, NULL,
		&err);
	ocl_check(err, "create buffer d_A");
	d_T = clCreateBuffer(ctx,
		CL_MEM_WRITE_ONLY | CL_MEM_HOST_READ_ONLY,
		memsize, NULL,
		&err);
	ocl_check(err, "create buffer d_T");

	cl_event init_evt, trans_evt, read_evt;

	init_evt = matinit(matinit_k, que, d_A, nrows_A, ncols_A);
	trans_evt = transpose(transpose_k, lws, vec, que, d_T, d_A, nrows_T, ncols_T, init_evt);

	cl_int *h_T = clEnqueueMapBuffer(que, d_T, CL_FALSE,
		CL_MAP_READ,
		0, memsize,
		1, &trans_evt, &read_evt, &err);

	clWaitForEvents(1, &read_evt);

	verify(h_T, nrows_T, ncols_T);

	const double runtime_init_ms = runtime_ms(init_evt);
	const double runtime_trans_ms = runtime_ms(trans_evt);
	const double runtime_read_ms = runtime_ms(read_evt);

	const double init_bw_gbs = 1.0*memsize/1.0e6/runtime_init_ms;
	const double trans_bw_gbs = 2.0*memsize/1.0e6/runtime_trans_ms;
	const double read_bw_gbs = memsize/1.0e6/runtime_read_ms;

	printf("init: %dx%d int in %gms: %g GB/s %g GE/s\n",
		nrows_A, ncols_A, runtime_init_ms, init_bw_gbs, nrows_A*ncols_A/1.0e6/runtime_init_ms);
	printf("trans: %dx%d int in %gms: %g GB/s %g GE/s\n",
		nrows_T, ncols_T, runtime_trans_ms, trans_bw_gbs, nrows_T*ncols_T/1.0e6/runtime_trans_ms);
	printf("read: %dx%d int in %gms: %g GB/s %g GE/s\n",
		nrows_T, ncols_T, runtime_read_ms, read_bw_gbs, nrows_T*ncols_T/1.0e6/runtime_read_ms);

	err = clEnqueueUnmapMemObject(que, d_T, h_T,
		0, NULL, NULL);
	ocl_check(err, "unmap T");

	clReleaseMemObject(d_T);
	clReleaseMemObject(d_A);
	clReleaseKernel(transpose_k);
	clReleaseKernel(matinit_k);
	clReleaseProgram(prog);
	clReleaseCommandQueue(que);
	clReleaseContext(ctx);
}
