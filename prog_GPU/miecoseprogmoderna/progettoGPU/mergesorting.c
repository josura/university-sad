#include <stdlib.h>
#include <stdio.h>
#include <time.h>

#define CL_TARGET_OPENCL_VERSION 120
#include "ocl_boiler.h"

void swap(int* a, int* b) 
{ 
    int t = *a; 
    *a = *b; 
    *b = t; 
} 
  
/* This function takes last element as pivot, places 
   the pivot element at its correct position in sorted 
    array, and places all smaller (smaller than pivot) 
   to left of pivot and all greater elements to right 
   of pivot */
int partition (int arr[], int low, int high) 
{ 
    int pivot = arr[high];    // pivot 
    int i = (low - 1);  // Index of smaller element 
  
    for (int j = low; j <= high- 1; j++) 
    { 
        // If current element is smaller than the pivot 
        if (arr[j] < pivot) 
        { 
            i++;    // increment index of smaller element 
            swap(&arr[i], &arr[j]); 
        } 
    } 
    swap(&arr[i + 1], &arr[high]); 
    return (i + 1); 
} 
  
/* The main function that implements QuickSort 
 arr[] --> Array to be sorted, 
  low  --> Starting index, 
  high  --> Ending index */
void quickSort(int arr[], int low, int high) 
{ 
    if (low < high) 
    { 
        /* pi is partitioning index, arr[p] is now 
           at right place */
        int pi = partition(arr, low, high); 
  
        // Separately sort elements before 
        // partition and after partition 
        quickSort(arr, low, pi - 1); 
        quickSort(arr, pi + 1, high); 
    } 
} 

void controlinput(char** argv, int argc){
	if(argc<3){
		fprintf(stderr,"usage: %s numero_elementi  lws\n",argv[0]);
		exit(1);
	}
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

cl_event sortparallel(cl_kernel sortinit_k,const size_t lws, cl_command_queue que,
	cl_mem d_v1, cl_int nels,const cl_event init_event)
{
 	size_t worksize=nels/2;
	const size_t gws[] = { round_mul_up(worksize, gws_align_init) };
	printf("init gws: %d | %zu = %zu\n", worksize, gws_align_init, gws[0]);
	cl_event sortinit_evt;
	cl_int err;
	cl_uint i = 0;
	err = clSetKernelArg(sortinit_k, i++, sizeof(d_v1), &d_v1);
	ocl_check(err, "set sortinit arg", i-1);
	err = clSetKernelArg(sortinit_k, i++, sizeof(nels), &nels);
	ocl_check(err, "set sortinit arg", i-1);

	err = clEnqueueNDRangeKernel(que, sortinit_k, 1,
		NULL, gws, &lws,
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

int * createArrDecr(int nels){
	int *arr = malloc(sizeof(int)*nels);
	for(int i = 0; i<nels ; i++){
		arr[i]=nels-i;
	}
	return arr;
}

int main(int argc,char** argv){
	controlinput(argv,argc);
	cl_int nels= atoi(argv[1]);
	const size_t lws=atoi(argv[2]);
	const size_t memsize = nels*sizeof(cl_int);
	int * arr = createArrDecr(nels);
	clock_t begin=clock();
	quickSort(arr,0,nels-1);
	clock_t end=clock();
	double exectime=((double)(end-begin)/CLOCKS_PER_SEC)*1000;
	printf("execution time quicksort %fms\n",exectime);
	cl_platform_id p = select_platform();
	cl_device_id d = select_device(p);
	cl_context ctx = create_context(p, d);
	cl_command_queue que = create_queue(ctx, d);
	cl_program prog = create_program("sorting.ocl", ctx, d);
	cl_int err;

	cl_kernel vecinit_k = clCreateKernel(prog, "vecinit", &err);
	ocl_check(err, "create kernel vecinit");
	cl_kernel sort_k = clCreateKernel(prog, "mergesortnaive", &err);
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

        init_evt = vecinit(vecinit_k, que, d_Sort, nels);
        sort_evt = sortparallel(sort_k, lws, que, d_Sort,  nels,init_evt);

        cl_int *h_Sort = clEnqueueMapBuffer(que, d_Sort, CL_FALSE,
                CL_MAP_READ,
                0, memsize,
                1, &sort_evt, &read_evt, &err);
	ocl_check(err,"map buffer");
        clWaitForEvents(1, &read_evt);
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
	printf("read: %d int in %gms: %g GB/s %g GE/s\n",
		nels, runtime_read_ms, read_bw_gbs, nels/1.0e6/runtime_read_ms);

	clReleaseMemObject(d_Sort);
	clReleaseKernel(sort_k);
	clReleaseKernel(vecinit_k);
	clReleaseProgram(prog);
	clReleaseCommandQueue(que);
	clReleaseContext(ctx);
	

}
