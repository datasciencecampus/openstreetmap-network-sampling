# summarise road data.
# for each city:
# num roads, num points

library(knitr)

x <- read.csv("england_and_wales.csv")

roads <- do.call(rbind, tapply(x$osm_way_id, list(x$city), function(x) {
  data.frame(roads=length(unique(x)), points=length(x))
}))

roads <- roads[with(roads, order(-roads, -points)), ]

print(kable(rbind(roads, TOTAL=colSums(roads), CITIES=c(nrow(roads), NA))))

