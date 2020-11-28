
library(readr)
data.genes <- readRDS("tcga_tumorali_norm.rds")
data.genes <- read_rds("tcga_tumorali_norm.rds")
data.genes <- data.genes[-(21045:21046),]

out_no_na_col <- function(M){
  logis <- rep(FALSE,ncol(M))
  for (i in 1:ncol(M)) {
    logis[i] <- any(is.na(M[,i]))
  }
  M[,-(which(logis))]
}

data.genes <- out_no_na_col(data.genes)

#training.data.genes <- data.genes[,1:8000]
#test.data.genes <- data.genes[,8001:ncol(data.genes)]
#rm(data.genes)

library(MASS)
library(cluster)
library(fitdistrplus)

miadnormhist <- function(x){
  dnorm(x,mean = normal_fit[1],sd=normal_fit[2]) *ncol(training.data.genes)
}

mialognormhist  <- function(x){
  (dlnorm(x,meanlog = lognormal_fit[1],sdlog=lognormal_fit[2])) *ncol(training.data.genes) 
} 

miaweibhist <- function(x){
  (dweibull(x,shape =  weibu_fit[1],scale=weibu_fit[2])) *ncol(training.data.genes)
} 

miadnorm <- function(x){
  dnorm(x,mean = normal_fit[1],sd=normal_fit[2]) 
}

mialognorm  <- function(x){
  (dlnorm(x,meanlog = lognormal_fit[1],sdlog=lognormal_fit[2])) 
} 

miaweib <- function(x){
  (dweibull(x,shape =  weibu_fit[1],scale=weibu_fit[2]))
} 


# hist(training.data.genes[1,],main = "distribution, not shifted",xlab = "gene values")
# curve(expr = miadnormhist,add = TRUE,col="red")
# legend("topright", lty = 1, 
#        col = c("red"), 
#        legend = c("normal distribution"))




# hist(training.data.genes[1,]-min(training.data.genes[1,])-0.001,main = "shifted distribution for genes",xlab = "gene values")
# curve(expr = mialognormhist,add = TRUE,col="yellow")
# curve(expr = miaweibhist,add = TRUE,col="violet")
# legend("topright", lty = 1, 
#        col = c("yellow","violet"), 
#        legend = c("lognormal distribution","weibull distribution"))
# 
# d <- density(training.data.genes[1,])
# plot(d,main = "density, not shifted",xlab = "gene values")
# polygon(d, col="light blue", border="black") 
# curve(expr = miadnorm,add = TRUE,col="red")
# legend("topright", lty = 1, 
#        col = c("red"), 
#        legend = c("normal distribution"))
# 
# d <- density(training.data.genes[1,]-min(training.data.genes[1,]-0.001))
# plot(d,main = "density,shifted",xlab = "gene values")
# polygon(d, col="light blue", border="black") 
# curve(expr = mialognorm,add = TRUE,col="yellow")
# curve(expr = miaweib,add = TRUE,col="violet")
# legend("topright", lty = 1, 
#        col = c("yellow","violet"), 
#        legend = c("lognormal distribution","weibull distribution"))


shifted_values <- function(M){
  retmat <- M
  for (i in 1:nrow(M)) {
    minim <- min(M[i,])
    if(minim<=0)retmat[i,] <- M[i,] - minim  +0.01
  }
  retmat
}

minors_row <- function(M){
  ret_vect <- rep(0,nrow(M))
  for (i in 1:nrow(M)) {
    ret_vect[i] <- length(M[i,M[i,]<=0])
  }
  ret_vect
}

#values of genes are negative so we need to shift them all to be positive
  # weibullpar <- function(M){
  #     retmat <- matrix(nrow = nrow(M),ncol = 2)
  #     for (i in 1:nrow(M)) {
  #       retmat[i,] <- fitdist( M[i,] ,distr = "weibull",lower = c(0.01,0.01))$estimate
  #     }
  #     retmat
  # }
  
  weibullpar_total <- function(M){
    global_min <- min(M)
    if(global_min<=0)M <- M -global_min +0.0001
    retmat <- matrix(nrow = nrow(M),ncol = 2)
    for (i in 1:nrow(M)) {
      retmat[i,] <- fitdist( M[i,] ,distr = "weibull",lower = c(0.01,0.01))$estimate
    }
    retmat
  }


# weibullpar2 <- function(M){
#   retmat <- matrix(nrow = nrow(M),ncol = 2)
#   for (i in 1:nrow(M)) {
#     retmat[i,] <- fitdistr( M[i,] ,densfun = "weibull",lower = c(0.01,0.01))$estimate
#   }
#   retmat
# }

  normalpar <- function(M){
    retmat <- matrix(nrow = nrow(M),ncol = 2)
    for (i in 1:nrow(M)) {
      retmat[i,] <- fitdistr(M[i,],densfun = "normal")$estimate
    }
    retmat
  }

# lognormalpar <- function(M){
#   retmat <- matrix(nrow = nrow(M),ncol = 2)
#   for (i in 1:nrow(M)) {
#     retmat[i,] <- fitdistr(M[i,],densfun = "lognormal")$estimate
#   }
#   retmat
# }

# lognormalpar_enhanched <- function(M){
#   minors_vect <- minors_row(M)
#   retmat <- matrix(nrow = nrow(M),ncol = 2)
#   for (i in 1:nrow(M)) {
#     if(minors[i]>100){
#       retmat[i,]<- fitdistr(M[i,] - min(M[i,]) +0.0001,densfun = "lognormal")$estimate
#     }
#     else{
#       retmat[i,] <- fitdistr(M[i,M[i,]>0],densfun = "lognormal")$estimate
#     }
#   }
#   retmat
# }

lognormalpar_total <- function(M){
  global_min <- min(M)
  if(global_min<=0)M <- M -global_min +0.0001
  retmat <- matrix(nrow = nrow(M),ncol = 2)
  for (i in 1:nrow(M)) {
      retmat[i,] <- fitdistr(M[i,],densfun = "lognormal")$estimate
  }
  retmat
}


#shifted_values.genes <- shifted_values(training.data.genes)
# parametri_weibull <- weibullpar(shifted_values(training.data.genes[1:2,]))
# parametri_normal <- normalpar(training.data.genes[1:2,])
# parametri_lognormal <- lognormalpar(shifted_values(training.data.genes[1:2,]))

# best.fit.ks <- function(M){
#   options(warn=-1)
#   shifted_values.genes <- shifted_values(M)
#   parametri_weibull <- weibullpar(shifted_values.genes)
#   parametri_normal <- normalpar(M)
#   parametri_lognormal <- lognormalpar(shifted_values.genes)
#   best.fit.ks <- parametri_lognormal
#   outdistri <- "lognormal"
#   if(ks.test(shifted_values.genes, "pweibull", parametri_weibull)$p.value > ks.test(M, "pnorm", parametri_normal)$p.value){
#     if (ks.test(shifted_values.genes, "pweibull", parametri_weibull)$p.value > ks.test(shifted_values.genes, "plnorm", parametri_lognormal)$p.value) {
#       best.fit.ks <- parametri_weibull
#       outdistri <- "weibull"
#     }
#   } else{
#     if (ks.test(M, "pnorm", parametri_normal)$p.value > ks.test(shifted_values.genes, "plnorm", parametri_lognormal)$p.value) {
#       best.fit.ks <- parametri_normal
#       outdistri <- "normal"
#     }
#   }
#   options(warn=0)
#   list(best.fit.ks,outdistri)
# }
# 
# fittonew <- best.fit.ks(training.data.genes[,1:20])
# 
#   best.fit.ks2 <- function(M){
#     shifted_values.genes <- shifted_values(M)
#     parametri_weibull <- weibullpar(shifted_values.genes)
#     parametri_normal <- normalpar(M)
#     parametri_lognormal <- lognormalpar(shifted_values.genes)
#     best.fit.ks <- parametri_lognormal
#     outdistri <- rep("lognormal",nrow(parametri_lognormal))
#     for (i in 1:nrow(parametri_lognormal)) {
#       if(ks.test(shifted_values.genes[i,], "pweibull", parametri_weibull[i,1], parametri_weibull[i,2])$statistic < ks.test(M[i,], "pnorm", parametri_normal[i,1], parametri_normal[i,2])$statistic){
#         if (ks.test(shifted_values.genes[i,], "pweibull", parametri_weibull[i,1], parametri_weibull[i,2])$statistic < ks.test(shifted_values.genes[i,], "plnorm", parametri_lognormal[i,1], parametri_lognormal[i,2])$statistic) {
#           best.fit.ks[i,] <- parametri_weibull[i,]
#           outdistri[i] <- "weibull"
#         }
#       } else{
#         if (ks.test(M[i,], "pnorm", parametri_normal[i,1], parametri_normal[i,2])$statistic < ks.test(shifted_values.genes[i,], "plnorm", parametri_lognormal[i,1], parametri_lognormal[i,2])$statistic) {
#           best.fit.ks[i,] <- parametri_normal[i,]
#           outdistri[i] <- "normal"
#         }
#       }
#     }
#     list(best.fit.ks,outdistri)
#   }
  
  best.fit.ks_total <- function(M){
    parametri_weibull <- weibullpar_total(M)
    parametri_normal <- normalpar(M)
    parametri_lognormal <- lognormalpar_total(M)
    best.fit.ks <- parametri_lognormal
    shifted_values.genes <- M - min(M) +0.0001
    outdistri <- rep("lognormal",nrow(parametri_lognormal))
    for (i in 1:nrow(parametri_lognormal)) {
      ks.test.weib <- ks.test(shifted_values.genes[i,], "pweibull", parametri_weibull[i,1], parametri_weibull[i,2])$statistic
      ks.test.norm <- ks.test(M[i,], "pnorm", parametri_normal[i,1], parametri_normal[i,2])$statistic
      ks.test.lognorm <-  ks.test(shifted_values.genes[i,], "plnorm", parametri_lognormal[i,1], parametri_lognormal[i,2])$statistic
        
      if((ks.test.weib < ks.test.norm) 
         && (ks.test.weib < ks.test.lognorm)) {
          best.fit.ks[i,] <- parametri_weibull[i,]
          outdistri[i] <- "weibull"
      }
      if((ks.test.lognorm < ks.test.norm) 
         && (ks.test.lognorm < ks.test.weib)) {
        best.fit.ks[i,] <- parametri_lognormal[i,]
        outdistri[i] <- "lognormal"
      }
      if((ks.test.norm < ks.test.weib) 
         && (ks.test.norm < ks.test.lognorm)) {
        best.fit.ks[i,] <- parametri_normal[i,]
        outdistri[i] <- "normal"
      }
    }
    list(best.fit.ks,outdistri)
  }
  
# fittonewhy2 <- best.fit.ks2(training.data.genes)

fit_total <- best.fit.ks_total(data.genes)
# fit_total2 <- best.fit.ks_total(data.genes)
#   fit_total[[1]] <- rbind(fit_total[[1]],fit_total2[[1]])
#   fit_total[[2]] <- c(fit_total[[2]],fit_total2[[2]])
fit_total[[1]] <- data.frame(fit_total[[1]],row.names = gene.names)
rm(data.genes)
  



###parte aggiuntiva che il professore mi ha trasciato dato che si è dimenticato di avermela data :VD
funzione_prob_greater_than <- function(M,par_fit){
  M <- out_no_na_col( M)
  ret_mat <- matrix(data = 0,nrow = nrow(M),ncol = ncol(M))
  if(fittonewhy[[2]]=="normal"){
    for (i in 1:nrow(M)) {
      ret_mat[i,] <- pnorm(M[i,],mean = par_fit[[1]][i,1],sd = par_fit[[1]][i,2],lower.tail = FALSE)
    }
  }else {
    shift_test <- shifted_weibull(M)
    if(fittonewhy[[2]]=="lognormal"){
      for (i in 1:nrow(M)) ret_mat[i,] <-plnorm(shift_test[i,],meanlog = par_fit[[1]][i,1],sdlog =  par_fit[[1]][i,2],lower.tail = FALSE) 
    } else {
      for (i in 1:nrow(M)) ret_mat[i,] <- pweibull(shift_test[i,],shape = par_fit[[1]][i,1],scale = par_fit[[1]][i,2],lower.tail = FALSE)
    }
  }
  ret_mat
}
# probabilità_test <- funzione_prob_greater_than(test.data.genes,fittonewhy)

funzione_prob_greater_than2 <- function(M,par_fit){
  shift_test <- shifted_values(M)
  ret_mat <- matrix(data = 0,nrow = nrow(M),ncol = ncol(M))
  for (i in 1:nrow(M)) {
    if(fittonewhy[[2]][i]=="normal"){
    
        ret_mat[i,] <- pnorm(M[i,],mean = par_fit[[1]][i,1],sd = par_fit[[1]][i,2],lower.tail = FALSE)
    }else {
      shift_test <- shifted_weibull(M)
      if(fittonewhy[[2]][i]=="lognormal"){
        ret_mat[i,] <-plnorm(shift_test[i,],meanlog = par_fit[[1]][i,1],sdlog =  par_fit[[1]][i,2],lower.tail = FALSE) 
      } else {
        ret_mat[i,] <- pweibull(shift_test[i,],shape = par_fit[[1]][i,1],scale = par_fit[[1]][i,2],lower.tail = FALSE)
      }
    }
  }
  ret_mat
}

funzione_ipotesi <- function(M,par_fit,significance){
  pval_mat <- funzione_prob_greater_than(M,par_fit)
  ret_mat <- matrix(FALSE,nrow = nrow(pval_mat),ncol = ncol(pval_mat))
  for (i in 1:nrow(pval_mat)) {
    for (j in 1:ncol(pval_mat)) {
        ret_mat[i,j] = pval_mat[i,j]>significance
    }
  }
  ret_mat
}
# ipotesi_test <- funzione_ipotesi(test.data.genes,fittonewhy,0.05)

funzione_ipotesi_opt <- function(M,par_fit,significance){
  greaterthanfunc <- function(x,y,mat,alpha){
    mat[x,y] <- (mat[x,y]>alpha) 
  }
  greaterthanfunc_vect <- Vectorize(greaterthanfunc,vectorize.args = c("x","y"))
  outer(1:nrow(M),1:ncol(M),greaterthanfunc_vect,funzione_prob_greater_than(M,par_fit),significance)
}
ipotesi_test <- funzione_ipotesi_opt(test.data.genes,fittonewhy,0.05)

library(proxy)

    dendro.cluster.pvalue_func <- function(M,par_fit){
      M <- out_no_na_col(M)
      distance_vec <- dist((funzione_prob_greater_than(M,par_fit)),method = "cosine",by_rows = FALSE)
      hiera_cluster.pvalue <- hclust(distance_vec)
    }

# hiera.clust <- dendro.cluster.pvalue_func(test.data.genes,fittonewhy)
# plot(hiera.clust, labels = colnames(out_no_na_col(test.data.genes)))

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

hcluster_pvalue_cos <- function(M,par_fit){
  distance_vec <- dist((funzione_prob_greater_than(M,par_fit)),method = "cosine",by_rows = FALSE)
  hiera <- hclust(distance_vec)
  opt_clust <- optimal_numclast(hiera,distance_vec)
  list(cutree(hiera,opt_clust),opt_clust)
    
}

# hiera.clust <-  hcluster_pvalue_cos(test.data.genes,fittonewhy)

hcluster_ipotesi_jac <- function(M,par_fit){
  distance_vec <- dist((funzione_ipotesi(M,par_fit,0.05)),method = "jaccard",by_rows = FALSE)
  hiera <- hclust(distance_vec) 
  opt_clust <- optimal_numclast(hiera,distance_vec)
  list(cutree(hiera,opt_clust),opt_clust)  
}

# hiera.clust_ipotesi <- hcluster_ipotesi_jac(test.data.genes,fittonewhy)


library(fpc)
# cluster_pvalue.pam <-pamk(funzione_prob_greater_than(test.data.genes,fittonewhy),krange = 1:3,criterion = "asw",usepam = TRUE)  


cluster_pvalue_cos <- function(M,par_fit){
  distance_vec <- dist((funzione_prob_greater_than(M,par_fit)),method = "cosine",by_rows = FALSE)
  pamk(distance_vec,diss = TRUE,krange =  2:(nrow(distance_vec)-1),criterion = "asw",usepam = TRUE)
}

cluster_ipotesi_jac <- function(M,par_fit){
  distance_vec <- dist((funzione_ipotesi(M,par_fit,0.05)),method = "jaccard",by_rows = FALSE)
  pamk(distance_vec,diss = TRUE,krange = 2:(nrow(distance_vec)-1),criterion = "asw",usepam = TRUE)
}
 
# cluster_pvalue_cos.pam <- cluster_pvalue_cos(test.data.genes,fittonewhy)
# cluster_ipotesi.pam <- cluster_ipotesi_jac(test.data.genes,fittonewhy)

#cluster_pvalue_cos.pam <- pamk(distance_vec,diss = TRUE,krange = 1:(nrow(distance_vec)-1),criterion = "asw",usepam = TRUE)
#cluster_ipotesi.pam <- pamk(distance_vec,diss = TRUE,krange = 1:(nrow(distance_vec)-1),criterion = "asw",usepam = TRUE)


# plot(cluster_pvalue_cos.pam$pamobject)
# 
# plot(cluster_ipotesi.pam$pamobject)
# 
# plot(hiera_cluster.ipotesi)
# 
# pool_malattie <- read.delim("malattie.txt",header = FALSE,sep = "\n")

patologie_func <- function(nrecord,pool){
  ret_vec <- rep(pool$V1[1],nrecord)
  for (i in 1:nrecord) {
    
    ret_vec[i] <-  pool$V1[floor(dim(pool_malattie[1])*runif(1,min = 0.1))]
    
  }
  ret_vec
}

# attr_vect <- patologie_func(ncol(out_no_na_col(test.data.genes)),pool_malattie)

attr_vect.cluster <- function(patologie_vect,cluster_vect,numk){
  vectret <- c(rep("",numk))
  vectnum <- rep(0,numk)
  for (i in 1:numk) {
    tt <- table(patologie_vect[cluster_vect== i])
    vectret[i] <- names(tt[tt==max(tt)])[1]
    vectnum[i] <- max(tt)
  }
  list(vectret,vectnum)
}
   
# attr_lust <- attr_vect.cluster(attr_vect,cluster_pvalue_cos.pam$pamobject$clustering,cluster_ipotesi.pam$nc)
# 
# attr_clust <- attr_vect.cluster(attr_vect,cutree(hiera_cluster.ipotesi,k=2),2)




# plot(silhouette(cluster_pvalue_cos.pam$pamobject$clustering,k=2),distance_vec.ipotesi)


#clustering sui parametri
library(fpc)
library(proxy)


hcluster_par_cos_weib <- function(par_fit){
  distance_vec <- dist(par_fit[[1]][par_fit[[2]]=="weibull",],method = "cosine")
  hiera <- hclust(distance_vec)
  opt_clust <- optimal_numclast(hiera,distance_vec)
  list(cutree(hiera,opt_clust),opt_clust)
  
}

hcluster_par_eucli_weib <- function(par_fit){
  distance_vec <- dist(par_fit[[1]][par_fit[[2]]=="weibull",],method = "euclidean")
  hiera <- hclust(distance_vec)
  opt_clust <- optimal_numclast(hiera,distance_vec)
  list(cutree(hiera,opt_clust),opt_clust)
  
}

cluster_par_eucli_weib <- function(par_fit){
  distance_vec <- dist(par_fit[[1]][par_fit[[2]]=="weibull",],method = "euclidean")
  pamk(distance_vec,diss = TRUE,krange =  2:(nrow(distance_vec)-1),criterion = "asw",usepam = TRUE)
}

cluster_par_cos_weib <- function(par_fit){
  distance_vec <- dist(par_fit[[1]][par_fit[[2]]=="weibull",],method = "cosine")
  pamk(distance_vec,diss = TRUE,krange =  2:(nrow(distance_vec)-1),criterion = "asw",usepam = TRUE)
}

hcluster_par_cos_norm <- function(par_fit){
  distance_vec <- dist(par_fit[[1]][par_fit[[2]]=="normal",],method = "cosine")
  hiera <- hclust(distance_vec)
  opt_clust <- optimal_numclast(hiera,distance_vec)
  list(cutree(hiera,opt_clust),opt_clust)
  
}

hcluster_par_eucli_norm <- function(par_fit){
  distance_vec <- dist(par_fit[[1]][par_fit[[2]]=="normal",],method = "euclidean")
  hiera <- hclust(distance_vec)
  opt_clust <- optimal_numclast(hiera,distance_vec)
  list(cutree(hiera,opt_clust),opt_clust)
  
}

cluster_par_eucli_norm <- function(par_fit){
  distance_vec <- dist(par_fit[[1]][par_fit[[2]]=="normal",],method = "euclidean")
  pamk(distance_vec,diss = TRUE,krange =  2:(nrow(distance_vec)-1),criterion = "asw",usepam = TRUE)
}

cluster_par_cos_norm <- function(par_fit){
  distance_vec <- dist(par_fit[[1]][par_fit[[2]]=="normal",],method = "cosine")
  pamk(distance_vec,diss = TRUE,krange =  2:(nrow(distance_vec)-1),criterion = "asw",usepam = TRUE)
}

hcluster_par_cos_lognorm <- function(par_fit){
  distance_vec <- dist(par_fit[[1]][par_fit[[2]]=="lognormal",],method = "cosine")
  hiera <- hclust(distance_vec)
  opt_clust <- optimal_numclast(hiera,distance_vec)
  list(cutree(hiera,opt_clust),opt_clust)
  
}

hcluster_par_eucli_lognorm <- function(par_fit){
  distance_vec <- dist(par_fit[[1]][par_fit[[2]]=="lognormal",],method = "euclidean")
  hiera <- hclust(distance_vec)
  opt_clust <- optimal_numclast(hiera,distance_vec)
  list(cutree(hiera,opt_clust),opt_clust)
  
}

cluster_par_eucli_lognorm <- function(par_fit){
  distance_vec <- dist(par_fit[[1]][par_fit[[2]]=="lognormal",],method = "euclidean")
  pamk(distance_vec,diss = TRUE,krange =  2:(nrow(distance_vec)-1),criterion = "asw",usepam = TRUE)
}

cluster_par_cos_lognorm <- function(par_fit){
  distance_vec <- dist(par_fit[[1]][par_fit[[2]]=="lognormal",],method = "cosine")
  pamk(distance_vec,diss = TRUE,krange =  2:(nrow(distance_vec)-1),criterion = "asw",usepam = TRUE)
}

cluster_par_eucli_lognorm_parallel <- function(par_fit){
  distance_vec <- parDist(par_fit[[1]][par_fit[[2]]=="lognormal",],method = "euclidean")
  pamk(distance_vec,diss = TRUE,krange =  2:(nrow(distance_vec)-1),criterion = "asw",usepam = TRUE)
}

pam.lognorm.eucli <- cluster_par_eucli_lognorm_parallel(fit_total)

temp_hcluster_par_eucli_lognorm <- function(distance_vec){
  hiera <- hclust(distance_vec)
  opt_clust <- optimal_numclast(hiera,distance_vec)
  list(cutree(hiera,opt_clust),opt_clust)
  
}

temp_cluster_par_eucli_lognorm_parallel <- function(distance_vec){
  pamk(distance_vec,diss = TRUE,krange =  2:(nrow(distance_vec)-1),criterion = "asw",usepam = TRUE)
}

optimal_kmeans <- function(M){
  max <- -2
  opt_clust <- -1
  ret_kmeans <- kmeans(M,centers = 3,nstart = 25)
  distances <- parDist(as.matrix(M))
  for (numk in 2:(50)) {
    ret_kmeans <- kmeans(M,centers = numk,nstart = 25)
    silcas <- silhouette(ret_kmeans$cluster,distances)
    if(class(silcas)=="silhouette" ){
      if(summary(silcas)$avg.width < max + 0.1)return(ret_kmeans)
    }
  }
  ret_kmeans
}

optimal_kmeansbinary <- function(M){
  l <- 2
  m <- 0
  r <- 50
  max <- -2
  ret_kmeans <- kmeans(M,centers = 3,nstart = 25)
  distances <- parDist(as.matrix(M))
  while ( l<=r ){
    m <-  l + ((r - l)* 2)
    ret_kmeans <- kmeans(M,centers = m,nstart = 25)
    silcas <- silhouette(ret_kmeans$cluster,distances)
    if (class(silcas)=="silhouette" && summary(silcas)$avg.width > max ){
      max <- summary(silcas)$avg.width
      l = m +1;
    }
    else
      r = m -1;
  }
  ret_kmeans
}

library(ClusterR)

optimal_ext_kmeans <- function(M){
  ottimali <- Optimal_Clusters_KMeans(M,max_clusters = 50,criterion = "silhouette")
  kmeans(M,centers = which(ottimali==max(ottimali))+1,nstart = 25)
}

kmeans_lognormal <- optimal_ext_kmeans(fit_total[[1]][fit_total[[2]]=="lognormal",])
kmeans_lognormal <- kmeans(fit_total[[1]][fit_total[[2]]=="lognormal",],centers = 3,nstart = 25)
kmeans_normal <- optimal_ext_kmeans(fit_total[[1]][fit_total[[2]]=="normal",])
kmeans_normal <- kmeans(fit_total[[1]][fit_total[[2]]=="normal",],centers = 3,nstart = 25)
kmeans_weibull <- optimal_ext_kmeans(fit_total[[1]][fit_total[[2]]=="weibull",])

kmeans_total_normal <- kmeans(normalpar(data.genes),centers = 3,nstart = 25)

plot(normalpar(data.genes),col=kmeans_total_normal$cluster,xlab="mean",ylab="sd",main="total normal clustering")
points(kmeans_total_normal$centers, col = 0, pch = 8, cex = 2)

plot(fit_total[[1]][fit_total[[2]]=="lognormal",],col=kmeans_lognormal$cluster,xlab="lmean",ylab="lsd",main="lognormal clustering")
points(kmeans_lognormal$centers, col = 0, pch = 8, cex = 2)

plot(fit_total[[1]][fit_total[[2]]=="normal",],col=kmeans_normal$cluster,xlab="mean",ylab="sd",main="normal clustering")
points(kmeans_normal$centers, col = 0, pch = 8, cex = 2)

plot(fit_total[[1]][fit_total[[2]]=="weibull",],col=kmeans_weibull$cluster,xlab="shape",ylab="scale",main="weibull clustering")
points(kmeans_weibull$centers, col = 0, pch = 8, cex = 2)

temp_kmeans_par_parallel <- function(distance_vec){
  kmeans(distance_vec,diss = TRUE,krange =  2:(nrow(distance_vec)-1),criterion = "asw",usepam = TRUE)
}
  
cluster_numeri <- temp_hcluster_par_eucli_lognorm(distance_vec_logn_eucli)  
cluster_numeri2 <- temp_cluster_par_eucli_lognorm_parallel(distance_vec_logn_eucli)
cluster_numeri3 <-  temp_cluster_par_eucli_lognorm_parallel(distance_vec_normal_eucli)
cluster_numeri4 <-  temp_cluster_par_eucli_lognorm_parallel(distance_vec_weib_eucli)