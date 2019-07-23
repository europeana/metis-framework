package eu.europeana.indexing.solr.facet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.junit.jupiter.api.Test;

class TestMimeTypeEncoding {

  @Test
  void testGetMimeTypeCode() {

    // Keep track of the found codes and mimetypes.
    final Set<Integer> foundCodes = new HashSet<>();
    final Set<String> foundMimeTypes = new HashSet<>();

    // Go by all the mime types.
    for (MimeTypeEncoding encoding : MimeTypeEncoding.values()) {

      // Check the mime type for existence.
      final String mimeType = encoding.getMimeType();
      assertFalse(foundMimeTypes.contains(mimeType));
      foundMimeTypes.add(mimeType);

      // Check that mime type name is lower case.
      assertEquals(mimeType.toLowerCase(Locale.ENGLISH), mimeType);
      
      // Check the code: should be greater than zero and smaller than the maximum
      final int code = encoding.getCode();
      assertTrue(code > 0);
      assertTrue(code < TechnicalFacet.MIME_TYPE.getMaxValue());

      // Check the code for existence.
      assertFalse(foundCodes.contains(code));
      foundCodes.add(code);

      // Check the static method.
      assertEquals(Integer.valueOf(code), MimeTypeEncoding.getMimeTypeCode(mimeType));
    }

    // Test non-existing code and empty/null codes.
    assertNull(MimeTypeEncoding.getMimeTypeCode("MY AWESOME MADE-UP MIME TYPE"));
    assertNull(MimeTypeEncoding.getMimeTypeCode("  "));
    assertNull(MimeTypeEncoding.getMimeTypeCode(null));
  }
}
