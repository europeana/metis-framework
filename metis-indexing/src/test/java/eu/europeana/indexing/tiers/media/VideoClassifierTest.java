package eu.europeana.indexing.tiers.media;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;

import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.indexing.utils.WebResourceLinkType;
import eu.europeana.indexing.utils.WebResourceWrapper;
import eu.europeana.metis.utils.MediaType;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
   * Whether there are thumbnails and/or a large image as edm:object is handled by
   * pre-classification: if either of these are not present, we know that the tier must be 0.
   */
  @Test
  void testPreClassifyEntity() {

    // The entity and some web resources
    final RdfWrapper entity = mock(RdfWrapper.class);
    final WebResourceWrapper largeImageResource = mock(WebResourceWrapper.class);
    doReturn(MediaType.IMAGE).when(largeImageResource).getMediaType();
    doReturn(100_000L).when(largeImageResource).getSize();
    final WebResourceWrapper smallImageResource = mock(WebResourceWrapper.class);
    doReturn(MediaType.IMAGE).when(smallImageResource).getMediaType();
    doReturn(99_000L).when(smallImageResource).getSize();
    final WebResourceWrapper nonImageResource = mock(WebResourceWrapper.class);
    doReturn(MediaType.VIDEO).when(nonImageResource).getMediaType();

    // if there is no edm:object
    doReturn(true).when(entity).hasThumbnails();
    assertEquals(MediaTier.T0, classifier.preClassifyEntity(entity));
    doReturn(false).when(entity).hasThumbnails();
    assertEquals(MediaTier.T0, classifier.preClassifyEntity(entity));

    // if there is an edm:object, but it is not an image
    doReturn(Collections.singletonList(nonImageResource)).when(entity).getWebResourceWrappers(
        EnumSet.of(WebResourceLinkType.OBJECT));
    doReturn(true).when(entity).hasThumbnails();
    assertEquals(MediaTier.T0, classifier.preClassifyEntity(entity));
    doReturn(false).when(entity).hasThumbnails();
    assertEquals(MediaTier.T0, classifier.preClassifyEntity(entity));

    // if there is an edm:object, but it is a small image
    doReturn(Arrays.asList(smallImageResource, nonImageResource)).when(entity)
        .getWebResourceWrappers(EnumSet.of(WebResourceLinkType.OBJECT));
    doReturn(true).when(entity).hasThumbnails();
    assertEquals(MediaTier.T0, classifier.preClassifyEntity(entity));
    doReturn(false).when(entity).hasThumbnails();
    assertEquals(MediaTier.T0, classifier.preClassifyEntity(entity));

    // if there is an edm:object, and it is a large image
    doReturn(Arrays.asList(largeImageResource, nonImageResource)).when(entity)
        .getWebResourceWrappers(EnumSet.of(WebResourceLinkType.OBJECT));
    doReturn(true).when(entity).hasThumbnails();
    assertNull(classifier.preClassifyEntity(entity));
    doReturn(false).when(entity).hasThumbnails();
    assertEquals(MediaTier.T0, classifier.preClassifyEntity(entity));
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
   * This now depends on whether the web resource is video, is embeddable and has a landing page, as
   * well as on the size of the video.
   */
  @Test
  void testClassifyWebResource() {

    // Try for large image
    testClassifyWebResource(MediaTier.T4, MediaTier.T0, false, false, 480);
    testClassifyWebResource(MediaTier.T4, MediaTier.T4, false, true, 480);
    testClassifyWebResource(MediaTier.T4, MediaTier.T1, true, false, 480);
    testClassifyWebResource(MediaTier.T4, MediaTier.T4, true, true, 480);

    // Try for small image
    testClassifyWebResource(MediaTier.T1, MediaTier.T0, false, false, 479);
    testClassifyWebResource(MediaTier.T4, MediaTier.T4, false, true, 479);
    testClassifyWebResource(MediaTier.T1, MediaTier.T1, true, false, 479);
    testClassifyWebResource(MediaTier.T4, MediaTier.T4, true, true, 479);
  }

  private void testClassifyWebResource(MediaTier expectedTierForVideoType,
      MediaTier expectedTierForOtherType, boolean hasLandingPage, boolean hasEmbeddableMedia,
      long height) {
    final WebResourceWrapper webResource = mock(WebResourceWrapper.class);
    doReturn(height).when(webResource).getHeight();
    doReturn(MediaType.VIDEO).when(webResource).getMediaType();
    assertEquals(expectedTierForVideoType,
        classifier.classifyWebResource(webResource, hasLandingPage, hasEmbeddableMedia));
    doReturn(MediaType.OTHER).when(webResource).getMediaType();
    assertEquals(expectedTierForOtherType,
        classifier.classifyWebResource(webResource, hasLandingPage, hasEmbeddableMedia));
    doReturn(null).when(webResource).getMediaType();
    assertEquals(expectedTierForOtherType,
        classifier.classifyWebResource(webResource, hasLandingPage, hasEmbeddableMedia));
  }
}
