make
let values=$((1024*1024))
for i in _hybrid _hybrid_counting _hybrid_counting_lmem _hybrid_counting_vect _hybrid_counting_vectlmem _hybrid_notlmem _hybrid_thread _hybrid_thread_v2 _sliding1 _counting_parallel_merge_no_repetition _counting_parallel_merge_with_repetition  _counting_parallel_merge_with_repetition_V2 _counting_parallel_merge_with_repetition_random _miosort_parallel_merge_no_rep; do for j in 128 256 512 ; do echo sorting$i $values $j; OCL_PLATFORM=1 ./sorting$i $values $j | grep sort: ; done; done 
OCL_PLATFORM=1 ./mergesorting 512 512
