package uk.gov.ons.datasciencecampus.osm;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Created by Phil on 13/06/2017.
 * &copy; (2017) Data Science Campus, ONS.
 */
public class Area {
  private final String name;
  private final Geometry geometry;

  public Area(String name, Geometry geometry) {
    this.name = name;
    this.geometry = geometry;
  }

  public String getName() {
    return name;
  }

  public Geometry getGeometry() {
    return geometry;
  }

  @Override
  public String toString() {
    return "Area{" +
            "name='" + name + '\'' +
            ", geometry=" + geometry +
            '}';
  }
}
