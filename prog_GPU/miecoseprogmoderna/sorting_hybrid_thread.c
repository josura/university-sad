#include <stdlib.h>
#include <stdio.h>
#include <time.h>
#include <pthread.h>

#define CL_TARGET_OPENCL_VERSION 120
#include "ocl_boiler.h"

#define min(a,b) a>b ? b : a
#define NUM_THREADS 4

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
   double exectime=((double)(end-begin)/CLOCKS_PER_SEC)*1000;
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

void printarr(int* arr,unsigned int nels,const char* str){
		printf("%s ",str);
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
	ocl_check(err, "set sortinit arg", i-1);
	err = clSetKernelArg(sortinit_k, i++, sizeof(nels), &nels);
	ocl_check(err, "set sortinit arg", i-1);
	err = clSetKernelArg(sortinit_k, i++, sizeof(cl_int)*_lws*2,NULL);
	ocl_check(err, "set sortinit localmemory arg",i-1);

	err = clEnqueueNDRangeKernel(que, sortinit_k, 1,
		NULL, gws, lws,
		1, &init_event, &sortinit_evt);
	
	ocl_check(err, "enqueue sortinit");

	return sortinit_evt;
}

typedef struct {
	int* arr; 
	int start; 
	int end;
	int lws;
	int nels;
}threadargs;

/* Iterative mergesort function to sort arr[0...n-1], for threads */
void *  mergeSortLocalThread(void * args) 
{ 
   threadargs * argom =(threadargs*) args;
   int * arr = argom->arr;
   int start = argom->start; 
   int end = argom->end;
   int lws = argom->lws;
   int nels = argom->nels;
   int curr_size;  
   int left_start;
   int n =  end - start;
      for (curr_size=lws; curr_size <= end-start-1; curr_size = 2*curr_size) 
   { 
       // Pick starting point of different subarrays of current size 
       for (left_start = start; left_start < end -1; left_start += 2*curr_size) 
       { 
           // Find ending point of left subarray. mid+1 is starting  
           // point of right 
           int mid = min(left_start + curr_size - 1, nels-1); 
  
           int right_end = min(left_start + 2*curr_size - 1, nels-1); 
  
           // Merge Subarrays arr[left_start...mid] & arr[mid+1...right_end] 
           merge(arr, left_start, mid, right_end); 
       } 
   } 
   pthread_exit(NULL);
} 



int hybrid_sort(cl_kernel sortinit_k,cl_int _lws, cl_command_queue que,
	cl_mem d_Sort, cl_int nels, cl_event init_evt, cl_int ** pointerto_h_Sort)
{	
	unsigned int memsize = nels * sizeof(cl_int);
	pthread_t threads_sorting[NUM_THREADS];
	 cl_event sort_evt, read_evt;
	int lws = _lws<<1;
	sort_evt = sortparallel(sortinit_k, _lws, que, d_Sort,  nels ,init_evt);
	int err;
	cl_int * h_Sort;
        *pointerto_h_Sort = h_Sort = clEnqueueMapBuffer(que, d_Sort, CL_FALSE,
                CL_MAP_READ,
                0, memsize,
                1, &sort_evt, &read_evt, &err);
	ocl_check(err,"map buffer");
        clWaitForEvents(1, &read_evt);
	int start[NUM_THREADS];
	int end[NUM_THREADS];
	int sottoelementi = nels/NUM_THREADS;
	int currstart=0;
	threadargs argums[NUM_THREADS];
	//sorting using various threads, not enough
	for(int i =0; i < NUM_THREADS; i++){
		start[i]=currstart;
		end[i]=((currstart+sottoelementi) < nels ? (currstart+sottoelementi) : nels);
		currstart+=sottoelementi;
		argums[i] = (threadargs){.arr = h_Sort, .start = start[i], .end = end[i] , .lws = lws, .nels= nels};
	}
	clock_t begin=clock();
	for(int i = 0; i< NUM_THREADS;i++){
		pthread_create(&(threads_sorting[i]),NULL,mergeSortLocalThread,argums+i);	
	}
	for(int i = 0; i< NUM_THREADS;i++){
		pthread_join(threads_sorting[i],NULL);	
	}
	//sorting final sequence
	currstart=0;
	for(int i =0; i < 2; i++){
		start[i]=currstart;
		end[i]=((currstart+ nels/2) < nels ? (currstart + nels/2) : nels);
		currstart+=nels/2;
		argums[i] = (threadargs){.arr = h_Sort, .start = start[i], .end = end[i] , .lws = lws, .nels= nels};
	}
	for(int i = 0; i< 2;i++){
		pthread_create(&(threads_sorting[i]),NULL,mergeSortLocalThread,argums+i);	
	}
	for(int i = 0; i< 2;i++){
		pthread_join(threads_sorting[i],NULL);	
	}
	mergeSortLocal(h_Sort,nels,nels/2);
	clock_t ending=clock();

	double exectime=((double)(ending-begin)/CLOCKS_PER_SEC)*1000;

	const double runtime_sort_ms = runtime_ms(sort_evt);
	const double runtime_read_ms = runtime_ms(read_evt);
	const double sort_bw_gbs = (sizeof(cl_int))*memsize/1.0e6/runtime_sort_ms;
	const double read_bw_gbs = sizeof(float)/1.0e6/runtime_read_ms;
	printf("sort: %d int in %gms: %g GB/s %g GE/s\n",
		nels, runtime_sort_ms, sort_bw_gbs, nels/1.0e6/runtime_sort_ms);
	printf("sort_local_host: %d int in %gms: %g GB/s %g GE/s\n",
		nels, exectime, sort_bw_gbs, nels/1.0e6/runtime_sort_ms);
	printf("read: %d int in %gms: %g GB/s %g GE/s\n",
		nels, runtime_read_ms, read_bw_gbs, nels/1.0e6/runtime_read_ms);
	return exectime;

	
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
	cl_kernel sort_k = clCreateKernel(prog, "local_miosort_lmem", &err);
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
	cl_event init_evt = vecinit(vecinit_k, que, d_Sort, nels );
	cl_int * h_Sort= NULL;
        clWaitForEvents(1, &init_evt);
	const double runtime_init_ms = runtime_ms(init_evt);
	printf("ci siamo\n");
	fflush(stdout);
	const double init_bw_gbs = 1.0*memsize/1.0e6/runtime_init_ms;

	printf("init: %d int in %gms: %g GB/s %g GE/s\n",
		nels, runtime_init_ms, init_bw_gbs, nels/1.0e6/runtime_init_ms);

	const double runtime_total_sort = hybrid_sort(sort_k, lws, que, d_Sort, nels, init_evt, &h_Sort);
	verify(h_Sort,nels);

	const double sort_bw_gbs = (sizeof(cl_int))*memsize/1.0e6/runtime_total_sort;
	
	printf("total_sort: %d int in %gms: %g GB/s %g GE/s\n",
		nels, runtime_total_sort, sort_bw_gbs, nels/1.0e6/runtime_total_sort);
	clReleaseMemObject(d_Sort);
	clReleaseKernel(sort_k);
	clReleaseKernel(vecinit_k);
	clReleaseProgram(prog);
	clReleaseCommandQueue(que);
	clReleaseContext(ctx);
	

}
