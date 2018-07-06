# road directions are not nuiform, they tend to be either n,e,s,w.
# also a strong bias toward east or west in this case.
# relatiionship between road incidents and sun direction?

library(ggplot2)
library(ggthemes)
library(gridExtra)


plot_city <- function(city, city.name="") {
  #bearings <- city$bearing
  bearings <- with(city, aggregate(bearing, list(osm_way_id), mean))$x
  bearings <- bearings[!is.na(bearings)]
  bearings <- floor(bearings)
  bearings[bearings == 360] <- 0
  df <- as.data.frame(table(bearings))
  df$bearings <- as.numeric(df$bearings)
  g <- ggplot(df, aes(x=bearings, y=Freq))
  g <- g + coord_polar(theta="x")
  g <- g + geom_bar(stat="identity")
  g <- g + scale_x_continuous(breaks=seq(0, 360, 60))
  g <- g + ggtitle(city.name)
  g + theme_tufte()
}

x <- read.csv("../hampshire.csv")

do.call(grid.arrange, lapply(unique(x[x$area != "", ]$area), function(city.name) {
  plot_city(subset(x, area == city.name), city.name)
}))

