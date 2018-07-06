#!/bin/bash
# OSM ROAD NETWORK SAMPLER
# ========================
#JAR=target/osm-roads-jar-with-dependencies.jar
JAR=target/osm-roads.jar
if [ ! -e $JAR ]; then
  echo "first run make"
  exit 0
fi
if [ $# == 4 ] ;then
  # osm road network sampler
  java -cp $JAR uk.gov.ons.datasciencecampus.osm.Sample "$@"
else
  echo $0 "<interpolation dist> <src road geojson> <src area polygon geojson> <dst>"
  echo "e.g.,"
  echo $0 "10 ../data/city_roads_geojson/Cardiff_xaaaa.geojson ../data/area_geojson/split/Cardiff_xaaaa.geojson ../data/csv_output/cardiff.csv"
  exit 1
fi
