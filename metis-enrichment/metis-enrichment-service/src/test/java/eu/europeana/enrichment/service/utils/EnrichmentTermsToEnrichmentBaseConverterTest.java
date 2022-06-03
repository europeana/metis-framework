package eu.europeana.enrichment.service.utils;

import static eu.europeana.enrichment.service.EnrichmentObjectUtils.areHashMapsWithListValuesEqual;
import static eu.europeana.enrichment.service.EnrichmentObjectUtils.areListsEqual;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.enrichment.api.external.model.Agent;
import eu.europeana.enrichment.api.external.model.Concept;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.Label;
import eu.europeana.enrichment.api.external.model.LabelResource;
import eu.europeana.enrichment.api.external.model.Organization;
import eu.europeana.enrichment.api.external.model.Part;
import eu.europeana.enrichment.api.external.model.Place;
import eu.europeana.enrichment.api.external.model.Resource;
import eu.europeana.enrichment.api.external.model.TimeSpan;
import eu.europeana.enrichment.api.external.model.VcardAddress;
import eu.europeana.enrichment.api.external.model.VcardAddresses;
import eu.europeana.enrichment.api.external.model.WebResource;
import eu.europeana.enrichment.internal.model.AbstractEnrichmentEntity;
import eu.europeana.enrichment.internal.model.Address;
import eu.europeana.enrichment.internal.model.AgentEnrichmentEntity;
import eu.europeana.enrichment.internal.model.ConceptEnrichmentEntity;
import eu.europeana.enrichment.internal.model.EnrichmentTerm;
import eu.europeana.enrichment.internal.model.OrganizationEnrichmentEntity;
import eu.europeana.enrichment.internal.model.PlaceEnrichmentEntity;
import eu.europeana.enrichment.internal.model.TimespanEnrichmentEntity;
import eu.europeana.enrichment.service.EnrichmentObjectUtils;
import eu.europeana.enrichment.utils.EntityType;
import eu.europeana.metis.exception.BadContentException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class EnrichmentTermsToEnrichmentBaseConverterTest {

  private static EnrichmentObjectUtils enrichmentObjectUtils;

  @BeforeAll
  static void prepare() {
    enrichmentObjectUtils = new EnrichmentObjectUtils();
  }

  @Test
  void convert() throws BadContentException {
    final List<EnrichmentTerm> enrichmentTerms = List
        .of(enrichmentObjectUtils.customAgentTerm, enrichmentObjectUtils.customConceptTerm);
    final List<EnrichmentBase> enrichmentBases = EnrichmentTermsToEnrichmentBaseConverter.convert(enrichmentTerms);

    final Agent agent = enrichmentBases.stream().filter(Agent.class::isInstance).findFirst()
        .map(Agent.class::cast).orElse(null);
    final Concept concept = enrichmentBases.stream().filter(Concept.class::isInstance).findFirst()
        .map(Concept.class::cast).orElse(null);

    assertConversion(enrichmentObjectUtils.customAgentTerm.getEnrichmentEntity(), agent,
        enrichmentObjectUtils.customAgentTerm.getEntityType());
    assertConversion(enrichmentObjectUtils.customConceptTerm.getEnrichmentEntity(), concept,
        enrichmentObjectUtils.customConceptTerm.getEntityType());
  }

  @Test
  void convertAgent() throws Exception {
    Agent agent = (Agent) EnrichmentTermsToEnrichmentBaseConverter.convert(enrichmentObjectUtils.agentTerm1);
    assertConversion(enrichmentObjectUtils.agentTerm1.getEnrichmentEntity(), agent,
        enrichmentObjectUtils.agentTerm1.getEntityType());

    Agent custom_agent = (Agent) EnrichmentTermsToEnrichmentBaseConverter.convert(enrichmentObjectUtils.customAgentTerm);
    assertConversion(enrichmentObjectUtils.customAgentTerm.getEnrichmentEntity(), custom_agent,
        enrichmentObjectUtils.customAgentTerm.getEntityType());
  }

  @Test
  void convertConcept() throws Exception {
    final Concept concept = (Concept) EnrichmentTermsToEnrichmentBaseConverter.convert(enrichmentObjectUtils.conceptTerm1);
    assertConversion(enrichmentObjectUtils.conceptTerm1.getEnrichmentEntity(), concept,
        enrichmentObjectUtils.conceptTerm1.getEntityType());

    final Concept customConcept = (Concept) EnrichmentTermsToEnrichmentBaseConverter
        .convert(enrichmentObjectUtils.customConceptTerm);
    assertConversion(enrichmentObjectUtils.customConceptTerm.getEnrichmentEntity(), customConcept,
        enrichmentObjectUtils.customConceptTerm.getEntityType());
  }

  @Test
  void convertTimespan() throws Exception {
    TimeSpan timespan = (TimeSpan) EnrichmentTermsToEnrichmentBaseConverter.convert(enrichmentObjectUtils.timespanTerm1);
    assertConversion(enrichmentObjectUtils.timespanTerm1.getEnrichmentEntity(), timespan,
        enrichmentObjectUtils.timespanTerm1.getEntityType());

    TimeSpan customTimespan = (TimeSpan) EnrichmentTermsToEnrichmentBaseConverter
        .convert(enrichmentObjectUtils.customTimespanTerm);
    assertConversion(enrichmentObjectUtils.customTimespanTerm.getEnrichmentEntity(), customTimespan,
        enrichmentObjectUtils.customTimespanTerm.getEntityType());
  }

  @Test
  void convertPlace() throws Exception {
    final Place place = (Place) EnrichmentTermsToEnrichmentBaseConverter.convert(enrichmentObjectUtils.placeTerm1);
    assertConversion(enrichmentObjectUtils.placeTerm1.getEnrichmentEntity(), place,
        enrichmentObjectUtils.placeTerm1.getEntityType());

    final Place customPlace = (Place) EnrichmentTermsToEnrichmentBaseConverter.convert(enrichmentObjectUtils.customPlaceTerm);
    assertConversion(enrichmentObjectUtils.customPlaceTerm.getEnrichmentEntity(), customPlace,
        enrichmentObjectUtils.customPlaceTerm.getEntityType());
  }

  @Test
  void convertOrganization() throws Exception {
    final Organization organization = (Organization) EnrichmentTermsToEnrichmentBaseConverter
        .convert(enrichmentObjectUtils.organizationTerm1);
    assertConversion(enrichmentObjectUtils.organizationTerm1.getEnrichmentEntity(), organization,
        enrichmentObjectUtils.organizationTerm1.getEntityType());

    final Organization customOrganization = (Organization) EnrichmentTermsToEnrichmentBaseConverter
        .convert(enrichmentObjectUtils.customOrganizationTerm);
    assertConversion(enrichmentObjectUtils.customOrganizationTerm.getEnrichmentEntity(),
        customOrganization, enrichmentObjectUtils.customOrganizationTerm.getEntityType());
  }

  @Test
  void convert_EnrichmentTermWithInvalidType() {
    final EnrichmentTerm enrichmentTerm = new EnrichmentTerm();
    enrichmentTerm.setEntityType(null);
    assertNull(EnrichmentTermsToEnrichmentBaseConverter.convert(enrichmentTerm));
  }

  void assertConversion(AbstractEnrichmentEntity expected, EnrichmentBase actual,
      EntityType entityType) throws BadContentException {

    switch (entityType) {
      case CONCEPT:
        assertConcept((ConceptEnrichmentEntity) expected, (Concept) actual);
        break;
      case TIMESPAN:
        assertTimespan((TimespanEnrichmentEntity) expected, (TimeSpan) actual);
        break;
      case AGENT:
        assertAgent((AgentEnrichmentEntity) expected, (Agent) actual);
        break;
      case PLACE:
        assertPlace((PlaceEnrichmentEntity) expected, (Place) actual);
        break;
      case ORGANIZATION:
        assertOrganization((OrganizationEnrichmentEntity) expected, (Organization) actual);
        break;
      default:
        throw new BadContentException("Invalid entity type value: " + entityType);
    }
    assertAbstractEnrichmentBase(expected, actual);
  }

  private void assertAgent(AgentEnrichmentEntity expected, Agent actual) {
    assertLabels(expected.getHiddenLabel(), actual.getHiddenLabel());
    assertLabels(expected.getFoafName(), actual.getFoafName());
    assertLabels(expected.getBegin(), actual.getBeginList());
    assertLabels(expected.getEnd(), actual.getEndList());
    assertLabels(expected.getDcIdentifier(), actual.getIdentifier());
    assertResources(expected.getEdmHasMet(), actual.getHasMet());
    assertLabelResources(expected.getRdaGr2BiographicalInformation(), actual.getBiographicalInformation());
    assertLabelResources(expected.getRdaGr2PlaceOfBirth(), actual.getPlaceOfBirth());
    assertLabelResources(expected.getRdaGr2PlaceOfDeath(), actual.getPlaceOfDeath());
    assertLabels(expected.getRdaGr2DateOfBirth(), actual.getDateOfBirth());
    assertLabels(expected.getRdaGr2DateOfDeath(), actual.getDateOfDeath());
    assertLabels(expected.getRdaGr2DateOfEstablishment(), actual.getDateOfEstablishment());
    assertLabels(expected.getRdaGr2DateOfTermination(), actual.getDateOfTermination());
    assertLabels(expected.getRdaGr2Gender(), actual.getGender());
    assertLabelResources(expected.getRdaGr2ProfessionOrOccupation(),
        actual.getProfessionOrOccupation());
    assertLabelResources(expected.getDcDate(), actual.getDate());
    assertLabelResources(expected.getRdaGr2PlaceOfBirth(), actual.getPlaceOfBirth());
    assertLabelResources(expected.getEdmIsRelatedTo(), actual.getIsRelatedTo());

    assertResources(expected.getEdmWasPresentAt(), actual.getWasPresentAt());
    assertTrue(areListsEqual(expected.getOwlSameAs(),
        actual.getSameAs().stream().map(Part::getResource).collect(Collectors.toList())));
  }

  private void assertTimespan(TimespanEnrichmentEntity expected, TimeSpan actual) {
    assertEquals(expected.getIsPartOf(),
        Optional.ofNullable(actual.getIsPartOf()).stream().flatMap(Collection::stream)
                .map(LabelResource::getResource).findFirst().orElse(null));
    assertLabelResources(expected.getDctermsHasPart(), actual.getHasPartsList());
    final List<String> actualOwlSameAs = actual.getSameAs() == null ? null
        : actual.getSameAs().stream().map(Part::getResource).collect(Collectors.toList());
    assertTrue(areListsEqual(expected.getOwlSameAs(), actualOwlSameAs));
    assertLabels(expected.getBegin(), List.of(actual.getBegin()));
    assertLabels(expected.getEnd(), List.of(actual.getEnd()));
    assertLabels(expected.getHiddenLabel(), actual.getHiddenLabel());
    assertEquals(expected.getIsNextInSequence(),
        Optional.ofNullable(actual.getIsNextInSequence()).map(Part::getResource).orElse(null));
  }

  private void assertConcept(ConceptEnrichmentEntity expected, Concept actual) {
    assertLabels(expected.getHiddenLabel(), actual.getHiddenLabel());
    assertLabels(expected.getNotation(), actual.getNotation());
    assertResources(expected.getBroader(), actual.getBroader());
    assertResources(expected.getBroadMatch(), actual.getBroadMatch());
    assertResources(expected.getCloseMatch(), actual.getCloseMatch());
    assertResources(expected.getExactMatch(), actual.getExactMatch());
    assertResources(expected.getInScheme(), actual.getInScheme());
    assertResources(expected.getNarrower(), actual.getNarrower());
    assertResources(expected.getNarrowMatch(), actual.getNarrowMatch());
    assertResources(expected.getRelated(), actual.getRelated());
    assertResources(expected.getRelatedMatch(), actual.getRelatedMatch());
  }

  private void assertPlace(PlaceEnrichmentEntity expected, Place actual) {
    assertEquals(expected.getIsPartOf(),
        Optional.ofNullable(actual.getIsPartOf()).stream().flatMap(Collection::stream)
                .map(LabelResource::getResource).findFirst().orElse(null));
    assertLabelResources(expected.getDcTermsHasPart(), actual.getHasPartsList());
    assertTrue(areListsEqual(expected.getOwlSameAs(),
        actual.getSameAs().stream().map(Part::getResource).collect(Collectors.toList())));
    assertEquals(Optional.ofNullable(expected.getLatitude()).map(Object::toString).orElse(null),
        actual.getLat());
    assertEquals(Optional.ofNullable(expected.getLongitude()).map(Object::toString).orElse(null),
        actual.getLon());
    assertEquals(Optional.ofNullable(expected.getAltitude()).map(Object::toString).orElse(null),
        actual.getAlt());
  }

  private void assertOrganization(OrganizationEnrichmentEntity expected, Organization actual) {
    assertTrue(areListsEqual(expected.getOwlSameAs(),
        actual.getSameAs().stream().map(WebResource::getResourceUri).collect(Collectors.toList())));
    assertEquals(expected.getEdmCountry().entrySet().iterator().next().getValue(),
        actual.getCountry());

    assertEquals(
        Optional.ofNullable(expected.getFoafPhone()).stream().flatMap(List::stream).findFirst()
            .orElse(null), actual.getPhone());
    assertEquals(
        Optional.ofNullable(expected.getFoafMbox()).stream().flatMap(List::stream).findFirst()
            .orElse(null), actual.getMbox());
    assertEquals(expected.getFoafHomepage(),
        Optional.ofNullable(actual.getHomepage()).map(Resource::getResource).orElse(null));
    assertEquals(expected.getFoafDepiction(),
        Optional.ofNullable(actual.getDepiction()).map(Resource::getResource).orElse(null));
    assertLabels(expected.getEdmAcronym(), actual.getAcronyms());
    assertLabelsMap(expected.getDcDescription(), actual.getDescriptions());

    final Address expectedAddress = expected.getAddress();
    final VcardAddresses actualAddresses = actual.getHasAddress();
    if (expectedAddress == null) {
      assertTrue(actualAddresses == null || CollectionUtils
          .isEmpty(actualAddresses.getVcardAddressesList()));
    } else {
      final List<VcardAddress> vcardAddressesList = actualAddresses.getVcardAddressesList();
      assertEquals(1, vcardAddressesList.size());
      final VcardAddress actualAddress = vcardAddressesList.get(0);
      assertEquals(expectedAddress.getVcardCountryName(), actualAddress.getCountryName());
      assertEquals(expectedAddress.getVcardLocality(), actualAddress.getLocality());
      assertEquals(expectedAddress.getVcardPostalCode(), actualAddress.getPostalCode());
      assertEquals(expectedAddress.getVcardPostOfficeBox(), actualAddress.getPostOfficeBox());
      assertEquals(expectedAddress.getVcardStreetAddress(), actualAddress.getStreetAddress());
      assertEquals(expectedAddress.getVcardHasGeo(),
          Optional.ofNullable(actualAddress.getHasGeo()).map(Resource::getResource).orElse(null));
    }
  }

  void assertAbstractEnrichmentBase(AbstractEnrichmentEntity expected, EnrichmentBase actual) {
    assertEquals(expected.getAbout(), actual.getAbout());
    assertLabels(expected.getAltLabel(), actual.getAltLabelList());
    assertLabels(expected.getPrefLabel(), actual.getPrefLabelList());
    assertLabels(expected.getNote(), actual.getNotes());
  }

  void assertLabels(Map<String, List<String>> expected, List<Label> actual) {
    final Map<String, List<String>> labelsMap = actual.stream().collect(Collectors
        .groupingBy(Label::getLang, Collectors.mapping(Label::getValue, Collectors.toList())));
    areHashMapsWithListValuesEqual(expected, labelsMap);
  }

  void assertLabelsMap(Map<String, String> expected, List<Label> actual) {
    actual.forEach(label -> assertEquals(expected.get(label.getLang()), label.getValue()));
  }

  void assertLabelResources(Map<String, List<String>> expected, List<LabelResource> actual) {
    final Map<String, List<String>> labelResourceMap = actual.stream()
        .filter(labelResource -> labelResource.getLang() != null).collect(Collectors
            .groupingBy(LabelResource::getLang,
                Collectors.mapping(LabelResource::getValue, Collectors.toList())));

    actual.stream().filter(labelResource -> labelResource.getLang() == null).forEach(
        labelResource -> labelResourceMap.put(labelResource.getResource(), new ArrayList<>()));
    areHashMapsWithListValuesEqual(expected, labelResourceMap);
  }

  void assertResources(String[] expected, List<Resource> actual) {
    assertEquals(Arrays.asList(Optional.ofNullable(expected).orElse(new String[]{})),
        Optional.ofNullable(actual).orElseGet(Collections::emptyList).stream()
            .map(Resource::getResource).collect(Collectors.toList()));
  }

}