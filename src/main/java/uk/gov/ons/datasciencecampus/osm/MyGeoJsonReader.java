package uk.gov.ons.datasciencecampus.osm;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.vividsolutions.jts.geom.Geometry;
import org.wololo.geojson.GeoJSONFactory;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.LineString;
import org.wololo.jts2geojson.GeoJSONReader;

/**
 * Created by Phil on 16/05/2017.
 * &copy; (2017) Data Science Campus, ONS.
 */

/**
 * import osm road data which has been serialised in geojson form.
 * e.g., by using the osmtogeojson nodejs util:
 * node --max_old_space_size=8192 `which osmtogeojson` roads.osm &gt; roads.geojson
 */
public class MyGeoJsonReader implements Reader {

  private FeatureCollection loadFeatureCollection(final String src) throws IOException {
    final String json = new String(Files.readAllBytes(Paths.get(src)));
    return (FeatureCollection) GeoJSONFactory.create(json);
  }

  private Feature loadFeature(final String src) throws IOException {
    final String json = new String(Files.readAllBytes(Paths.get(src)));
    return (Feature) GeoJSONFactory.create(json);
  }

  /**
   * use feature props to check if road.
   */
  private boolean isRoad(final Map<String, Object> props) {
    // filter by road type
    final String road_type = (String) props.getOrDefault("highway", "");
    if (!(road_type.equals("primary")
            || road_type.equals("secondary")
            || road_type.equals("tertiary")
            || road_type.equals("unclassified")
            || road_type.equals("residential")))
      return false;

    // filter out other types
    if (props.containsKey("junction")) return false;
    final String surface_type = (String) props.getOrDefault("surface", "");
    if (surface_type.equals("sand")
            || surface_type.equals("mud")
            || surface_type.equals("dirt")
            || surface_type.equals("earth")
            || surface_type.equals("ground")
            || surface_type.equals("grass")
            || surface_type.equals("wood")
            || surface_type.equals("metal"))
      return false;
    final String surface_quality = (String) props.getOrDefault("tracktype", "grade1");
    if (!surface_quality.equals("grade1")) return false;
    return true;
  }

  /**
   * keep roads.
   */
  private Feature[] filter(final Feature[] features) {
    final List<Feature> l = new ArrayList<>();
    for (final Feature f : features)
      if (isRoad(f.getProperties()) && f.getGeometry() instanceof LineString) l.add(f);
    return l.toArray(new Feature[0]);
  }

  /**
   * load org.wololo.geojson roads.
   */
  public Feature[] loadRoads(final String src)
          throws UnsupportedOperationException, IOException {
    final FeatureCollection fc = loadFeatureCollection(src);
    return filter(fc.getFeatures());
  }

  @Override
  public Map<String, Geometry> loadAreas(final String src) throws IOException {
    final Map<String, Geometry> areas = new HashMap<>();
    final GeoJSONReader reader = new GeoJSONReader();
    final FeatureCollection fc = loadFeatureCollection(src);
    final Feature[] features = fc.getFeatures();
    for (final Feature feature : features) {
      final Map<String, Object> props = feature.getProperties();
      final String city = (String) props.get("tcity15nm");
      final Geometry geometry = reader.read(feature.getGeometry());
      assert !areas.containsKey(city);
      areas.put(city, geometry);
    }
    return areas;
  }

  @Override
  public Area loadArea(final String src) throws IOException {
    final GeoJSONReader reader = new GeoJSONReader();
    final Feature feature = loadFeature(src);
    final Map<String, Object> props = feature.getProperties();
    final String name = (String) props.get("tcity15nm");
    assert name != null;
    final Geometry geometry = reader.read(feature.getGeometry());
    return new Area(name, geometry);
  }

}