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

  private static Stream<Arguments> testClassifyWebResource() {
    return Stream.of(
        //Anything below low resolution
        Arguments.of(MediaTier.T0, -1),
        Arguments.of(MediaTier.T0, 0),
        Arguments.of(MediaTier.T0, 99_999),
        //Small resolution but below medium resolution
        Arguments.of(MediaTier.T1, 100_000),
        Arguments.of(MediaTier.T1, 419_999),
        //Medium resolution but below high resolution
        Arguments.of(MediaTier.T2, 420_000),
        Arguments.of(MediaTier.T2, 949_999),
        //High resolution
        Arguments.of(MediaTier.T4, 950_000),
        Arguments.of(MediaTier.T4, 5_000_000)
    );
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
   * Whether there are thumbnails is handled by pre-classification: if there are none, we know that the tier must be 0.
   */
  @Test
  void testPreClassifyEntity() {
    final RdfWrapper entity = mock(RdfWrapper.class);
    doReturn(true).when(entity).hasThumbnails();
    assertNull(classifier.preClassifyEntity(entity));
    doReturn(false).when(entity).hasThumbnails();
    assertEquals(MediaTier.T0, classifier.preClassifyEntity(entity));
  }

  @ParameterizedTest(name = "[{index}] - expectedTier:{0} for resolution:{1}")
  @MethodSource("testClassifyWebResource")
  void testClassifyWebResource(MediaTier expectedTier, long resolution) {
    final WebResourceWrapper webResource = mock(WebResourceWrapper.class);
    doReturn(resolution).when(webResource).getSize();
    doReturn(MediaType.IMAGE).when(webResource).getMediaType();
    testClassifyWebResource(webResource, expectedTier);
  }

  @Test
  void testClassifyWebResource_NonImageMediaType() {
    final WebResourceWrapper webResource = mock(WebResourceWrapper.class);
    //Other type
    doReturn(MediaType.OTHER).when(webResource).getMediaType();
    testClassifyWebResource(webResource, MediaTier.T0);

    //Null type
    doReturn(null).when(webResource).getMediaType();
    testClassifyWebResource(webResource, MediaTier.T0);
  }

  private void testClassifyWebResource(WebResourceWrapper webResource, MediaTier expectedTier) {
    assertEquals(expectedTier, classifier.classifyWebResource(webResource, false, false));
    assertEquals(expectedTier, classifier.classifyWebResource(webResource, true, false));
    assertEquals(expectedTier, classifier.classifyWebResource(webResource, false, true));
    assertEquals(expectedTier, classifier.classifyWebResource(webResource, true, true));
  }

  @Test
  void extractResolutionTierMetadataTest() {
    final Long resolution = 100L;
    final MediaTier mediaTier = MediaTier.T0;
    final WebResourceWrapper webResource = mock(WebResourceWrapper.class);
    when(webResource.getSize()).thenReturn(resolution);
    final ResolutionTierMetadata resolutionTierMetadata = classifier.extractResolutionTierMetadata(webResource, mediaTier);
    assertNotNull(resolutionTierMetadata);
    //Image resolution should be set
    assertEquals(resolution, resolutionTierMetadata.getImageResolution());
    assertEquals(mediaTier, resolutionTierMetadata.getImageResolutionTier());
    //Vertical resolution should not be set
    assertNull(resolutionTierMetadata.getVerticalResolution());
    assertNull(resolutionTierMetadata.getVerticalResolutionTier());
  }

  @Test
  void getMediaType() {
    assertEquals(MediaType.IMAGE, classifier.getMediaType());
  }
}
