package eu.europeana.enrichment.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.enrichment.api.external.model.Agent;
import eu.europeana.enrichment.api.external.model.Concept;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentResultBaseWrapper;
import eu.europeana.enrichment.api.external.model.Label;
import eu.europeana.enrichment.api.external.model.LabelResource;
import eu.europeana.enrichment.api.external.model.Organization;
import eu.europeana.enrichment.api.external.model.Part;
import eu.europeana.enrichment.api.external.model.Place;
import eu.europeana.enrichment.api.external.model.Resource;
import eu.europeana.enrichment.api.external.model.TimeSpan;
import eu.europeana.metis.schema.convert.RdfConversionUtils;
import eu.europeana.metis.schema.convert.SerializationException;
import eu.europeana.metis.schema.jibx.AgentType;
import eu.europeana.metis.schema.jibx.Alt;
import eu.europeana.metis.schema.jibx.Concept.Choice;
import eu.europeana.metis.schema.jibx.Lat;
import eu.europeana.metis.schema.jibx.LiteralType;
import eu.europeana.metis.schema.jibx.PlaceType;
import eu.europeana.metis.schema.jibx.RDF;
import eu.europeana.metis.schema.jibx.ResourceOrLiteralType;
import eu.europeana.metis.schema.jibx.ResourceType;
import eu.europeana.metis.schema.jibx.TimeSpanType;
import eu.europeana.metis.schema.jibx._Long;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.util.CollectionUtils;

public class EntityMergeEngineTest {

  private static Place createPlace() {
    Place place = new Place();

    place.setAbout("aboutP0");
    place.setAlt("123.456");

    Label label1 = new Label("labelP1");
    Label label2 = new Label("langP2", "labelP2");
    place.setAltLabelList(List.of(label1, label2));

    LabelResource part1 = new LabelResource("partP1");
    place.setHasPartsList(List.of(part1));

    LabelResource part2 = new LabelResource("partP2");
    place.setIsPartOf(List.of(part2));

    place.setLat("12.34");
    place.setLon("43.21");

    Label label3 = new Label("labelP3");
    place.setNotes(List.of(label3));

    Label label4 = new Label("langP4", "labelP4");
    place.setPrefLabelList(List.of(label4));

    Part part3 = new Part("partP3");
    Part part4 = new Part("partP4");
    place.setSameAs(List.of(part3, part4));

    return place;
  }

  private static Place createFirstPlaceWithNullValues() {
    Place place = new Place();
    place.setAbout("aboutP1");
    place.setAltLabelList(Arrays.asList(new Label[]{null}));
    place.setHasPartsList(Collections.emptyList());
    place.setIsPartOf(null);
    place.setNotes(List.of(new Label()));
    place.setPrefLabelList(null);
    return place;
  }

  private static Place createSecondPlaceWithNullValues() {
    Place place = new Place();
    place.setAbout("aboutP2");
    place.setHasPartsList(List.of(new LabelResource()));
    place.setIsPartOf(List.of(new LabelResource("")));
    return place;
  }

  private static Agent createAgent() {
    Agent agent = new Agent();

    agent.setAbout("aboutA1");

    Label label1 = new Label("LangA1", "labelA1");
    agent.setAltLabelList(List.of(label1));

    Label label2 = new Label("labelA2");
    agent.setBeginList(List.of(label2));

    LabelResource label3 = new LabelResource("labelA3");
    LabelResource label4 = new LabelResource("labelA4");
    agent.setBiographicalInformation(List.of(label3, label4));

    LabelResource labelResource1 = new LabelResource("LangA1", "labelResourceA1");
    labelResource1.setResource("resource1");
    agent.setDate(List.of(labelResource1));

    LabelResource labelResource5 = new LabelResource("labelResourceA5");
    labelResource5.setResource("resource5");
    agent.setPlaceOfBirth(List.of(labelResource5));

    LabelResource labelResource6 = new LabelResource("labelResourceA6");
    labelResource6.setResource("resource6");
    agent.setPlaceOfDeath(List.of(labelResource6));

    Label label5 = new Label("LangA5", "labelA5");
    agent.setDateOfBirth(List.of(label5));

    Label label6 = new Label("labelA6");
    Label label19 = new Label("labelA19");
    agent.setDateOfDeath(Arrays.asList(null, label6, label19));

    Label label7 = new Label("labelA7");
    agent.setDateOfEstablishment(Arrays.asList(null, label7));

    Label label8 = new Label("labelA8");
    agent.setDateOfTermination(List.of(label8));

    Label label9 = new Label("labelA9");
    agent.setEndList(List.of(label9));

    Label label18 = new Label("labelA18");
    agent.setFoafName(List.of(label18));

    Label label10 = new Label("labelA10");
    agent.setGender(List.of(label10));

    Resource label11 = new Resource("labelA11");
    agent.setHasMet(List.of(label11));

    Label label12 = new Label("LangA12", "labelA12");
    Label label13 = new Label("LangA13", "labelA13");
    agent.setHiddenLabel(List.of(label12, label13));

    Label label14 = new Label("labelA14");
    agent.setIdentifier(List.of(label14));

    LabelResource labelResource2 = new LabelResource("labelResourceA2");
    labelResource1.setResource("resource2");
    LabelResource labelResource3 = new LabelResource("resource3");
    agent.setIsRelatedTo(List.of(labelResource2, labelResource3));

    Label label15 = new Label("labelA15");
    Label label20 = new Label("labelA20");
    agent.setNotes(List.of(label15, label20));

    Label label16 = new Label("LangA16", "labelA16");
    Label label17 = new Label("labelA17");
    agent.setPrefLabelList(List.of(label16, label17));

    LabelResource labelResource4 = new LabelResource();
    agent.setProfessionOrOccupation(List.of(labelResource4));

    Part part1 = new Part("partP1");
    Part part2 = new Part();
    agent.setSameAs(List.of(part1, part2));

    Resource resource1 = new Resource("resource1");
    Resource resource2 = new Resource();
    agent.setWasPresentAt(List.of(resource1, resource2));

    return agent;
  }

  private static Organization createOrganization() {
    Organization organization = new Organization();

    organization.setAbout("aboutO1");
    Label label1 = new Label("LangA1", "labelA1");
    Label label2 = new Label("labelA2");
    organization.setPrefLabelList(Arrays.asList(label1, label2));

    return organization;
  }

  private static Agent createAgentWithNullValues() {
    Agent agent = new Agent();
    agent.setAbout("aboutA2");
    agent.setDate(List.of(new LabelResource()));
    agent.setDateOfBirth(Arrays.asList(null, new Label()));
    agent.setDateOfDeath(Arrays.asList(new Label(), null));
    agent.setDateOfEstablishment(Arrays.asList(new Label[]{null}));
    agent.setDateOfTermination(null);
    return agent;
  }

  private static Concept createConcept() {
    Concept concept = new Concept();

    concept.setAbout("aboutC1");

    Label label1 = new Label("LangC1", "labelC1");
    Label label2 = new Label("labelC2");
    concept.setAltLabelList(List.of(label1, label2));

    Resource resouce1 = new Resource("resourceC1");
    Resource resouce2 = new Resource("resourceC2");
    concept.setBroader(List.of(resouce1, resouce2));

    Resource resouce3 = new Resource("resourceC3");
    Resource resouce4 = new Resource("resourceC4");
    concept.setBroadMatch(List.of(resouce3, resouce4));

    Resource resouce5 = new Resource("resourceC5");
    Resource resouce6 = new Resource("resourceC6");
    concept.setCloseMatch(List.of(resouce5, resouce6));

    Resource resouce7 = new Resource("resourceC7");
    Resource resouce8 = new Resource("resourceC8");
    concept.setExactMatch(List.of(resouce7, resouce8));

    Label label3 = new Label("labelC3");
    Label label4 = new Label("labelC4");
    concept.setHiddenLabel(Arrays.asList(null, label3, label4));

    Resource resouce9 = new Resource("resourceC9");
    Resource resouce10 = new Resource("resourceC10");
    concept.setInScheme(List.of(resouce9, resouce10));

    Resource resouce11 = new Resource("resourceC11");
    Resource resouce21 = new Resource("resourceC21");
    concept.setNarrower(List.of(resouce11, resouce21));

    Resource resouce12 = new Resource("resourceC12");
    Resource resouce22 = new Resource("resourceC22");
    concept.setNarrowMatch(List.of(resouce12, resouce22));

    Label label7 = new Label("labelC7");
    Label label8 = new Label("labelC8");
    concept.setNotation(List.of(label7, label8));

    Label label5 = new Label("labelC5");
    concept.setNotes(List.of(label5));

    Label label6 = new Label("labelC6");
    concept.setPrefLabelList(List.of(label6));

    Resource resouce14 = new Resource("resourceC14");
    Resource resouce24 = new Resource("resourceC24");
    concept.setRelated(Arrays.asList(resouce14, resouce24, null));

    Resource resouce15 = new Resource("resourceC15");
    Resource resouce25 = new Resource("resourceC25");
    concept.setRelatedMatch(List.of(resouce15, resouce25));

    return concept;
  }

  private static Concept createConceptWithNullValues() {
    Concept concept = new Concept();
    concept.setAbout("aboutC2");
    concept.setBroader(List.of(new Resource()));
    concept.setBroadMatch(Arrays.asList(null, new Resource()));
    concept.setNarrower(Arrays.asList(new Resource(), null));
    concept.setNarrowMatch(Arrays.asList(new Resource[]{null}));
    concept.setRelated(List.of(new Resource[]{}));
    concept.setRelatedMatch(null);
    return concept;
  }

  private static TimeSpan createTimeSpan() {
    TimeSpan timespan = new TimeSpan();

    timespan.setAbout("aboutT1");

    Label label1 = new Label("labelT1");
    Label label2 = new Label("langT2", "labelT2");
    timespan.setAltLabelList(List.of(label1, label2));

    Label label5 = new Label("labelT5");
    timespan.setBegin(label5);

    Label label6 = new Label("labelT6");
    timespan.setEnd(label6);

    LabelResource part1 = new LabelResource("partT1");
    timespan.setHasPartsList(List.of(part1));

    LabelResource part2 = new LabelResource("partT2");
    timespan.setIsPartOf(List.of(part2));

    Label label7 = new Label("LangT7", "labelT7");
    Label label8 = new Label("LangT8", "labelT8");
    timespan.setHiddenLabel(List.of(label7, label8));

    Label label3 = new Label("labelT3");
    timespan.setNotes(List.of(label3));

    Label label4 = new Label("langT4", "labelT4");
    timespan.setPrefLabelList(List.of(label4));

    Part part3 = new Part("partT3");
    Part part4 = new Part("partT4");
    timespan.setSameAs(List.of(part3, part4));

    return timespan;
  }

  private void verifyPlace(Place original, PlaceType copy) {
    assertNotNull(copy);
    verifyString(original.getAbout(), copy.getAbout(), true);
    verifyFloat(original.getAlt(), copy.getAlt(), Alt::getAlt);
    verifyList(original.getAltLabelList(), copy.getAltLabelList(), this::verifyLabel);
    verifyList(original.getHasPartsList(), copy.getHasPartList(), this::verifyLabelResource);
    if (original.getIsPartOf() != null) {
      verifyList(original.getIsPartOf(), copy.getIsPartOfList(), this::verifyLabelResource);
    }
    verifyFloat(original.getLat(), copy.getLat(), Lat::getLat);
    verifyFloat(original.getLon(), copy.getLong(), _Long::getLong);
    verifyList(original.getNotes(), copy.getNoteList(), this::verifyLabel);
    verifyList(original.getPrefLabelList(), copy.getPrefLabelList(), this::verifyLabel);
    verifyList(original.getSameAs(), copy.getSameAList(), this::verifyPart);
    assertNull(copy.getIsNextInSequence());
  }

  private void verifyAgent(Agent original, AgentType copy) {
    assertNotNull(copy);
    verifyString(original.getAbout(), copy.getAbout(), true);
    verifyList(original.getAltLabelList(), copy.getAltLabelList(), this::verifyLabel);
    verifyFirstListItem(original.getBeginList(), copy.getBegin(), this::verifyLabel);
    verifyList(original.getBiographicalInformation(), copy.getBiographicalInformationList(),
        this::verifyLabelResource);
    verifyList(original.getDate(), copy.getDateList(), this::verifyLabelResource);
    verifyList(original.getPlaceOfBirth(), copy.getPlaceOfBirthList(), this::verifyLabelResource);
    verifyList(original.getPlaceOfDeath(), copy.getPlaceOfDeathList(), this::verifyLabelResource);
    verifyFirstListItem(original.getDateOfBirth(), copy.getDateOfBirth(), this::verifyLabel);
    verifyFirstListItem(original.getDateOfDeath(), copy.getDateOfDeath(), this::verifyLabel);
    verifyFirstListItem(original.getDateOfEstablishment(), copy.getDateOfEstablishment(),
        this::verifyLabel);
    verifyFirstListItem(original.getDateOfTermination(), copy.getDateOfTermination(),
        this::verifyLabel);
    verifyFirstListItem(original.getEndList(), copy.getEnd(), this::verifyLabel);
    verifyFirstListItem(original.getGender(), copy.getGender(), this::verifyLabel);
    verifyList(original.getHasMet(), copy.getHasMetList(), this::verifyResource);
    verifyList(original.getIdentifier(), copy.getIdentifierList(), this::verifyLabel);
    verifyList(original.getIsRelatedTo(), copy.getIsRelatedToList(), this::verifyLabelResource);
    verifyList(original.getNotes(), copy.getNoteList(), this::verifyLabel);
    verifyList(original.getPrefLabelList(), copy.getPrefLabelList(), this::verifyLabel);
    verifyList(original.getProfessionOrOccupation(), copy.getProfessionOrOccupationList(),
        this::verifyLabelResource);
    verifyList(original.getSameAs(), copy.getSameAList(), this::verifyPart);
    assertTrue(copy.getHasPartList().isEmpty());
    assertTrue(copy.getIsPartOfList().isEmpty());
    assertTrue(copy.getNameList().isEmpty());
  }

  private void verifyOrganization(Organization original, eu.europeana.metis.schema.jibx.Organization copy) {
    assertNotNull(copy);
    verifyString(original.getAbout(), copy.getAbout(), true);
    verifyList(original.getPrefLabelList(), copy.getPrefLabelList(), this::verifyLabel);
    verifyList(original.getAcronyms(), copy.getAcronymList(), this::verifyLabel);
    assertNull(copy.getHomepage());
    assertNull(copy.getLogo());

  }

  private void verifyConcept(Concept original, eu.europeana.metis.schema.jibx.Concept copy) {
    assertNotNull(copy);
    verifyString(original.getAbout(), copy.getAbout(), true);
    int choicesCount = 0;
    choicesCount += verifyChoiceList(original.getAltLabelList(), copy, Choice::ifAltLabel,
        Choice::getAltLabel, this::verifyLabel);
    choicesCount += verifyChoiceList(original.getBroader(), copy, Choice::ifBroader,
        Choice::getBroader, this::verifyResource);
    choicesCount += verifyChoiceList(original.getBroadMatch(), copy, Choice::ifBroadMatch,
        Choice::getBroadMatch, this::verifyResource);
    choicesCount += verifyChoiceList(original.getCloseMatch(), copy, Choice::ifCloseMatch,
        Choice::getCloseMatch, this::verifyResource);
    choicesCount += verifyChoiceList(original.getExactMatch(), copy, Choice::ifExactMatch,
        Choice::getExactMatch, this::verifyResource);
    choicesCount += verifyChoiceList(original.getInScheme(), copy, Choice::ifInScheme,
        Choice::getInScheme, this::verifyResource);
    choicesCount += verifyChoiceList(original.getNarrower(), copy, Choice::ifNarrower,
        Choice::getNarrower, this::verifyResource);
    choicesCount += verifyChoiceList(original.getNarrowMatch(), copy, Choice::ifNarrowMatch,
        Choice::getNarrowMatch, this::verifyResource);
    choicesCount += verifyChoiceList(original.getNotation(), copy, Choice::ifNotation,
        Choice::getNotation, this::verifyLabel);
    choicesCount += verifyChoiceList(original.getNotes(), copy, Choice::ifNote, Choice::getNote,
        this::verifyLabel);
    choicesCount += verifyChoiceList(original.getPrefLabelList(), copy, Choice::ifPrefLabel,
        Choice::getPrefLabel, this::verifyLabel);
    choicesCount += verifyChoiceList(original.getRelated(), copy, Choice::ifRelated,
        Choice::getRelated, this::verifyResource);
    choicesCount += verifyChoiceList(original.getRelatedMatch(), copy, Choice::ifRelatedMatch,
        Choice::getRelatedMatch, this::verifyResource);
    // Checks for choices with unsupported types set.
    assertEquals(0, copy.getChoiceList().size() - choicesCount);
  }

  private void verifyTimespan(TimeSpan original, TimeSpanType copy) {
    assertNotNull(copy);
    verifyString(original.getAbout(), copy.getAbout(), true);
    verifyList(original.getAltLabelList(), copy.getAltLabelList(), this::verifyLabel);
    verifyLabel(original.getBegin(), copy.getBegin());
    verifyLabel(original.getEnd(), copy.getEnd());
    verifyList(original.getHasPartsList(), copy.getHasPartList(), this::verifyLabelResource);
    verifyList(original.getIsPartOf(), copy.getIsPartOfList(), this::verifyLabelResource);
    verifyList(original.getNotes(), copy.getNoteList(), this::verifyLabel);
    verifyList(original.getPrefLabelList(), copy.getPrefLabelList(), this::verifyLabel);
    verifyList(original.getHiddenLabel(), copy.getHiddenLabelList(), this::verifyLabel);
    verifyList(original.getSameAs(), copy.getSameAList(), this::verifyPart);
    assertNull(copy.getIsNextInSequence());
  }

  // Compares those choices of the right type. Returns the number of choices covered.
  private <O, C> int verifyChoiceList(List<O> original, eu.europeana.metis.schema.jibx.Concept copy,
      Predicate<Choice> choiceFilter, Function<Choice, C> choiceGetter,
      BiConsumer<O, C> objectVerification) {
    final List<C> items = copy.getChoiceList().stream().filter(choiceFilter).map(choiceGetter)
        .collect(Collectors.toList());
    verifyList(original, items, objectVerification);
    return items.size();
  }

  // Compares an original list with a copied list.
  private <O, C> void verifyList(List<O> original, List<C> copy,
      BiConsumer<O, C> objectVerification) {

    // We compare all non-null originals.
    final List<O> filteredOriginal;
    if (original != null) {
      filteredOriginal = original.stream().filter(Objects::nonNull).collect(Collectors.toList());
    } else {
      filteredOriginal = Collections.emptyList();
    }

    // Make sure the copy exists and compare it.
    assertNotNull(copy);
    assertEquals(filteredOriginal.size(), copy.size());
    for (int index = 0; index < filteredOriginal.size(); index++) {
      objectVerification.accept(filteredOriginal.get(index), copy.get(index));
    }
  }

  // Compares an original list where only the first entry is copied.
  private <O, C> void verifyFirstListItem(List<O> original, C copy,
      BiConsumer<O, C> objectVerification) {

    // Find the first non-null item.
    O firstItem = null;
    if (original != null) {
      for (O item : original) {
        if (item != null) {
          firstItem = item;
          break;
        }
      }
    }

    // If there is an item, we check it, otherwise there can be no copy.
    if (firstItem != null) {
      assertNotNull(copy);
      objectVerification.accept(firstItem, copy);
    } else {
      assertNull(copy);
    }
  }

  private void verifyLabel(Label original, LiteralType copy) {
    if (original.getLang() != null) {
      assertNotNull(copy.getLang());
      verifyString(original.getLang(), copy.getLang().getLang(), false);
    } else {
      assertNull(copy.getLang());
    }
    verifyString(original.getValue(), copy.getString(), false);
  }

  private void verifyResource(Resource original, ResourceType copy) {
    verifyString(original.getResource(), copy.getResource(), false);
  }

  private void verifyLabelResource(LabelResource original, ResourceOrLiteralType copy) {
    if (original.getLang() != null) {
      assertNotNull(copy.getLang());
      verifyString(original.getLang(), copy.getLang().getLang(), false);
    } else {
      assertNull(copy.getLang());
    }
    if (original.getResource() != null) {
      assertNotNull(copy.getResource());
      verifyString(original.getResource(), copy.getResource().getResource(), false);
    } else {
      assertNull(copy.getResource());
    }
    verifyString(original.getValue(), copy.getString(), false);
  }

  private void verifyPart(Part original, ResourceType copy) {
    verifyString(original.getResource(), copy.getResource(), false);
  }

  private void verifyString(String original, String copy, boolean allowNullInCopy) {
    final String revisedOriginal = original == null && !allowNullInCopy ? "" : original;
    assertEquals(revisedOriginal, copy);
  }

  private <T> void verifyFloat(String original, T copy, Function<T, Float> floatExtractor) {
    if (original == null) {
      assertNull(copy);
    } else {
      assertNotNull(copy);
      assertEquals(Float.valueOf(original), floatExtractor.apply(copy));
    }
  }

  private void verifyRdf(RDF rdf, int agentCount, int conceptCount, int placeCount,
      int timeSpanCount, int organizationCount) {

    // Four main lists.
    if (agentCount == 0) {
      assertTrue(CollectionUtils.isEmpty(rdf.getAgentList()));
    } else {
      assertNotNull(rdf.getAgentList());
      assertEquals(agentCount, rdf.getAgentList().size());
    }
    if (conceptCount == 0) {
      assertTrue(CollectionUtils.isEmpty(rdf.getConceptList()));
    } else {
      assertNotNull(rdf.getConceptList());
      assertEquals(conceptCount, rdf.getConceptList().size());
    }
    if (placeCount == 0) {
      assertTrue(CollectionUtils.isEmpty(rdf.getPlaceList()));
    } else {
      assertNotNull(rdf.getPlaceList());
      assertEquals(placeCount, rdf.getPlaceList().size());
    }
    if (timeSpanCount == 0) {
      assertTrue(CollectionUtils.isEmpty(rdf.getTimeSpanList()));
    } else {
      assertNotNull(rdf.getTimeSpanList());
      assertEquals(timeSpanCount, rdf.getTimeSpanList().size());
    }
    if (organizationCount == 0) {
      assertTrue(CollectionUtils.isEmpty(rdf.getOrganizationList()));
    } else {
      assertNotNull(rdf.getOrganizationList());
      assertEquals(organizationCount, rdf.getOrganizationList().size());
    }

    // Other lists should be empty.
    assertNotNull(rdf.getAggregationList());
    assertNotNull(rdf.getDatasetList());
    assertNotNull(rdf.getEuropeanaAggregationList());
    assertNotNull(rdf.getLicenseList());
    assertNotNull(rdf.getOrganizationList());
    assertNotNull(rdf.getProvidedCHOList());
    assertNotNull(rdf.getProxyList());
    assertNotNull(rdf.getServiceList());
    assertNotNull(rdf.getWebResourceList());
    assertEquals(0, rdf.getAggregationList().size());
    assertEquals(0, rdf.getDatasetList().size());
    assertEquals(0, rdf.getEuropeanaAggregationList().size());
    assertEquals(0, rdf.getLicenseList().size());
    assertEquals(0, rdf.getProvidedCHOList().size());
    assertEquals(0, rdf.getProxyList().size());
    assertEquals(0, rdf.getServiceList().size());
    assertEquals(0, rdf.getWebResourceList().size());
  }

  @Test
  public void testMergePlace() throws SerializationException {

    // Create input
    final List<EnrichmentBase> inputList = new ArrayList<>();
    inputList.add(createPlace());
    inputList.add(createFirstPlaceWithNullValues());
    inputList.add(createSecondPlaceWithNullValues());
    final List<EnrichmentBase> enrichmentResultBaseWrapperList = EnrichmentResultBaseWrapper
        .createEnrichmentResultBaseWrapperList(Collections.singletonList(inputList))
        .stream().map(EnrichmentResultBaseWrapper::getEnrichmentBaseList).flatMap(List::stream)
        .collect(Collectors.toList());

    // Perform merge
    RDF rdf = new RDF();
    new EntityMergeEngine().mergeReferenceEntities(rdf, enrichmentResultBaseWrapperList, null);

    // Verify RDF
    verifyRdf(rdf, 0, 0, 3, 0, 0);

    // Verify content
    verifyPlace((Place) inputList.get(0), rdf.getPlaceList().get(0));
    verifyPlace((Place) inputList.get(1), rdf.getPlaceList().get(1));
    verifyPlace((Place) inputList.get(2), rdf.getPlaceList().get(2));

    // Convert RDF to string as extra test that everything is OK.
    RdfConversionUtils.convertRdfToString(rdf);
  }

  @Test
  public void testMergeOtherTypes() throws SerializationException {

    // Create input
    final List<EnrichmentBase> inputList = new ArrayList<>();
    inputList.add(createAgent());
    inputList.add(createConcept());
    inputList.add(createTimeSpan());
    inputList.add(createAgentWithNullValues());
    inputList.add(createConceptWithNullValues());
    inputList.add(createOrganization());
    final List<EnrichmentBase> enrichmentResultBaseWrapperList = EnrichmentResultBaseWrapper
        .createEnrichmentResultBaseWrapperList(Collections.singletonList(inputList))
        .stream().map(EnrichmentResultBaseWrapper::getEnrichmentBaseList).flatMap(List::stream)
        .collect(Collectors.toList());

    // Perform merge
    RDF rdf = new RDF();
    rdf.setPlaceList(null);
    rdf.setAgentList(null);
    rdf.setConceptList(null);
    rdf.setTimeSpanList(null);
    rdf.setOrganizationList(null);
    new EntityMergeEngine().mergeReferenceEntities(rdf, enrichmentResultBaseWrapperList, null);

    // Verify RDF
    verifyRdf(rdf, 2, 2, 0, 1, 1);

    // Verify content
    verifyAgent((Agent) inputList.get(0), rdf.getAgentList().get(0));
    verifyConcept((Concept) inputList.get(1), rdf.getConceptList().get(0));
    verifyTimespan((TimeSpan) inputList.get(2), rdf.getTimeSpanList().get(0));
    verifyAgent((Agent) inputList.get(3), rdf.getAgentList().get(1));
    verifyConcept((Concept) inputList.get(4), rdf.getConceptList().get(1));
    verifyOrganization((Organization) inputList.get(5), rdf.getOrganizationList().get(0));

    // Convert RDF to string as extra test that everything is OK.
    RdfConversionUtils.convertRdfToString(rdf);
  }

  @Test
  public void testMergeUnknownType() {
    final List<EnrichmentBase> inputList = new ArrayList<>();
    inputList.add(new EnrichmentBase() {
    });
    final List<EnrichmentBase> enrichmentResultBaseWrapperList = EnrichmentResultBaseWrapper
        .createEnrichmentResultBaseWrapperList(Collections.singletonList(inputList))
        .stream().map(EnrichmentResultBaseWrapper::getEnrichmentBaseList).flatMap(List::stream)
        .collect(Collectors.toList());
    RDF rdf = new RDF();
    assertThrows(IllegalArgumentException.class, () -> new EntityMergeEngine()
        .mergeReferenceEntities(rdf, enrichmentResultBaseWrapperList, null));
  }
}
