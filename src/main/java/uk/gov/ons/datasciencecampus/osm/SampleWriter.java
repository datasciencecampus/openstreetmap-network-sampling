package uk.gov.ons.datasciencecampus.osm;

/**
 * Created by Phil on 24/05/2017.
 * &copy; (2017) Data Science Campus, ONS.
 */
public class SampleWriter implements Writer {

  @Override
  public void writeCsv(final Road[] roads, final String dst) {
    final StringBuilder sb = new StringBuilder()
            .append("id").append(",")
            .append("osm_way_id").append(",")
            .append("road_name").append(",")
            .append("road_type").append(",")
            .append("sidewalk").append(",")
            .append("abutters").append(",")
            .append("lanes").append(",")
            .append("lighting").append(",")
            .append("surface").append(",")
            .append("speed_limit").append(",")
            .append("sequence").append(",")
            .append("city").append(",")
            .append("latitude").append(",")
            .append("longitude").append(",")
            .append("bearing").append("\n");
    for (final Road road : roads)
      sb.append(road.toCsv());
    Util.dumpString(sb.toString(), dst);
  }
}
