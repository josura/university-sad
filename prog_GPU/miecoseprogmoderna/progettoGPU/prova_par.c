#include <stdlib.h>
#include <stdio.h>
#include <time.h>
#include <math.h>
#include "parallel_sorting.h"

#define min(a,b) a>b ? b : a

  
  
  
void controlinput(char** argv, int argc){
	if(argc<4){
		fprintf(stderr,"usage: %s numero_elementi lwsbottom lwstop\n",argv[0]);
		exit(1);
	}
}



cl_event vecinit_random(cl_kernel vecinit_k, cl_command_queue que,
	cl_mem d_v1, cl_int nels)
{
	const size_t gws[] = { round_mul_up(nels, 32) };
	printf("init gws: %d | %zu = %zu\n", nels, 32, gws[0]);
	cl_event vecinit_evt;
	cl_int err;

	cl_uint i = 0;
	srand(time(NULL));
	cl_int seeds[2];
	seeds[0]=rand();
	srand(time(NULL));
	seeds[1]=rand();
	err = clSetKernelArg(vecinit_k, i++, sizeof(d_v1), &d_v1);
	ocl_check(err, "set vecinit arg1", i-1);
	err = clSetKernelArg(vecinit_k, i++, sizeof(cl_int), seeds);
	ocl_check(err, "set vecinit arg2", i-1);
	err = clSetKernelArg(vecinit_k, i++, sizeof(cl_int), seeds + 1);
	ocl_check(err, "set vecinit arg3", i-1);
	err = clSetKernelArg(vecinit_k, i++, sizeof(nels), &nels);
	ocl_check(err, "set vecinit arg4", i-1);

	err = clEnqueueNDRangeKernel(que, vecinit_k, 1,
		NULL, gws, NULL,
		0, NULL, &vecinit_evt);
	
	ocl_check(err, "enqueue vecinit");

	return vecinit_evt;
}



void verify(int * arr, unsigned int nels){
	for(int i=1;i<nels;i++){
		if(arr[i]<arr[i-1]){
			fprintf(stderr,"mismatch %u < %u",arr[i],arr[i-1]);
			exit(1);
		}
	}
}

int main(int argc,char** argv){
	controlinput(argv,argc);
	cl_int nels= atoi(argv[1]);
	const size_t memsize = nels*sizeof(cl_int);
	cl_int lws1 = atoi(argv[2]),lws2 = atoi(argv[3]);

	cl_platform_id p = select_platform();
	cl_device_id d = select_device(p);
	cl_context ctx = create_context(p, d);
	cl_command_queue que = create_queue(ctx, d);
	cl_program prog = create_program("sorting.ocl", ctx, d);
	cl_int err;

	cl_kernel vecinit_k = clCreateKernel(prog, "vecinit_random", &err);
	ocl_check(err, "create kernel vecinit");
	



	cl_mem d_Sort1 = NULL;

	

	d_Sort1 = clCreateBuffer(ctx,
                CL_MEM_READ_WRITE | CL_MEM_HOST_READ_ONLY | CL_MEM_ALLOC_HOST_PTR,
                memsize, NULL,
                &err);
        ocl_check(err, "create buffer d_Sort1");
	
        cl_event init_evt, read_evt;

        init_evt = vecinit_random(vecinit_k, que, d_Sort1, nels );
	const double total_time=sort_mergevect_parallelmerge(lws1, lws2,que,prog,ctx,d_Sort1,d_Sort1,nels,init_evt);
	cl_int *h_Sort; 
	h_Sort = clEnqueueMapBuffer(que, d_Sort1, CL_FALSE,
		CL_MAP_READ,
		0, memsize,
		0, NULL, &read_evt, &err);
	ocl_check(err,"map buffer d_Sort1");

        clWaitForEvents(1, &read_evt);
	//printarr(h_Sort,nels);
	verify(h_Sort,nels);
	const double runtime_init_ms = runtime_ms(init_evt);
	const double runtime_read_ms = runtime_ms(read_evt);

	const double init_bw_gbs = 1.0*memsize/1.0e6/runtime_init_ms;

	const double read_bw_gbs = memsize/1.0e6/runtime_read_ms;

	printf("init: %d int in %gms: %g GB/s %g GE/s\n",
		nels, runtime_init_ms, init_bw_gbs, nels/1.0e6/runtime_init_ms);
	printf("read: %d int in %gms: %g GB/s %g GE/s\n",
		nels, runtime_read_ms, read_bw_gbs, nels/1.0e6/runtime_read_ms);

	printf("total_sort: %d int in %gms: %g GB/s %g GE/s\n",
		nels, total_time, memsize*log2(nels), (nels)*log2(nels)/1.0e6/total_time);
	
	
	clReleaseMemObject(d_Sort1);
	clReleaseKernel(vecinit_k);
	clReleaseProgram(prog);
	clReleaseCommandQueue(que);
	clReleaseContext(ctx);
}
