function [I] = errorDiffusion(I,M,k)

I = double(I);
[rM, cM, ~]= size(M);
[sX, sY, ~]= size(I);

cMoff=floor(cM/2);
