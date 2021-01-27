package eu.europeana.enrichment.service;

import static eu.europeana.enrichment.service.EnrichmentObjectUtils.agentTerm1;
import static eu.europeana.enrichment.service.EnrichmentObjectUtils.customAgentTerm;
import static eu.europeana.enrichment.service.EnrichmentObjectUtils.areHashMapsWithListValuesEqual;
import static eu.europeana.enrichment.service.EnrichmentObjectUtils.areListsEqual;
import static eu.europeana.enrichment.service.EnrichmentObjectUtils.conceptTerm1;
import static eu.europeana.enrichment.service.EnrichmentObjectUtils.customConceptTerm;
import static eu.europeana.enrichment.service.EnrichmentObjectUtils.customPlaceTerm;
import static eu.europeana.enrichment.service.EnrichmentObjectUtils.customTimespanTerm;
import static eu.europeana.enrichment.service.EnrichmentObjectUtils.organizationTerm1;
import static eu.europeana.enrichment.service.EnrichmentObjectUtils.placeTerm1;
import static eu.europeana.enrichment.service.EnrichmentObjectUtils.timespanTerm1;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.enrichment.api.external.model.Agent;
import eu.europeana.enrichment.api.external.model.Concept;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.Label;
import eu.europeana.enrichment.api.external.model.LabelResource;
import eu.europeana.enrichment.api.external.model.Part;
import eu.europeana.enrichment.api.external.model.Place;
import eu.europeana.enrichment.api.external.model.Resource;
import eu.europeana.enrichment.api.external.model.Timespan;
import eu.europeana.enrichment.internal.model.AbstractEnrichmentEntity;
import eu.europeana.enrichment.internal.model.AgentEnrichmentEntity;
import eu.europeana.enrichment.internal.model.ConceptEnrichmentEntity;
import eu.europeana.enrichment.internal.model.EnrichmentTerm;
import eu.europeana.enrichment.internal.model.PlaceEnrichmentEntity;
import eu.europeana.enrichment.internal.model.TimespanEnrichmentEntity;
import eu.europeana.enrichment.utils.EntityType;
import eu.europeana.metis.exception.BadContentException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class ConverterTest {

  @Test
  void convert() throws BadContentException {
    final List<EnrichmentTerm> enrichmentTerms = List.of(customAgentTerm, customConceptTerm);
    final List<EnrichmentBase> enrichmentBases = Converter.convert(enrichmentTerms);

    final Agent agent = enrichmentBases.stream().filter(Agent.class::isInstance).findFirst()
        .map(Agent.class::cast).orElse(null);
    final Concept concept = enrichmentBases.stream().filter(Concept.class::isInstance).findFirst()
        .map(Concept.class::cast).orElse(null);

    assertConversion(customAgentTerm.getEnrichmentEntity(), agent, customAgentTerm.getEntityType());
    assertConversion(customConceptTerm.getEnrichmentEntity(), concept,
        customConceptTerm.getEntityType());
  }

  @Test
  void convertAgent() throws Exception {
    Agent agent = (Agent) Converter.convert(agentTerm1);
    assertConversion(agentTerm1.getEnrichmentEntity(), agent, agentTerm1.getEntityType());

    Agent custom_agent = (Agent) Converter.convert(customAgentTerm);
    assertConversion(customAgentTerm.getEnrichmentEntity(), custom_agent,
        customAgentTerm.getEntityType());
  }

  @Test
  void convertConcept() throws Exception {
    final Concept concept = (Concept) Converter.convert(conceptTerm1);
    assertConversion(conceptTerm1.getEnrichmentEntity(), concept, conceptTerm1.getEntityType());

    final Concept customConcept = (Concept) Converter.convert(customConceptTerm);
    assertConversion(customConceptTerm.getEnrichmentEntity(), customConcept,
        customConceptTerm.getEntityType());
  }

  @Test
  void convertTimespan() throws Exception {
    Timespan timespan = (Timespan) Converter.convert(timespanTerm1);
    assertConversion(timespanTerm1.getEnrichmentEntity(), timespan, timespanTerm1.getEntityType());

    Timespan customTimespan = (Timespan) Converter.convert(customTimespanTerm);
    assertConversion(customTimespanTerm.getEnrichmentEntity(), customTimespan,
        customTimespanTerm.getEntityType());
  }

  @Test
  void convertPlace() throws Exception {
    final Place place = (Place) Converter.convert(placeTerm1);
    assertConversion(placeTerm1.getEnrichmentEntity(), place, placeTerm1.getEntityType());

    final Place customPlace = (Place) Converter.convert(customPlaceTerm);
    assertConversion(customPlaceTerm.getEnrichmentEntity(), customPlace,
        customPlaceTerm.getEntityType());
  }

  @Test
  void convertOtherObject_returns_null() {
    final EnrichmentBase organization = new EnrichmentBase() {
    };
    assertThrows(BadContentException.class,
        () -> assertConversion(organizationTerm1.getEnrichmentEntity(), organization,
            organizationTerm1.getEntityType()));
  }

  @Test
  void convert_EnrichmentTermWithInvalidType() {
    final EnrichmentTerm enrichmentTerm = new EnrichmentTerm();
    enrichmentTerm.setEntityType(null);
    assertNull(Converter.convert(enrichmentTerm));
  }

  @Test
  void convert_EnrichmentTermNotSupportedType() {
    final EnrichmentTerm enrichmentTerm = new EnrichmentTerm();
    enrichmentTerm.setEntityType(EntityType.ORGANIZATION);
    assertNull(Converter.convert(enrichmentTerm));
  }

  void assertConversion(AbstractEnrichmentEntity expected, EnrichmentBase actual,
      EntityType entityType) throws BadContentException {

    switch (entityType) {
      case CONCEPT:
        assertConcept((ConceptEnrichmentEntity) expected, (Concept) actual);
        break;
      case TIMESPAN:
        assertTimespan((TimespanEnrichmentEntity) expected, (Timespan) actual);
        break;
      case AGENT:
        assertAgent((AgentEnrichmentEntity) expected, (Agent) actual);
        break;
      case PLACE:
        assertPlace((PlaceEnrichmentEntity) expected, (Place) actual);
        break;
      case ORGANIZATION:
        //Organization not supported for enrichment
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
    assertLabels(expected.getEdmHasMet(), actual.getHasMet());
    assertLabels(expected.getRdaGr2BiographicalInformation(), actual.getBiographicaInformation());
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

  private void assertTimespan(TimespanEnrichmentEntity expected, Timespan actual) {
    assertEquals(expected.getIsPartOf(),
        Optional.ofNullable(actual.getIsPartOf()).map(Part::getResource).orElse(null));
    assertParts(expected.getDctermsHasPart(), actual.getHasPartsList());
    final List<String> actualOwlSameAs = actual.getSameAs() == null ? null
        : actual.getSameAs().stream().map(Part::getResource).collect(Collectors.toList());
    assertTrue(areListsEqual(expected.getOwlSameAs(), actualOwlSameAs));
    assertLabels(expected.getBegin(), actual.getBeginList());
    assertLabels(expected.getEnd(), actual.getEndList());
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
        Optional.ofNullable(actual.getIsPartOf()).map(Part::getResource).orElse(null));
    assertParts(expected.getDcTermsHasPart(), actual.getHasPartsList());
    assertTrue(areListsEqual(expected.getOwlSameAs(),
        actual.getSameAs().stream().map(Part::getResource).collect(Collectors.toList())));
    assertEquals(Optional.ofNullable(expected.getLatitude()).map(Object::toString).orElse(null),
        actual.getLat());
    assertEquals(Optional.ofNullable(expected.getLongitude()).map(Object::toString).orElse(null),
        actual.getLon());
    assertEquals(Optional.ofNullable(expected.getAltitude()).map(Object::toString).orElse(null),
        actual.getAlt());
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

  void assertParts(Map<String, List<String>> expected, List<Part> actual) {
    final Map<String, List<String>> partsMap = new HashMap<>();
    partsMap.put("def", actual.stream().map(Part::getResource).collect(Collectors.toList()));
    areHashMapsWithListValuesEqual(expected, partsMap);
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