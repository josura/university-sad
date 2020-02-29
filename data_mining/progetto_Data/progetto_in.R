esem.matr <- matrix(c(1:12),nrow = 4)

funziona <- function(x,y,M){
  M[x,y] <- 100*x+y
}

funziona_vect <- Vectorize(funziona,vectorize.args = c("x","y"))

outer(1:nrow(esem.matr),1:ncol(esem.matr),funziona_vect,esem.matr)

esempio <- as.data.frame(outer(1:nrow(esem.matr),1:ncol(esem.matr),funziona_vect,esem.matr),row.names = c("gene1","gene2","gene3"))
esempio

library(MASS)
library(cluster)

  weibullpar <- function(M){
    retmat <- matrix(nrow = nrow(M),ncol = 2)
    for (i in 1:nrow(M)) {
      retmat[i,] <- fitdistr(M[i,],densfun = "weibull")$estimate
    }
    retmat
  }

parametri_weibull <- weibullpar(esem.matr)
parametri_weibull
funzione_prob_greater_than <- function(M){
  par_weib <- weibullpar(M)
  ret_mat <- matrix(data = 0,nrow = nrow(M),ncol = ncol(M))
  for (i in 1:nrow(M)) {
    ret_mat[i,] <- pweibull(M[i,],shape = par_weib[i,1],scale = par_weib[i,2],lower.tail = FALSE)
  }
  ret_mat
}
funzione_prob_greater_than(esem.matr)

funzione_ipotesi <- function(M,significance){
  pval_mat <- funzione_prob_greater_than(M)
  ret_mat <- matrix(FALSE,nrow = nrow(M),ncol = ncol(M))
  for (i in 1:nrow(M)) {
    for (j in 1:ncol(M)) {
        ret_mat[i,j] = pval_mat[i,j]>significance
    }
  }
  ret_mat
}
funzione_ipotesi(esem.matr,0.05)

funzione_ipotesi_opt <- function(M,significance){
  greaterthanfunc <- function(x,y,M,alpha){
    M[x,y] <- (M[x,y]>alpha) 
  }
  greaterthanfunc_vect <- Vectorize(greaterthanfunc,vectorize.args = c("x","y"))
  outer(1:nrow(M),1:ncol(M),greaterthanfunc_vect,funzione_prob_greater_than(M),significance)
}
funzione_ipotesi_opt(esem.matr,0.05)

library(proxy)

hiera_cluster.pvalue <- hclust(distance_vec)

plot(hiera_cluster.pvalue)

hiera_cluster.pvalue

library(fpc)
cluster_pvalue.pam <-pamk(funzione_prob_greater_than(esem.matr),krange = 1:3,criterion = "asw",usepam = TRUE)  

cluster_pvalue.pam

cluster_pvalue_cos <- function(M){
  distance_vec <- dist(t(funzione_prob_greater_than(M)),method = "cosine")
  pamk(distance_vec,diss = TRUE,krange = 1:(ncol(M)-1),criterion = "asw",usepam = TRUE)
}

cluster_ipotesi_jac <- function(M){
  distance_vec <- dist(t(funzione_ipotesi_opt(M,0.05)),method = "jaccard")
  pamk(distance_vec,diss = TRUE,krange = 1:(ncol(M)-1),criterion = "asw",usepam = TRUE)
}

optimal_numclast <- function(hierarch,distances){
  max <- -2
  opt_clust <- -1
  for (numk in 2:(ncol(distances)-1)) {
    silcas <- silhouette(cutree(hierarch,numk),distances)
    if(class(silcas)=="silhouette" ){
      if(summary(silcas)$avg.width > max)
        opt_clust <- numk
      else return(opt_clust)
    }
  }
  opt_clust
}

optimal_numclast_binary <- function(hierarch,distances){
  l <- 2
  m <- 0
  r <- ncol(distances)-1
  max <- -2
  while ( l<=r ){
    m <-  l + ((r - l)* 2)
    silcas <- silhouette(cutree(hierarch,m),distances)
    if (class(silcas)=="silhouette" && summary(silcas)$avg.width > max ){
      max <- summary(silcas)$avg.width
      l = m +1;
    }
    else
      r = m -1;
  }
  m
}

hcluster_pvalue_cos <- function(M){
  distance_vec <- dist(t(funzione_prob_greater_than(M)),method = "cosine")
  hiera <- hclust(distance_vec)
  opt_clust <- optimal_numclast(hiera,distance_vec)
  list(cutree(hiera,opt_clust),opt_clust)
  
}

hcluster_pvalue_cos(esem.matr)

hcluster_ipotesi_jac <- function(M){
  distance_vec <- dist(t(funzione_ipotesi_opt(M,0.05)),method = "jaccard")
  hiera <- hclust(distance_vec) 
  opt_clust <- optimal_numclast(hiera,distance_vec)
  list(cutree(hiera,opt_clust),opt_clust)  
}

hcluster_ipotesi_jac(esem.matr)

cluster_pvalue_cos.pam <- pamk(distance_vec,diss = TRUE,krange = 1:(nrow(distance_vec)-1),criterion = "asw",usepam = TRUE)

cluster_pvalue.pam

plot(cluster_pvalue.pam)

plot(cluster_pvalue_cos.pam$pamobject)

plot(cluster_pvalue.pam$pamobject)


distance_vec.ipotesi <- dist(t(funzione_ipotesi(esem.matr,0.05)),method = "jaccard")

hiera_cluster.ipotesi <-  hclust(distance_vec.ipotesi,method = "complete")

plot(hiera_cluster.ipotesi)

cutree(hiera_cluster.ipotesi,k=2)

esem.attri <- matrix(data = c("scoliosi","scoliosi","tubercolosi"),nrow = 1)
esem.attri


attr_vect.cluster <- function(patologie_vect,cluster_vect,numk){
  vectret <- c(rep("",numk))
  for (i in 1:numk) {
    tt <- table(patologie_vect[cluster_vect== numk])
    vectret[i] <- names(tt[tt==max(tt)])[1]
  }
  vectret
}
  
attr_clust <- attr_vect.cluster(esem.attri,cutree(hiera_cluster.ipotesi,k=2),2)



plot(hiera_cluster.ipotesi,labels = attr_clust)

plot(silhouette(cutree(hcluster_ipotesi_jac(esem.matr),k=2),distance_vec.ipotesi))
