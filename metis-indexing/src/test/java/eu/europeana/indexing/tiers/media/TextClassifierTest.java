package eu.europeana.indexing.tiers.media;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;

import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.tiers.model.Tier;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.indexing.utils.WebResourceWrapper;
import eu.europeana.metis.utils.MediaType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TextClassifierTest {

  private static TextClassifier classifier;

  @BeforeAll
  static void setupMocks() {
    classifier = spy(new TextClassifier());
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
   * If there are no web resources, the result is tier 0 or tier 1, depending on the landing page.
   */
  @Test
  void testClassifyEntityWithoutWebResources() {
    final RdfWrapper entity = mock(RdfWrapper.class);
    assertEquals(MediaTier.T1, classifier.classifyEntityWithoutWebResources(entity, true));
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

  private void testClassifyWebResource(long imageResolution, MediaTier expectedTier) {
    final WebResourceWrapper webResource = mock(WebResourceWrapper.class);
    doReturn(imageResolution).when(webResource).getSize();

    // As Text type (PDF)
    doReturn("application/pdf; extraParameters").when(webResource).getMimeType();
    doReturn(MediaType.TEXT).when(webResource).getMediaType();
    testClassifyWebResource(webResource, MediaTier.T4, MediaTier.T4);

    // As Text type (non-PDF)
    doReturn("other text").when(webResource).getMimeType();
    doReturn(MediaType.TEXT).when(webResource).getMediaType();
    testClassifyWebResource(webResource, MediaTier.T1, MediaTier.T0);

    // As Image type
    doReturn("any image").when(webResource).getMimeType();
    doReturn(MediaType.IMAGE).when(webResource).getMediaType();
    testClassifyWebResource(webResource, Tier.max(expectedTier, MediaTier.T1), expectedTier);

    // As Other type
    doReturn(null).when(webResource).getMimeType();
    doReturn(MediaType.OTHER).when(webResource).getMediaType();
    testClassifyWebResource(webResource, MediaTier.T1, MediaTier.T0);

    // As unknown type
    doReturn(null).when(webResource).getMimeType();
    doReturn(null).when(webResource).getMediaType();
    testClassifyWebResource(webResource, MediaTier.T1, MediaTier.T0);
  }

  private void testClassifyWebResource(WebResourceWrapper webResource,
      MediaTier tierWithLandingPage, MediaTier tierWithoutLandingPage) {
    assertEquals(tierWithoutLandingPage, classifier.classifyWebResource(webResource, false, false));
    assertEquals(tierWithLandingPage, classifier.classifyWebResource(webResource, true, false));
    assertEquals(tierWithoutLandingPage, classifier.classifyWebResource(webResource, false, true));
    assertEquals(tierWithLandingPage, classifier.classifyWebResource(webResource, true, true));
  }
}
