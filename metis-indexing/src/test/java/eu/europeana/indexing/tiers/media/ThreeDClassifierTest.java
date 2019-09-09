package eu.europeana.indexing.tiers.media;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;

import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.indexing.utils.WebResourceWrapper;
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
   * If there are no web resources, the result is always tier 0.
   */
  @Test
  void testClassifyEntityWithoutWebResources() {
    final RdfWrapper entity = mock(RdfWrapper.class);
    doReturn(true).when(entity).hasThumbnails();
    assertEquals(MediaTier.T0, classifier.classifyEntityWithoutWebResources(entity, true));
    assertEquals(MediaTier.T0, classifier.classifyEntityWithoutWebResources(entity, false));
  }

  /**
   * This now depends only on the mime type.
   */
  @Test
  void testClassifyWebResource() {
    testClassifyWebResource("any mime type", MediaTier.T4);
    testClassifyWebResource("   ", MediaTier.T0);
    testClassifyWebResource("", MediaTier.T0);
    testClassifyWebResource(null, MediaTier.T0);
  }

  private void testClassifyWebResource(String mimeType, MediaTier expectedTier) {
    final WebResourceWrapper webResource = mock(WebResourceWrapper.class);
    doReturn(mimeType).when(webResource).getMimeType();
    assertEquals(expectedTier, classifier.classifyWebResource(webResource, false, false));
    assertEquals(expectedTier, classifier.classifyWebResource(webResource, true, false));
    assertEquals(expectedTier, classifier.classifyWebResource(webResource, false, true));
    assertEquals(expectedTier, classifier.classifyWebResource(webResource, true, true));
  }
}
