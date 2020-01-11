LDLIBS=-lOpenCL -pthread -lm
#LDLIBS=-framework OpenCL

TARGETS = parallelizzo
TARGETS+= vecsmoothmio vecsmoothvect vecsmooth_v4 vecsmoothvectlmem
TARGETS+= transpose transpose_lmem_vect transpose_img transpose_img_vect transpose_img_lmem
TARGETS+= reduce2 reduce4 reduce4x4 reduce4x4mod
TARGETS+= sorting mergesorting sorting_sliding sorting_hybrid sorting_hybrid_thread sorting_hybrid_thread_v2 sorting_hybrid_notlmem sorting_hybrid_counting sorting_hybrid_counting_lmem sorting_hybrid_counting_vect
TARGETS+= sorting_hybrid_counting_vectlmem simplesort
TARGETS+= scan1


all: $(TARGETS)

