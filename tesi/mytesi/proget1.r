

library(minpack.lm)
fitexp <- nlsLM(totale_attualmente_positivi ~ alp*(data^bet),start = list(alp=0.01,bet=0.01), data = datichemiserv )
fitexp1 <- nlsLM(totale_attualmente_positivi ~(1+ alp*data)^bet,start = list(alp=0.01,bet=0.01), data = datichemiserv )
coeffi_pow <- coef(fitexp)
coeffi_pow1 <- coef(fitexp1)
tutplot  + stat_function(fun = function(x) coeffi_pow[1]*(x^coeffi_pow[2]) ) + xlim(0,30)
tutplot  + stat_function(fun = function(x) (1+coeffi_pow1[1]*x)^coeffi_pow1[2] ) + xlim(0,30)