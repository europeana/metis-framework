package eu.europeana.indexing.tiers.media;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;

import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.indexing.utils.WebResourceWrapper;
import eu.europeana.metis.schema.model.MediaType;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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

  private static Stream<Arguments> testClassifyWebResource() {
    return Stream.of(
        //Has embeddable media
        Arguments.of(MediaTier.T4, true, true),
        //Has embeddable media but no landing page
        Arguments.of(MediaTier.T4, false, true),
        //Does not have embeddable media but has landing page
        Arguments.of(MediaTier.T1, true, false),
        //Does not have any of the two
        Arguments.of(MediaTier.T0, false, false)
    );
  }

  @ParameterizedTest(name = "[{index}] - expectedTierForOtherType:{0} for hasLandingPage:{1}, hasEmbeddableMedia{2}")
  @MethodSource("testClassifyWebResource")
  void testClassifyWebResource(MediaTier expectedTierForOtherType, boolean hasLandingPage,
      boolean hasEmbeddableMedia) {
    final WebResourceWrapper webResource = mock(WebResourceWrapper.class);
    doReturn(MediaType.AUDIO).when(webResource).getMediaType();
    assertEquals(MediaTier.T4, classifier.classifyWebResource(webResource, hasLandingPage, hasEmbeddableMedia));
    doReturn(MediaType.OTHER).when(webResource).getMediaType();
    assertEquals(expectedTierForOtherType, classifier.classifyWebResource(webResource, hasLandingPage, hasEmbeddableMedia));
    doReturn(null).when(webResource).getMediaType();
    assertEquals(expectedTierForOtherType, classifier.classifyWebResource(webResource, hasLandingPage, hasEmbeddableMedia));
  }

  @Test
  void extractResolutionTierMetadataTest() {
    assertNotNull(classifier.extractResolutionTierMetadata(mock(WebResourceWrapper.class), MediaTier.T0));
  }

  @Test
  void getMediaType() {
    assertEquals(MediaType.AUDIO, classifier.getMediaType());
  }
}
