

datacounting <- read.csv("sorting.csv")
library(dplyr)
library(ggplot2)
group_by(datacounting,nome)
programmi <- distinct(datacounting,nome)[,1]
class(programmi)
plot(datacounting)


plotsorting <- function(){
  dev.off()
  for (i in 1:length(programmi)){
    par(new=TRUE)
    plot(datacounting[datacounting$nome == programmi[i],]$nels,datacounting[datacounting$nome == programmi[i],]$runtime,"b",col = i,xlab = "nels",ylab = "runtime")
  }
  legend(x=1,y=180,legend = programmi,lty=1:length(programmi), col = c(1:length(programmi)),title="sorting type")  
}
plotsorting()

ggplotsorting <- function(){
  dev.off()
  tuttoplot <- ggplot() +  geom_line(data =datacounting[datacounting$nome == programmi[1],][,2:3],aes(x=nels,y=runtime),color=1)  + xlab("nels") + ylab("runtime(ms)")
  for (i in 2:length(programmi)){
    tuttoplot <- tuttoplot + geom_line(data =datacounting[datacounting$nome == programmi[i],][,2:3],aes(x=nels,y=runtime),color=i)
  }
  tuttoplot
  #legend(x=1,y=180,legend = programmi,lty=1:length(programmi), col = c(1:length(programmi)),title="sorting type")  
}
ggplotsorting()

ggplotsortingmore <- function(){
  ggplot(data = datacounting,aes(x=nels,y=runtime_leaves,group=nome,shape=nome,colour=nome)) + geom_line() + geom_point() +ylab("runtime(ms)")
}

ggplotsortingmore()


ggplot(data = datacounting,aes(x=nome,y=bandwidth,group=nome,shape=nome,colour=nome)) + geom_boxplot() + theme(axis.text.x = element_blank()) 

data_plot_countingHalF <- datacounting[datacounting$nome == "sorting_counting_HALF.csv",2:3]
data_plot_countingHalF
plot(data_plot_countingHalF$nels,data_plot_countingHalF$runtime,"b",col="red")
primiprogdati <- datacounting[datacounting$nome == programmi[1],]
datacounting <- read.csv("table_counting_HALF_discreta.csv")
data_plot_countingHalF <- datacounting[datacounting$nome == "sorting_counting_HALF.csv",2:3]
data_plot_countingHalF
par(new=TRUE)
plot(data_plot_countingHalF$nels,data_plot_countingHalF$runtime,"b",col = "green")
