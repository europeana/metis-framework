package eu.europeana.indexing.tiers.metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import eu.europeana.corelib.definitions.jibx.ConformsTo;
import eu.europeana.corelib.definitions.jibx.Contributor;
import eu.europeana.corelib.definitions.jibx.Coverage;
import eu.europeana.corelib.definitions.jibx.Created;
import eu.europeana.corelib.definitions.jibx.Creator;
import eu.europeana.corelib.definitions.jibx.CurrentLocation;
import eu.europeana.corelib.definitions.jibx.Date;
import eu.europeana.corelib.definitions.jibx.Description;
import eu.europeana.corelib.definitions.jibx.EuropeanaType.Choice;
import eu.europeana.corelib.definitions.jibx.Extent;
import eu.europeana.corelib.definitions.jibx.Format;
import eu.europeana.corelib.definitions.jibx.HasFormat;
import eu.europeana.corelib.definitions.jibx.HasMet;
import eu.europeana.corelib.definitions.jibx.HasPart;
import eu.europeana.corelib.definitions.jibx.HasType;
import eu.europeana.corelib.definitions.jibx.HasVersion;
import eu.europeana.corelib.definitions.jibx.Incorporates;
import eu.europeana.corelib.definitions.jibx.IsDerivativeOf;
import eu.europeana.corelib.definitions.jibx.IsFormatOf;
import eu.europeana.corelib.definitions.jibx.IsNextInSequence;
import eu.europeana.corelib.definitions.jibx.IsPartOf;
import eu.europeana.corelib.definitions.jibx.IsReferencedBy;
import eu.europeana.corelib.definitions.jibx.IsRelatedTo;
import eu.europeana.corelib.definitions.jibx.IsReplacedBy;
import eu.europeana.corelib.definitions.jibx.IsRepresentationOf;
import eu.europeana.corelib.definitions.jibx.IsRequiredBy;
import eu.europeana.corelib.definitions.jibx.IsSimilarTo;
import eu.europeana.corelib.definitions.jibx.IsSuccessorOf;
import eu.europeana.corelib.definitions.jibx.IsVersionOf;
import eu.europeana.corelib.definitions.jibx.Issued;
import eu.europeana.corelib.definitions.jibx.Medium;
import eu.europeana.corelib.definitions.jibx.Provenance;
import eu.europeana.corelib.definitions.jibx.ProxyFor;
import eu.europeana.corelib.definitions.jibx.ProxyIn;
import eu.europeana.corelib.definitions.jibx.ProxyType;
import eu.europeana.corelib.definitions.jibx.Publisher;
import eu.europeana.corelib.definitions.jibx.Realizes;
import eu.europeana.corelib.definitions.jibx.References;
import eu.europeana.corelib.definitions.jibx.Relation;
import eu.europeana.corelib.definitions.jibx.Replaces;
import eu.europeana.corelib.definitions.jibx.Requires;
import eu.europeana.corelib.definitions.jibx.ResourceOrLiteralType;
import eu.europeana.corelib.definitions.jibx.ResourceOrLiteralType.Resource;
import eu.europeana.corelib.definitions.jibx.ResourceType;
import eu.europeana.corelib.definitions.jibx.Rights;
import eu.europeana.corelib.definitions.jibx.SameAs;
import eu.europeana.corelib.definitions.jibx.Source;
import eu.europeana.corelib.definitions.jibx.Spatial;
import eu.europeana.corelib.definitions.jibx.Subject;
import eu.europeana.corelib.definitions.jibx.TableOfContents;
import eu.europeana.corelib.definitions.jibx.Temporal;
import eu.europeana.corelib.definitions.jibx.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class ResourceLinkFromProxyTest {

  @Test
  void testGetLink() {
    testGetLink(ResourceLinkFromProxy.PROXY_FOR, ProxyFor::new, ProxyType::setProxyFor);
    testGetLink(ResourceLinkFromProxy.IS_REPRESENTATION_OF, IsRepresentationOf::new,
        ProxyType::setIsRepresentationOf);
  }

  private <T extends ResourceType> void testGetLink(ResourceLinkFromProxy resource,
      Supplier<T> constructor, BiConsumer<ProxyType, T> setter) {

    // Create objects
    final ProxyType proxy = new ProxyType();
    final T value = constructor.get();

    // Test with null list
    setter.accept(proxy, null);
    assertNoLinks(proxy, resource);
    assertNoValues(proxy, resource);

    // Test with null value and empty value
    setter.accept(proxy, value);
    value.setResource(null);
    assertNoLinks(proxy, resource);
    assertNoValues(proxy, resource);
    value.setResource(" ");
    assertNoLinks(proxy, resource);
    assertNoValues(proxy, resource);

    // Test with actual value
    final String testResource = "test resource";
    value.setResource(testResource);
    final List<String> links = resource.getLinkAndValueGetter()
        .getLinks(proxy).collect(Collectors.toList());
    assertEquals(Collections.singletonList(testResource), links);
    assertNoValues(proxy, resource);
  }

  @Test
  void testGetLinks() {
    testGetLinks(ResourceLinkFromProxy.PROXY_IN, ProxyIn::new, ProxyType::setProxyInList);
    testGetLinks(ResourceLinkFromProxy.SAME_AS, SameAs::new, ProxyType::setSameAList);
    testGetLinks(ResourceLinkFromProxy.HAS_MET, HasMet::new, ProxyType::setHasMetList);
    testGetLinks(ResourceLinkFromProxy.INCORPORATES, Incorporates::new,
        ProxyType::setIncorporateList);
    testGetLinks(ResourceLinkFromProxy.IS_DERIVATIVE_OF, IsDerivativeOf::new,
        ProxyType::setIsDerivativeOfList);
    testGetLinks(ResourceLinkFromProxy.IS_NEXT_IN_SEQUENCE, IsNextInSequence::new,
        ProxyType::setIsNextInSequenceList);
    testGetLinks(ResourceLinkFromProxy.IS_SIMILAR_TO, IsSimilarTo::new,
        ProxyType::setIsSimilarToList);
    testGetLinks(ResourceLinkFromProxy.IS_SUCCESSOR_OF, IsSuccessorOf::new,
        ProxyType::setIsSuccessorOfList);
    testGetLinks(ResourceLinkFromProxy.REALIZES, Realizes::new, ProxyType::setRealizeList);
  }

  private <T extends ResourceType> void testGetLinks(ResourceLinkFromProxy resource,
      Supplier<T> constructor, BiConsumer<ProxyType, List<T>> setter) {

    // Create objects
    final ProxyType proxy = new ProxyType();
    final T value1 = constructor.get();
    final T value2 = constructor.get();

    // Test with null list
    setter.accept(proxy, null);
    assertNoLinks(proxy, resource);
    assertNoValues(proxy, resource);

    // Test with null value and empty value
    setter.accept(proxy, Arrays.asList(value1, value2));
    value1.setResource(null);
    value2.setResource(" ");
    assertNoLinks(proxy, resource);
    assertNoValues(proxy, resource);

    // Test with actual values
    final String testResource1 = "test resource 1";
    final String testResource2 = "test resource 2";
    value1.setResource(testResource1);
    value2.setResource(testResource2);
    final List<String> linksDifferent = resource.getLinkAndValueGetter()
        .getLinks(proxy).collect(Collectors.toList());
    assertEquals(Arrays.asList(testResource1, testResource2), linksDifferent);
    assertNoValues(proxy, resource);

    // Test with the same values
    value2.setResource(testResource1);
    final List<String> linksSame = resource.getLinkAndValueGetter()
        .getLinks(proxy).collect(Collectors.toList());
    assertEquals(Arrays.asList(testResource1, testResource1), linksSame);
    assertNoValues(proxy, resource);
  }

  @Test
  void testGetLinkAndValue() {
    testGetLinkAndValue(ResourceLinkFromProxy.CURRENT_LOCATION, CurrentLocation::new,
        ProxyType::setCurrentLocation);
  }

  private <T extends ResourceOrLiteralType> void testGetLinkAndValue(ResourceLinkFromProxy resource,
      Supplier<T> constructor, BiConsumer<ProxyType, T> setter) {

    // Create objects
    final ProxyType proxy = new ProxyType();
    final T value = constructor.get();

    // Test with null list
    setter.accept(proxy, null);
    assertNoLinks(proxy, resource);
    assertNoValues(proxy, resource);

    // Test with null value and empty value
    setter.accept(proxy, value);
    value.setResource(null);
    value.setString(null);
    assertNoLinks(proxy, resource);
    assertNoValues(proxy, resource);
    value.setResource(new Resource());
    value.getResource().setResource(" ");
    value.setString(" ");
    assertNoLinks(proxy, resource);
    assertNoValues(proxy, resource);

    // Test with actual value
    final String testResource = "test resource";
    final String testLiteral = "test literal";
    value.getResource().setResource(testResource);
    value.setString(testLiteral);
    final List<String> links = resource.getLinkAndValueGetter()
        .getLinks(proxy).collect(Collectors.toList());
    final List<String> values = resource.getLinkAndValueGetter()
        .getValues(proxy).collect(Collectors.toList());
    assertEquals(Collections.singletonList(testResource), links);
    assertEquals(Collections.singletonList(testLiteral), values);
  }

  @Test
  void testGetLinksAndValues() {
    testGetLinksAndValues(ResourceLinkFromProxy.HAS_TYPE, HasType::new, ProxyType::setHasTypeList);
    testGetLinksAndValues(ResourceLinkFromProxy.IS_RELATED_TO, IsRelatedTo::new,
        ProxyType::setIsRelatedToList);
  }

  private <T extends ResourceOrLiteralType> void testGetLinksAndValues(
      ResourceLinkFromProxy resource, Supplier<T> constructor,
      BiConsumer<ProxyType, List<T>> setter) {

    // Create objects
    final ProxyType proxy = new ProxyType();
    final T value1 = constructor.get();
    final T value2 = constructor.get();

    // Test with null list
    setter.accept(proxy, null);
    assertNoLinks(proxy, resource);
    assertNoValues(proxy, resource);

    // Test with null value and empty value
    setter.accept(proxy, Arrays.asList(value1, value2));
    value1.setResource(null);
    value1.setString(null);
    value2.setResource(new Resource());
    value2.getResource().setResource(" ");
    value2.setString(" ");
    assertNoLinks(proxy, resource);
    assertNoValues(proxy, resource);

    // Test with actual value
    final String testResource1 = "test resource 1";
    final String testResource2 = "test resource 2";
    final String testLiteral1 = "test literal 1";
    final String testLiteral2 = "test literal 2";
    value1.setResource(new Resource());
    value1.getResource().setResource(testResource1);
    value2.getResource().setResource(testResource2);
    value1.setString(testLiteral1);
    value2.setString(testLiteral2);
    final List<String> linksDifferent = resource.getLinkAndValueGetter()
        .getLinks(proxy).collect(Collectors.toList());
    assertEquals(Arrays.asList(testResource1, testResource2), linksDifferent);
    final List<String> valuesDifferent = resource.getLinkAndValueGetter()
        .getValues(proxy).collect(Collectors.toList());
    assertEquals(Arrays.asList(testLiteral1, testLiteral2), valuesDifferent);

    // Test with the same values
    value2.getResource().setResource(testResource1);
    value2.setString(testLiteral1);
    final List<String> linksSame = resource.getLinkAndValueGetter()
        .getLinks(proxy).collect(Collectors.toList());
    assertEquals(Arrays.asList(testResource1, testResource1), linksSame);
    final List<String> valuesSame = resource.getLinkAndValueGetter()
        .getValues(proxy).collect(Collectors.toList());
    assertEquals(Arrays.asList(testLiteral1, testLiteral1), valuesSame);

  }

  @Test
  void testGetLinksAndValuesInChoice() {
    testGetLinksAndValuesInChoice(ResourceLinkFromProxy.CONTRIBUTOR, Contributor::new,
        Choice::setContributor);
    testGetLinksAndValuesInChoice(ResourceLinkFromProxy.COVERAGE, Coverage::new,
        Choice::setCoverage);
    testGetLinksAndValuesInChoice(ResourceLinkFromProxy.CREATOR, Creator::new, Choice::setCreator);
    testGetLinksAndValuesInChoice(ResourceLinkFromProxy.DATE, Date::new, Choice::setDate);
    testGetLinksAndValuesInChoice(ResourceLinkFromProxy.DESCRIPTION, Description::new,
        Choice::setDescription);
    testGetLinksAndValuesInChoice(ResourceLinkFromProxy.FORMAT, Format::new, Choice::setFormat);
    testGetLinksAndValuesInChoice(ResourceLinkFromProxy.PUBLISHER, Publisher::new,
        Choice::setPublisher);
    testGetLinksAndValuesInChoice(ResourceLinkFromProxy.RELATION, Relation::new,
        Choice::setRelation);
    testGetLinksAndValuesInChoice(ResourceLinkFromProxy.RIGHTS, Rights::new, Choice::setRights);
    testGetLinksAndValuesInChoice(ResourceLinkFromProxy.SOURCE, Source::new, Choice::setSource);
    testGetLinksAndValuesInChoice(ResourceLinkFromProxy.SUBJECT, Subject::new, Choice::setSubject);
    testGetLinksAndValuesInChoice(ResourceLinkFromProxy.TYPE, Type::new, Choice::setType);
    testGetLinksAndValuesInChoice(ResourceLinkFromProxy.CONFORMS_TO, ConformsTo::new,
        Choice::setConformsTo);
    testGetLinksAndValuesInChoice(ResourceLinkFromProxy.CREATED, Created::new, Choice::setCreated);
    testGetLinksAndValuesInChoice(ResourceLinkFromProxy.EXTENT, Extent::new, Choice::setExtent);
    testGetLinksAndValuesInChoice(ResourceLinkFromProxy.HAS_FORMAT, HasFormat::new,
        Choice::setHasFormat);
    testGetLinksAndValuesInChoice(ResourceLinkFromProxy.HAS_PART, HasPart::new, Choice::setHasPart);
    testGetLinksAndValuesInChoice(ResourceLinkFromProxy.HAS_VERSION, HasVersion::new,
        Choice::setHasVersion);
    testGetLinksAndValuesInChoice(ResourceLinkFromProxy.IS_FORMAT_OF, IsFormatOf::new,
        Choice::setIsFormatOf);
    testGetLinksAndValuesInChoice(ResourceLinkFromProxy.IS_PART_OF, IsPartOf::new,
        Choice::setIsPartOf);
    testGetLinksAndValuesInChoice(ResourceLinkFromProxy.IS_REFERENCED_BY, IsReferencedBy::new,
        Choice::setIsReferencedBy);
    testGetLinksAndValuesInChoice(ResourceLinkFromProxy.IS_REPLACED_BY, IsReplacedBy::new,
        Choice::setIsReplacedBy);
    testGetLinksAndValuesInChoice(ResourceLinkFromProxy.IS_REQUIRED_BY, IsRequiredBy::new,
        Choice::setIsRequiredBy);
    testGetLinksAndValuesInChoice(ResourceLinkFromProxy.ISSUED, Issued::new, Choice::setIssued);
    testGetLinksAndValuesInChoice(ResourceLinkFromProxy.IS_VERSION_OF, IsVersionOf::new,
        Choice::setIsVersionOf);
    testGetLinksAndValuesInChoice(ResourceLinkFromProxy.MEDIUM, Medium::new, Choice::setMedium);
    testGetLinksAndValuesInChoice(ResourceLinkFromProxy.PROVENANCE, Provenance::new,
        Choice::setProvenance);
    testGetLinksAndValuesInChoice(ResourceLinkFromProxy.REFERENCES, References::new,
        Choice::setReferences);
    testGetLinksAndValuesInChoice(ResourceLinkFromProxy.REPLACES, Replaces::new,
        Choice::setReplaces);
    testGetLinksAndValuesInChoice(ResourceLinkFromProxy.REQUIRES, Requires::new,
        Choice::setRequires);
    testGetLinksAndValuesInChoice(ResourceLinkFromProxy.SPATIAL, Spatial::new, Choice::setSpatial);
    testGetLinksAndValuesInChoice(ResourceLinkFromProxy.TABLE_OF_CONTENTS, TableOfContents::new,
        Choice::setTableOfContents);
    testGetLinksAndValuesInChoice(ResourceLinkFromProxy.TEMPORAL, Temporal::new,
        Choice::setTemporal);
  }

  private <T extends ResourceOrLiteralType> void testGetLinksAndValuesInChoice(
      ResourceLinkFromProxy resource, Supplier<T> constructor, BiConsumer<Choice, T> setter) {

    // Create objects
    final ProxyType proxy = new ProxyType();
    final T value1 = constructor.get();
    final T value2 = constructor.get();

    // Test with null list
    proxy.setChoiceList(null);
    assertNoLinks(proxy, resource);
    assertNoValues(proxy, resource);

    // Test with null or empty choices
    proxy.setChoiceList(Arrays.asList(null,new Choice()));
    setter.accept(proxy.getChoiceList().get(1), null);
    assertNoLinks(proxy, resource);
    assertNoValues(proxy, resource);

    // Test with null value and empty value
    proxy.setChoiceList(Arrays.asList(new Choice(), new Choice()));
    setter.accept(proxy.getChoiceList().get(0), value1);
    setter.accept(proxy.getChoiceList().get(1), value2);
    value1.setResource(null);
    value1.setString(null);
    value2.setResource(new Resource());
    value2.getResource().setResource(" ");
    value2.setString(" ");
    assertNoLinks(proxy, resource);
    assertNoValues(proxy, resource);

    // Test with actual value
    final String testResource1 = "test resource 1";
    final String testResource2 = "test resource 2";
    final String testLiteral1 = "test literal 1";
    final String testLiteral2 = "test literal 2";
    value1.setResource(new Resource());
    value1.getResource().setResource(testResource1);
    value2.getResource().setResource(testResource2);
    value1.setString(testLiteral1);
    value2.setString(testLiteral2);
    final List<String> linksDifferent = resource.getLinkAndValueGetter()
        .getLinks(proxy).collect(Collectors.toList());
    assertEquals(Arrays.asList(testResource1, testResource2), linksDifferent);
    final List<String> valuesDifferent = resource.getLinkAndValueGetter()
        .getValues(proxy).collect(Collectors.toList());
    assertEquals(Arrays.asList(testLiteral1, testLiteral2), valuesDifferent);

    // Test with the same values
    value2.getResource().setResource(testResource1);
    value2.setString(testLiteral1);
    final List<String> linksSame = resource.getLinkAndValueGetter()
        .getLinks(proxy).collect(Collectors.toList());
    assertEquals(Arrays.asList(testResource1, testResource1), linksSame);
    final List<String> valuesSame = resource.getLinkAndValueGetter()
        .getValues(proxy).collect(Collectors.toList());
    assertEquals(Arrays.asList(testLiteral1, testLiteral1), valuesSame);
  }


  private void assertNoLinks(ProxyType proxy, ResourceLinkFromProxy resource) {
    assertEquals(0, resource.getLinkAndValueGetter().getLinks(proxy).count());
  }

  private void assertNoValues(ProxyType proxy, ResourceLinkFromProxy resource) {
    assertEquals(0, resource.getLinkAndValueGetter().getValues(proxy).count());
  }
}
