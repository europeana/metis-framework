package eu.europeana.metis.mediaprocessing.extraction;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import eu.europeana.metis.mediaprocessing.model.Resource;
import eu.europeana.metis.mediaprocessing.model.UrlType;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class MediaProcessorTest {

  @Test
  void testShouldExtractMetadata() {

    // Create media processor and resource
    final MediaProcessor mediaProcessor = (resource, detectedMimeType) -> null;
    final Resource resource = mock(Resource.class);

    // Try where answer is no
    final Set<UrlType> shouldNotExtractSet = Collections.emptySet();
    assertFalse(UrlType.shouldExtractMetadata(shouldNotExtractSet));
    doReturn(shouldNotExtractSet).when(resource).getUrlTypes();
    assertFalse(mediaProcessor.shouldExtractMetadata(resource));

    // Try where answer is yes
    final Set<UrlType> shouldExtractSet = new HashSet<>(Arrays.asList(UrlType.values()));
    assertTrue(UrlType.shouldExtractMetadata(shouldExtractSet));
    doReturn(shouldExtractSet).when(resource).getUrlTypes();
    assertTrue(mediaProcessor.shouldExtractMetadata(resource));
  }
}
