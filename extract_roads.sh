#!/bin/bash
## extract road network from osm.

OSM_DATA="great-britain-latest.osm"
JSON_CITIES="data/area_geojson"
NATIONAL_ROADS="gb_roads.osm"
OUTPUT="data/city_roads_geojson"

mkdir -p $OUTPUT

# 0) get ons major town and city polygons.
cd $JSON_CITIES
./split.sh
cd -

# 1) download data
if [ ! -f $OSM_DATA.bz2 ] && [ ! -f $OSM_DATA ] ;then 
  echo "downloading."
  wget http://download.geofabrik.de/europe/$OSM_DATA.bz2
fi

if [ ! -f $OSM_DATA ] ;then
  echo "unziping.."
  bunzip2 $OSM_DATA.bz2
fi

# 2) next extract all roads from the osm snapshot  
if [ ! -f $NATIONAL_ROADS ] ;then
  echo "extracting."
  osmium tags-filter $OSM_DATA w/highway=primary,secondary,tertiary,unclassified,residential \
      -o $NATIONAL_ROADS
fi

# 3) for each city, filter out road network and convert to geojson.
echo "filtering."
for city in $(ls $JSON_CITIES/split |sed 's/ /;/g') ;do
  city_file=$(echo $city |sed 's/;/ /g')
  if [ ! -f $OUTPUT/"$city_file" ] ;then
    echo "extracting $city..."
    osmium extract -p $JSON_CITIES/split/"$city_file" $NATIONAL_ROADS -o _roads.osm
    node --max_old_space_size=8192 $(which osmtogeojson) _roads.osm > $OUTPUT/"$city_file"
    rm -f _roads.osm
  fi
done
