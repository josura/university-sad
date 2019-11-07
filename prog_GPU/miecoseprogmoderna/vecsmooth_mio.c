#include <stdlib.h>
#include <stdio.h>
#include <time.h>
#include <unistd.h>
#define CL_TARGET_OPENCL_VERSION 120
#include "ocl_boiler.h"

size_t gws_align_init;
size_t gws_align_smooth;

cl_event vecinit(cl_kernel vecinit_k, cl_command_queue que, int lws_cli,
	cl_mem d_v1, cl_int nels)
{
	const size_t lws[] = { lws_cli };
	const size_t gws[] = { round_mul_up(nels, lws[0]) };
	printf("init gws: %d | %d | %zu = %zu\n", nels, lws_cli, gws_align_init, gws[0]);
	cl_event vecinit_evt;
	cl_int err;

	cl_uint i = 0;
	err = clSetKernelArg(vecinit_k, i++, sizeof(d_v1), &d_v1);
	ocl_check(err, "set vecinit arg", i-1);
	err = clSetKernelArg(vecinit_k, i++, sizeof(nels), &nels);
	ocl_check(err, "set vecinit arg", i-1);

	err = clEnqueueNDRangeKernel(que, vecinit_k, 1,
		NULL, gws, lws,
		0, NULL, &vecinit_evt);

	ocl_check(err, "enqueue vecinit");

	return vecinit_evt;
}

cl_event vecsmooth(cl_kernel vecsmooth_k, cl_command_queue que, int lws_cli,
	cl_mem d_vsmooth, cl_mem d_v1, cl_int nels,
	cl_event init_evt)
{
	const size_t lws[] = { lws_cli };
	const size_t gws[] = { round_mul_up(nels, lws[0]) };
	printf("smooth gws: %d | %d | %zu = %zu\n", nels, lws_cli, gws_align_smooth, gws[0]);
	cl_event vecsmooth_evt;
	cl_int err;

	cl_uint i = 0;
	err = clSetKernelArg(vecsmooth_k, i++, sizeof(d_vsmooth), &d_vsmooth);
	ocl_check(err, "set vecsmooth arg", i-1);
	err = clSetKernelArg(vecsmooth_k, i++, sizeof(d_v1), &d_v1);
	ocl_check(err, "set vecsmooth arg", i-1);
	err = clSetKernelArg(vecsmooth_k, i++, (lws[0])*sizeof(cl_int), NULL);
	ocl_check(err, "set vecsmooth arg", i-1);
	err = clSetKernelArg(vecsmooth_k, i++, sizeof(nels), &nels);
	ocl_check(err, "set vecsmooth arg", i-1);

	err = clEnqueueNDRangeKernel(que, vecsmooth_k, 1,
		NULL, gws, lws,
		1, &init_evt, &vecsmooth_evt);

	ocl_check(err, "enqueue vecsmooth");

	return vecsmooth_evt;
}

void verify(const int *vsmooth, int nels)
{
	for (int i = 0; i < nels; ++i) {
		int target = i == nels - 1 ? i - 1 : i;
		if (vsmooth[i] != target) {
			fprintf(stderr, "mismatch @ %d : %d != %d\n",
				i, vsmooth[i], target);
			exit(3);
		}
	}
}


int main(int argc, char *argv[])
{
	if (argc <= 2) {
		fprintf(stderr, "%s nels lws\n", argv[0]);
		exit(1);
	}

	const int nels = atoi(argv[1]);
	const size_t memsize = nels*sizeof(cl_int);

	const int lws = atoi(argv[2]);
	if (lws <= 0) {
		fprintf(stderr, "lws must be postive\n");
		exit(1);
	}

	cl_platform_id p = select_platform();
	cl_device_id d = select_device(p);
	cl_context ctx = create_context(p, d);
	cl_command_queue que = create_queue(ctx, d);
	cl_program prog = create_program("vecsmooth.ocl", ctx, d);
	cl_int err;

	cl_kernel vecinit_k = clCreateKernel(prog, "vecinit", &err);
	ocl_check(err, "create kernel vecinit");
	cl_kernel vecsmooth_k = clCreateKernel(prog, "vecsmooth_lmem", &err);
	ocl_check(err, "create kernel vecsmooth");

	/* get information about the preferred work-group size multiple */
	err = clGetKernelWorkGroupInfo(vecinit_k, d,
		CL_KERNEL_PREFERRED_WORK_GROUP_SIZE_MULTIPLE,
		sizeof(gws_align_init), &gws_align_init, NULL);
	ocl_check(err, "preferred wg multiple for init");
	err = clGetKernelWorkGroupInfo(vecsmooth_k, d,
		CL_KERNEL_PREFERRED_WORK_GROUP_SIZE_MULTIPLE,
		sizeof(gws_align_smooth), &gws_align_smooth, NULL);
	ocl_check(err, "preferred wg multiple for smooth");


	cl_mem d_v1 = NULL, d_vsmooth = NULL;

	d_v1 = clCreateBuffer(ctx,
		CL_MEM_READ_WRITE | CL_MEM_HOST_NO_ACCESS,
		memsize, NULL,
		&err);
	ocl_check(err, "create buffer d_v1");

	d_vsmooth = clCreateBuffer(ctx,
		CL_MEM_WRITE_ONLY | CL_MEM_HOST_READ_ONLY |
		CL_MEM_ALLOC_HOST_PTR,
		memsize, NULL,
		&err);
	ocl_check(err, "create buffer d_vsmooth");

	cl_event init_evt, smooth_evt, read_evt;

	init_evt = vecinit(vecinit_k, que, lws, d_v1, nels);

	smooth_evt = vecsmooth(vecsmooth_k, que, lws, d_vsmooth, d_v1, nels, init_evt);

	cl_int *h_vsmooth = clEnqueueMapBuffer(que, d_vsmooth, CL_FALSE,
		CL_MAP_READ,
		0, memsize,
		1, &smooth_evt, &read_evt, &err);

	clWaitForEvents(1, &read_evt);

	verify(h_vsmooth, nels);

	const double runtime_init_ms = runtime_ms(init_evt);
	const double runtime_smooth_ms = runtime_ms(smooth_evt);
	const double runtime_read_ms = runtime_ms(read_evt);

	const double init_bw_gbs = 1.0*memsize/1.0e6/runtime_init_ms;
	const double smooth_bw_gbs = 4.0*memsize/1.0e6/runtime_smooth_ms;
	const double read_bw_gbs = memsize/1.0e6/runtime_read_ms;

	printf("init: %d int in %gms: %g GB/s %g GE/s\n",
		nels, runtime_init_ms, init_bw_gbs, nels/1.0e6/runtime_init_ms);
	printf("smooth : %d int in %gms: %g GB/s %g GE/s\n",
		nels, runtime_smooth_ms, smooth_bw_gbs, nels/1.0e6/runtime_smooth_ms);
	printf("read: %d int in %gms: %g GB/s %g GE/s\n",
		nels, runtime_read_ms, read_bw_gbs, nels/1.0e6/runtime_read_ms);

	err = clEnqueueUnmapMemObject(que, d_vsmooth, h_vsmooth,
		0, NULL, NULL);
	ocl_check(err, "unmap vsmooth");

	clReleaseMemObject(d_vsmooth);
	clReleaseMemObject(d_v1);
	clReleaseKernel(vecsmooth_k);
	clReleaseKernel(vecinit_k);
	clReleaseProgram(prog);
	clReleaseCommandQueue(que);
	clReleaseContext(ctx);
}
