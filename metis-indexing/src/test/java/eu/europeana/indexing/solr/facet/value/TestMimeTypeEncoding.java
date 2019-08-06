package eu.europeana.indexing.solr.facet.value;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import eu.europeana.indexing.utils.WebResourceWrapper;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.junit.jupiter.api.Test;

class TestMimeTypeEncoding {

  @Test
  void testGetMimeTypeCode() {

    // The web resource
    final WebResourceWrapper webResource = mock(WebResourceWrapper.class);

    // Keep track of the found codes and mimetypes.
    final Set<Integer> foundCodes = new HashSet<>();
    final Set<String> foundMimeTypes = new HashSet<>();

    // Go by all the mime types.
    for (MimeTypeEncoding encoding : MimeTypeEncoding.values()) {

      // Check the mime type for existence.
      final String mimeType = encoding.getValue();
      assertFalse(foundMimeTypes.contains(mimeType));
      foundMimeTypes.add(mimeType);

      // Check that mime type name is lower case.
      assertEquals(mimeType.toLowerCase(Locale.ENGLISH), mimeType);
      
      // Check the code for existence.
      final int code = encoding.getCode();
      assertFalse(foundCodes.contains(code));
      foundCodes.add(code);

      // Check the static method.
      doReturn(encoding.getValue()).when(webResource).getMimeType();
      assertEquals(encoding, MimeTypeEncoding.categorizeMimeType(webResource));
    }

    // Test non-existing value and empty/null codes.
    doReturn("MY AWESOME MADE-UP MIME TYPE").when(webResource).getMimeType();
    assertNull(MimeTypeEncoding.categorizeMimeType(webResource));
    doReturn("  ").when(webResource).getMimeType();
    assertNull(MimeTypeEncoding.categorizeMimeType(webResource));
    doReturn(null).when(webResource).getMimeType();
    assertNull(MimeTypeEncoding.categorizeMimeType(webResource));
  }
}
