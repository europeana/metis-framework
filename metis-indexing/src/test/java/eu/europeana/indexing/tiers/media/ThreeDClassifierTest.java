package eu.europeana.indexing.tiers.media;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.utils.LicenseType;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.indexing.utils.WebResourceLinkType;
import eu.europeana.indexing.utils.WebResourceWrapper;
import eu.europeana.metis.schema.model.MediaType;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;

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

  @Test
  void testClassifyWebResource_tier4Result() {
    final WebResourceWrapper webResource = mock(WebResourceWrapper.class);
    Set<WebResourceLinkType> mockSetResponse = Set.of(WebResourceLinkType.HAS_VIEW, WebResourceLinkType.IS_SHOWN_BY);
    Optional<LicenseType> licenseType = Optional.of(LicenseType.OPEN);
    when(webResource.getLinkTypes()).thenReturn(mockSetResponse);
    when(webResource.getMimeType()).thenReturn("video");
    when(webResource.getLicenseType()).thenReturn(licenseType).thenReturn(licenseType);
    assertEquals(MediaTier.T4, classifier.classifyWebResource(webResource, true, false));
  }

  @Test
  void testClassifyWebResource_tier3Result() {
    final WebResourceWrapper webResource = mock(WebResourceWrapper.class);
    Set<WebResourceLinkType> mockSetResponse = Set.of(WebResourceLinkType.HAS_VIEW, WebResourceLinkType.IS_SHOWN_BY);
    Optional<LicenseType> licenseType = Optional.of(LicenseType.RESTRICTED);
    when(webResource.getLinkTypes()).thenReturn(mockSetResponse);
    when(webResource.getMimeType()).thenReturn("video");
    when(webResource.getLicenseType()).thenReturn(licenseType).thenReturn(licenseType);
    assertEquals(MediaTier.T3, classifier.classifyWebResource(webResource, true, false));
  }

  @Test
  void testClassifyWebResource_tier2Result() {
    final WebResourceWrapper webResource = mock(WebResourceWrapper.class);
    Set<WebResourceLinkType> mockSetResponse = Set.of(WebResourceLinkType.HAS_VIEW, WebResourceLinkType.IS_SHOWN_BY);
    Optional<LicenseType> licenseType = Optional.of(LicenseType.CLOSED);
    when(webResource.getLinkTypes()).thenReturn(mockSetResponse);
    when(webResource.getMimeType()).thenReturn("video");
    when(webResource.getLicenseType()).thenReturn(licenseType).thenReturn(licenseType);
    assertEquals(MediaTier.T2, classifier.classifyWebResource(webResource, true, false));
  }

  @Test
  void testClassifyWebResource_tier1Result() {
    final WebResourceWrapper webResource = mock(WebResourceWrapper.class);
    Optional<LicenseType> licenseType = Optional.of(LicenseType.CLOSED);
    when(webResource.getLinkTypes()).thenReturn(Set.of(WebResourceLinkType.IS_SHOWN_AT));
    when(webResource.getMimeType()).thenReturn("video");
    when(webResource.getLicenseType()).thenReturn(licenseType).thenReturn(licenseType);
    assertEquals(MediaTier.T1, classifier.classifyWebResource(webResource, true, false));
  }

  @Test
  void testClassifyWebResource_tier0Result() {
    final WebResourceWrapper webResource = mock(WebResourceWrapper.class);
    Optional<LicenseType> licenseType = Optional.of(LicenseType.CLOSED);
    when(webResource.getLinkTypes()).thenReturn(Set.of());
    when(webResource.getMimeType()).thenReturn("video");
    when(webResource.getLicenseType()).thenReturn(licenseType).thenReturn(licenseType);
    assertEquals(MediaTier.T0, classifier.classifyWebResource(webResource, false, false));
  }

  //TODO: tier 0 With null values

  @Test
  void extractResolutionTierMetadataTest() {
    assertNotNull(classifier.extractResolutionTierMetadata(mock(WebResourceWrapper.class), MediaTier.T0));
  }

  @Test
  void getMediaType() {
    assertEquals(MediaType.THREE_D, classifier.getMediaType());
  }
}
