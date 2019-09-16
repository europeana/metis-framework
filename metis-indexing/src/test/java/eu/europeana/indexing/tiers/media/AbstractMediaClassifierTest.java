package eu.europeana.indexing.tiers.media;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.utils.LicenseType;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.indexing.utils.WebResourceLinkType;
import eu.europeana.indexing.utils.WebResourceWrapper;

class AbstractMediaClassifierTest {

  private static AbstractMediaClassifier classifier;

  @BeforeAll
  static void setup() {
    classifier = spy(new MediaClassifierImpl());
  }

  static class MediaClassifierImpl extends AbstractMediaClassifier {

    @Override
    MediaTier preClassifyEntity(RdfWrapper entity) {
      return null;
    }

    @Override
    MediaTier classifyEntityWithoutWebResources(RdfWrapper entity, boolean hasLandingPage) {
      return null;
    }

    @Override
    MediaTier classifyWebResource(WebResourceWrapper webResource, boolean hasLandingPage,
        boolean hasEmbeddableMedia) {
      return null;
    }
  }

  @BeforeEach
  void resetMocks() {
    reset(classifier);
  }

  @Test
  void testClassify() {

    // Some constants.
    final boolean hasLandingPage = true;
    final LicenseType entityLicense = LicenseType.RESTRICTED;
    final MediaTier highTier = MediaTier.T3;
    final MediaTier lowTier = MediaTier.T2;
    final MediaTier emptyTier = MediaTier.T1;
    final MediaTier preClassifyTier = MediaTier.T4;

    // Create web resources.
    final WebResourceWrapper resource1 = mock(WebResourceWrapper.class);
    doReturn(highTier).when(classifier).classifyWebResource(eq(resource1), eq(entityLicense),
        eq(hasLandingPage), anyBoolean());
    final WebResourceWrapper resource2 = mock(WebResourceWrapper.class);
    doReturn(highTier).when(classifier).classifyWebResource(eq(resource2), eq(entityLicense),
        eq(hasLandingPage), anyBoolean());
    final WebResourceWrapper resource3 = mock(WebResourceWrapper.class);
    doReturn(lowTier).when(classifier).classifyWebResource(eq(resource3), eq(entityLicense),
        eq(hasLandingPage), anyBoolean());

    // Create entity
    final RdfWrapper entity = mock(RdfWrapper.class);
    doReturn(entityLicense).when(entity).getLicenseType();
    doReturn(hasLandingPage).when(entity).hasLandingPage();
    doReturn(emptyTier).when(classifier).classifyEntityWithoutWebResources(entity, hasLandingPage);
    doReturn(null).when(classifier).preClassifyEntity(entity);

    // Test for all resources
    doReturn(Arrays.asList(resource1, resource2, resource3)).when(entity).getWebResourceWrappers(
        EnumSet.of(WebResourceLinkType.HAS_VIEW, WebResourceLinkType.IS_SHOWN_BY));
    assertEquals(highTier, classifier.classify(entity));
    
    // Test for one resource
    doReturn(Collections.singletonList(resource3)).when(entity).getWebResourceWrappers(
        EnumSet.of(WebResourceLinkType.HAS_VIEW, WebResourceLinkType.IS_SHOWN_BY));
    assertEquals(lowTier, classifier.classify(entity));
    
    // Test for no resource
    doReturn(Collections.emptyList()).when(entity).getWebResourceWrappers(
        EnumSet.of(WebResourceLinkType.HAS_VIEW, WebResourceLinkType.IS_SHOWN_BY));
    assertEquals(emptyTier, classifier.classify(entity));
    
    // Test pre-classification
    doReturn(preClassifyTier).when(classifier).preClassifyEntity(entity);
    assertEquals(preClassifyTier, classifier.classify(entity));
  }
  
  @Test
  void testClassifyWebResource() {

    // Tier 0, regardless of license.
    testClassifyWebResource(MediaTier.T0, MediaTier.T0, MediaTier.T0);

    // Tier 1, regardless of license.
    testClassifyWebResource(MediaTier.T1, MediaTier.T1, MediaTier.T1);

    // Tier 2, regardless of license.
    testClassifyWebResource(MediaTier.T2, MediaTier.T2, MediaTier.T2);

    // Tier 3, but if no license is present, this is reduced to tier 2.
    testClassifyWebResource(MediaTier.T2, MediaTier.T3, MediaTier.T3);

    // Tier 4, but if no open license is present, this is reduced to tier 2 or 3.
    testClassifyWebResource(MediaTier.T2, MediaTier.T3, MediaTier.T4);
  }

  void testClassifyWebResource(MediaTier tierWithoutLicense, MediaTier tierWithRestrictedLicense,
      MediaTier tierWithOpenLicense) {

    // Create web resource and mock subclass' classification method.
    final boolean hasLandingPage = true;
    final boolean hasEmbeddableMedia = false;
    final WebResourceWrapper webResource = mock(WebResourceWrapper.class);
    doReturn(tierWithOpenLicense).when(classifier).classifyWebResource(webResource, hasLandingPage,
        hasEmbeddableMedia);

    // In case the web resource has no license type.
    doReturn(null).when(webResource).getLicenseType();
    assertEquals(tierWithoutLicense,
        classifier.classifyWebResource(webResource, null, hasLandingPage, hasEmbeddableMedia));
    assertEquals(tierWithRestrictedLicense, classifier.classifyWebResource(webResource,
        LicenseType.RESTRICTED, hasLandingPage, hasEmbeddableMedia));
    assertEquals(tierWithOpenLicense, classifier.classifyWebResource(webResource, LicenseType.OPEN,
        hasLandingPage, hasEmbeddableMedia));

    // In case the web resource has a restricted license type.
    doReturn(LicenseType.RESTRICTED).when(webResource).getLicenseType();
    assertEquals(tierWithRestrictedLicense,
        classifier.classifyWebResource(webResource, null, hasLandingPage, hasEmbeddableMedia));
    assertEquals(tierWithRestrictedLicense, classifier.classifyWebResource(webResource,
        LicenseType.RESTRICTED, hasLandingPage, hasEmbeddableMedia));
    assertEquals(tierWithOpenLicense, classifier.classifyWebResource(webResource, LicenseType.OPEN,
        hasLandingPage, hasEmbeddableMedia));

    // In case the web resource has an open license type.
    doReturn(LicenseType.OPEN).when(webResource).getLicenseType();
    assertEquals(tierWithOpenLicense,
        classifier.classifyWebResource(webResource, null, hasLandingPage, hasEmbeddableMedia));
    assertEquals(tierWithOpenLicense, classifier.classifyWebResource(webResource,
        LicenseType.RESTRICTED, hasLandingPage, hasEmbeddableMedia));
    assertEquals(tierWithOpenLicense, classifier.classifyWebResource(webResource, LicenseType.OPEN,
        hasLandingPage, hasEmbeddableMedia));
  }
}
