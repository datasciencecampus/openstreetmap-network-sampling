package uk.gov.ons.datasciencecampus.osm;

/**
 * Created by Phil on 17/05/2017.
 * &copy; (2017) Data Science Campus, ONS.
 */
public interface Writer {
  void writeCsv(Road[] roads, String dst);
}