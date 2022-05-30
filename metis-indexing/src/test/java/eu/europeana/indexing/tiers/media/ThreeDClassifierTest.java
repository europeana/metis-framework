package eu.europeana.indexing.tiers.media;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.indexing.utils.WebResourceLinkType;
import eu.europeana.indexing.utils.WebResourceWrapper;
import eu.europeana.metis.schema.model.MediaType;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ThreeDClassifierTest {

  private static ThreeDClassifier classifier;

  @BeforeAll
  static void setupMocks() {
    classifier = spy(new ThreeDClassifier());
  }

  @BeforeEach
  void resetMocks() {
    reset(classifier);
  }

  /**
   * This method should always return null: we always need to look at the web resources.
   */
  @Test
  void testPreClassifyEntity() {
    assertNull(classifier.preClassifyEntity(mock(RdfWrapper.class)));
  }

  /**
   * If there are no web resources, the result is tier 0 or 1 depending on the landing page.
   */
  @Test
  void testClassifyEntityWithoutWebResources() {
    final RdfWrapper entity = mock(RdfWrapper.class);
    assertEquals(MediaTier.T1, classifier.classifyEntityWithoutWebResources(entity, true));
    assertEquals(MediaTier.T0, classifier.classifyEntityWithoutWebResources(entity, false));
  }

  @Test
  void testClassifyWebResource_tier4Result() {
    final WebResourceWrapper webResource = mock(WebResourceWrapper.class);
    Set<WebResourceLinkType> mockSetResponse = Set.of(WebResourceLinkType.HAS_VIEW, WebResourceLinkType.IS_SHOWN_BY);
    when(webResource.getLinkTypes()).thenReturn(mockSetResponse);
    when(webResource.getMimeType()).thenReturn("video");
    assertEquals(MediaTier.T4, classifier.classifyWebResource(webResource, true, false));
  }

  @Test
  void testClassifyWebResource_tier0NullValuesResult() {
    final WebResourceWrapper webResource = mock(WebResourceWrapper.class);
    when(webResource.getLinkTypes()).thenReturn(null);
    assertEquals(MediaTier.T0, classifier.classifyWebResource(webResource, false, false));
  }

  @Test
  void extractResolutionTierMetadataTest() {
    assertNotNull(classifier.extractResolutionTierMetadata(mock(WebResourceWrapper.class), MediaTier.T0));
  }

  @Test
  void getMediaType() {
    assertEquals(MediaType.THREE_D, classifier.getMediaType());
  }
}
