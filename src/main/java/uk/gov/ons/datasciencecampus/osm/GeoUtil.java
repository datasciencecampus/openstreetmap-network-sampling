package uk.gov.ons.datasciencecampus.osm;

import java.util.List;
import java.util.ArrayList;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import com.vividsolutions.jts.linearref.LinearLocation;
import org.opengis.referencing.operation.TransformException;

import static java.lang.Math.sin;
import static java.lang.Math.cos;
import static java.lang.Math.atan2;
import static java.lang.Math.asin;
import static java.lang.Math.toRadians;
import static java.lang.Math.toDegrees;
import static java.lang.Math.PI;

/**
 * Created by Phil on 16/05/2017.
 * &copy; (2017) Data Science Campus, ONS.
 */
public final class GeoUtil {
  private static double EARTH_RADIUS = 6371009d;


  private CoordinateReferenceSystem crsWGS84;
  private final GeometryFactory gf = new GeometryFactory();


  private GeoUtil() {
    try {
      this.crsWGS84 = CRS.decode("EPSG:4326");
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(-1);
    }
  }

  private final static class LazyHolder {
    static final GeoUtil INSTANCE = new GeoUtil();
  }

  public static GeoUtil getInstance() {
    return LazyHolder.INSTANCE;
  }


  /**
   * get a point given starting point, direction and distance.
   *
   * @param from     starting point
   * @param distance distance (metres) from starting point
   * @param bearing  direction from starting point
   * @return @see Coordinate
   */
  public Coordinate desintationPoint(final Coordinate from,
                                     final double distance,
                                     final double bearing) {
    final double fromLat = from.y;
    final double fromLng = from.x;
    final double distanceRadians = distance / EARTH_RADIUS;
    final double latRadians = toRadians(fromLat);
    final double lngRadians = toRadians(fromLng);
    final double bearingRadians = toRadians(bearing);
    final double destinationLat = asin(sin(latRadians) * cos(distanceRadians) +
            cos(latRadians) * sin(distanceRadians) * cos(bearingRadians));
    double destinationLng = lngRadians + atan2(sin(bearingRadians) * sin(distanceRadians) * cos(latRadians),
            cos(distanceRadians) - sin(latRadians) * sin(destinationLat));
    destinationLng = (destinationLng + 3 * PI) % (2 * PI) - PI;
    return new Coordinate(toDegrees(destinationLng), toDegrees(destinationLat));
  }

  /**
   * make a linestring from a starting point to an end point.
   *
   * @param from     starting point
   * @param distance distance (metres) from starting point
   * @param bearing  direction from starting point
   * @return
   */
  public LineString projectLineString(final Coordinate from,
                                      final double distance,
                                      final double bearing) {
    final Coordinate to = desintationPoint(from, distance, bearing);
    final Coordinate[] coordinates = new Coordinate[]{from, to};
    return new LineString(new CoordinateArraySequence(coordinates), gf);
  }

  /**
   * angle from point A to point B (in degrees)
   * <p>
   * ripped from http://www.movable-type.co.uk/scripts/latlong.html
   *
   * @param lat1 point 1 latitude
   * @param lng1 point 1 longitude
   * @param lat2 point 2 latitude
   * @param lng2 point 2 longitude
   * @return as-the-crow-flys bearing from point 1 to point 2.
   */
  public double forwardAzimuth(final double lat1, final double lng1,
                               final double lat2, final double lng2) {
    final double radLat1 = toRadians(lat1);
    final double radLat2 = toRadians(lat2);
    final double deltaLon = toRadians(lng2 - lng1);
    final double y = sin(deltaLon) * cos(radLat2);
    final double x = cos(radLat1) * sin(radLat2) - sin(radLat1) * cos(radLat2) * cos(deltaLon);
    final double bearing = atan2(y, x);
    return (toDegrees(bearing) + 360) % 360;
  }

  public double distance(final double lat1, final double lng1,
                         final double lat2, final double lng2)
          throws org.opengis.referencing.operation.TransformException {
    final Coordinate c1 = new Coordinate(lng1, lat1);
    final Coordinate c2 = new Coordinate(lng2, lat2);
    return distance(c1, c2);
  }

  public double distance(final Coordinate c1, final Coordinate c2)
          throws org.opengis.referencing.operation.TransformException {
    return JTS.orthodromicDistance(c1, c2, crsWGS84);
  }


  /**
   * interpolate a point along a linestrin==g given a 0 &lt; ratio &lte; 1.
   */
  public MyPoint interpolate(final Coordinate c1,
                             final Coordinate c2,
                             final double segmentRatio) {
    if (segmentRatio <= 0 || segmentRatio > 1) throw new IllegalArgumentException();
    final Coordinate c = LinearLocation.pointAlongSegmentByFraction(c1, c2, segmentRatio);
    final Point p = gf.createPoint(c);
    final double bearing = forwardAzimuth(c1.y, c1.x, c2.y, c2.x);
    return new MyPoint(p, bearing);
  }

  /**
   * convenience function: linestring midpoint
   */
  public MyPoint midpoint(final Coordinate c1, final Coordinate c2) {
    return interpolate(c1, c2, 0.5);
  }

  /**
   * get equidistant points along a linestring.
   * <p>
   * distance expressed in metres.
   * if the length of the linestring is &lt; distance, original will be returned.
   */
  public MyPoint[] interpolate(final LineString ls, final double interpolDistance) {
    // see interpolate() in street.py (much easier using shapely :/
    final Coordinate[] coords = ls.getCoordinates();

    if (coords.length == 1) throw new IllegalArgumentException();

    final List<MyPoint> points = new ArrayList<>();

    try {

      // if just 2 coords and segment length <= interpolDistance, return the midpoint.
      if (coords.length == 2 && distance(coords[0], coords[1]) <= interpolDistance) {
        final MyPoint myPoint = midpoint(coords[0], coords[1]);
        myPoint.setSequence(1);
        points.add(myPoint);
      }

      else {

        Coordinate prevInterpolated = null;
        double cumDist = 0d;
        int i = 1, len = coords.length;
        int j = 0;
        while (i < len) {

          // current segment
          // previous is either last actual point on linestring or the last point we interpolated.
          final Coordinate prev = prevInterpolated == null ? coords[i - 1] : prevInterpolated;
          final Coordinate curr = coords[i];

          // distance from last point till now
          final double segmentDistance = distance(prev, curr);
          final double preCumDist = cumDist;
          cumDist += segmentDistance;

          // keep going
          if (cumDist < interpolDistance) {
            prevInterpolated = null;
            i++; // only keep going if we did not interpolate
          } else {
            // cumDist >= interpolDistance
            // else interpolate a point on this segment
            final double segmentRatio = (interpolDistance - preCumDist) / segmentDistance;
            assert 0 < segmentRatio && segmentRatio <= 1;

            // interpolate a point along this segment.
            final MyPoint interpolatedPoint = interpolate(prev, curr, segmentRatio);
            interpolatedPoint.setSequence(++j);
            points.add(interpolatedPoint);

            // set previous to the point we just interpolated and reset to this new (interpolated) point
            // note: or could just set coords[i] = interpolated since prev = coords[i-1] in next pass...
            prevInterpolated = interpolatedPoint.getPoint().getCoordinate();
            cumDist = 0;
          }

        }

      }

    } catch (TransformException e) {
      e.printStackTrace();
    }
    return points.toArray(new MyPoint[0]);
  }

  /**
   * round a coordinate
   *
   * @param coordinate lat or lng to round.
   * @param precision  @see Precision
   * @return rounded coordinate
   */
  public double round(final double coordinate, Precision precision) {
    final double dec;
    if (precision == null)
      dec = 1d;
    else {
      switch (precision) {
        case ONE_CM:
          dec = Math.pow(10, 7);
          break;
        case TEN_CM:
          dec = Math.pow(10, 6);
          break;
        case ONE_METRE:
          dec = Math.pow(10, 5);
          break;
        case TEN_METRES:
          dec = Math.pow(10, 4);
          break;
        default:
          dec = 1d;
      }

    }
    return (double) Math.round(coordinate * dec) / dec;
  }

  /**
   * get convex hull for array of roads.
   *
   * @param roads @see Road
   * @return convex hull @see Geometry
   */
  public Geometry getConvexHull(final Road[] roads) {
    final List<Geometry> points = new ArrayList<>();
    for (final Road road : roads) {
      for (final MyPoint myPoint : road) {
        points.add(myPoint.getPoint());
      }
    }
    final GeometryCollection gc = new GeometryCollection(points.toArray(new Geometry[0]), gf);
    return gc.convexHull();
  }

  public GeometryFactory getGf() {
    return gf;
  }

}
