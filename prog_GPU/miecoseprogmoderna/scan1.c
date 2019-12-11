#include <stdlib.h>
#include <stdio.h>
#include <time.h>

//#define CL_TARGET_OPENCL_VERSION 120
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

cl_event scan(cl_kernel scan_k, cl_command_queue que,
	cl_mem d_vscan, cl_mem d_v1,cl_int _lws, cl_int nels,
	cl_event init_evt)
{
	const size_t gws[] = { nels };
	const size_t lws[] = {_lws};
	printf("sum gws: %d | %zu = %zu\n", nels, gws_align_sum, gws[0]);
	cl_event scan_evt;
	cl_int err;

	cl_uint i = 0;
	err = clSetKernelArg(scan_k, i++, sizeof(d_vscan), &d_vscan);
	ocl_check(err, "set scan arg", i-1);
	err = clSetKernelArg(scan_k, i++, sizeof(d_v1), &d_v1);
	ocl_check(err, "set scan arg", i-1);
	err = clSetKernelArg(scan_k, i++, _lws*sizeof(cl_int), NULL);
	ocl_check(err, "set scan arg", i-1);
	err = clSetKernelArg(scan_k, i++, sizeof(nels), &nels);
	ocl_check(err, "set scan arg", i-1);

	err = clEnqueueNDRangeKernel(que, scan_k, 1,
		NULL, gws, lws,
		1, &init_evt, &scan_evt);

	ocl_check(err, "enqueue scan");

	return scan_evt;
}

void verify(const int *vscan, int numels)
{
        int target = 0;
        for (int i = 1; i <= numels; i++) {
                target += i;
                if (vscan[i-1] != target) {
                        fprintf(stderr, "mismatch @ %d: %d != %d\n",
                                i, vscan[i-1], target);
                        exit(2);
                }
        }
}

void printarr(int * arr, int nels){
	for(int i=0;i<nels;i++){
		printf("%i ",arr[i]);
	}
	printf("\n");
}

int main(int argc, char *argv[])
{
	if (argc <= 2) {
		fprintf(stderr, "specify number of elements and lws\n");
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
	cl_program prog = create_program("scan1.ocl", ctx, d);
	cl_int err;

	cl_kernel vecinit_k = clCreateKernel(prog, "vecinit", &err);
	ocl_check(err, "create kernel vecinit");
	cl_kernel scan_k = clCreateKernel(prog, "scan_lmem", &err);
	ocl_check(err, "create kernel scan_lmem");

	/* get information about the preferred work-group size multiple */
	err = clGetKernelWorkGroupInfo(vecinit_k, d,
		CL_KERNEL_PREFERRED_WORK_GROUP_SIZE_MULTIPLE,
		sizeof(gws_align_init), &gws_align_init, NULL);
	ocl_check(err, "preferred wg multiple for init");
	err = clGetKernelWorkGroupInfo(scan_k, d,
		CL_KERNEL_PREFERRED_WORK_GROUP_SIZE_MULTIPLE,
		sizeof(gws_align_sum), &gws_align_sum, NULL);
	ocl_check(err, "preferred wg multiple for sum");

	cl_mem d_v1 = NULL, d_vscan = NULL;

	d_v1 = clCreateBuffer(ctx,
		CL_MEM_READ_ONLY | CL_MEM_HOST_NO_ACCESS,
		memsize, NULL,
		&err);
	ocl_check(err, "create buffer d_v1");

	d_vscan = clCreateBuffer(ctx,
		CL_MEM_READ_WRITE | CL_MEM_HOST_READ_ONLY | CL_MEM_ALLOC_HOST_PTR,
		memsize, NULL,
		&err);
	ocl_check(err, "create buffer d_vscan");

	cl_event init_evt,scan_evt,read_evt;

	init_evt=vecinit( vecinit_k, que, d_v1, nels);
	scan_evt=scan(scan_k, que, d_vscan, d_v1,lws, nels, init_evt);

	cl_int* risultato=clEnqueueMapBuffer(que, d_vscan,CL_TRUE , CL_MAP_READ , 0, memsize, 1, &scan_evt , &read_evt,&err);
	ocl_check(err, "read result");
	clWaitForEvents(1,&read_evt);
	verify(risultato, nels);
	//printarr(risultato,nels);
	const double runtime_init_ms = runtime_ms(init_evt);
	const double runtime_read_ms = runtime_ms(read_evt);

	const double init_bw_gbs = 1.0*memsize/1.0e6/runtime_init_ms;
	const double read_bw_gbs = sizeof(int)/1.0e6/runtime_read_ms;

	printf("init: %d int in %gms: %g GB/s %g GE/s\n",
		nels, runtime_init_ms, init_bw_gbs, nels/1.0e6/runtime_init_ms);


	const double runtime_scan_ms = total_runtime_ms(scan_evt, scan_evt);
	printf("scan : %d int in %gms: %g GE/s\n",
		nels, runtime_scan_ms, nels/1.0e6/runtime_scan_ms);

	printf("read: 1 int in %gms: %g GB/s %g GE/s\n",
		runtime_read_ms, read_bw_gbs, 1.0/1.0e6/runtime_read_ms);
	err = clEnqueueUnmapMemObject(que, d_vscan, risultato, 0, NULL, NULL);
        ocl_check(err, "unmap scan");
	clReleaseMemObject(d_v1);
	clReleaseKernel(scan_k);
	clReleaseKernel(vecinit_k);
	clReleaseProgram(prog);
	clReleaseCommandQueue(que);
	clReleaseContext(ctx);
}
