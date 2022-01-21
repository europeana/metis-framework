package eu.europeana.indexing.tiers.media;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.tiers.view.ResolutionTierMetadata;
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

class VideoClassifierTest {

  private static VideoClassifier classifier;

  @BeforeAll
  static void setupMocks() {
    classifier = spy(new VideoClassifier());
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
        //Has video type and High resolution for all combinations of hasLandingPage and hasEmbeddableMedia
        Arguments.of(MediaTier.T4, MediaType.VIDEO, false, false, 480),
        Arguments.of(MediaTier.T4, MediaType.VIDEO, false, true, 480),
        Arguments.of(MediaTier.T4, MediaType.VIDEO, true, false, 480),
        Arguments.of(MediaTier.T4, MediaType.VIDEO, true, true, 480),

        //Has video type, resolution low and hasEmbeddableMedia with all combinations of hasLandingPage
        Arguments.of(MediaTier.T4, MediaType.VIDEO, false, true, 479),
        Arguments.of(MediaTier.T4, MediaType.VIDEO, true, true, 479),
        //No video type, resolution high and hasEmbeddableMedia true and hasLandingPage false
        Arguments.of(MediaTier.T4, MediaType.OTHER, false, true, 480),

        //Has video type and everything else false and low
        Arguments.of(MediaTier.T1, MediaType.VIDEO, false, false, 479),

        //Has no video type, has landing page and everything else false and low
        Arguments.of(MediaTier.T1, MediaType.OTHER, true, false, 479),

        //Has no video type and everything false and low
        Arguments.of(MediaTier.T0, MediaType.OTHER, false, false, 479)
    );
  }

  @ParameterizedTest(name = "[{index}] - expectedTier:{0} for mediaType:{1}, hasLandingPage:{2}, hasEmbeddableMedia:{3}, height:{4}")
  @MethodSource("testClassifyWebResource")
  void testClassifyWebResource(MediaTier expectedTier, MediaType mediaType, boolean hasLandingPage,
      boolean hasEmbeddableMedia, long height) {
    final WebResourceWrapper webResource = mock(WebResourceWrapper.class);
    doReturn(height).when(webResource).getHeight();
    doReturn(mediaType).when(webResource).getMediaType();
    assertEquals(expectedTier, classifier.classifyWebResource(webResource, hasLandingPage, hasEmbeddableMedia));
  }

  @Test
  void extractResolutionTierMetadataTest() {
    final Long resolution = 100L;
    final MediaTier mediaTier = MediaTier.T0;
    final WebResourceWrapper webResource = mock(WebResourceWrapper.class);
    when(webResource.getHeight()).thenReturn(resolution);
    final ResolutionTierMetadata resolutionTierMetadata = classifier.extractResolutionTierMetadata(webResource, mediaTier);
    assertNotNull(resolutionTierMetadata);
    //Vertical resolution should be set
    assertEquals(resolution, resolutionTierMetadata.getVerticalResolution());
    assertEquals(mediaTier, resolutionTierMetadata.getVerticalResolutionTier());
    //Image resolution should not be set
    assertNull(resolutionTierMetadata.getImageResolution());
    assertNull(resolutionTierMetadata.getImageResolutionTier());
  }

  @Test
  void getMediaType() {
    assertEquals(MediaType.VIDEO, classifier.getMediaType());
  }
}
