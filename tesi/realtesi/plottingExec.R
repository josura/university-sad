library(ggplot2)

datacounting <- read.csv("risultati.csv")
datacountingaltro <- read.csv("risultatiAltro.csv")
dataCumulativi <- read.csv("risultaticumulativi.csv")
dataQueryIncrementali <- read.csv("risultatiQueryincrementaliTot.csv")
dataTargetIncrementali <- read.csv("risultatiTargetIncrementaliTot.csv")

dataBoxTop <- read.csv("RisultatiBoxTot.csv")
dataBoxTopbigdata <- read.csv("RisultatiBoxTotNonOOF.csv")

primaquery <- datacounting[datacounting$QueryID == datacounting[1,1],]
secondaquery <- datacounting[datacounting$QueryID == datacounting[2,1],]
primaqueryaltro <- datacountingaltro[datacountingaltro$QueryID == datacountingaltro[1,1],]
secondaqueryaltro <- datacountingaltro[datacountingaltro$QueryID == datacountingaltro[2,1],]

datacountingtutto <- read.csv("risultatiTot.csv")
primaquerytot <- datacountingtutto[datacounting$QueryID == datacounting[1,1],]
secondaquerytot <- datacountingtutto[datacounting$QueryID == datacounting[2,1],]
ggplot() +  
  geom_line(data =primaquerytot,aes(x=numnodesTarget,y=tNum_occtTime..secs.,group=algoritmo,color=algoritmo)) +
  xlab("numNodes") + 
  ylab("runtime(sec)")
ggplot() +  
  geom_line(data =secondaquerytot,aes(x=numnodesTarget,y=tNum_occtTime..secs.,group=algoritmo,color=algoritmo)) +
  xlab("numNodes") + 
  ylab("runtime(sec)")
ggplot() +  
  geom_line(data =primaquerytot,aes(x=numedgesTarget,y=tNum_occtTime..secs.,group=algoritmo,color=algoritmo)) +
  xlab("numEdges") + 
  ylab("runtime(sec)")
ggplot() +  
  geom_line(data =secondaquerytot,aes(x=numedgesTarget,y=tNum_occtTime..secs.,group=algoritmo,color=algoritmo)) +
  xlab("numEdges") + 
  ylab("runtime(sec)")


  ggplot() +  
  geom_line(data =primaquery,aes(x=numnodesTarget,y=tNum_occtTime..secs.),color=1)  + 
  geom_line(data =primaqueryaltro,aes(x=numnodesTarget,y=tNum_occtTime..secs.),color=3)  + 
  xlab("numNodes") + 
  ylab("runtime(sec)")
ggplot() +  
  geom_line(data =secondaquery,aes(x=numnodesTarget,y=tNum_occtTime..secs.),color=1)  + 
  geom_line(data =secondaqueryaltro,aes(x=numnodesTarget,y=tNum_occtTime..secs.),color=3)  + 
  xlab("numNodes") + 
  ylab("runtime(sec)")
ggplot() +  
  geom_line(data =secondaquery,aes(x=numedgesTarget,y=tNum_occtTime..secs.),color=1)  + 
  geom_line(data =secondaqueryaltro,aes(x=numedgesTarget,y=tNum_occtTime..secs.),color=3)  + 
  xlab("numEdges") + ylab("runtime(sec)")
ggplot() +  
  geom_line(data =primaquery,aes(x=numedgesTarget,y=tNum_occtTime..secs.),color=1)  + 
  geom_line(data =primaqueryaltro,aes(x=numedgesTarget,y=tNum_occtTime..secs.),color=3)  + 
  xlab("numEdges") + ylab("runtime(sec)")

ggplot() +  geom_line(data =dataCumulativi[1:18,],aes(x=numnodes,y=tNum_occtTime..secs.,group=algorithm,color=algorithm)) +
  xlab("numNodes") + 
  ylab("runtime(sec)")
  
ggplot() +  geom_line(data =dataCumulativi[1:18,],aes(x=numedges,y=tNum_occtTime..secs.,group=algorithm,color=algorithm)) +
  xlab("numEdges") + 
  ylab("runtime(sec)")


ggplot() +  geom_line(data =dataQueryIncrementali,aes(x=numNodes,y=execTime,group=algoritmo,color=algoritmo)) +
  xlab("numNodes") + 
  ylab("runtime(sec)")

ggplot() +  geom_line(data =dataQueryIncrementali,aes(x=numEdges,y=execTime,group=algoritmo,color=algoritmo)) +
  xlab("numEdges") + 
  ylab("runtime(sec)")

#target variabili
ggplot() +  geom_line(data =dataTargetIncrementali,aes(x=numNodes,y=execTime,group=algoritmo,color=algoritmo)) +
  xlab("numNodes") + 
  ylab("runtime(sec)")

ggplot() +  geom_line(data =dataTargetIncrementali,aes(x=numEdges,y=execTime,group=algoritmo,color=algoritmo)) +
  xlab("numEdges") + 
  ylab("runtime(sec)")

dataBoxTop$numnodesQuery <-  factor(dataBoxTop$numnodesQuery,levels = c('4','8','16','32','48'),ordered = TRUE)
dataBoxTopbigdata$numnodesQuery <-  factor(dataBoxTopbigdata$numnodesQuery,levels = c('4','8','16','32','48'),ordered = TRUE)

ggplot(dataBoxTop[dataBoxTop$dataset=="email-dnc",],aes(x=numnodesQuery,y=execTime)) + geom_boxplot(aes(colour=algoritmo)) + ggtitle("email-dnc")
ggplot(dataBoxTop[dataBoxTop$dataset=="ia-contacts_dublin",],aes(x=numnodesQuery,y=execTime)) + geom_boxplot(aes(colour=algoritmo)) + ggtitle("ia-contacts_dublin")
ggplot(dataBoxTop[dataBoxTop$dataset=="SFHH-conf-sensor",],aes(x=numnodesQuery,y=execTime)) + geom_boxplot(aes(colour=algoritmo)) + ggtitle("SFHH-conf-sensor")
ggplot(dataBoxTop[dataBoxTop$dataset=="fb-forum",],aes(x=numnodesQuery,y=execTime)) + geom_boxplot(aes(colour=algoritmo)) + ggtitle("fb-forum")
ggplot(dataBoxTop[dataBoxTop$dataset=="edit-enwikibooks",],aes(x=numnodesQuery,y=execTime)) + geom_boxplot(aes(colour=algoritmo)) + ggtitle("edit-enwikibooks")

ggplot(dataBoxTopbigdata[dataBoxTopbigdata$dataset=="Ca-cit-HepPh",],aes(x=numnodesQuery,y=execTime)) + geom_boxplot(aes(colour="TemporalRI")) + ggtitle("Ca-cit-HepPh")
ggplot(dataBoxTopbigdata[dataBoxTopbigdata$dataset=="Fb-wosn-friend",],aes(x=numnodesQuery,y=execTime)) + geom_boxplot(aes(colour="TemporalRI")) + ggtitle("Fb-wosn-friend")
ggplot(dataBoxTopbigdata[dataBoxTopbigdata$dataset=="Ia-enron-email-dynamic",],aes(x=numnodesQuery,y=execTime)) + geom_boxplot(aes(colour="TemporalRI")) + ggtitle("ia-enron-email-dynamic")
ggplot(dataBoxTopbigdata[dataBoxTopbigdata$dataset=="Soc-epinions-trust-dir",],aes(x=numnodesQuery,y=execTime)) + geom_boxplot(aes(colour="TemporalRI")) + ggtitle("soc-epinions-trust-dir")
