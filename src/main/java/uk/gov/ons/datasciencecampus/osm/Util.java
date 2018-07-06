package uk.gov.ons.datasciencecampus.osm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Phil on 17/05/2017.
 * &copy; (2017) Data Science Campus, ONS.
 */
public class Util {

  public static int dumpString(final String s, final String dst) {
    if (s == null) throw new IllegalArgumentException("nope.");
    try {
      final byte[] b = s.getBytes();
      Files.write(Paths.get(dst), b, StandardOpenOption.CREATE_NEW);
      return b.length;
    } catch (final IOException e) {
      e.printStackTrace();
    }
    return -1;
  }
}
