

for nome_prog in sorting_counting_parallel_merge sorting_merging_parallel_merge sorting_miosort_parallel_merge ;do 
	for i in {1..60};do 
		let values=1024*512*$i
		OCL_PLATFORM=1 ./$nome_prog $values 256 256 | grep ciao
		echo $nome_prog $values
	done
done 
