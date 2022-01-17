package eu.europeana.indexing.tiers.metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.indexing.tiers.metadata.EnablingElement.EnablingElementGroup;
import eu.europeana.metis.schema.jibx.AboutType;
import eu.europeana.metis.schema.jibx.Contributor;
import eu.europeana.metis.schema.jibx.Created;
import eu.europeana.metis.schema.jibx.Creator;
import eu.europeana.metis.schema.jibx.CurrentLocation;
import eu.europeana.metis.schema.jibx.EuropeanaType.Choice;
import eu.europeana.metis.schema.jibx.Format;
import eu.europeana.metis.schema.jibx.HasMet;
import eu.europeana.metis.schema.jibx.Issued;
import eu.europeana.metis.schema.jibx.Medium;
import eu.europeana.metis.schema.jibx.ProxyType;
import eu.europeana.metis.schema.jibx.Publisher;
import eu.europeana.metis.schema.jibx.ResourceOrLiteralType;
import eu.europeana.metis.schema.jibx.ResourceOrLiteralType.Resource;
import eu.europeana.metis.schema.jibx.ResourceType;
import eu.europeana.metis.schema.jibx.Spatial;
import eu.europeana.metis.schema.jibx.Subject;
import eu.europeana.metis.schema.jibx.Temporal;
import eu.europeana.metis.schema.jibx.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class EnablingElementTest {

  @Test
  void testAnalyze() {
    testChoiceElementWithFixedGroup(EnablingElement.DCTERMS_CREATED, EnablingElementGroup.TEMPORAL,
        Created::new, Choice::setCreated);
    testChoiceElementWithFixedGroup(EnablingElement.DCTERMS_ISSUED, EnablingElementGroup.TEMPORAL,
        Issued::new, Choice::setIssued);
    testChoiceElementWithFixedGroup(EnablingElement.DCTERMS_TEMPORAL, EnablingElementGroup.TEMPORAL,
        Temporal::new, Choice::setTemporal);
    testChoiceElementWithFixedGroup(EnablingElement.DC_FORMAT, EnablingElementGroup.CONCEPTUAL,
        Format::new, Choice::setFormat);
    testChoiceElementWithFixedGroup(EnablingElement.DC_TYPE, EnablingElementGroup.CONCEPTUAL,
        Type::new, Choice::setType);
    testChoiceElementWithFixedGroup(EnablingElement.DCTERMS_MEDIUM, EnablingElementGroup.CONCEPTUAL,
        Medium::new, Choice::setMedium);
    testChoiceElementWithFixedGroup(EnablingElement.DC_CREATOR, EnablingElementGroup.PERSONAL,
        Creator::new, Choice::setCreator);
    testChoiceElementWithFixedGroup(EnablingElement.DC_CONTRIBUTOR, EnablingElementGroup.PERSONAL,
        Contributor::new, Choice::setContributor);
    testChoiceElementWithFixedGroup(EnablingElement.DC_PUBLISHER, EnablingElementGroup.PERSONAL,
        Publisher::new, Choice::setPublisher);
    testChoiceElementWithFixedGroup(EnablingElement.DCTERMS_SPATIAL, EnablingElementGroup.GEOGRAPHICAL,
        Spatial::new, Choice::setSpatial);
    testElementWithFixedGroup(EnablingElement.EDM_CURRENT_LOCATION, EnablingElementGroup.GEOGRAPHICAL,
        CurrentLocation::new, ProxyType::setCurrentLocation);
    testElementWithCandidateGroups(EnablingElement.EDM_HAS_MET,
        EnumSet.of(EnablingElementGroup.TEMPORAL, EnablingElementGroup.PERSONAL),
        HasMet::new, ProxyType::setHasMetList, ResourceType::setResource);
    testChoiceElementWithCandidateGroups(EnablingElement.DC_SUBJECT, EnumSet
        .of(EnablingElementGroup.CONCEPTUAL, EnablingElementGroup.PERSONAL,
            EnablingElementGroup.GEOGRAPHICAL), Subject::new, Choice::setSubject);
  }

  private <T extends ResourceOrLiteralType> void testChoiceElementWithFixedGroup(EnablingElement element,
      EnablingElementGroup group, Supplier<T> constructor, BiConsumer<Choice, T> setter) {
    final BiConsumer<ProxyType, T> wrappedSetter = (proxy, value)->{
      proxy.setChoiceList(new ArrayList<>());
      proxy.getChoiceList().add(new Choice());
      setter.accept(proxy.getChoiceList().get(0), value);
    } ;
    testElementWithFixedGroup(element, group, constructor, wrappedSetter);
  }

  private <T extends ResourceOrLiteralType> void testElementWithFixedGroup(EnablingElement element,
      EnablingElementGroup group, Supplier<T> constructor, BiConsumer<ProxyType, T> setter) {

    final Set<EnablingElementGroup> groups = Collections.singleton(group);

    // Test with empty list
    assertTrue(element.analyze(Collections.emptyList(), Collections.emptyMap()).isEmpty());

    // Test with empty proxies
    final ProxyType proxy1 = new ProxyType();
    final ProxyType proxy2 = new ProxyType();
    assertTrue(element.analyze(Arrays.asList(proxy1, proxy2), Collections.emptyMap()).isEmpty());

    // Set just a value in one of them.
    // Note: we don't have to check a variety of values, ResourceLinkFromProxyTest already does.
    final T value = constructor.get();
    setter.accept(proxy1, value);
    value.setString("test value");
    assertEquals(groups, element.analyze(Arrays.asList(proxy1, proxy2), Collections.emptyMap()));
    assertEquals(groups, element.analyze(Arrays.asList(proxy2, proxy1), Collections.emptyMap()));

    // Set just a link in one of them.
    // Note: we don't have to check a variety of links, ResourceLinkFromProxyTest already does.
    value.setString(null);
    value.setResource(new Resource());
    value.getResource().setResource("test link");
    assertEquals(groups, element.analyze(Arrays.asList(proxy1, proxy2), Collections.emptyMap()));
    assertEquals(groups, element.analyze(Arrays.asList(proxy2, proxy1), Collections.emptyMap()));
  }

  private <T > void testElementWithCandidateGroups(
      EnablingElement element, Set<EnablingElementGroup> candidates, Supplier<T> constructor,
      BiConsumer<ProxyType, List<T>> objectSetter, BiConsumer<T, String> linkSetter) {

    // Create link map.
    final String link1="link 1";
    final String link2 = "link 2";
    final String link3 = "link 3";
    final String link4= "link 4";
    final Set<EnablingElementGroup> groups1 = EnumSet.noneOf(EnablingElementGroup.class);
    final Set<EnablingElementGroup> groups2 = EnumSet.of(EnablingElementGroup.TEMPORAL);
    final Set<EnablingElementGroup> groups3 = EnumSet.of(EnablingElementGroup.CONCEPTUAL);
    final Set<EnablingElementGroup> groups4 = EnumSet.allOf(EnablingElementGroup.class);
    final Map<String, Set<Class<? extends AboutType>>> typeMap = new HashMap<>();
    typeMap.put(link1, getTypes(groups1));
    typeMap.put(link2, getTypes(groups2));
    typeMap.put(link3, getTypes(groups3));
    typeMap.put(link4, getTypes(groups4));

    // Test with empty list
    assertTrue(element.analyze(Collections.emptyList(), typeMap).isEmpty());

    // Test with empty proxies
    final ProxyType proxy1 = new ProxyType();
    final ProxyType proxy2 = new ProxyType();
    assertTrue(element.analyze(Arrays.asList(proxy1, proxy2), typeMap).isEmpty());

    // Set just a link in one of them - this should trigger a result.
    // Note: we don't have to check a variety of links, ResourceLinkFromProxyTest already does.
    final T value1 = constructor.get();
    final T value2 = constructor.get();
    linkSetter.accept(value1, link4);
    objectSetter.accept(proxy1, Arrays.asList(value1, value2));
    assertEquals(candidates, element.analyze(Arrays.asList(proxy1, proxy2), typeMap));
    assertEquals(candidates, element.analyze(Arrays.asList(proxy2, proxy1), typeMap));

    // Set multiple links and compare.
    linkSetter.accept(value1, link2);
    linkSetter.accept(value2, link3);
    final Set<EnablingElementGroup> result = new HashSet<>();
    result.addAll(groups2);
    result.addAll(groups3);
    result.retainAll(candidates);
    assertEquals(result, element.analyze(Arrays.asList(proxy1, proxy2), typeMap));
    assertEquals(result, element.analyze(Arrays.asList(proxy2, proxy1), typeMap));

    // Test for the empty set.
    linkSetter.accept(value1, link1);
    linkSetter.accept(value2, link1);
    assertTrue(element.analyze(Arrays.asList(proxy1, proxy2), typeMap).isEmpty());
    assertTrue(element.analyze(Arrays.asList(proxy2, proxy1), typeMap).isEmpty());
  }

  private <T extends ResourceOrLiteralType> void testChoiceElementWithCandidateGroups(
      EnablingElement element, Set<EnablingElementGroup> candidates, Supplier<T> constructor,
      BiConsumer<Choice, T> setter) {

    // Do the testing on the links.
    final BiConsumer<ProxyType, List<T>> objectSetter = (proxy, values) -> {
      proxy.setChoiceList(new ArrayList<>());
      Optional.ofNullable(values).stream().flatMap(Collection::stream).forEach(value -> {
        final Choice choice = new Choice();
        proxy.getChoiceList().add(choice);
        setter.accept(choice, value);
      });
    };
    final BiConsumer<T, String> linkSetter = (value, link) -> {
      value.setResource(new Resource());
      value.getResource().setResource(link);
    };
    testElementWithCandidateGroups(element, candidates, constructor, objectSetter, linkSetter);

    // Test literals: they do not create any match (but links still do, proving the setup is correct).
    final String link = "link";
    final Map<String, Set<Class<? extends AboutType>>> typeMap = Collections
        .singletonMap(link, getTypes(EnumSet.allOf(EnablingElementGroup.class)));
    final T value = constructor.get();
    value.setString(link);
    final ProxyType proxy = new ProxyType();
    objectSetter.accept(proxy, Collections.singletonList(value));
    assertTrue(element.analyze(Collections.singletonList(proxy), typeMap).isEmpty());
    linkSetter.accept(value, link);
    assertEquals(candidates, element.analyze(Collections.singletonList(proxy), typeMap));
  }

  private Set<Class<? extends AboutType>> getTypes(Set<EnablingElementGroup> groups){
    return groups.stream().map(EnablingElementGroup::getContextualClass).collect(Collectors.toSet());
  }
}
