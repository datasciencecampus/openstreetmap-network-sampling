#!/bin/bash
## split geojson into 1 file per city.
## deps:
## * nodejs, npm, geosplit
## json_pp

IN="ons_major_towns_and_cities_dec_2015.geojson"
OUT="split"
CITY_KEY="tcity15nm"

function sanity {
  if [ ! -f $IN ] ;then
    # snatch data from http://geoportal.statistics.gov.uk/datasets/major-towns-and-cities-december-2015-boundaries
    echo "downloading ons major-towns-and-cities geojson data..."
    curl -s https://opendata.arcgis.com/datasets/58b0dfa605d5459b80bf08082999b27c_0.geojson > $IN
  fi
  if [ -d $OUT ] ;then
    echo "geojson data already split."
    exit 0
  fi
}

function split {
  mkdir -p $OUT
  geojsplit $IN -l 1 -k $CITY_KEY -o $OUT
}

function split_fix {
  for city in $(ls $OUT |sed 's/ /;/g') ;do
    city_file=$(echo $city |sed 's/;/ /g')
    cat "$OUT/$city_file" \
        |sed 's/^{\"type\":\"FeatureCollection\",\"features\":\[//; s/\]}$//' \
        |json_pp \
        > $OUT/_tmp.geojson
    mv -v $OUT/_tmp.geojson "$OUT/$city_file"
  done
}

command -v geojsplit >/dev/null 2>&1 || {
  echo "install geojsplit. 1) install npm + nodejs 2) npm -i geojsplit"; exit 1;
}

sanity
split
split_fix
