package uk.gov.ons.datasciencecampus.osm.test;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import org.opengis.referencing.operation.TransformException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uk.gov.ons.datasciencecampus.osm.*;

import static org.testng.Assert.*;
import static uk.gov.ons.datasciencecampus.osm.test.GeoUtilTest.Fixture.*;

/**
 * Created by Phil on 16/05/2017.
 * &copy; (2017) Data Science Campus, ONS.
 */
public class GeoUtilTest {

  // some test data
  interface Fixture {

    ////// small one
    // start
    double x1 = -3.1483692;
    double y1 = 51.5077946;

    // expected midpoint
    double x2 = -3.148364;
    double y2 = 51.507705;

    // end
    double x3 = -3.1483587;
    double y3 = 51.5076154;

    ////// big one
    // (starts with a hook, then traces a road)
    double[][] coords = {
            {51.505536, -3.107547},
            {51.505271, -3.108014},
            {51.505211, -3.108100},
            {51.504677, -3.108304},
            {51.501738, -3.106834},
            {51.497607, -3.100153},
            {51.501853, -3.085608},
            {51.510834, -3.062151},
            {51.511068, -3.061733},
            {51.511388, -3.061476},
            {51.511775, -3.060500},
            {51.512156, -3.058462},
            {51.515648, -3.045171},
            {51.519413, -3.043729},
            {51.519505, -3.043744},
            {51.519620, -3.043811},
            {51.519728, -3.043921},
            {51.519687, -3.043704},
            {51.519681, -3.043649},
            {51.519680, -3.043558},
            {51.519711, -3.043491},
            {51.519794, -3.043429},
            {51.519936, -3.043367},
            {51.520049, -3.043252},
            {51.520249, -3.042989},
            {51.520804, -3.042105},
            {51.520089, -3.040654},
            {51.519905, -3.040438},
            {51.519790, -3.040363},
            {51.519381, -3.040258},
            {51.519281, -3.040137},
            {51.519256, -3.039917},
            {51.519568, -3.037789},
            {51.519552, -3.036628},
            {51.519142, -3.032781},
            {51.519188, -3.032531},
            {51.519420, -3.031748}
    };
  }

  private final GeometryFactory gf = new GeometryFactory();

  // this'll do.
  private final Precision precision = Precision.ONE_CM;

  // sut
  private GeoUtil geoUtil;

  @BeforeClass
  public void init() {
    this.geoUtil = GeoUtil.getInstance();
  }

  @Test
  public void round() {
    assertEquals(geoUtil.round(-3.3133731337, Precision.ONE_CM), -3.3133731);
    assertEquals(geoUtil.round(-3.3133731337, Precision.TEN_CM), -3.313373);
    assertEquals(geoUtil.round(-3.3133731337, Precision.ONE_METRE), -3.31337);
    assertEquals(geoUtil.round(-3.3133731337, Precision.TEN_METRES), -3.3134);
    assertEquals(geoUtil.round(100, null), 100, 0.0000001);
    assertEquals(geoUtil.round(100, Precision.EXACT), 100, 0.0000001);

  }

  @Test
  public void interpolatePoints() {

    final Coordinate c1 = new Coordinate(x1, y1);
    final Coordinate c2 = new Coordinate(x3, y3);

    // get the midpoint.
    final MyPoint myPoint = geoUtil.interpolate(c1, c2, 0.5);
    assertNotNull(myPoint);

    final Point point = myPoint.getPoint();
    assertNotNull(point);

    assertEquals(geoUtil.round(point.getX(), precision), x2);
    assertEquals(geoUtil.round(point.getY(), precision), y2);
    assertEquals(myPoint.getBearing(), 177.91, 1);
  }

  /**
   * interpolation_distance &gt; line_string distance
   * should return midpoint.
   */
  @Test
  public void interpolateShortLineString() {
    final Coordinate[] coordinates = new Coordinate[]{
            new Coordinate(x1, y1), new Coordinate(x3, y3)
    };
    final CoordinateSequence cs = new CoordinateArraySequence(coordinates);

    final LineString ls = new LineString(cs, gf);
    final MyPoint[] myPoints = geoUtil.interpolate(ls, 1000);

    assertNotNull(myPoints);
    assertTrue(myPoints.length == 1);

    final MyPoint myPoint = myPoints[0];
    assertNotNull(myPoint);

    final Point point = myPoint.getPoint();
    assertNotNull(point);

    assertEquals(geoUtil.round(point.getX(), precision), x2);
    assertEquals(geoUtil.round(point.getY(), precision), y2);
  }

  // debugging
  // way/23698847,Heathfield Terrace,residential,,,,,,,,,,,
  @Test
  public void anotherShortOne() {

    final Coordinate[] coordinates = new Coordinate[]{
            new Coordinate(0.1650763, 51.401734),
            new Coordinate(0.1651242, 51.4017662)
    };
    final CoordinateSequence cs = new CoordinateArraySequence(coordinates);

    final LineString ls = new LineString(cs, gf);
    final MyPoint[] myPoints = geoUtil.interpolate(ls, 10);

    assertNotNull(myPoints);
    assertTrue(myPoints.length == 1);

    final MyPoint myPoint = myPoints[0];
    assertNotNull(myPoint);

    final Point point = myPoint.getPoint();
    assertNotNull(point);
  }

  /**
   * long one. some points &lt; 10m apart, somme &gt; 10m apart.
   */
  @Test
  public void interpolateLongStraightLineString() {
    final int interpolDistance = 100;
    final double delta = 0.01; // 1cm of error.
    final double[][] coords = Fixture.coords;

    final Coordinate[] coordinates = new Coordinate[coords.length];
    for (int i = 0, len = coords.length; i < len; i++) {
      final double[] coord = coords[i];
      coordinates[i] = new Coordinate(coord[1], coord[0]);
    }

    final CoordinateSequence cs = new CoordinateArraySequence(coordinates);

    final LineString ls = new LineString(cs, gf);
    final MyPoint[] myPoints = geoUtil.interpolate(ls, interpolDistance);
    assertNotNull(myPoints);
    assertTrue(myPoints.length > 0);

    // debug
    //Util.dumpString(GeoJsonUtil.toGeoJsonPoints(myPoints), "/tmp/x.geojson");
    //Util.dumpString(GeoJsonUtil.toGeoJsonLines(myPoints, 50), "/tmp/y.geojson");

    try {
      for (int i = 1, len = myPoints.length; i < len; i++) {
        final Coordinate prev = myPoints[i - 1].getPoint().getCoordinate();
        final Coordinate next = myPoints[i].getPoint().getCoordinate();
        final double pDist = geoUtil.distance(prev, next);
        assertTrue(pDist - interpolDistance <= delta, "point " + i + " error");
      }
    } catch (TransformException e) {
      e.printStackTrace();
    }
    assertNotNull(myPoints);
  }

  @Test
  public void forwardAzimuth() {
    final double bearing = geoUtil.forwardAzimuth(y1, x1, y3, x3);
    assertEquals(bearing, 177.91, 1);
  }

  @Test
  public void distance() {
    try {
      final double distance = geoUtil.distance(y1, x1, y3, x3);
      assertEquals(distance, 20, 0.1); // 10cm tolerance
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

}
