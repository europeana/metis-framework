package eu.europeana.indexing.solr.crf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.junit.Test;

public class TestColorEncoding {

  @Test
  public void testGetColorCode() {

    // Keep track of the found codes and colors.
    final Set<Integer> foundCodes = new HashSet<>();
    final Set<String> foundColors = new HashSet<>();

    // Go by all the mime types.
    for (ColorEncoding encoding : ColorEncoding.values()) {

      // Check the mime type for existence.
      final String Color = encoding.getHexString();
      assertFalse(foundColors.contains(Color));
      foundColors.add(Color);

      // Check that color name is upper case.
      assertEquals(Color.toUpperCase(Locale.ENGLISH), Color);

      // Check the code: should be greater than zero and smaller than the maximum
      final int code = encoding.getCode();
      assertTrue(code > 0);
      assertTrue(code < TechnicalFacet.IMAGE_COLOUR_PALETTE.getMaxValue());

      // Check the code for existence.
      assertFalse(foundCodes.contains(code));
      foundCodes.add(code);

      // Check the static method.
      assertEquals(Integer.valueOf(code), ColorEncoding.getColorCode(Color));
      assertEquals(Integer.valueOf(code), ColorEncoding.getColorCode("#" + Color));
    }

    // Test non-existing code and empty/null codes.
    assertEquals(Integer.valueOf(0),
        ColorEncoding.getColorCode("MY AWESOME MADE-UP COLOR"));
    assertEquals(Integer.valueOf(0), ColorEncoding.getColorCode("  "));
    assertEquals(Integer.valueOf(0), ColorEncoding.getColorCode(null));
  }
}
