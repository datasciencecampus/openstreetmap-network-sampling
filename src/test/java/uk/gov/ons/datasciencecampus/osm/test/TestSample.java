package uk.gov.ons.datasciencecampus.osm.test;


import org.testng.annotations.Test;
import uk.gov.ons.datasciencecampus.osm.Road;
import uk.gov.ons.datasciencecampus.osm.Sample;

import java.util.List;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

/**
 * Created by Phil on 01/06/2017.
 * &copy; (2017) Data Science Campus, ONS.
 */
public class TestSample {
  private Sample sample = new Sample();

  @Test
  public void testSplitRoadsA() {
    Road[] roads = new Road[]{
            new Road("a"),
            new Road("b"),
            new Road("c"),
            new Road("d"),
            new Road("e"),
            new Road("f"),
    };
    List<Road[]> split = sample.splitRoads(roads, 2);
    assertNotNull(split);
    assertEquals(2, split.size());

    Road[] seg1 = split.get(0);
    assertNotNull(seg1);
    assertEquals(3, seg1.length);
    assertEquals("a", seg1[0].getRoadName());
    assertEquals("b", seg1[1].getRoadName());
    assertEquals("c", seg1[2].getRoadName());

    Road[] seg2 = split.get(1);
    assertNotNull(seg2);
    assertEquals(3, seg2.length);
    assertEquals("d", seg2[0].getRoadName());
    assertEquals("e", seg2[1].getRoadName());
    assertEquals("f", seg2[2].getRoadName());
  }

  @Test
  public void testSplitRoadsB() {
    Road[] roads = new Road[]{
            new Road("a"),
            new Road("b"),
            new Road("c"),
            new Road("d"),
            new Road("e"),
            new Road("f"),
    };
    List<Road[]> split = sample.splitRoads(roads, 1);
    assertNotNull(split);
    assertEquals(1, split.size());

    Road[] seg1 = split.get(0);
    assertNotNull(seg1);
    assertEquals(6, seg1.length);
    assertEquals("a", seg1[0].getRoadName());
    assertEquals("b", seg1[1].getRoadName());
    assertEquals("c", seg1[2].getRoadName());
    assertEquals("d", seg1[3].getRoadName());
    assertEquals("e", seg1[4].getRoadName());
    assertEquals("f", seg1[5].getRoadName());
  }

  @Test
  public void testSplitRoadsC() {
    Road[] roads = new Road[]{
            new Road("a"),
            new Road("b"),
            new Road("c"),
            new Road("d"),
            new Road("e"),
            new Road("f"),
    };
    List<Road[]> split = sample.splitRoads(roads, 3);
    assertNotNull(split);
    assertEquals(3, split.size());

    Road[] seg1 = split.get(0);
    assertNotNull(seg1);
    assertEquals(2, seg1.length);
    assertEquals("a", seg1[0].getRoadName());
    assertEquals("b", seg1[1].getRoadName());

    Road[] seg2 = split.get(1);
    assertNotNull(seg2);
    assertEquals(2, seg2.length);
    assertEquals("c", seg2[0].getRoadName());
    assertEquals("d", seg2[1].getRoadName());

    Road[] seg3 = split.get(2);
    assertNotNull(seg3);
    assertEquals(2, seg3.length);
    assertEquals("e", seg3[0].getRoadName());
    assertEquals("f", seg3[1].getRoadName());
  }

  @Test
  public void testSplitRoadsD() {
    Road[] roads = new Road[]{
            new Road("a"),
            new Road("b"),
            new Road("c"),
            new Road("d"),
            new Road("e"),
            new Road("f"),
    };
    List<Road[]> split = sample.splitRoads(roads, 6);
    assertNotNull(split);
    assertEquals(6, split.size());

    Road[] seg1 = split.get(0);
    assertNotNull(seg1);
    assertEquals(1, seg1.length);
    assertEquals("a", seg1[0].getRoadName());

    Road[] seg2 = split.get(1);
    assertNotNull(seg2);
    assertEquals(1, seg2.length);
    assertEquals("b", seg2[0].getRoadName());

    Road[] seg3 = split.get(2);
    assertNotNull(seg3);
    assertEquals(1, seg3.length);
    assertEquals("c", seg3[0].getRoadName());

    Road[] seg4 = split.get(3);
    assertNotNull(seg4);
    assertEquals(1, seg4.length);
    assertEquals("d", seg4[0].getRoadName());

    Road[] seg5 = split.get(4);
    assertNotNull(seg5);
    assertEquals(1, seg5.length);
    assertEquals("e", seg5[0].getRoadName());

    Road[] seg6 = split.get(5);
    assertNotNull(seg6);
    assertEquals(1, seg6.length);
    assertEquals("f", seg6[0].getRoadName());
  }

  @Test
  public void testSplitRoadsE() {
    Road[] roads = new Road[]{
            new Road("a"),
            new Road("b"),
            new Road("c"),
            new Road("d"),
            new Road("e"),
            new Road("f"),
            new Road("g"),
    };
    List<Road[]> split = sample.splitRoads(roads, 2);
    assertNotNull(split);
    assertEquals(2, split.size());

    Road[] seg1 = split.get(0);
    assertNotNull(seg1);
    assertEquals(4, seg1.length);
    assertEquals("a", seg1[0].getRoadName());
    assertEquals("b", seg1[1].getRoadName());
    assertEquals("c", seg1[2].getRoadName());
    assertEquals("d", seg1[3].getRoadName());

    Road[] seg2 = split.get(1);
    assertNotNull(seg2);
    assertEquals(3, seg2.length);
    assertEquals("e", seg2[0].getRoadName());
    assertEquals("f", seg2[1].getRoadName());
    assertEquals("g", seg2[2].getRoadName());
  }

}
