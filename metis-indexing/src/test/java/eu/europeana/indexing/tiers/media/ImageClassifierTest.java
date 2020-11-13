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
import eu.europeana.metis.schema.model.MediaType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ImageClassifierTest {

  private static ImageClassifier classifier;

  @BeforeAll
  static void setupMocks() {
    classifier = spy(new ImageClassifier());
  }

  @BeforeEach
  void resetMocks() {
    reset(classifier);
  }

  /**
   * Whether there are thumbnails is handled by pre-classification: if there are none, we know that
   * the tier must be 0.
   */
  @Test
  void testPreClassifyEntity() {
    final RdfWrapper entity = mock(RdfWrapper.class);
    doReturn(true).when(entity).hasThumbnails();
    assertNull(classifier.preClassifyEntity(entity));
    doReturn(false).when(entity).hasThumbnails();
    assertEquals(MediaTier.T0, classifier.preClassifyEntity(entity));
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
   * This now depends only on whether the web resource is an image and what resolution it has.
   */
  @Test
  void testClassifyWebResource() {
    testClassifyWebResource(-1, MediaTier.T0);
    testClassifyWebResource(0, MediaTier.T0);
    testClassifyWebResource(99_999, MediaTier.T0);
    testClassifyWebResource(100_000, MediaTier.T1);
    testClassifyWebResource(419_999, MediaTier.T1);
    testClassifyWebResource(420_000, MediaTier.T2);
    testClassifyWebResource(949_999, MediaTier.T2);
    testClassifyWebResource(950_000, MediaTier.T4);
    testClassifyWebResource(5_000_000, MediaTier.T4);
  }

  private void testClassifyWebResource(long resolution, MediaTier expectedTier) {
    final WebResourceWrapper webResource = mock(WebResourceWrapper.class);
    doReturn(resolution).when(webResource).getSize();
    doReturn(MediaType.IMAGE).when(webResource).getMediaType();
    testClassifyWebResource(webResource, expectedTier);
    doReturn(MediaType.OTHER).when(webResource).getMediaType();
    testClassifyWebResource(webResource, MediaTier.T0);
    doReturn(null).when(webResource).getMediaType();
    testClassifyWebResource(webResource, MediaTier.T0);
  }

  private void testClassifyWebResource(WebResourceWrapper webResource, MediaTier expectedTier) {
    assertEquals(expectedTier, classifier.classifyWebResource(webResource, false, false));
    assertEquals(expectedTier, classifier.classifyWebResource(webResource, true, false));
    assertEquals(expectedTier, classifier.classifyWebResource(webResource, false, true));
    assertEquals(expectedTier, classifier.classifyWebResource(webResource, true, true));
  }
}
