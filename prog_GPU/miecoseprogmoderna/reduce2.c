#include <stdlib.h>
#include <stdio.h>
#include <time.h>

#define CL_TARGET_OPENCL_VERSION 120
#include "ocl_boiler.h"

size_t gws_align_init;
size_t gws_align_sum;

cl_event vecinit(cl_kernel vecinit_k, cl_command_queue que,
	cl_mem d_v1, cl_int nels)
{
	const size_t gws[] = { round_mul_up(nels, gws_align_init) };
	printf("init gws: %d | %zu = %zu\n", nels, gws_align_init, gws[0]);
	cl_event vecinit_evt;
	cl_int err;

	cl_uint i = 0;
	err = clSetKernelArg(vecinit_k, i++, sizeof(d_v1), &d_v1);
	ocl_check(err, "set vecinit arg", i-1);
	err = clSetKernelArg(vecinit_k, i++, sizeof(nels), &nels);
	ocl_check(err, "set vecinit arg", i-1);

	err = clEnqueueNDRangeKernel(que, vecinit_k, 1,
		NULL, gws, NULL,
		0, NULL, &vecinit_evt);

	ocl_check(err, "enqueue vecinit");

	return vecinit_evt;
}

cl_event reduce2(cl_kernel reduce2_k, cl_command_queue que,
	cl_mem d_vsum, cl_mem d_v1, cl_int npairs,
	cl_event init_evt)
{
	const size_t gws[] = { round_mul_up(npairs, gws_align_sum) };
	printf("sum gws: %d | %zu = %zu\n", npairs, gws_align_sum, gws[0]);
	cl_event reduce2_evt;
	cl_int err;

	cl_uint i = 0;
	err = clSetKernelArg(reduce2_k, i++, sizeof(d_vsum), &d_vsum);
	ocl_check(err, "set reduce2 arg", i-1);
	err = clSetKernelArg(reduce2_k, i++, sizeof(d_v1), &d_v1);
	ocl_check(err, "set reduce2 arg", i-1);
	err = clSetKernelArg(reduce2_k, i++, sizeof(npairs), &npairs);
	ocl_check(err, "set reduce2 arg", i-1);

	err = clEnqueueNDRangeKernel(que, reduce2_k, 1,
		NULL, gws, NULL,
		1, &init_evt, &reduce2_evt);

	ocl_check(err, "enqueue reduce2");

	return reduce2_evt;
}

void verify(const float vsum, int nels)
{
	unsigned long long expected = (unsigned long long)(nels+1)*(nels/2);
	if (vsum != expected) {
		fprintf(stderr, "mismatch @ %g != %d\n", vsum, nels);
		exit(3);
	}
}


int main(int argc, char *argv[])
{
	if (argc <= 1) {
		fprintf(stderr, "specify number of elements\n");
		exit(1);
	}

	const int nels = atoi(argv[1]);
	const size_t memsize = nels*sizeof(cl_int);

	cl_platform_id p = select_platform();
	cl_device_id d = select_device(p);
	cl_context ctx = create_context(p, d);
	cl_command_queue que = create_queue(ctx, d);
	cl_program prog = create_program("reduce2.ocl", ctx, d);
	cl_int err;

	cl_kernel vecinit_k = clCreateKernel(prog, "vecinit", &err);
	ocl_check(err, "create kernel vecinit");
	cl_kernel reduce2_k = clCreateKernel(prog, "reduce2", &err);
	ocl_check(err, "create kernel reduce2");

	/* get information about the preferred work-group size multiple */
	err = clGetKernelWorkGroupInfo(vecinit_k, d,
		CL_KERNEL_PREFERRED_WORK_GROUP_SIZE_MULTIPLE,
		sizeof(gws_align_init), &gws_align_init, NULL);
	ocl_check(err, "preferred wg multiple for init");
	err = clGetKernelWorkGroupInfo(reduce2_k, d,
		CL_KERNEL_PREFERRED_WORK_GROUP_SIZE_MULTIPLE,
		sizeof(gws_align_sum), &gws_align_sum, NULL);
	ocl_check(err, "preferred wg multiple for sum");

	cl_mem d_v1 = NULL, d_v2 = NULL;

	d_v1 = clCreateBuffer(ctx,
		CL_MEM_READ_WRITE | CL_MEM_HOST_READ_ONLY,
		memsize, NULL,
		&err);
	ocl_check(err, "create buffer d_v1");

	d_v2 = clCreateBuffer(ctx,
		CL_MEM_READ_WRITE | CL_MEM_HOST_READ_ONLY,
		memsize/2, NULL,
		&err);
	ocl_check(err, "create buffer d_v2");

	// compute number of passes needed to reduce the whole array
	int npass = 0;
	{
		int tmp = nels;
		while (tmp >>= 1) ++npass;
	}
	printf("expected %d passes\n", npass);
	fflush(stdout);

	cl_event init_evt, reduce_evt[npass+1], read_evt;

	init_evt = vecinit(vecinit_k, que, d_v1, nels);

	reduce_evt[0] = init_evt;
	int nels_current = nels;
	for (int p = 0; p < npass; ++p) {
		int npairs = nels_current/2;
		if (npairs == 0) ocl_check(CL_INVALID_ARG_INDEX, "sbagghiasti");
		reduce_evt[p+1] = reduce2(reduce2_k, que, d_v2, d_v1, npairs, reduce_evt[p]);
		cl_mem t = d_v1;
		d_v1 = d_v2;
		d_v2 = t;
		nels_current = npairs;
	}

	float risultato;
	err = clEnqueueReadBuffer(que, d_v1, CL_TRUE, 0, sizeof(risultato), &risultato,
		1, reduce_evt + npass, &read_evt);
	ocl_check(err, "read result");

	verify(risultato, nels);

	const double runtime_init_ms = runtime_ms(init_evt);
	const double runtime_read_ms = runtime_ms(read_evt);

	const double init_bw_gbs = 1.0*memsize/1.0e6/runtime_init_ms;
	const double read_bw_gbs = sizeof(float)/1.0e6/runtime_read_ms;

	printf("init: %d int in %gms: %g GB/s %g GE/s\n",
		nels, runtime_init_ms, init_bw_gbs, nels/1.0e6/runtime_init_ms);

	nels_current = nels;
	size_t mem_current = memsize;
	for (int p = 0; p < npass; ++p) {
		const double runtime_pass_ms = runtime_ms(reduce_evt[p+1]);
		const double pass_bw_gbs = (1.5*mem_current)/1.0e6/runtime_pass_ms;
		printf("reduce%04d : %d float in %gms: %g GB/s %g GE/s\n", p,
			nels_current, runtime_pass_ms, pass_bw_gbs,
			nels_current/1.0e6/runtime_pass_ms);
		nels_current /= 2;
		mem_current /= 2;
	}

	const double runtime_reduction_ms = total_runtime_ms(reduce_evt[1], reduce_evt[npass]);
	printf("reduce : %d float in %gms: %g GE/s\n",
		nels, runtime_reduction_ms, nels/1.0e6/runtime_reduction_ms);

	printf("read: 1 int in %gms: %g GB/s %g GE/s\n",
		runtime_read_ms, read_bw_gbs, 1.0/1.0e6/runtime_read_ms);

	clReleaseMemObject(d_v2);
	clReleaseMemObject(d_v1);
	clReleaseKernel(reduce2_k);
	clReleaseKernel(vecinit_k);
	clReleaseProgram(prog);
	clReleaseCommandQueue(que);
	clReleaseContext(ctx);
}
