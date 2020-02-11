

datacounting <- read.csv("table_counting_HALF_integrata.csv")
data_plot_countingHalF <- datacounting[datacounting$nome == "sorting_counting_HALF.csv",2:3]
data_plot_countingHalF
plot(data_plot_countingHalF$nels,data_plot_countingHalF$runtime,"b",col="red")

datacounting <- read.csv("table_counting_HALF_discreta.csv")
data_plot_countingHalF <- datacounting[datacounting$nome == "sorting_counting_HALF.csv",2:3]
data_plot_countingHalF
par(new=TRUE)
plot(data_plot_countingHalF$nels,data_plot_countingHalF$runtime,"b",col = "green")
