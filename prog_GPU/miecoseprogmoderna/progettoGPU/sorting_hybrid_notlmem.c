#include <stdlib.h>
#include <stdio.h>
#include <time.h>

#define CL_TARGET_OPENCL_VERSION 120
#include "ocl_boiler.h"

#define min(a,b) a>b ? b : a

void merge(int arr[], int l, int m, int r); 
  
// Utility function to find minimum of two integers 
//int min(int x, int y) { return (x<y)? x :y; } 
  
  
/* Iterative mergesort function to sort arr[0...n-1] */
double mergeSortLocal(int arr[], int n, int lws) 
{ 
   int curr_size;  
   int left_start;
  
   clock_t begin=clock();
   for (curr_size=lws; curr_size<=n-1; curr_size = 2*curr_size) 
   { 
       // Pick starting point of different subarrays of current size 
#pragma omp parallel for 
	for (left_start=0; left_start<n-1; left_start += 2*curr_size) 
       { 
           // Find ending point of left subarray. mid+1 is starting  
           // point of right 
           int mid = min(left_start + curr_size - 1, n-1); 
  
           int right_end = min(left_start + 2*curr_size - 1, n-1); 
  
           // Merge Subarrays arr[left_start...mid] & arr[mid+1...right_end] 
           merge(arr, left_start, mid, right_end); 
       } 
   } 
   clock_t end=clock();
double exectime=(((double)(end)-(double)(begin))/CLOCKS_PER_SEC)*1000;
   return exectime;
} 
  
/* Function to merge the two haves arr[l..m] and arr[m+1..r] of array arr[] */
void merge(int arr[], int l, int m, int r) 
{ 
    int i, j, k; 
    int n1 = m - l + 1; 
    int n2 =  r - m; 
  
    /* create temp arrays */
    int L[n1], R[n2]; 
  
    /* Copy data to temp arrays L[] and R[] */
    for (i = 0; i < n1; i++) 
        L[i] = arr[l + i]; 
    for (j = 0; j < n2; j++) 
        R[j] = arr[m + 1+ j]; 
  
    /* Merge the temp arrays back into arr[l..r]*/
    i = 0; 
    j = 0; 
    k = l; 
    while (i < n1 && j < n2) 
    { 
        if (L[i] <= R[j]) 
        { 
            arr[k] = L[i]; 
            i++; 
        } 
        else
        { 
            arr[k] = R[j]; 
            j++; 
        } 
        k++; 
    } 
  
    /* Copy the remaining elements of L[], if there are any */
    while (i < n1) 
    { 
        arr[k] = L[i]; 
        i++; 
        k++; 
    } 
  
    /* Copy the remaining elements of R[], if there are any */
    while (j < n2) 
    { 
        arr[k] = R[j]; 
        j++; 
        k++; 
    } 
} 

void controlinput(char** argv, int argc){
	if(argc<3){
		fprintf(stderr,"usage: %s numero_elementi lws\n",argv[0]);
		exit(1);
	}
}

void printarr(int* arr,unsigned int nels){
		for(int i =0;i < nels;i++){
			printf("%i ",arr[i]);
			}
		printf("\n");

}


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

cl_event sortparallel(cl_kernel sortinit_k,cl_int _lws, cl_command_queue que,
	cl_mem d_v1, cl_int nels, cl_event init_event)
{
	const size_t workitem=(nels + 1)/2; 
	const size_t gws[] = { round_mul_up(workitem, gws_align_init) };
	const size_t lws[] = { _lws };
	printf("init gws e workitem : %d | %zu = %zu  %i\n", nels, gws_align_init, gws[0],workitem);
	cl_event sortinit_evt;
	cl_int err;

	cl_uint i = 0;
	err = clSetKernelArg(sortinit_k, i++, sizeof(d_v1), &d_v1);
	ocl_check(err, "set sortinit arg1", i-1);
	err = clSetKernelArg(sortinit_k, i++, sizeof(nels), &nels);
	ocl_check(err, "set sortinit arg2", i-1);
	err = clSetKernelArg(sortinit_k, i++, sizeof(cl_int)*_lws*2,NULL);
	ocl_check(err, "set sortinit localmemory arg",i-1);

	err = clEnqueueNDRangeKernel(que, sortinit_k, 1,
		NULL, gws, lws,
		1, &init_event, &sortinit_evt);
	
	ocl_check(err, "enqueue sortinit");

	return sortinit_evt;
}


void verify(int * arr, unsigned int nels){
	for(int i=0;i<nels;i++){
		if(arr[i]!=i+1){
			fprintf(stderr,"mismatch %llu != %llu",arr[i],i+1);
			exit(1);
		}
	}
}

int main(int argc,char** argv){
	controlinput(argv,argc);
	cl_int nels= atoi(argv[1]);
	const size_t memsize = nels*sizeof(cl_int);
	cl_int lws = atoi(argv[2]);

	cl_platform_id p = select_platform();
	cl_device_id d = select_device(p);
	cl_context ctx = create_context(p, d);
	cl_command_queue que = create_queue(ctx, d);
	cl_program prog = create_program("sorting.ocl", ctx, d);
	cl_int err;

	cl_kernel vecinit_k = clCreateKernel(prog, "vecinit", &err);
	ocl_check(err, "create kernel vecinit");
	cl_kernel sort_k = clCreateKernel(prog, "local_miosort", &err);
	ocl_check(err, "create kernel miosort");


	/* get information about the preferred work-group size multiple */
	err = clGetKernelWorkGroupInfo(vecinit_k, d,
		CL_KERNEL_PREFERRED_WORK_GROUP_SIZE_MULTIPLE,
		sizeof(gws_align_init), &gws_align_init, NULL);
	ocl_check(err, "preferred wg multiple for init");
	err = clGetKernelWorkGroupInfo(sort_k, d,
		CL_KERNEL_PREFERRED_WORK_GROUP_SIZE_MULTIPLE,
		sizeof(gws_align_sum), &gws_align_sum, NULL);
	ocl_check(err, "preferred wg multiple for sum");

	cl_mem d_Sort = NULL;

	

	d_Sort = clCreateBuffer(ctx,
                CL_MEM_READ_WRITE | CL_MEM_HOST_READ_ONLY | CL_MEM_ALLOC_HOST_PTR,
                memsize, NULL,
                &err);
        ocl_check(err, "create buffer d_Sort");

        cl_event init_evt, sort_evt, read_evt;

        init_evt = vecinit(vecinit_k, que, d_Sort, nels );
        sort_evt = sortparallel(sort_k, lws, que, d_Sort,  nels ,init_evt);

        cl_int *h_Sort = clEnqueueMapBuffer(que, d_Sort, CL_FALSE,
                CL_MAP_READ,
                0, memsize,
                1, &sort_evt, &read_evt, &err);
	ocl_check(err,"map buffer");
        clWaitForEvents(1, &read_evt);
	//printarr(h_Sort,nels);
	double time_local_merge = mergeSortLocal(h_Sort,nels,lws<<1);
	//printarr(h_Sort,nels);
	verify(h_Sort,nels);
	const double runtime_init_ms = runtime_ms(init_evt);
	const double runtime_sort_ms = runtime_ms(sort_evt);
	const double runtime_read_ms = runtime_ms(read_evt);

	const double init_bw_gbs = 1.0*memsize/1.0e6/runtime_init_ms;
	const double sort_bw_gbs = (sizeof(cl_int))*memsize/1.0e6/runtime_sort_ms;
	const double read_bw_gbs = sizeof(float)/1.0e6/runtime_read_ms;

	printf("init: %d int in %gms: %g GB/s %g GE/s\n",
		nels, runtime_init_ms, init_bw_gbs, nels/1.0e6/runtime_init_ms);
	printf("sort: %d int in %gms: %g GB/s %g GE/s\n",
		nels, runtime_sort_ms, sort_bw_gbs, nels/1.0e6/runtime_sort_ms);
	printf("sort_local_host: %d int in %gms: %g GB/s %g GE/s\n",
		nels, time_local_merge, sort_bw_gbs, nels/1.0e6/runtime_sort_ms);
	printf("read: %d int in %gms: %g GB/s %g GE/s\n",
		nels, runtime_read_ms, read_bw_gbs, nels/1.0e6/runtime_read_ms);

	printf("total_sort: %d int in %gms: %g GB/s %g GE/s\n",
		nels, runtime_sort_ms+time_local_merge, sort_bw_gbs, nels/1.0e6/runtime_sort_ms);
	clReleaseMemObject(d_Sort);
	clReleaseKernel(sort_k);
	clReleaseKernel(vecinit_k);
	clReleaseProgram(prog);
	clReleaseCommandQueue(que);
	clReleaseContext(ctx);
	

}
