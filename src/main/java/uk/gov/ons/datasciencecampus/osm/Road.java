package uk.gov.ons.datasciencecampus.osm;

import java.util.Iterator;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

/**
 * Created by Phil on 16/05/2017.
 * &copy; (2017) Data Science Campus, ONS.
 */
public class Road implements Iterable<MyPoint> {
  private String osmWayid;
  private String roadName;
  private String roadType;
  private String sidewalk;
  private String abutters;
  private int lanes;
  private String lighting;
  private String surface;
  private int speedLimit;
  private boolean intersectsArea;

  private final List<MyPoint> points = new ArrayList<>();

  public Road(final String roadName) {
    this.roadName = roadName;
  }

  public Road(final MyPoint[] points) {
    this.points.addAll(Arrays.asList(points));
  }

  public Road addPoint(final MyPoint point) {
    points.add(point);
    return this;
  }

  public String getOsmWayid() {
    return osmWayid;
  }

  public void setOsmWayid(String osmWayid) {
    this.osmWayid = osmWayid;
  }

  public String getRoadName() {
    return roadName;
  }

  public void setRoadName(String roadName) {
    this.roadName = roadName;
  }

  public String getRoadType() {
    return roadType;
  }

  public void setRoadType(String roadType) {
    this.roadType = roadType;
  }

  public String getSidewalk() {
    return sidewalk;
  }

  public void setSidewalk(String sidewalk) {
    this.sidewalk = sidewalk;
  }

  public String getAbutters() {
    return abutters;
  }

  public void setAbutters(String abutters) {
    this.abutters = abutters;
  }

  public int getLanes() {
    return lanes;
  }

  public void setLanes(int lanes) {
    this.lanes = lanes;
  }

  public String getLighting() {
    return lighting;
  }

  public void setLighting(String lighting) {
    this.lighting = lighting;
  }

  public String getSurface() {
    return surface;
  }

  public void setSurface(String surface) {
    this.surface = surface;
  }

  public int getSpeedLimit() {
    return speedLimit;
  }

  public void setSpeedLimit(int speedLimit) {
    this.speedLimit = speedLimit;
  }

  public boolean isIntersectsArea() {
    return intersectsArea;
  }

  public void setIntersectsArea(boolean intersectsArea) {
    this.intersectsArea = intersectsArea;
  }

  @Override
  public Iterator<MyPoint> iterator() {
    return points.iterator();
  }

  public String toString() {
    final List<MyPoint> p = this.points;
    final int len = p.size();
    if (len == 0) return "<empty>";
    final StringBuilder sb = new StringBuilder();
    for (final MyPoint myPoint : p)
      sb.append(myPoint).append(",");
    return sb.append(p.get(len - 1)).toString();
  }

  public String toCsv() {
    if (!intersectsArea) return "";
    final List<MyPoint> points = this.points;
    final int len = points == null ? 0 : points.size();
    if (len == 0) return "";
    final StringBuilder sb = new StringBuilder();
    final StringBuilder roadProps = new StringBuilder()
            .append(osmWayid).append(",")
            .append(roadName).append(",")
            .append(roadType != null ? roadType : "").append(",")
            .append(sidewalk != null ? sidewalk : "").append(",")
            .append(abutters != null ? abutters : "").append(",")
            .append(lanes != 0 ? Integer.toString(lanes) : "").append(",")
            .append(lighting != null ? lighting : "").append(",")
            .append(surface != null ? surface : "").append(",")
            .append(speedLimit != 0 ? Integer.toString(speedLimit) : "").append(",");
    for (int i = 0; i < len; i++) {
      final MyPoint myPoint = points.get(i);
      if (myPoint.getCity() != null)
        sb.append(roadProps).append(myPoint.toCsv()).append("\n");
    }
    return sb.toString();
  }
}