package uk.gov.ons.datasciencecampus.osm;

import com.vividsolutions.jts.geom.Point;

/**
 * Created by Phil on 16/05/2017.
 * &copy; (2017) Data Science Campus, ONS.
 */
public class MyPoint {
  private int sequence;
  private String city;

  // lat, lng
  private final Point point;

  // angle of line between previous and next point
  private final double bearing;

  public MyPoint(final Point point,
                 final double bearing) {
    this.point = point;
    this.bearing = bearing;
  }

  public int getSequence() {
    return sequence;
  }

  public void setSequence(int sequence) {
    this.sequence = sequence;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public double getBearing() {
    return bearing;
  }

  public Point getPoint() {
    return point;
  }

  public String toString() {
    return point + String.format("%.2f", bearing);
  }

  public String toCsv() {
    return new StringBuilder()
            .append(sequence).append(",")
            .append(city != null ? city : "").append(",")
            .append(String.format("%.6f", point.getY())).append(",")
            .append(String.format("%.6f", point.getX())).append(",")
            .append(String.format("%.2f", bearing))
            .toString();
  }
}

