#include <stdlib.h>
#include <stdio.h>
#include <time.h>
#include <math.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#define CL_TARGET_OPENCL_VERSION 120
#include "ocl_boiler.h"

#define min(a,b) a>b ? b : a

  
  
  
void controlinput(char** argv, int argc){
	if(argc<4){
		fprintf(stderr,"usage: %s numero_elementi lws localmemorysize\n",argv[0]);
		exit(1);
	}
}

void printarr(int* arr,unsigned int nels){
		for(int i =0;i < nels;i++){
			printf("%i ",arr[i]);
			}
		printf("\n");

}

int doesFileExist(const char *filename) {
    struct stat st;
    int result = stat(filename, &st);
    return result == 0;
}


size_t gws_align_init;
size_t gws_align_sort;

cl_event vecinit_random(cl_kernel vecinit_k, cl_command_queue que,
	cl_mem d_v1, cl_int nels)
{
	const size_t gws[] = { round_mul_up(nels, gws_align_init) };

	printf("init gws: %d | %zu = %zu\n", nels, gws_align_init, gws[0]);
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

cl_event sortparallel(cl_kernel sortinit_k,cl_int _lws, cl_command_queue que,
	cl_mem d_v1, cl_int nels, cl_event init_event)
{
	const size_t workitem=nels; 
	const size_t gws[] = { round_mul_up(workitem, _lws ) };
	const size_t lws[] = { _lws};
	printf("sort_base gws e workitem : %d | %zu = %zu  %li\n", nels, gws_align_init, gws[0],workitem);
	cl_event sortinit_evt;
	cl_int err;

	cl_uint i = 0;
	err = clSetKernelArg(sortinit_k, i++, sizeof(d_v1), &d_v1);
	ocl_check(err, "set sortinit arg1", i-1);
	err = clSetKernelArg(sortinit_k, i++, sizeof(nels), &nels);
	ocl_check(err, "set sortinit arg2", i-1);
	err = clSetKernelArg(sortinit_k, i++, sizeof(d_v1), &d_v1);
	ocl_check(err, "set sortinit arg3", i-1);
	err = clSetKernelArg(sortinit_k, i++, sizeof(cl_int)*lws[0],NULL);
	ocl_check(err, "set sortinit localmemory arg",i-1);

	err = clEnqueueNDRangeKernel(que, sortinit_k, 1,
		NULL, gws, lws,
		1, &init_event, &sortinit_evt);
	
	ocl_check(err, "enqueue sortinit");

	return sortinit_evt;
}

cl_event sortparallelmerge(cl_kernel sortinit_k,cl_int _lws, cl_command_queue que,
	cl_mem d_v1,cl_mem d_vout, cl_int nels, cl_event init_event,cl_int current_merge_size,cl_int local_size)
{
	const size_t workitem=round_mul_up((nels),current_merge_size); 
	const size_t gws[] = { round_mul_up(workitem,_lws) };
	const size_t lws[] = { _lws };
	printf("merge nels gws_align workitem : %d | %zu = %zu \n", nels, gws_align_init, gws[0]);
	cl_event sortinit_evt;
	cl_int err;

	cl_uint i = 0;
	err = clSetKernelArg(sortinit_k, i++, sizeof(d_vout), &d_vout);
	ocl_check(err, "set mergeinit arg1", i-1);
	err = clSetKernelArg(sortinit_k, i++, sizeof(d_v1), &d_v1);
	ocl_check(err, "set mergeinit arg3", i-1);
	err = clSetKernelArg(sortinit_k, i++, sizeof(nels), &nels);
	ocl_check(err, "set mergeeinit arg2", i-1);
	err = clSetKernelArg(sortinit_k, i++, sizeof(current_merge_size),&current_merge_size);
	ocl_check(err, "set mergeinit arg4",i-1);
	err = clSetKernelArg(sortinit_k, i++, sizeof(cl_int)*local_size,NULL);
	ocl_check(err, "set merge localmemory arg",i-1);
	err = clSetKernelArg(sortinit_k, i++, sizeof(local_size), &local_size);
	ocl_check(err, "set mergeeinit argfin", i-1);

	err = clEnqueueNDRangeKernel(que, sortinit_k, 1,
		NULL, gws, lws,
		1, &init_event, &sortinit_evt);
	
	ocl_check(err, "enqueue sortinitparallelmerge");

	return sortinit_evt;
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
	cl_int lws = atoi(argv[2]);
	cl_int localsize = atoi(argv[3]);
	if(nels & 3){
		printf("number of elements must be a multiple of 4\n");
		exit(1);
	}

	cl_platform_id p = select_platform();
	cl_device_id d = select_device(p);
	cl_context ctx = create_context(p, d);
	cl_command_queue que = create_queue(ctx, d);
	cl_program prog = create_program("sorting.ocl", ctx, d);
	cl_int err;

	cl_kernel vecinit_k = clCreateKernel(prog, "vecinit_random", &err);
	ocl_check(err, "create kernel vecinit");
	cl_kernel sort_k = clCreateKernel(prog, "local_count_sort_vectlmemV3", &err);
	ocl_check(err, "create kernel miocountsort");
	cl_kernel sort_merge_k = clCreateKernel(prog, "mergebinaryWithRepParallelLocalFinal2", &err);
	ocl_check(err, "create kernel merging");
	


	/* get information about the preferred work-group size multiple */
	err = clGetKernelWorkGroupInfo(vecinit_k, d,
		CL_KERNEL_PREFERRED_WORK_GROUP_SIZE_MULTIPLE,
		sizeof(gws_align_init), &gws_align_init, NULL);
	ocl_check(err, "preferred wg multiple for init");
	err = clGetKernelWorkGroupInfo(sort_k, d,
		CL_KERNEL_PREFERRED_WORK_GROUP_SIZE_MULTIPLE,
		sizeof(gws_align_sort), &gws_align_sort, NULL);
	ocl_check(err, "preferred wg multiple for sort");

	cl_mem d_Sort1 = NULL,d_Sort2 = NULL;

	

	d_Sort1 = clCreateBuffer(ctx,
                CL_MEM_READ_WRITE | CL_MEM_HOST_READ_ONLY | CL_MEM_ALLOC_HOST_PTR,
                memsize, NULL,
                &err);
        ocl_check(err, "create buffer d_Sort1");
	
	d_Sort2 = clCreateBuffer(ctx,
                CL_MEM_READ_WRITE | CL_MEM_HOST_READ_ONLY | CL_MEM_ALLOC_HOST_PTR,
                memsize, NULL,
                &err);
        ocl_check(err, "create buffer d_Sort2");

        cl_event init_evt, sort_evt, merge_evt1, merge_evt2, read_evt;

        init_evt = vecinit_random(vecinit_k, que, d_Sort1, nels );
        cl_int *h_Sort; 
        sort_evt = sortparallel(sort_k, lws, que, d_Sort1,  nels ,init_evt);
	/*h_Sort = clEnqueueMapBuffer(que, d_Sort1, CL_FALSE,
			CL_MAP_READ,
			0, memsize,
                	1, &sort_evt, &read_evt, &err);
	ocl_check(err,"map buffer d_Sort1 init");

	clWaitForEvents(1, &read_evt);
	printarr(h_Sort,nels);*/

	int turn=0;	
	double total_time_merge=0;
	int current_merge_size = lws;
	merge_evt1=sort_evt;
	int pass=1;
	while(current_merge_size<nels){
		if(turn ==0){
			turn = 1;
			merge_evt2 = sortparallelmerge(sort_merge_k, lws, que, d_Sort1,d_Sort2, nels, merge_evt1,current_merge_size,localsize);
			clWaitForEvents(1, &merge_evt2);

			const double runtime_merge_ms = runtime_ms(merge_evt2);
			total_time_merge += runtime_merge_ms;
			const double merge_bw_gbs = memsize*log2(nels)/1.0e6/runtime_merge_ms;
			printf("merge_parziale_lws%i destinazione Sort2: %d int in %gms: %g GB/s %g GE/s\n",
					current_merge_size,nels, runtime_merge_ms, merge_bw_gbs, (nels)/1.0e6/runtime_merge_ms);
			/*h_Sort = clEnqueueMapBuffer(que, d_Sort2, CL_FALSE,
				CL_MAP_READ,
				0, memsize,
				1, &merge_evt2, &read_evt, &err);
			ocl_check(err,"map buffer d_Sort1 init");

			clWaitForEvents(1, &read_evt);
			printarr(h_Sort,nels);*/
			

		}
		else{
			turn = 0;
			merge_evt1 = sortparallelmerge(sort_merge_k, lws, que, d_Sort2,d_Sort1, nels, merge_evt2,current_merge_size,localsize);
			clWaitForEvents(1, &merge_evt1);
			const double runtime_merge_ms = runtime_ms(merge_evt1);
			total_time_merge += runtime_merge_ms;
			const double merge_bw_gbs = memsize*log2(nels)/1.0e6/runtime_merge_ms;
			printf("merge_parziale_lws%i destinazione Sort1: %d int in %gms: %g GB/s %g GE/s\n",
					current_merge_size,nels, runtime_merge_ms, merge_bw_gbs, (nels)/1.0e6/runtime_merge_ms);
			/*h_Sort = clEnqueueMapBuffer(que, d_Sort1, CL_FALSE,
				CL_MAP_READ,
				0, memsize,
				1, &merge_evt1, &read_evt, &err);
			ocl_check(err,"map buffer d_Sort1 init");

			clWaitForEvents(1, &read_evt);
			printarr(h_Sort,nels);*/
			


		}
		current_merge_size<<=1;
		pass++;
	}       
       	if(turn == 0){
		h_Sort = clEnqueueMapBuffer(que, d_Sort1, CL_FALSE,
			CL_MAP_READ,
			0, memsize,
                	1, &merge_evt1, &read_evt, &err);
		ocl_check(err,"map buffer d_Sort1");
	}else{
		h_Sort = clEnqueueMapBuffer(que, d_Sort2, CL_FALSE,
			CL_MAP_READ,
			0, memsize,
                	1, &merge_evt2, &read_evt, &err);
		ocl_check(err,"map buffer d_Sort2");
	}
	clWaitForEvents(1, &read_evt);
	//printarr(h_Sort,nels);
	//verify(h_Sort,nels);
	const double runtime_init_ms = runtime_ms(init_evt);
	const double runtime_sort_ms = runtime_ms(sort_evt);
	const double runtime_read_ms = runtime_ms(read_evt);

	const double init_bw_gbs = 1.0*memsize/1.0e6/runtime_init_ms;
	const double sort_bw_gbs = memsize*log2(nels)/1.0e6/runtime_sort_ms;
	const double merge_bw_gbs =pass*memsize*log2(nels)/1.0e6/total_time_merge;

	const double read_bw_gbs = memsize/1.0e6/runtime_read_ms;

	printf("init: %d int in %gms: %g GB/s %g GE/s\n",
		nels, runtime_init_ms, init_bw_gbs, nels/1.0e6/runtime_init_ms);
	printf("sort: %d int in %gms: %g GB/s %g GE/s\n",
		nels, runtime_sort_ms, sort_bw_gbs, (nels)/1.0e6/runtime_sort_ms);
	printf("read: %d int in %gms: %g GB/s %g GE/s\n",
		nels, runtime_read_ms, read_bw_gbs, nels/1.0e6/runtime_read_ms);

	printf("total_sort: %d int in %gms: %g GB/s %g GE/s\n",
		nels, runtime_sort_ms + total_time_merge, (sort_bw_gbs + merge_bw_gbs)/2, (nels)/1.0e6/runtime_sort_ms);
	
	
	clReleaseMemObject(d_Sort1);
	clReleaseKernel(sort_k);
	clReleaseKernel(sort_merge_k);
	clReleaseKernel(vecinit_k);
	clReleaseProgram(prog);
	clReleaseCommandQueue(que);
	clReleaseContext(ctx);
	if( ! doesFileExist( "sorting.csv") ) {
		FILE* pFile;
		pFile=fopen("sorting.csv", "w");

	   	if(pFile==NULL) {
		    perror("Error opening file.");
		}
		else {
			fprintf(pFile,"nome, nels,runtime_leaves, runtime_tot, bandwidth\n");
		}
		fclose(pFile);

	}
	char buffer[256];
	sprintf(buffer,"%i,%g, %g, %g",nels,runtime_sort_ms,runtime_sort_ms + total_time_merge,merge_bw_gbs);
	execlp("./append_mio", "./append_mio","sorting.csv" ,argv[0],buffer, (char*)NULL);
        perror("append_dati_fallito");
	
}
