#!/bin/bash
## for each region, sample the points.
## sampling involves interpolating 10 metre intervals along all roads,
## calculation of the bearing of the interpolated point (angle of line
## from previous and next point) and then assigning areas to each point.
## the area in this case is the ons_major_towns_and_cities_dec_2015
## definition of a major city which has been derived from the ons
## definition of a built up area. finally, aggregate into 1 csv.

# paths relative to osm_parser
areas="data/area_geojson/split"
csv_output="data/csv_output"

# interpolation distance (10 metres)
ipd=10

function process_regions
{
  for city_roads in $(ls data/city_roads_geojson/*.geojson |sed 's/ /;/g') ;do
    city_file=$(echo $city_roads |sed 's/data\/city_roads_geojson\///; s/\.geojson//; s/;/ /g')
    city_name=$(echo $city_file |sed 's/_xaaaa//')
    if [ ! -f "${csv_output}/${city_name}.csv" ] ;then
      echo "procesing ${city_name}..."
      ./run_network_sampler.sh $ipd "data/city_roads_geojson/${city_file}.geojson" "${areas}/${city_file}.geojson" "${csv_output}/${city_name}.csv"
    fi
  done
}

function aggregate
{
  echo "osm_way_id,road_name,road_type,sidewalk,abutters,lanes,lighting,surface,speed_limit,sequence,city,latitude,longitude,bearing" > ${csv_output}/final/england_and_wales.csv
  for i in $(ls ${csv_output}/*.csv |sed 's/ /;/g') ;do
    city=$(echo $i |sed 's/^.*\///; s/\.csv//; s/;/ /g')
    echo "aggregating ${city}..."
    # cat region, skip first line
    tail -n+2 "${csv_output}/${city}.csv" >> ${csv_output}/final/england_and_wales.csv
    # if need to add extra column (e.g., csv filename/area)    
    #|awk -v x="$city" -F ',' '{print $0","x}'
  done
}

echo "processing the regions..."
process_regions

echo "aggregating the results..."
aggregate
