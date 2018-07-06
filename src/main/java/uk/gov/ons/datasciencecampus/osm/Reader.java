package uk.gov.ons.datasciencecampus.osm;

import com.vividsolutions.jts.geom.Geometry;
import org.wololo.geojson.Feature;

import java.io.IOException;
import java.util.Map;


/**
 * Created by Phil on 16/05/2017.
 * &copy; (2017) Data Science Campus, ONS.
 */

/**
 * read from some source containing osm data.
 * e.g., raw osm, xml, geojson etc.
 */
public interface Reader {

  Feature[] loadRoads(String src) throws IOException;

  Map<String, Geometry> loadAreas(String src) throws IOException;

  Area loadArea(String src) throws IOException;

}