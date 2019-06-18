package eu.europeana.indexing.solr.facet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.junit.jupiter.api.Test;

class TestColorEncoding {

  @Test
  void testGetColorCode() {

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
    assertNull(ColorEncoding.getColorCode("MY AWESOME MADE-UP COLOR"));
    assertNull(ColorEncoding.getColorCode("  "));
    assertNull(ColorEncoding.getColorCode(null));
  }
}
