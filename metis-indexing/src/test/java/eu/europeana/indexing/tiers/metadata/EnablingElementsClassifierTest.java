package eu.europeana.indexing.tiers.metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import eu.europeana.indexing.tiers.metadata.EnablingElement.EnablingElementGroup;
import eu.europeana.indexing.tiers.metadata.EnablingElementsClassifier.EnablingElementInventory;
import eu.europeana.indexing.tiers.model.MetadataTier;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.metis.schema.jibx.AboutType;
import eu.europeana.metis.schema.jibx.AgentType;
import eu.europeana.metis.schema.jibx.Concept;
import eu.europeana.metis.schema.jibx.Created;
import eu.europeana.metis.schema.jibx.EuropeanaType.Choice;
import eu.europeana.metis.schema.jibx.HasMet;
import eu.europeana.metis.schema.jibx.PlaceType;
import eu.europeana.metis.schema.jibx.ProxyType;
import eu.europeana.metis.schema.jibx.ResourceOrLiteralType.Resource;
import eu.europeana.metis.schema.jibx.TimeSpanType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.Test;

class EnablingElementsClassifierTest {

  @Test
  void testClassify() {

    // Create mocked objects
    final RdfWrapper entity = mock(RdfWrapper.class);
    final EnablingElementsClassifier classifier = spy(new EnablingElementsClassifier());
    final EnablingElementInventory inventory = mock(EnablingElementInventory.class);
    doReturn(inventory).when(classifier).performEnablingElementInventory(entity);

    // Zero groups/elements
    doReturn(Collections.emptySet()).when(inventory).getElements();
    doReturn(Collections.emptySet()).when(inventory).getGroups();
    assertEquals(MetadataTier.T0, classifier.classify(entity).getTier());

    // One group, various element counts
    doReturn(Set.of(EnablingElementGroup.PERSONAL)).when(inventory).getGroups();
    doReturn(Set.of(EnablingElement.DC_TYPE)).when(inventory).getElements();
    assertEquals(MetadataTier.TA, classifier.classify(entity).getTier());
    doReturn(Set.of(EnablingElement.DC_TYPE, EnablingElement.DCTERMS_ISSUED)).when(inventory).getElements();
    assertEquals(MetadataTier.TA, classifier.classify(entity).getTier());
    doReturn(Set.of(EnablingElement.DC_TYPE, EnablingElement.DCTERMS_ISSUED, EnablingElement.DCTERMS_CREATED)).when(inventory)
                                                                                                              .getElements();
    assertEquals(MetadataTier.TA, classifier.classify(entity).getTier());
    doReturn(Set.of(EnablingElement.DC_TYPE, EnablingElement.DCTERMS_ISSUED, EnablingElement.DCTERMS_CREATED,
        EnablingElement.EDM_HAS_MET)).when(inventory).getElements();
    assertEquals(MetadataTier.TA, classifier.classify(entity).getTier());

    // Two groups, various element counts
    doReturn(Set.of(EnablingElementGroup.PERSONAL, EnablingElementGroup.CONCEPTUAL)).when(inventory).getGroups();
    doReturn(Set.of(EnablingElement.DC_TYPE)).when(inventory).getElements();
    assertEquals(MetadataTier.TA, classifier.classify(entity).getTier());
    doReturn(Set.of(EnablingElement.DC_TYPE, EnablingElement.DCTERMS_ISSUED)).when(inventory).getElements();
    assertEquals(MetadataTier.TA, classifier.classify(entity).getTier());
    doReturn(Set.of(EnablingElement.DC_TYPE, EnablingElement.DCTERMS_ISSUED, EnablingElement.DCTERMS_CREATED)).when(inventory)
                                                                                                              .getElements();
    assertEquals(MetadataTier.TB, classifier.classify(entity).getTier());
    doReturn(Set.of(EnablingElement.DC_TYPE, EnablingElement.DCTERMS_ISSUED, EnablingElement.DCTERMS_CREATED,
        EnablingElement.EDM_HAS_MET)).when(inventory).getElements();
    assertEquals(MetadataTier.TC, classifier.classify(entity).getTier());
  }

  @Test
  void testPerformEnablingElementInventory() {

    // Create mocked objects
    final RdfWrapper entity = mock(RdfWrapper.class);
    final List<ProxyType> providerProxies = new ArrayList<>();
    doReturn(providerProxies).when(entity).getProviderProxies();
    final EnablingElementsClassifier classifier = spy(new EnablingElementsClassifier());
    final Map<String, Set<Class<? extends AboutType>>> contextualObjectMap = new HashMap<>();
    doReturn(contextualObjectMap).when(classifier).createContextualObjectMap(entity);

    // Set behavior of analyze result
    doReturn(Collections.emptySet()).when(classifier)
                                    .analyzeForElement(any(), same(providerProxies), same(contextualObjectMap));
    doReturn(new HashSet<>(
        Arrays.asList(EnablingElementGroup.PERSONAL, EnablingElementGroup.CONCEPTUAL)))
        .when(classifier).analyzeForElement(eq(EnablingElement.DCTERMS_ISSUED),
            same(providerProxies), same(contextualObjectMap));
    doReturn(Collections.singleton(EnablingElementGroup.PERSONAL))
        .when(classifier).analyzeForElement(eq(EnablingElement.DCTERMS_CREATED),
            same(providerProxies), same(contextualObjectMap));
    doReturn(Collections.singleton(EnablingElementGroup.PERSONAL))
        .when(classifier).analyzeForElement(eq(EnablingElement.EDM_HAS_MET), same(providerProxies),
            same(contextualObjectMap));
    doReturn(Collections.singleton(EnablingElementGroup.TEMPORAL))
        .when(classifier).analyzeForElement(eq(EnablingElement.DC_SUBJECT), same(providerProxies),
            same(contextualObjectMap));

    // Make the call and verify
    final EnablingElementInventory result = classifier.performEnablingElementInventory(entity);
    assertTrue(CollectionUtils.isEqualCollection(
        Set.of(EnablingElementGroup.PERSONAL, EnablingElementGroup.CONCEPTUAL, EnablingElementGroup.TEMPORAL),
        result.getGroups()));
    assertTrue(CollectionUtils.isEqualCollection(
        Set.of(EnablingElement.DC_SUBJECT, EnablingElement.DCTERMS_ISSUED, EnablingElement.DCTERMS_CREATED,
            EnablingElement.EDM_HAS_MET), result.getElements()));
  }


  @Test
  void testCreateContextualObjectMap() {

    // Create entity and classifier
    final EnablingElementsClassifier classifier = new EnablingElementsClassifier();
    final RdfWrapper entity = mock(RdfWrapper.class);

    // Test with empty lists. Note: null lists, null objects and blank about values cannot happen.
    doReturn(Collections.emptyList()).when(entity).getAgents();
    doReturn(Collections.emptyList()).when(entity).getConcepts();
    doReturn(Collections.emptyList()).when(entity).getPlaces();
    doReturn(Collections.emptyList()).when(entity).getTimeSpans();
    assertTrue(classifier.createContextualObjectMap(entity).isEmpty());

    // Create actual test data
    final String link1 = "link 1";
    final String link2 = "link 2";
    final String link3 = "link 3";
    final String link4 = "link 4";
    final String link5 = "link 5";
    final AgentType agent = new AgentType();
    agent.setAbout(link1);
    final Concept concept = new Concept();
    concept.setAbout(link2);
    final PlaceType place = new PlaceType();
    place.setAbout(link3);
    final TimeSpanType timeSpan = new TimeSpanType();
    timeSpan.setAbout(link4);
    final PlaceType duplicatePlace = new PlaceType();
    duplicatePlace.setAbout(link5);
    final TimeSpanType duplicateTimeSpan = new TimeSpanType();
    duplicateTimeSpan.setAbout(link5);
    doReturn(Collections.singletonList(agent)).when(entity).getAgents();
    doReturn(Collections.singletonList(concept)).when(entity).getConcepts();
    doReturn(Arrays.asList(place, duplicatePlace)).when(entity).getPlaces();
    doReturn(Arrays.asList(timeSpan, duplicateTimeSpan)).when(entity).getTimeSpans();

    // make the call and verify
    final Map<String, Set<Class<? extends AboutType>>> result = classifier
        .createContextualObjectMap(entity);
    assertEquals(Collections.singleton(AgentType.class), result.get(link1));
    assertEquals(Collections.singleton(Concept.class), result.get(link2));
    assertEquals(Collections.singleton(PlaceType.class), result.get(link3));
    assertEquals(Collections.singleton(TimeSpanType.class), result.get(link4));
    assertEquals(new HashSet<>(Arrays.asList(PlaceType.class, TimeSpanType.class)),
        result.get(link5));
  }

  @Test
  void testAnalyzeForElements() {

    // Create some objects.
    final EnablingElementsClassifier classifier = new EnablingElementsClassifier();
    final String link = "link";
    final Map<String, Set<Class<? extends AboutType>>> contextualObjectMap = Collections
        .singletonMap(link, EnumSet.allOf(EnablingElementGroup.class).stream()
                                   .map(EnablingElementGroup::getContextualClass).collect(Collectors.toSet()));

    // Create proxy with Created and a HasMet.
    final Created created = new Created();
    created.setString(link);
    created.setResource(new Resource());
    created.getResource().setResource(link);
    final HasMet hasMet = new HasMet();
    hasMet.setResource(link);
    final ProxyType proxy = new ProxyType();
    proxy.setChoiceList(Collections.singletonList(new Choice()));
    proxy.getChoiceList().get(0).setCreated(created);
    proxy.setHasMetList(Collections.singletonList(hasMet));
    final List<ProxyType> proxies = Collections.singletonList(proxy);

    // Test for some queries that the answer is the same as calling the enum directly. The enum
    // itself is tested in its own unit test class.
    assertEquals(
        EnablingElement.EDM_HAS_MET.analyze(proxies, contextualObjectMap),
        classifier.analyzeForElement(EnablingElement.EDM_HAS_MET, proxies, contextualObjectMap)
    );
    assertEquals(
        EnablingElement.DCTERMS_CREATED.analyze(proxies, contextualObjectMap),
        classifier.analyzeForElement(EnablingElement.DCTERMS_CREATED, proxies, contextualObjectMap)
    );
    assertEquals(
        EnablingElement.DCTERMS_ISSUED.analyze(proxies, contextualObjectMap),
        classifier.analyzeForElement(EnablingElement.DCTERMS_ISSUED, proxies, contextualObjectMap)
    );
  }
}
