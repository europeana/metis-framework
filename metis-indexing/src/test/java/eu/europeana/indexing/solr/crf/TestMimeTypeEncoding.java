package eu.europeana.indexing.solr.crf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.junit.Test;

public class TestMimeTypeEncoding {

  @Test
  public void testGetMimeTypeCode() {

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
    assertEquals(Integer.valueOf(0),
        MimeTypeEncoding.getMimeTypeCode("MY AWESOME MADE-UP MIME TYPE"));
    assertEquals(Integer.valueOf(0), MimeTypeEncoding.getMimeTypeCode("  "));
    assertEquals(Integer.valueOf(0), MimeTypeEncoding.getMimeTypeCode(null));
  }
}
