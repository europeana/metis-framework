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

class AudioClassifierTest {

  private static AudioClassifier classifier;

  @BeforeAll
  static void setupMocks() {
    classifier = spy(new AudioClassifier());
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
   * This now depends on whether the web resource is audio, is embeddable and has a landing page.
   */
  @Test
  void testClassifyWebResource() {
    testClassifyWebResource(MediaTier.T0, false, false);
    testClassifyWebResource(MediaTier.T4, false, true);
    testClassifyWebResource(MediaTier.T1, true, false);
    testClassifyWebResource(MediaTier.T4, true, true);
  }

  private void testClassifyWebResource(MediaTier expectedTierForOtherType, boolean hasLandingPage,
      boolean hasEmbeddableMedia) {
    final WebResourceWrapper webResource = mock(WebResourceWrapper.class);
    doReturn(MediaType.AUDIO).when(webResource).getMediaType();
    assertEquals(MediaTier.T4, classifier.classifyWebResource(webResource, hasLandingPage, hasEmbeddableMedia));
    doReturn(MediaType.OTHER).when(webResource).getMediaType();
    assertEquals(expectedTierForOtherType, classifier.classifyWebResource(webResource, hasLandingPage, hasEmbeddableMedia));
    doReturn(null).when(webResource).getMediaType();
    assertEquals(expectedTierForOtherType, classifier.classifyWebResource(webResource, hasLandingPage, hasEmbeddableMedia));
  }
}
