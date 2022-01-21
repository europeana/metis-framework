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
    assertEquals(MediaTier.T0, classifier.classifyEntityWithoutWebResources(entity, true));
    assertEquals(MediaTier.T0, classifier.classifyEntityWithoutWebResources(entity, false));
  }

  private static Stream<Arguments> testClassifyWebResource() {
    return Stream.of(
        //If mime type is not blank
        Arguments.of(MediaTier.T4, "any mime type"),
        //If mime type is blank(only spaces)
        Arguments.of(MediaTier.T0, "   "),
        //If mime type is blank(empty string)
        Arguments.of(MediaTier.T0, ""),
        //If mime type is blank(null)
        Arguments.of(MediaTier.T0, null)
    );
  }

  @ParameterizedTest(name = "[{index}] - expectedTier:{0} for mimeType:{1}")
  @MethodSource("testClassifyWebResource")
  void testClassifyWebResource(MediaTier expectedTier, String mimeType) {
    final WebResourceWrapper webResource = mock(WebResourceWrapper.class);
    doReturn(mimeType).when(webResource).getMimeType();
    //Any combination of hasLandingPage and hasEmbeddableMedia should not change the result
    assertEquals(expectedTier, classifier.classifyWebResource(webResource, false, false));
    assertEquals(expectedTier, classifier.classifyWebResource(webResource, true, false));
    assertEquals(expectedTier, classifier.classifyWebResource(webResource, false, true));
    assertEquals(expectedTier, classifier.classifyWebResource(webResource, true, true));
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
