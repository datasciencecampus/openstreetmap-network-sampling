#!/bin/bash
## generate points to work with.

# 1. extract the road network for each city
./extract_roads.sh

# 2. point interpolation and filtering
./sample_points.sh
