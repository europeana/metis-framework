package eu.europeana.indexing.tiers.metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import eu.europeana.indexing.tiers.model.MetadataTier;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.metis.schema.jibx.Concept;
import eu.europeana.metis.schema.jibx.CurrentLocation;
import eu.europeana.metis.schema.jibx.EuropeanaProxy;
import eu.europeana.metis.schema.jibx.EuropeanaType.Choice;
import eu.europeana.metis.schema.jibx.PlaceType;
import eu.europeana.metis.schema.jibx.ProxyType;
import eu.europeana.metis.schema.jibx.ResourceOrLiteralType;
import eu.europeana.metis.schema.jibx.TimeSpanType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class LanguageClassifierTest {

  @ParameterizedTest
  @EnumSource
  void testClassify(ClassifierMode classifierMode) {

    // Create mocks
    final LanguageClassifier classifier = spy(new LanguageClassifier(classifierMode));
    final LanguageTagStatistics statistics = mock(LanguageTagStatistics.class);
    final RdfWrapper entity = mock(RdfWrapper.class);
    doReturn(statistics).when(classifier).createLanguageTagStatistics(entity, classifierMode);

    // Try for different values of the ratio
    doReturn(0.0).when(statistics).getPropertiesWithLanguageRatio();
    assertEquals(MetadataTier.T0, classifier.classifyBreakdown(entity).getMetadataTier());
    doReturn(0.24).when(statistics).getPropertiesWithLanguageRatio();
    assertEquals(MetadataTier.T0, classifier.classifyBreakdown(entity).getMetadataTier());
    doReturn(0.25).when(statistics).getPropertiesWithLanguageRatio();
    assertEquals(MetadataTier.TA, classifier.classifyBreakdown(entity).getMetadataTier());
    doReturn(0.49).when(statistics).getPropertiesWithLanguageRatio();
    assertEquals(MetadataTier.TA, classifier.classifyBreakdown(entity).getMetadataTier());
    doReturn(0.50).when(statistics).getPropertiesWithLanguageRatio();
    assertEquals(MetadataTier.TB, classifier.classifyBreakdown(entity).getMetadataTier());
    doReturn(0.74).when(statistics).getPropertiesWithLanguageRatio();
    assertEquals(MetadataTier.TB, classifier.classifyBreakdown(entity).getMetadataTier());
    doReturn(0.75).when(statistics).getPropertiesWithLanguageRatio();
    assertEquals(MetadataTier.TC, classifier.classifyBreakdown(entity).getMetadataTier());
    doReturn(1.0).when(statistics).getPropertiesWithLanguageRatio();
    assertEquals(MetadataTier.TC, classifier.classifyBreakdown(entity).getMetadataTier());
  }

  static Stream<Arguments> enumSource() {
    return Stream.of(
        Arguments.arguments(ClassifierMode.ALL_PROXIES, 1,1,1,3),
        Arguments.arguments(ClassifierMode.PROVIDER_PROXIES, 1,1,0,2)
    );
  }

  @ParameterizedTest
  @MethodSource("enumSource")
  void testCreateLanguageTagStatistics(ClassifierMode classifierMode,
      int expectedP1, int expectedP2, int expectedP3, int expectedTimes) {

    // Create the RDF
    final RdfWrapper rdf = mock(RdfWrapper.class);

    // Create the contextual classes
    final PlaceType place1 = new PlaceType();
    place1.setAbout("place about 1");
    final PlaceType place2 = new PlaceType();
    place2.setAbout("place about 2");
    final TimeSpanType timeSpan = new TimeSpanType();
    timeSpan.setAbout("time span about");
    final Concept concept = new Concept();
    concept.setAbout("concept about");
    doReturn(Arrays.asList(place1, place2)).when(rdf).getPlaces();
    doReturn(Collections.singletonList(timeSpan)).when(rdf).getTimeSpans();
    doReturn(Collections.singletonList(concept)).when(rdf).getConcepts();

    // Create proxies
    final ProxyType providerProxy1 = new ProxyType();
    providerProxy1.setEuropeanaProxy(new EuropeanaProxy());
    providerProxy1.getEuropeanaProxy().setEuropeanaProxy(false);
    final ProxyType providerProxy2 = new ProxyType();
    providerProxy2.setEuropeanaProxy(new EuropeanaProxy());
    providerProxy2.getEuropeanaProxy().setEuropeanaProxy(false);
    doReturn(Arrays.asList(providerProxy1, providerProxy2, null)).when(rdf).getProviderProxies();
    final ProxyType providerProxy3 = new ProxyType();
    providerProxy3.setEuropeanaProxy(new EuropeanaProxy());
    providerProxy3.getEuropeanaProxy().setEuropeanaProxy(true);
    doReturn(Arrays.asList(providerProxy1, providerProxy2, providerProxy3, null)).when(rdf).getProxies();

    // Test the method.
    final LanguageClassifier classifier = spy(new LanguageClassifier(classifierMode));
    final LanguageTagStatistics result = classifier.createLanguageTagStatistics(rdf, classifierMode);
    verify(classifier, times(expectedP1)).addProxyToStatistics(providerProxy1, result);
    verify(classifier, times(expectedP2)).addProxyToStatistics(providerProxy2, result);
    verify(classifier, times(expectedP3)).addProxyToStatistics(providerProxy3, result);
    verify(classifier, times(expectedTimes)).addProxyToStatistics(any(), any());
  }

  @ParameterizedTest
  @EnumSource
  void testAddProxyToStatistics(ClassifierMode classifierMode) {

    // Create mocks
    final LanguageClassifier classifier = spy(new LanguageClassifier(classifierMode));
    final LanguageTagStatistics statistics = mock(LanguageTagStatistics.class);

    // Test with proxy without data.
    final ProxyType proxy = new ProxyType();
    proxy.setHasTypeList(null);
    proxy.setIsRelatedToList(null);
    proxy.setChoiceList(null);
    classifier.addProxyToStatistics(proxy, statistics);
    verify(statistics, times(1))
        .addToStatistics((ResourceOrLiteralType) null, PropertyType.EDM_CURRENT_LOCATION);
    verify(statistics, times(1))
        .addToStatistics((List<ResourceOrLiteralType>) null, PropertyType.EDM_HAS_TYPE);
    verify(statistics, times(1))
        .addToStatistics((List<ResourceOrLiteralType>) null, PropertyType.EDM_IS_RELATED_TO);
    verifyNoMoreInteractions(statistics);

    // Test with values
    proxy.setCurrentLocation(new CurrentLocation());
    proxy.setHasTypeList(new ArrayList<>());
    proxy.setIsRelatedToList(new ArrayList<>());
    proxy.setChoiceList(new ArrayList<>());
    proxy.getChoiceList().add(new Choice());
    proxy.getChoiceList().add(null);
    classifier.addProxyToStatistics(proxy, statistics);
    verify(statistics, times(1))
        .addToStatistics(same(proxy.getCurrentLocation()), eq(PropertyType.EDM_CURRENT_LOCATION));
    verify(statistics, times(1))
        .addToStatistics(same(proxy.getHasTypeList()), eq(PropertyType.EDM_HAS_TYPE));
    verify(statistics, times(1))
        .addToStatistics(same(proxy.getIsRelatedToList()), eq(PropertyType.EDM_IS_RELATED_TO));
    verify(statistics, times(1))
        .addToStatistics(same(proxy.getChoiceList().get(0)));
    verify(statistics, times(1)).addToStatistics(isNull());
    verifyNoMoreInteractions(statistics);
  }
}
