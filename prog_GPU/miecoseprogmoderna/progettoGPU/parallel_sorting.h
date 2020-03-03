#include <stdlib.h>
#include <stdio.h>
#define CL_TARGET_OPENCL_VERSION 120
#include "ocl_boiler.h"

cl_event sortparallelmerge(cl_kernel sortinit_k,cl_int _lws, cl_command_queue que,
        cl_mem d_v1,cl_mem d_vout, cl_int nels, cl_event init_event,cl_int current_merge_size) {
        const size_t workitem=nels;
        const size_t gws[] = { round_mul_up(workitem, _lws) };
        const size_t lws[] = { _lws };
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

        err = clEnqueueNDRangeKernel(que, sortinit_k, 1,
                NULL, gws, lws,
                1, &init_event, &sortinit_evt);

        ocl_check(err, "enqueue sortinitparallelmerge");

        return sortinit_evt;
}


cl_event sortparallel_leaves_counting(cl_int _lws, cl_command_queue que,cl_program prog, cl_mem d_v1, cl_int nels, cl_event init_event){
	if(nels & 3){
		fprintf(stderr,"number of elements must be divisible by 4");
		exit(0);
	}
        cl_int err;
	cl_kernel sortinit_k = clCreateKernel(prog, "stable_local_count_sort_vectlmemV3", &err);
        ocl_check(err, "create kernel miocountsort");
	const size_t workitem=nels; 
        const size_t gws[] = { round_mul_up(workitem, _lws) };
        const size_t lws[] = { _lws };
        cl_event sortinit_evt;

        cl_uint i = 0;
        err = clSetKernelArg(sortinit_k, i++, sizeof(d_v1), &d_v1);
        ocl_check(err, "set sortinit arg1", i-1);
        err = clSetKernelArg(sortinit_k, i++, sizeof(nels), &nels);
        ocl_check(err, "set sortinit arg2", i-1);
        err = clSetKernelArg(sortinit_k, i++, sizeof(d_v1), &d_v1);
        ocl_check(err, "set sortinit arg3", i-1);
        err = clSetKernelArg(sortinit_k, i++, sizeof(cl_int)*_lws,NULL);
        ocl_check(err, "set sortinit localmemory arg",i-1);

        err = clEnqueueNDRangeKernel(que, sortinit_k, 1,
                NULL, gws, lws, 
                1, &init_event, &sortinit_evt);

        ocl_check(err, "enqueue sortinit");

        return sortinit_evt;
}


cl_event sortparallel_leaves_merging(cl_int _lws, cl_command_queue que,cl_program prog, cl_mem d_v1, cl_int nels, cl_event init_event){
        cl_int err;
	cl_kernel sortinit_k = clCreateKernel(prog, "stable_local_count_sort_vectlmemV3", &err);
        ocl_check(err, "create kernel miocountsort");
	const size_t workitem=nels; 
        const size_t gws[] = { round_mul_up(workitem, _lws) };
        const size_t lws[] = { _lws };
        cl_event sortinit_evt;

        cl_uint i = 0;
        err = clSetKernelArg(sortinit_k, i++, sizeof(d_v1), &d_v1);
        ocl_check(err, "set sortinit arg1", i-1);
        err = clSetKernelArg(sortinit_k, i++, sizeof(nels), &nels);
        ocl_check(err, "set sortinit arg2", i-1);
        err = clSetKernelArg(sortinit_k, i++, sizeof(d_v1), &d_v1);
        ocl_check(err, "set sortinit arg3", i-1);
        err = clSetKernelArg(sortinit_k, i++, sizeof(cl_int)*_lws,NULL);
        ocl_check(err, "set sortinit localmemory arg",i-1);

        err = clEnqueueNDRangeKernel(que, sortinit_k, 1,
                NULL, gws, lws, 
                1, &init_event, &sortinit_evt);

        ocl_check(err, "enqueue sortinit");

	clReleaseKernel(sortinit_k);
        return sortinit_evt;
}


cl_event sortparallel_leaves_mergingvect(cl_int _lws, cl_command_queue que,cl_program prog, cl_mem d_v1, cl_int nels, cl_event init_event){
	if(nels & 3){
		fprintf(stderr,"number of elements must be divisible by 4");
		exit(0);
	}
        cl_int err;
	cl_kernel sortinit_k = clCreateKernel(prog, "stable_local_count_sort_vectlmemV3", &err);
        ocl_check(err, "create kernel miocountsort");
	const size_t workitem=nels; 
        const size_t gws[] = { round_mul_up(workitem, _lws) };
        const size_t lws[] = { _lws };
        cl_event sortinit_evt;

        cl_uint i = 0;
        err = clSetKernelArg(sortinit_k, i++, sizeof(d_v1), &d_v1);
        ocl_check(err, "set sortinit arg1", i-1);
        err = clSetKernelArg(sortinit_k, i++, sizeof(nels), &nels);
        ocl_check(err, "set sortinit arg2", i-1);
        err = clSetKernelArg(sortinit_k, i++, sizeof(d_v1), &d_v1);
        ocl_check(err, "set sortinit arg3", i-1);
        err = clSetKernelArg(sortinit_k, i++, sizeof(cl_int)*_lws,NULL);
        ocl_check(err, "set sortinit localmemory arg",i-1);

        err = clEnqueueNDRangeKernel(que, sortinit_k, 1,
                NULL, gws, lws, 
                1, &init_event, &sortinit_evt);

        ocl_check(err, "enqueue sortinit");

	clReleaseKernel(sortinit_k);
        return sortinit_evt;
}


cl_event sortparallel_leaves_assemblylane(cl_int _lws, cl_command_queue que,cl_program prog, cl_mem d_v1, cl_int nels, cl_event init_event){
	cl_int err;
	cl_kernel sortinit_k = clCreateKernel(prog, "local_miosort_lmemV3", &err);
        ocl_check(err, "create kernel countsort");
	const size_t workitem=(nels+1)/2; 
        const size_t gws[] = { round_mul_up(workitem, _lws) };
        const size_t lws[] = { _lws };
        cl_event sortinit_evt;

        cl_uint i = 0;
        err = clSetKernelArg(sortinit_k, i++, sizeof(d_v1), &d_v1);
        ocl_check(err, "set sortinit arg1", i-1);
        err = clSetKernelArg(sortinit_k, i++, sizeof(nels), &nels);
        ocl_check(err, "set sortinit arg2", i-1);
        err = clSetKernelArg(sortinit_k, i++, sizeof(d_v1), &d_v1);
        ocl_check(err, "set sortinit arg3", i-1);
        err = clSetKernelArg(sortinit_k, i++, sizeof(cl_int)*_lws,NULL);
        ocl_check(err, "set sortinit localmemory arg",i-1);

        err = clEnqueueNDRangeKernel(que, sortinit_k, 1,
                NULL, gws, lws, 
                1, &init_event, &sortinit_evt);

        ocl_check(err, "enqueue sortinit");

	clReleaseKernel(sortinit_k);
        return sortinit_evt;

}

double sort_count_parallelmerge(cl_int _lws1,cl_int _lws2, cl_command_queue que,cl_program prog,cl_context ctx, cl_mem d_Sort1,cl_mem d_vout, cl_int nels, cl_event init_event){
	cl_int err;
	cl_kernel sort_merge_k = clCreateKernel(prog, "mergebinaryWithRepParallelV4", &err);
        ocl_check(err, "create kernel merging");
 	cl_mem d_Sort2 = NULL;
	uint memsize = nels * sizeof(cl_int);
	d_Sort2 = clCreateBuffer(ctx, 
                CL_MEM_READ_WRITE | CL_MEM_HOST_READ_ONLY | CL_MEM_ALLOC_HOST_PTR, 
                memsize, NULL, 
                &err); 
        ocl_check(err, "create buffer d_Sort2");

	cl_event sort_evt = sortparallel_leaves_counting( _lws1, que,prog, d_Sort1,  nels ,init_event);
        int turn=0;
        double total_time_merge=0;
        int current_merge_size = _lws1;
        cl_event merge_evt1=sort_evt,merge_evt2;
        while(current_merge_size<nels){
                if(turn ==0){
                        turn = 1;
                        merge_evt2 = sortparallelmerge(sort_merge_k,_lws2, que, d_Sort1,d_Sort2, nels, merge_evt1,current_merge_size);
                        clWaitForEvents(1, &merge_evt2);
                        const double runtime_merge_ms = runtime_ms(merge_evt2);
                        total_time_merge += runtime_merge_ms;

                }
                else{
                        turn = 0;
                        merge_evt1 = sortparallelmerge(sort_merge_k, _lws2, que, d_Sort2,d_Sort1, nels, merge_evt2,current_merge_size);
                        clWaitForEvents(1, &merge_evt1);
                        const double runtime_merge_ms = runtime_ms(merge_evt1);
                        total_time_merge += runtime_merge_ms;
                }
                current_merge_size<<=1;
        }
        if(turn){
                clReleaseMemObject(d_Sort1);
		d_vout =d_Sort2;
        }else{
                clReleaseMemObject(d_Sort2);
		d_vout =d_Sort1;
        }

        clReleaseKernel(sort_merge_k);
	
	return (total_time_merge + runtime_ms(sort_evt));	
}

double sort_count_HALFparallelmerge(cl_int _lws1,cl_int _lws2, cl_command_queue que,cl_program prog,cl_context ctx, cl_mem d_Sort1,cl_mem d_vout, cl_int nels, cl_event init_event){
	cl_int err;
	cl_kernel sort_merge_k = clCreateKernel(prog, "mergebinaryWithRepHalfParallelV2", &err);
        ocl_check(err, "create kernel merging");
 	cl_mem d_Sort2 = NULL;
	uint memsize = nels * sizeof(cl_int);
	d_Sort2 = clCreateBuffer(ctx, 
                CL_MEM_READ_WRITE | CL_MEM_HOST_READ_ONLY | CL_MEM_ALLOC_HOST_PTR, 
                memsize, NULL, 
                &err); 
        ocl_check(err, "create buffer d_Sort2");

	cl_event sort_evt = sortparallel_leaves_counting( _lws1, que,prog, d_Sort1,  nels ,init_event);
        int turn=0;
        double total_time_merge=0;
        int current_merge_size = _lws1;
        cl_event merge_evt1=sort_evt,merge_evt2;
        while(current_merge_size<nels){
                if(turn ==0){
                        turn = 1;
                        merge_evt2 = sortparallelmerge(sort_merge_k,_lws2, que, d_Sort1,d_Sort2, nels, merge_evt1,current_merge_size);
                        clWaitForEvents(1, &merge_evt2);
                        const double runtime_merge_ms = runtime_ms(merge_evt2);
                        total_time_merge += runtime_merge_ms;

                }
                else{
                        turn = 0;
                        merge_evt1 = sortparallelmerge(sort_merge_k, _lws2, que, d_Sort2,d_Sort1, nels, merge_evt2,current_merge_size);
                        clWaitForEvents(1, &merge_evt1);
                        const double runtime_merge_ms = runtime_ms(merge_evt1);
                        total_time_merge += runtime_merge_ms;
                }
                current_merge_size<<=1;
        }
        if(turn){
                clReleaseMemObject(d_Sort1);
		d_vout =d_Sort2;
        }else{
                clReleaseMemObject(d_Sort2);
		d_vout =d_Sort1;
        }

        clReleaseKernel(sort_merge_k);
	
	return (total_time_merge + runtime_ms(sort_evt));
}

double sort_merge_parallelmerge(cl_int _lws1,cl_int _lws2, cl_command_queue que,cl_program prog,cl_context ctx, cl_mem d_Sort1,cl_mem d_vout, cl_int nels, cl_event init_event){
	cl_int err;
	cl_kernel sort_merge_k = clCreateKernel(prog, "mergebinaryWithRepParallelV4", &err);
        ocl_check(err, "create kernel merging");
 	cl_mem d_Sort2 = NULL;
	uint memsize = nels * sizeof(cl_int);
	d_Sort2 = clCreateBuffer(ctx, 
                CL_MEM_READ_WRITE | CL_MEM_HOST_READ_ONLY | CL_MEM_ALLOC_HOST_PTR, 
                memsize, NULL, 
                &err); 
        ocl_check(err, "create buffer d_Sort2");

	cl_event sort_evt = sortparallel_leaves_merging( _lws1, que,prog, d_Sort1,  nels ,init_event);
        int turn=0;
        double total_time_merge=0;
        int current_merge_size = _lws1;
        cl_event merge_evt1=sort_evt,merge_evt2;
        while(current_merge_size<nels){
                if(turn ==0){
                        turn = 1;
                        merge_evt2 = sortparallelmerge(sort_merge_k,_lws2, que, d_Sort1,d_Sort2, nels, merge_evt1,current_merge_size);
                        clWaitForEvents(1, &merge_evt2);
                        const double runtime_merge_ms = runtime_ms(merge_evt2);
                        total_time_merge += runtime_merge_ms;

                }
                else{
                        turn = 0;
                        merge_evt1 = sortparallelmerge(sort_merge_k, _lws2, que, d_Sort2,d_Sort1, nels, merge_evt2,current_merge_size);
                        clWaitForEvents(1, &merge_evt1);
                        const double runtime_merge_ms = runtime_ms(merge_evt1);
                        total_time_merge += runtime_merge_ms;
                }
                current_merge_size<<=1;
        }
        if(turn){
                clReleaseMemObject(d_Sort1);
		d_vout =d_Sort2;
        }else{
                clReleaseMemObject(d_Sort2);
		d_vout =d_Sort1;
        }

        clReleaseKernel(sort_merge_k);
	
	return (total_time_merge + runtime_ms(sort_evt));
}

double sort_merge_HALFparallelmerge(cl_int _lws1,cl_int _lws2, cl_command_queue que,cl_program prog,cl_context ctx, cl_mem d_Sort1,cl_mem d_vout, cl_int nels, cl_event init_event){
	cl_int err;
	cl_kernel sort_merge_k = clCreateKernel(prog, "mergebinaryWithRepHalfParallelV2", &err);
        ocl_check(err, "create kernel merging");
 	cl_mem d_Sort2 = NULL;
	uint memsize = nels * sizeof(cl_int);
	d_Sort2 = clCreateBuffer(ctx, 
                CL_MEM_READ_WRITE | CL_MEM_HOST_READ_ONLY | CL_MEM_ALLOC_HOST_PTR, 
                memsize, NULL, 
                &err); 
        ocl_check(err, "create buffer d_Sort2");

	cl_event sort_evt = sortparallel_leaves_merging( _lws1, que,prog, d_Sort1,  nels ,init_event);
        int turn=0;
        double total_time_merge=0;
        int current_merge_size = _lws1;
        cl_event merge_evt1=sort_evt,merge_evt2;
        while(current_merge_size<nels){
                if(turn ==0){
                        turn = 1;
                        merge_evt2 = sortparallelmerge(sort_merge_k,_lws2, que, d_Sort1,d_Sort2, nels, merge_evt1,current_merge_size);
                        clWaitForEvents(1, &merge_evt2);
                        const double runtime_merge_ms = runtime_ms(merge_evt2);
                        total_time_merge += runtime_merge_ms;

                }
                else{
                        turn = 0;
                        merge_evt1 = sortparallelmerge(sort_merge_k, _lws2, que, d_Sort2,d_Sort1, nels, merge_evt2,current_merge_size);
                        clWaitForEvents(1, &merge_evt1);
                        const double runtime_merge_ms = runtime_ms(merge_evt1);
                        total_time_merge += runtime_merge_ms;
                }
                current_merge_size<<=1;
        }
        if(turn){
                clReleaseMemObject(d_Sort1);
		d_vout =d_Sort2;
        }else{
                clReleaseMemObject(d_Sort2);
		d_vout =d_Sort1;
        }

        clReleaseKernel(sort_merge_k);
	
	return (total_time_merge + runtime_ms(sort_evt));
}

double sort_mergevect_parallelmerge(cl_int _lws1,cl_int _lws2, cl_command_queue que,cl_program prog,cl_context ctx, cl_mem d_Sort1,cl_mem d_vout, cl_int nels, cl_event init_event){
	cl_int err;
	cl_kernel sort_merge_k = clCreateKernel(prog, "mergebinaryWithRepParallelV4", &err);
        ocl_check(err, "create kernel merging");
 	cl_mem d_Sort2 = NULL;
	uint memsize = nels * sizeof(cl_int);
	d_Sort2 = clCreateBuffer(ctx, 
                CL_MEM_READ_WRITE | CL_MEM_HOST_READ_ONLY | CL_MEM_ALLOC_HOST_PTR, 
                memsize, NULL, 
                &err); 
        ocl_check(err, "create buffer d_Sort2");

	cl_event sort_evt = sortparallel_leaves_mergingvect( _lws1, que,prog, d_Sort1,  nels ,init_event);
        int turn=0;
        double total_time_merge=0;
        int current_merge_size = _lws1;
        cl_event merge_evt1=sort_evt,merge_evt2;
        while(current_merge_size<nels){
                if(turn ==0){
                        turn = 1;
                        merge_evt2 = sortparallelmerge(sort_merge_k,_lws2, que, d_Sort1,d_Sort2, nels, merge_evt1,current_merge_size);
                        clWaitForEvents(1, &merge_evt2);
                        const double runtime_merge_ms = runtime_ms(merge_evt2);
                        total_time_merge += runtime_merge_ms;

                }
                else{
                        turn = 0;
                        merge_evt1 = sortparallelmerge(sort_merge_k, _lws2, que, d_Sort2,d_Sort1, nels, merge_evt2,current_merge_size);
                        clWaitForEvents(1, &merge_evt1);
                        const double runtime_merge_ms = runtime_ms(merge_evt1);
                        total_time_merge += runtime_merge_ms;
                }
                current_merge_size<<=1;
        }
        if(turn){
                clReleaseMemObject(d_Sort1);
		d_vout =d_Sort2;
        }else{
                clReleaseMemObject(d_Sort2);
		d_vout =d_Sort1;
        }

        clReleaseKernel(sort_merge_k);
	
	return (total_time_merge + runtime_ms(sort_evt));
}

double sort_mergevect_HALFparallelmerge(cl_int _lws1,cl_int _lws2, cl_command_queue que,cl_program prog,cl_context ctx, cl_mem d_Sort1,cl_mem d_vout, cl_int nels, cl_event init_event){
	cl_int err;
	cl_kernel sort_merge_k = clCreateKernel(prog, "mergebinaryWithRepHalfParallelV2", &err);
        ocl_check(err, "create kernel merging");
 	cl_mem d_Sort2 = NULL;
	uint memsize = nels * sizeof(cl_int);
	d_Sort2 = clCreateBuffer(ctx, 
                CL_MEM_READ_WRITE | CL_MEM_HOST_READ_ONLY | CL_MEM_ALLOC_HOST_PTR, 
                memsize, NULL, 
                &err); 
        ocl_check(err, "create buffer d_Sort2");

	cl_event sort_evt = sortparallel_leaves_mergingvect( _lws1, que,prog, d_Sort1,  nels ,init_event);
        int turn=0;
        double total_time_merge=0;
        int current_merge_size = _lws1;
        cl_event merge_evt1=sort_evt,merge_evt2;
        while(current_merge_size<nels){
                if(turn ==0){
                        turn = 1;
                        merge_evt2 = sortparallelmerge(sort_merge_k,_lws2, que, d_Sort1,d_Sort2, nels, merge_evt1,current_merge_size);
                        clWaitForEvents(1, &merge_evt2);
                        const double runtime_merge_ms = runtime_ms(merge_evt2);
                        total_time_merge += runtime_merge_ms;

                }
                else{
                        turn = 0;
                        merge_evt1 = sortparallelmerge(sort_merge_k, _lws2, que, d_Sort2,d_Sort1, nels, merge_evt2,current_merge_size);
                        clWaitForEvents(1, &merge_evt1);
                        const double runtime_merge_ms = runtime_ms(merge_evt1);
                        total_time_merge += runtime_merge_ms;
                }
                current_merge_size<<=1;
        }
        if(turn){
                clReleaseMemObject(d_Sort1);
		d_vout =d_Sort2;
        }else{
                clReleaseMemObject(d_Sort2);
		d_vout =d_Sort1;
        }

        clReleaseKernel(sort_merge_k);
	
	return (total_time_merge + runtime_ms(sort_evt));
}



double sort_assemblylane_parallelmerge(cl_int _lws1,cl_int _lws2, cl_command_queue que,cl_program prog,cl_context ctx, cl_mem d_Sort1,cl_mem d_vout, cl_int nels, cl_event init_event){
	cl_int err;
	cl_kernel sort_merge_k = clCreateKernel(prog, "mergebinaryWithRepParallelV4", &err);
        ocl_check(err, "create kernel merging");
 	cl_mem d_Sort2 = NULL;
	uint memsize = nels * sizeof(cl_int);
	d_Sort2 = clCreateBuffer(ctx, 
                CL_MEM_READ_WRITE | CL_MEM_HOST_READ_ONLY | CL_MEM_ALLOC_HOST_PTR, 
                memsize, NULL, 
                &err); 
        ocl_check(err, "create buffer d_Sort2");

	cl_event sort_evt = sortparallel_leaves_assemblylane( _lws1, que,prog, d_Sort1,  nels ,init_event);
        int turn=0;
        double total_time_merge=0;
        int current_merge_size = _lws1;
        cl_event merge_evt1=sort_evt,merge_evt2;
        while(current_merge_size<nels){
                if(turn ==0){
                        turn = 1;
                        merge_evt2 = sortparallelmerge(sort_merge_k,_lws2, que, d_Sort1,d_Sort2, nels, merge_evt1,current_merge_size);
                        clWaitForEvents(1, &merge_evt2);
                        const double runtime_merge_ms = runtime_ms(merge_evt2);
                        total_time_merge += runtime_merge_ms;

                }
                else{
                        turn = 0;
                        merge_evt1 = sortparallelmerge(sort_merge_k, _lws2, que, d_Sort2,d_Sort1, nels, merge_evt2,current_merge_size);
                        clWaitForEvents(1, &merge_evt1);
                        const double runtime_merge_ms = runtime_ms(merge_evt1);
                        total_time_merge += runtime_merge_ms;
                }
                current_merge_size<<=1;
        }
        if(turn){
                clReleaseMemObject(d_Sort1);
		d_vout =d_Sort2;
        }else{
                clReleaseMemObject(d_Sort2);
		d_vout =d_Sort1;
        }

        clReleaseKernel(sort_merge_k);
	
	return (total_time_merge + runtime_ms(sort_evt));

}

double sort_assemblylane_HALFparallelmerge(cl_int _lws1,cl_int _lws2, cl_command_queue que,cl_program prog,cl_context ctx, cl_mem d_Sort1,cl_mem d_vout, cl_int nels, cl_event init_event){
	cl_int err;
	cl_kernel sort_merge_k = clCreateKernel(prog, "mergebinaryWithRepHalfParallelV2", &err);
        ocl_check(err, "create kernel merging");
 	cl_mem d_Sort2 = NULL;
	uint memsize = nels * sizeof(cl_int);
	d_Sort2 = clCreateBuffer(ctx, 
                CL_MEM_READ_WRITE | CL_MEM_HOST_READ_ONLY | CL_MEM_ALLOC_HOST_PTR, 
                memsize, NULL, 
                &err); 
        ocl_check(err, "create buffer d_Sort2");

	cl_event sort_evt = sortparallel_leaves_assemblylane( _lws1, que,prog, d_Sort1,  nels ,init_event);
        int turn=0;
        double total_time_merge=0;
        int current_merge_size = _lws1;
        cl_event merge_evt1=sort_evt,merge_evt2;
        while(current_merge_size<nels){
                if(turn ==0){
                        turn = 1;
                        merge_evt2 = sortparallelmerge(sort_merge_k,_lws2, que, d_Sort1,d_Sort2, nels, merge_evt1,current_merge_size);
                        clWaitForEvents(1, &merge_evt2);
                        const double runtime_merge_ms = runtime_ms(merge_evt2);
                        total_time_merge += runtime_merge_ms;

                }
                else{
                        turn = 0;
                        merge_evt1 = sortparallelmerge(sort_merge_k, _lws2, que, d_Sort2,d_Sort1, nels, merge_evt2,current_merge_size);
                        clWaitForEvents(1, &merge_evt1);
                        const double runtime_merge_ms = runtime_ms(merge_evt1);
                        total_time_merge += runtime_merge_ms;
                }
                current_merge_size<<=1;
        }
        if(turn){
                clReleaseMemObject(d_Sort1);
		d_vout =d_Sort2;
        }else{
                clReleaseMemObject(d_Sort2);
		d_vout =d_Sort1;
        }

        clReleaseKernel(sort_merge_k);
	
	return (total_time_merge + runtime_ms(sort_evt));

}
