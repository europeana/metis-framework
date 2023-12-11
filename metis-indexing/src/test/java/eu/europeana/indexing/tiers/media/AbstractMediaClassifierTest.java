package eu.europeana.indexing.tiers.media;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.tiers.view.ContentTierBreakdown;
import eu.europeana.indexing.tiers.view.MediaResourceTechnicalMetadata;
import eu.europeana.indexing.tiers.view.MediaResourceTechnicalMetadata.MediaResourceTechnicalMetadataBuilder;
import eu.europeana.indexing.tiers.view.ResolutionTierMetadata;
import eu.europeana.indexing.tiers.view.ResolutionTierMetadata.ResolutionTierMetadataBuilder;
import eu.europeana.indexing.utils.LicenseType;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.indexing.utils.WebResourceLinkType;
import eu.europeana.indexing.utils.WebResourceWrapper;
import eu.europeana.metis.schema.model.MediaType;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AbstractMediaClassifierTest {

  private static AbstractMediaClassifier classifier;

  @BeforeAll
  static void setup() {
    classifier = spy(new AbstractMediaClassifier() {
      @Override
      TierClassification<MediaTier, ContentTierBreakdown> preClassifyEntity(RdfWrapper entity) {
        return null;
      }

      @Override
      MediaTier classifyEntityWithoutWebResources(RdfWrapper entity, boolean hasLandingPage) {
        return null;
      }

      @Override
      MediaTier classifyWebResource(WebResourceWrapper webResource, boolean hasLandingPage, boolean hasEmbeddableMedia) {
        return null;
      }

      @Override
      ResolutionTierMetadata extractResolutionTierMetadata(WebResourceWrapper webResource, MediaTier mediaTier) {
        return null;
      }

      @Override
      MediaType getMediaType() {
        return null;
      }
    });
  }

  @BeforeEach
  void resetMocks() {
    reset(classifier);
  }

  @Test
  void testClassify_PreClassify() {
    final RdfWrapper entity = mock(RdfWrapper.class);
    doReturn(MediaTier.T0).when(classifier).preClassifyEntity(entity);
    assertEquals(MediaTier.T0, classifier.classify(entity).getTier());
  }

  @Test
  void testClassify_WithoutWebResources() {
    final RdfWrapper entity = mock(RdfWrapper.class);
    final boolean hasLandingPage = true;
    final boolean hasThumbnails = true;
    final LicenseType entityLicense = LicenseType.OPEN;
    doReturn(hasLandingPage).when(entity).hasLandingPage();
    doReturn(hasThumbnails).when(entity).hasThumbnails();
    doReturn(entityLicense).when(entity).getLicenseType();
    doReturn(null).when(classifier).preClassifyEntity(entity);
    doReturn(Collections.emptyList()).when(entity).getWebResourceWrappers(
        EnumSet.of(WebResourceLinkType.HAS_VIEW, WebResourceLinkType.IS_SHOWN_BY));
    //Has embeddable media will be true
    doReturn(Set.of("https://soundcloud.com/")).when(entity).getUrlsOfTypes(Set.of(WebResourceLinkType.IS_SHOWN_BY));
    doReturn(MediaTier.T0).when(classifier).classifyEntityWithoutWebResources(entity, hasLandingPage);
    assertEquals(MediaTier.T0, classifier.classify(entity).getTier());
  }

  @Test
  void testClassify_WithWebResources() {
    final RdfWrapper entity = mock(RdfWrapper.class);
    final boolean hasLandingPage = true;
    final boolean hasThumbnails = true;
    final LicenseType entityLicense = LicenseType.OPEN;
    final WebResourceWrapper resource1 = mock(WebResourceWrapper.class);
    final WebResourceWrapper resource2 = mock(WebResourceWrapper.class);
    final MediaResourceTechnicalMetadata mediaResourceTechnicalMetadata1 = new MediaResourceTechnicalMetadataBuilder(
        new ResolutionTierMetadataBuilder().build())
        .setResourceUrl("resourceUrl")
        .setMediaType(MediaType.AUDIO)
        .setElementLinkTypes(Collections.emptySet())
        .setLicenseType(LicenseType.OPEN)
        .setMediaTier(MediaTier.T3)
        .setMediaTierBeforeLicenseCorrection(MediaTier.T4)
        .build();
    final MediaResourceTechnicalMetadata mediaResourceTechnicalMetadata2 = new MediaResourceTechnicalMetadataBuilder(
        new ResolutionTierMetadataBuilder().build())
        .setResourceUrl("resourceUrl")
        .setMediaType(MediaType.AUDIO)
        .setElementLinkTypes(Collections.emptySet())
        .setLicenseType(LicenseType.OPEN)
        .setMediaTier(MediaTier.T4)
        .setMediaTierBeforeLicenseCorrection(MediaTier.T2)
        .build();

    doReturn(Arrays.asList(resource1, resource2)).when(entity).getWebResourceWrappers(
        EnumSet.of(WebResourceLinkType.HAS_VIEW, WebResourceLinkType.IS_SHOWN_BY));
    doReturn(hasLandingPage).when(entity).hasLandingPage();
    doReturn(hasThumbnails).when(entity).hasThumbnails();
    doReturn(entityLicense).when(entity).getLicenseType();
    doReturn(null).when(classifier).preClassifyEntity(entity);
    //Has embeddable media will be true
    doReturn(Set.of("https://soundcloud.com/")).when(entity).getUrlsOfTypes(Set.of(WebResourceLinkType.IS_SHOWN_BY));

    // Create web resources.
    doReturn(mediaResourceTechnicalMetadata1).when(classifier)
                                             .classifyWebResourceAndLicense(resource1, entityLicense, hasLandingPage, true);
    doReturn(mediaResourceTechnicalMetadata2).when(classifier)
                                             .classifyWebResourceAndLicense(resource2, entityLicense, hasLandingPage, true);
    assertEquals(MediaTier.T4, classifier.classify(entity).getTier());
  }

  @ParameterizedTest(name = "[{index}] - For entityLicenseType:{0}, resourceLicenseType:{1}, resourceTier{2} expectedTier is {3}")
  @MethodSource("classifyWebResourceAndLicense")
  void classifyWebResourceAndLicense(LicenseType entityLicenseType, LicenseType resourceLicenseType, MediaTier resourceTier,
      MediaTier expectedMediaTier) {
    final boolean hasLandingPage = true;
    final boolean hasEmbeddableMedia = false;
    final WebResourceWrapper webResource = mock(WebResourceWrapper.class);
    when(webResource.getAbout()).thenReturn("about");
    when(webResource.getMediaType()).thenReturn(MediaType.IMAGE);
    when(webResource.getMimeType()).thenReturn("mimeType");
    when(webResource.getLinkTypes()).thenReturn(Set.of(WebResourceLinkType.OBJECT));
    when(webResource.getLicenseType()).thenReturn(Optional.ofNullable(resourceLicenseType));

    doReturn(resourceTier).when(classifier).classifyWebResource(webResource, hasLandingPage, hasEmbeddableMedia);
    when(classifier.extractResolutionTierMetadata(webResource, resourceTier)).thenReturn(
        new ResolutionTierMetadata.ResolutionTierMetadataBuilder().build());
    assertEquals(expectedMediaTier, classifier.classifyWebResourceAndLicense(webResource, entityLicenseType,
        hasLandingPage, hasEmbeddableMedia).getMediaTier());
  }

  static Stream<Arguments> classifyWebResourceAndLicense() {
    return Stream.of(
        //Same resource license tier and resource tier remains the same
        Arguments.of(LicenseType.OPEN, LicenseType.OPEN, MediaTier.T4, LicenseType.OPEN.getMediaTier()),
        //Low resource tier brings result down
        Arguments.of(LicenseType.OPEN, LicenseType.OPEN, MediaTier.T0, MediaTier.T0),
        //Low resource license tier brings result down
        Arguments.of(LicenseType.OPEN, LicenseType.CLOSED, MediaTier.T4, LicenseType.CLOSED.getMediaTier()),

        //Null resource license. Same entity license tier and resource tier remains the same
        Arguments.of(LicenseType.OPEN, null, MediaTier.T4, LicenseType.OPEN.getMediaTier()),
        //Null resource license. Low entity tier brings result down
        Arguments.of(LicenseType.OPEN, null, MediaTier.T0, MediaTier.T0),
        //Null resource license. Low entity license tier brings result down
        Arguments.of(LicenseType.CLOSED, null, MediaTier.T4, LicenseType.CLOSED.getMediaTier()),

        //If entity is also null then LicenseType.CLOSED should be chosen without exception thrown
        Arguments.of(null, null, MediaTier.T4, LicenseType.CLOSED.getMediaTier())
    );
  }

}
