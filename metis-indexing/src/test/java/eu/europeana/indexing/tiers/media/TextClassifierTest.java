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

  private static Stream<Arguments> testClassifyWebResource() {
    return Stream.of(
        //Has pdf mime type
        Arguments.of(MediaTier.T4, MediaType.TEXT, "application/pdf; extraParameters", -1, true),
        //No pdf mime type and high resolution
        Arguments.of(MediaTier.T4, MediaType.IMAGE, "other text", 950_000, false),
        Arguments.of(MediaTier.T4, MediaType.IMAGE, "other text", 5_000_000, false),
        //No pdf mime type and medium resolution below high resolution
        Arguments.of(MediaTier.T2, MediaType.IMAGE, "other text", 420_000, false),
        Arguments.of(MediaTier.T2, MediaType.IMAGE, "other text", 949_999, false),
        //No pdf mime type, lower than medium resolution and hasLandingPage
        Arguments.of(MediaTier.T1, MediaType.IMAGE, "other text", 100_000, true),
        Arguments.of(MediaTier.T1, MediaType.IMAGE, "other text", 419_999, true),
        //No pdf mime type, lower than medium resolution and small resolution and no hasLandingPage
        Arguments.of(MediaTier.T1, MediaType.IMAGE, "other text", 100_000, false),
        Arguments.of(MediaTier.T1, MediaType.IMAGE, "other text", 419_999, false),
        //No pdf mime type, no hasLandingPage, lower than small resolution
        Arguments.of(MediaTier.T0, MediaType.IMAGE, "other text", 99_999, false),
        Arguments.of(MediaTier.T0, MediaType.IMAGE, "other text", 0, false),

        //No pdf mime type but also no IMAGE media type and hasLandingPage false
        Arguments.of(MediaTier.T0, MediaType.OTHER, "other text", 5_000_000, false),
        //No pdf mime type but also no IMAGE media type and hasLandingPage true
        Arguments.of(MediaTier.T1, MediaType.OTHER, "other text", 5_000_000, true),

        //No pdf mime type but also null media type and hasLandingPage false
        Arguments.of(MediaTier.T0, null, "other text", 5_000_000, false),
        //No pdf mime type but also null media type and hasLandingPage true
        Arguments.of(MediaTier.T1, null, "other text", 5_000_000, true)
    );
  }

  private void testClassifyWebResource(MediaTier expectedTier, WebResourceWrapper webResource, boolean hasLandingPage) {
    assertEquals(expectedTier, classifier.classifyWebResource(webResource, hasLandingPage, false));
    assertEquals(expectedTier, classifier.classifyWebResource(webResource, hasLandingPage, true));
  }

  @ParameterizedTest(name = "[{index}] - expectedTier:{0} for mediaType:{1}, mimeType:{2}, imageResolution:{3}, hasLandingPage:{4}")
  @MethodSource("testClassifyWebResource")
  void testClassifyWebResource(MediaTier expectedTier, MediaType mediaType, String mimeType, long imageResolution,
      boolean hasLandingPage) {
    final WebResourceWrapper webResource = mock(WebResourceWrapper.class);
    doReturn(imageResolution).when(webResource).getSize();
    doReturn(mediaType).when(webResource).getMediaType();
    doReturn(mimeType).when(webResource).getMimeType();

    // As Text type (PDF)
    testClassifyWebResource(expectedTier, webResource, hasLandingPage);
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
    assertEquals(MediaType.TEXT, classifier.getMediaType());
  }
}
