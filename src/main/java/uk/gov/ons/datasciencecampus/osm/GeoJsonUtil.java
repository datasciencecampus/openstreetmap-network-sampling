package uk.gov.ons.datasciencecampus.osm;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;
import org.wololo.jts2geojson.GeoJSONWriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Phil on 17/05/2017.
 * &copy; (2017) Data Science Campus, ONS.
 */
public class GeoJsonUtil {
  /**
   * convert an array of points to a geo_json feature collection string.
   *
   * @param points array of points
   * @return string of geojson.
   */
  public static String toGeoJsonPoints(final MyPoint[] points) {
    final GeoJSONWriter geoJSONWriter = new GeoJSONWriter();
    final int len = points.length;
    final Feature[] features = new Feature[len];
    for (int i = 0; i < len; i++) {
      final String feature_id = Integer.toString(i);
      final org.wololo.geojson.Point wololo_point
              = (org.wololo.geojson.Point) geoJSONWriter.write(points[i].getPoint());
      final Map<String, Object> feature_properties = new HashMap<>();
      // geojson simple style spec
      // https://github.com/mapbox/simplestyle-spec/tree/master/1.1.0
      feature_properties.put("marker-symbol", "circle");
      feature_properties.put("marker-size", "small");
      feature_properties.put("marker-color", "#ff0000");
      features[i] = new Feature(feature_id, wololo_point, feature_properties);
    }
    final FeatureCollection featureCollection = new FeatureCollection(features);
    return featureCollection.toString();
  }

  /**
   * convert points into geo_json feature collection of linestrings projected given point's angle+90 degrees and
   * a fixed distance.
   *
   * @param points   array of points
   * @param distance fixed distance to project.
   * @return string of geojson.
   */
  public static String toGeoJsonLines(final MyPoint[] points,
                                      final double distance) {
    final GeoUtil geoUtil = GeoUtil.getInstance();
    final GeoJSONWriter geoJSONWriter = new GeoJSONWriter();
    final int len = points.length;
    final Feature[] features = new Feature[len];
    for (int i = 0; i < len; i++) {
      final String feature_id = Integer.toString(i);
      final MyPoint myPoint = points[i];
      final double bearing = (myPoint.getBearing() + 90) % 360;
      final com.vividsolutions.jts.geom.LineString lineString
              = geoUtil.projectLineString(myPoint.getPoint().getCoordinate(), distance, bearing);
      final org.wololo.geojson.LineString wololo_line
              = (org.wololo.geojson.LineString) geoJSONWriter.write(lineString);
      final Map<String, Object> feature_properties = new HashMap<>();
      features[i] = new Feature(feature_id, wololo_line, feature_properties);
    }
    final FeatureCollection featureCollection = new FeatureCollection(features);
    return featureCollection.toString();
  }

  /**
   * geojson filtered roads for debugging.
   *
   * @param roads @see Road.
   * @return string on geojson.
   */
  public static String toGeoJsonLines(final Road[] roads) {
    final GeometryFactory gf = GeoUtil.getInstance().getGf();
    final GeoJSONWriter geoJSONWriter = new GeoJSONWriter();
    final int len = roads.length;
    final List<Feature> features = new ArrayList<>();
    for (int i = 0; i < len; i++) {
      final Road road = roads[i];
      if (!road.isIntersectsArea())
        continue;
      final List<Coordinate> coords = new ArrayList<>();
      for (final MyPoint myPoint : road) {
        final Point p = myPoint.getPoint();
        coords.add(new Coordinate(p.getX(), p.getY()));
      }
      if (coords.size() < 2)
        continue;
      final CoordinateSequence cs = new CoordinateArraySequence(coords.toArray(new Coordinate[0]));
      final String feature_id = Integer.toString(i);
      final com.vividsolutions.jts.geom.LineString lineString = new LineString(cs, gf);
      final org.wololo.geojson.LineString wololo_line
              = (org.wololo.geojson.LineString) geoJSONWriter.write(lineString);
      final Map<String, Object> feature_properties = new HashMap<>();
      features.add(new Feature(feature_id, wololo_line, feature_properties));
    }
    final FeatureCollection featureCollection = new FeatureCollection(features.toArray(new Feature[0]));
    return featureCollection.toString();
  }

}
