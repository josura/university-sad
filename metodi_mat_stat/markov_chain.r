#tento una implementazione delle markov chain
library(markovchain)
statesNames <- c("giorgio1","giorgio2","giorgio3")
mat3 <- matrix(c(0.9, 0.1 , 0 , 0.7 , 0.3 , 0 , 0 ,0.1 ,0.9),nrow = 3,byrow = TRUE)
modello.markov <- new("markovchain",transitionMatrix = mat3,states=statesNames)
modello.markov
modello.markov1 <- new("markovchain",transitionMatrix = matrix(c(1,0,0,0,1,0,0,0,1),nrow = 3),states=statesNames)
modello.markov^2
steadyStates(modello.markov)
steadyStates(modello.markov1)


#visualizzazione della markov-chain
plot(modello.markov)
plot(modello.markov1)
library(igraph)

modello.markov2 <- new("markovchain",transitionMatrix = matrix(c(0.1,0.9,0,0,0.2,0.8,0.9,0,0.1),nrow = 3,byrow = TRUE),states=c("0","1","2"))
plot(modello.markov2)

g <- graph(edges = c("2","2" ,"2","0" ,"0","0" ,"0","1" ,"1","1"))
E(g)$weight <- c(0.1,0.9,0.1,0.9,1)
plot(g,edge.label = E(g)$weight)

transition.matrix <- as_adjacency_matrix(graph = g,attr = "weight")
transition.matrix2 <-matrix(c(0.1,0.9,0,0,0.2,0.8,0.9,0,0.1),nrow = 3,byrow = TRUE)

g <- graph_from_adjacency_matrix(transition.matrix2,weighted = TRUE)
V(g)$name <- c(2,0,1)
plot(g,edge.label = E(g)$weight)

states <- colnames(transition.matrix2) <- rownames(transition.matrix2) <- c(2,0,1)
initialstate <- c(0.6,0.3,0.1)

setClass("Markov_chain", slots=list(transition_matrix="matrix", initial_state="numeric", states="character"))
Markov_chain <- function(transitmatrix, initstate,stat){
  for(i in 1:nrow(transitmatrix)){
    cont <- 0
    for(j in 1:ncol(transitmatrix)){
      cont <- cont + transitmatrix[i,j]
    }
    if(cont <= 0.99 || cont >=1.01)stop("la matrice non è stocastica")
  }
  cont <- 0
  for(i in 1:length(initstate)){
    cont <- cont + initstate[i]
  }
  if(cont <= 0.99 || cont >=1.01)stop(paste("gli stati iniziali non sommano a 1 ma a " ,as.character(cont),sep = " "))
  new("Markov_chain",transition_matrix = transitmatrix, initial_state = initstate, states = stat)
}
mia_chain <- Markov_chain(transition.matrix2,initialstate,as.character( states))

plotmark <- function(markovch){
  gr <- graph_from_adjacency_matrix(markovch@transition_matrix,weighted = TRUE)
  V(gr)$name <- markovch@states
  plot(gr,edge.label = E(gr)$weight)
}
plotmark(mia_chain)

get_index_state <- function(markovch,state){
  i <- 1
  for (currstate in markovch@states) {
    if(state == currstate)return(i)
    i=i+1
  }
  0
}

legge_congiunta <- function(markovch,temp = 2,istate=-1,jstate=-1){
  if(class(markovch)!="Markov_chain")stop("devi passare una markov_chain")
  
  tempmat <- markovch@transition_matrix 
  if(temp==1){
    if(get_index_state(markovch,istate)==0 || get_index_state(markovch,jstate)==0){
      return(tempmat)
    } else {return((tempmat)[get_index_state(markovch,istate),get_index_state(markovch,jstate)]) }
    
  }
  for(i in 1:temp){
    tempmat <- tempmat %*% markovch@transition_matrix
  }
  if(get_index_state(markovch,istate)==0 || get_index_state(markovch,jstate)==0){
    tempmat
  }
  else{ 
    (tempmat)[get_index_state(markovch,istate),get_index_state(markovch,jstate)]
  }
}

legge_congiunta_occupazione <- function(markovch,temp = 2,istate=-1,jstate=-1){
  if(class(markovch)!="Markov_chain")stop("devi passare una markov_chain")
  tempmat <- markovch@transition_matrix 
  for(i in 1:temp){
    tempmat <- tempmat %*% markovch@transition_matrix
  }
  if(get_index_state(markovch,istate)==0 || get_index_state(markovch,jstate)==0){
    markovch@initial_state %*% tempmat
  }
  else{ 
    (markovch@initial_state %*% tempmat)[get_index_state(markovch,jstate)]
  }
}

legge_congiunta_occupazioneN <- function(markovch,stati,tempi,numstati){
  sum <-  0
  for(k in 1:numstati){
    multk <- markovch@initial_state[k]*legge_congiunta(markovch,tempi[1],k,stati[1])
     
    for(j in 2:numstati){
      multk= multk * legge_congiunta(markovch,tempi[j]-tempi[j-1],stati[j-1],stati[j])
    }
    sum <- sum +multk
  }
  sum	
}


legge_congiunta(mia_chain,3)
legge_congiunta(mia_chain,3,istate = 2,jstate = 0)
legge_congiunta_occupazione(mia_chain,3)
legge_congiunta_occupazione(mia_chain,3,2,0)
legge_congiunta_occupazioneN(mia_chain,c(0,2,1),c(3, 6,8),3)

communication <- function(markovch,istate,jstate){
  for(i in 1:length(markovch@states)){
    if(legge_congiunta(markovch,i,istate,jstate) > 0) return (TRUE)
  }
  FALSE
}
communication(mia_chain,0,1)
communication(mia_chain,0,2)


closure_subset <- function(markovch,subset){
  for(i in subset){
    for(j in markovch@states){
      if(!(j %in% subset)){
        if(communication(markovch,i,j)==TRUE) return(FALSE)
      }
    }
  }
  TRUE
}

closure_subset(mia_chain,c(2,1,0))

irreducible_subset <- function(markovch,subset){
  if(! closure_subset(markovch,subset))stop("il sottoinsieme non è chiuso")
  for(i in subset){
    for(j in subset){
      if(i!=j){
        if(communication(markovch,i,j)== FALSE) return(FALSE)
      }
    }
  }
  TRUE
}

#irreducible_subset(mia_chain,c(0))

irreducible <- function(markovch){
  for(i in markovch@states){
    for(j in markovch@states){
      if(i!=j){
        if(communication(markovch,i,j)== FALSE) return(FALSE)
      }
    }
  }
  TRUE
}
irreducible(mia_chain)
#irreducible(Markov_chain(matrix(c(0,1,0,0,0,1,1,0,0),nrow = 3,byrow = TRUE),initialstate,as.character( states)))

  absorbing_state <- function(markovch,state){
    index <- get_index_state(markovch,state)
    if((markovch@transition_matrix[index,index])!=1){FALSE}
    else {TRUE}
  }

absorbing_state(mia_chain,1)


recurrent_state <- function(markovch,state){
    for(i in markovch@states){
      if(communication(markovch,state,i)){
        if(!communication(markovch,i,state))return(FALSE)
      }
    }
  TRUE
}
recurrent_state(markovch = mia_chain,0)


periodic_state_period <- function(markovch,state){
  for (i in 1:length(markovch@states)) {
    if(legge_congiunta(markovch,i,state,state))return(i)
  }
  -1
}

periodic_state <- function(markovch,state){
  if(periodic_state_period(markovch,state)<=1)return(FALSE)
  TRUE
}

periodic_state_period(mia_chain,2)
periodic_state(mia_chain,1)

aperiodic_chain <- function(markovch){
  for(i in markovch@states){
    if(periodic_state(markovch,i))return(FALSE)
  }
  TRUE
}
aperiodic_chain(mia_chain)


regularity_chain <- function(markovch){
        tempmat <-  markovch@transition_matrix
        for(i in 1:length(markovch@states)){
          count <- 0;
          for(row in 1:length(markovch@states)){
            for(col in 1:length(markovch@states)){
              if(tempmat[row,col]>0){count <- count + 1}
            }	
          }
          if(count>=length(markovch@states)^2)return(TRUE)
          tempmat = tempmat %*% markovch@transition_matrix
        }
        return(FALSE)
}

regularity_chain(mia_chain)

plotmark(mia_chain)
stationary_state_matrixmult <- function(markovch){
  if(!regularity_chain(markovch))stop("la catena non è regolare")
  library(expm)
  markovch@initial_state%*%(markovch@transition_matrix %^% 50)
}

stationary_state_matrixmult(mia_chain)

stationary_state_eigenvect <- function(markovch){
  if(!regularity_chain(markovch))stop("la catena non è regolare")
  eigenvect <- eigen(t( markovch@transition_matrix))[[2]]
  eigenval <- eigen(t(mia_chain@transition_matrix),only.values = TRUE)[[1]]
  for(i in 1:ncol(eigenvect)){
    if(Re( eigenval[i])>= 1-0.001 && Re( eigenval[i])<= 1+0.001){
      normalizer <- 1/( rep(1,nrow(eigenvect)) %*% Re(eigenvect[,i]) )
      return(Re(eigenvect[,i])%*% normalizer)
      # contperc <- 0
      # contequi <- 0
      # for (j in 1:nrow(eigenvect)) {
      #     contequitemp <- 0
      #   contperc <- contperc + eigenvect[j,i]
      #   for (st in 1:nrow(eigenvect)) {
      #     contequitemp <- contequitemp + eigenvect[st,i] * markovch@transition_matrix[st,j]
      #   }
      #   if(contequitemp >= Re(eigenvect[j,i])-0.001 && contequitemp <= Re(eigenvect[j,i])+0.001){contequi <- contequi +1}
      # }
      # if(contequi>=nrow(eigenvect) && contperc>=1){return(eigenvect[,i])}
    }
  }
  c(0,0,0)
}
stationary_state_eigenvect(mia_chain)
