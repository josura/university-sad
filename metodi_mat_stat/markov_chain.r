#tento una implementazione delle markov chain
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


modello.markov2 <- new("markovchain",transitionMatrix = matrix(c(0.1,0.9,0,0,1,0,0.9,0,0.1),nrow = 3,byrow = TRUE),states=c("0","1","2"))
plot(modello.markov2)

g <- graph(edges = c("2","2" ,"2","0" ,"0","0" ,"0","1" ,"1","1"))
E(g)$weight <- c(0.1,0.9,0.1,0.9,1)
plot(g,edge.label = E(g)$weight)

transition.matrix <- as_adjacency_matrix(graph = g,attr = "weight")
transition.matrix2 <-matrix(c(0.1,0.9,0,0,1,0,0.9,0,0.1),nrow = 3,byrow = TRUE)

states <- colnames(transition.matrix2) <- rownames(transition.matrix2) <- c(2,0,1)
initialstate <- c(0.6,0.3,0.1)

setClass("Markov_chain", slots=list(transition_matrix="matrix", initial_state="numeric", states="character"))
Markov_chain <- function(transitmatrix, initstate,stat){
  for(i in 1:nrow(transitmatrix)){
    cont <- 0
    for(j in 1:ncol(transitmatrix)){
      cont <- cont + transitmatrix[i,j]
    }
    if(cont != 1 )stop("la matrice non è stocastica")
  }
  new("Markov_chain",transition_matrix = transitmatrix, initial_state = initstate, states = stat)
}

legge_congiunta <- function(markovch,temp = 2,istate=0,jstate=0){
  if(class(markovch)!="Markov_chain")stop("devi passare una markov_chain")
  tempmat <- markovch@transition_matrix 
  for(i in 1:temp){
    tempmat <- tempmat %*% markovch@transition_matrix
  }
  if(istate==0 || jstate==0){
    tempmat
  }
  else{ 
    tempmat[istate,jstate]
  }
}
legge_congiunta(mia_chain,3)
legge_congiunta(mia_chain,3,istate = 1,jstate = 2)
initialstate%*%transition.matrix2  
