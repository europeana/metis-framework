package eu.europeana.indexing.solr.facet.value;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.junit.jupiter.api.Test;

class TestImageColorEncoding {

  @Test
  void testCategorizeImageColor() {

    // Keep track of the found codes and colors.
    final Set<Integer> foundCodes = new HashSet<>();
    final Set<String> foundColors = new HashSet<>();

    // Go by all the mime types.
    for (ImageColorEncoding encoding : ImageColorEncoding.values()) {

      // Check the mime type for existence.
      final String color = encoding.getHexString();
      assertFalse(foundColors.contains(color));
      foundColors.add(color);

      // Check that color name is upper case.
      assertEquals(color.toUpperCase(Locale.ENGLISH), color);

      // Check the code for existence.
      final int code = encoding.getCode();
      assertFalse(foundCodes.contains(code));
      foundCodes.add(code);

      // Check the static method.
      assertEquals(encoding, ImageColorEncoding.categorizeImageColor(color));
      assertEquals(encoding, ImageColorEncoding.categorizeImageColor("#" + color));
    }

    // Test non-existing value and empty/null codes.
    assertNull(ImageColorEncoding.categorizeImageColor("MY AWESOME MADE-UP COLOR"));
    assertNull(ImageColorEncoding.categorizeImageColor("  "));
    assertNull(ImageColorEncoding.categorizeImageColor(null));
  }
}
