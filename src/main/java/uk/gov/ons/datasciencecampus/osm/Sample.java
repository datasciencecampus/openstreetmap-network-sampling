package uk.gov.ons.datasciencecampus.osm;

/**
 * Created by Phil on 16/05/2017.
 * &copy; (2017) Data Science Campus, ONS.
 */

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import org.wololo.geojson.Feature;
import org.wololo.jts2geojson.GeoJSONReader;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

/**
 * sample points from road network
 */
public class Sample {
  private final static String USE
          = "<interpolation distance> <src road geojson> <src area polygon geojson> <dst dir>";
  private final GeoUtil geoUtil = GeoUtil.getInstance();
  private final GeoJSONReader reader = new GeoJSONReader();
  private final MyGeoJsonReader myGeoJsonReader = new MyGeoJsonReader();

  private MyPoint[] interpolate(final Feature road, final double interpolationDistance) {
    final LineString lineString = (LineString) reader.read(road.getGeometry());
    return geoUtil.interpolate(lineString, interpolationDistance);
  }

  public void shuffle(final Road[] roads) {
    final Random prng = new Random();
    for (int i = 0, len = roads.length; i < len; i++) {
      final int j = prng.nextInt(len);
      final Road tmp = roads[i];
      roads[i] = roads[j];
      roads[j] = tmp;
    }
  }

  private String getProperty(final String key, final Map<String, Object> props) {
    final String s = (String) props.getOrDefault(key, "");
    // sanitise
    return s.replaceAll(",", ";");
  }

  public Feature[] loadRoads(final String srcRoads) throws IOException {
    return myGeoJsonReader.loadRoads(srcRoads);
  }

  public Road[] interpolateRoads(final Feature[] roads, final double interpolationDistance) {
    // now, for each road feature create an interpolated road.
    final int len = roads.length;
    final Road[] interpolatedRoads = new Road[len];
    for (int i = 0; i < len; i++) {
      final Feature road = roads[i];
      final MyPoint[] myPoints = interpolate(road, interpolationDistance);
      assert (myPoints.length > 0);
      final Road interpolatedRoad = new Road(myPoints);
      final Map<String, Object> props = road.getProperties();
      interpolatedRoad.setOsmWayid(getProperty("id", props));
      interpolatedRoad.setRoadName(getProperty("name", props));
      interpolatedRoad.setRoadType(getProperty("highway", props));
      interpolatedRoad.setSidewalk(getProperty("sidewalk", props));
      interpolatedRoad.setAbutters(getProperty("abutters", props));
      if (props.containsKey("lanes")) {
        final String lanes = getProperty("lanes", props);
        if (lanes != null & lanes.matches("^[1-9]$")) {
          interpolatedRoad.setLanes(Integer.parseInt(lanes));
        }
      }
      interpolatedRoad.setLighting(getProperty("lit", props));
      interpolatedRoad.setSurface(getProperty("surface", props));
      if (props.containsKey("maxspeed")) {
        final String maxSpeed = getProperty("maxspeed", props);
        if (maxSpeed != null && maxSpeed.endsWith("mph")) {
          final String mphs = maxSpeed.substring(0, maxSpeed.indexOf("mph") - 1);
          final int mph = (mphs != null && !mphs.equals("")) ? Integer.parseInt(mphs) : 0;
          interpolatedRoad.setSpeedLimit(mph);
        }

      }
      interpolatedRoads[i] = interpolatedRoad;
    }
    return interpolatedRoads;
  }

  public void assignArea(final Road[] roads, final String srcArea) throws IOException {
    final Area area = myGeoJsonReader.loadArea(srcArea);
    final String name = area.getName();
    final Geometry geometry = area.getGeometry();
    for (final Road road : roads) {
      System.out.println("processing " + road.getRoadName());
      for (final MyPoint myPoint : road) {
        if (geometry.contains(myPoint.getPoint())) {
          myPoint.setCity(name);
          road.setIntersectsArea(true);
        }
      }
    }
  }

  public List<Road[]> splitRoads(final Road[] roads, final int n) {
    final int len = roads.length;
    final int sub = (int) Math.ceil(len / (double) n);
    final List<Road[]> split = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      final int from = i * sub;
      final int to = Math.min(from + sub, len);
      final Road[] subArray = new Road[to - from];
      for (int j = from; j < to; j++) {
        subArray[j - from] = roads[j];
      }
      split.add(subArray);
    }
    return split;
  }

  public static void main(String[] args) throws IOException {
    if (args.length < 3) {
      System.out.println(USE);
      System.exit(0);
    }

    final double interpolationDistance = Double.parseDouble(args[0]);
    final String srcRoads = args[1];
    final String srcArea = args[2];
    final String dst = args[3];

    final Sample sample = new Sample();

    // read in road geojson (for the city)
    final Feature[] roads = sample.loadRoads(srcRoads);

    // now, for each road feature create an interpolated road.
    double t = System.currentTimeMillis();
    final Road[] interpolatedRoads = sample.interpolateRoads(roads, interpolationDistance);
    System.out.println("interpolation took " + (System.currentTimeMillis() - t) + "ms.");


    // for each point on each interpolated road, assign an area.
    // do this in parallel. split roads into sub-arrays of #roads / #cores

    // distribute uniformly
    sample.shuffle(interpolatedRoads);

    final int cores = Runtime.getRuntime().availableProcessors();
    System.out.println("using " + cores + " cpu cores.");
    final List<Road[]> split = sample.splitRoads(interpolatedRoads, cores);
    final CountDownLatch countDownLatch = new CountDownLatch(cores);

    // assign areas using #core threads.
    t = System.currentTimeMillis();
    for (final Road[] splitRoads : split) {
      Executors.defaultThreadFactory().newThread(new Runnable() {
        @Override
        public void run() {
          try {
            System.out.println("minion " + Thread.currentThread().getName() + " starting.");

            // cut off the interpolated points if they are not inside the city.
            sample.assignArea(splitRoads, srcArea);

            System.out.println("minion " + Thread.currentThread().getName() + " time to die...");
            countDownLatch.countDown();
          } catch (IOException e) {
            System.err.println(e.getMessage());
          }
        }
      }).start();
    }

    // wait here for all threads to finish work.
    try {
      countDownLatch.await();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    System.out.println("area filtering took " + (System.currentTimeMillis() - t) + "ms.");

    // write out the interpolated roads
    final Writer writer = new SampleWriter();
    writer.writeCsv(interpolatedRoads, dst);
  }
}
