# just remove points with no associated city.
x <- read.csv("england_and_wales.csv")
x <- subset(x, city != "")
x <- subset(x, road_name != "")
write.csv(x, "england_and_wales.csv", row.names=F, quote=F, na="")
