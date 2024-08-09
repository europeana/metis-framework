package eu.europeana.indexing.tiers.metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import eu.europeana.indexing.tiers.metadata.ContextualClassesClassifier.ContextualClassesStatistics;
import eu.europeana.indexing.tiers.model.MetadataTier;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.metis.schema.jibx.AgentType;
import eu.europeana.metis.schema.jibx.Begin;
import eu.europeana.metis.schema.jibx.Broader;
import eu.europeana.metis.schema.jibx.CloseMatch;
import eu.europeana.metis.schema.jibx.Concept;
import eu.europeana.metis.schema.jibx.Concept.Choice;
import eu.europeana.metis.schema.jibx.DateOfBirth;
import eu.europeana.metis.schema.jibx.DateOfDeath;
import eu.europeana.metis.schema.jibx.End;
import eu.europeana.metis.schema.jibx.ExactMatch;
import eu.europeana.metis.schema.jibx.Lat;
import eu.europeana.metis.schema.jibx.LiteralType;
import eu.europeana.metis.schema.jibx.Narrower;
import eu.europeana.metis.schema.jibx.Note;
import eu.europeana.metis.schema.jibx.PlaceOfBirth;
import eu.europeana.metis.schema.jibx.PlaceOfDeath;
import eu.europeana.metis.schema.jibx.PlaceType;
import eu.europeana.metis.schema.jibx.PrefLabel;
import eu.europeana.metis.schema.jibx.ProfessionOrOccupation;
import eu.europeana.metis.schema.jibx.ProxyType;
import eu.europeana.metis.schema.jibx.Related;
import eu.europeana.metis.schema.jibx.ResourceOrLiteralType;
import eu.europeana.metis.schema.jibx.ResourceOrLiteralType.Resource;
import eu.europeana.metis.schema.jibx.ResourceType;
import eu.europeana.metis.schema.jibx.SameAs;
import eu.europeana.metis.schema.jibx.TimeSpanType;
import eu.europeana.metis.schema.jibx._Long;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class ContextualClassesBreakdownClassifierTest {

  @Test
  void testHasProperty() {
    final ContextualClassesClassifier classifier = new ContextualClassesClassifier();

    // Test for literals
    assertFalse(classifier.hasProperty((LiteralType) null));
    final LiteralType literal = new LiteralType();
    literal.setString(null);
    assertFalse(classifier.hasProperty(literal));
    literal.setString(" ");
    assertFalse(classifier.hasProperty(literal));
    literal.setString("test 1");
    assertTrue(classifier.hasProperty(literal));

    // Test for resources
    assertFalse(classifier.hasProperty((ResourceType) null));
    final ResourceType resource = new ResourceType();
    resource.setResource(null);
    assertFalse(classifier.hasProperty(resource));
    resource.setResource(" ");
    assertFalse(classifier.hasProperty(resource));
    resource.setResource("test 2");
    assertTrue(classifier.hasProperty(resource));

    // Test for resource/literal objects
    assertFalse(classifier.hasProperty((ResourceOrLiteralType) null));
    final ResourceOrLiteralType object = new ResourceOrLiteralType();
    object.setResource(null);
    object.setString(null);
    assertFalse(classifier.hasProperty(object));
    object.setString(" ");
    assertFalse(classifier.hasProperty(object));
    object.setString("test");
    assertTrue(classifier.hasProperty(object));
    object.setString(null);
    object.setResource(new Resource());
    object.getResource().setResource(null);
    assertFalse(classifier.hasProperty(object));
    object.getResource().setResource(" ");
    assertFalse(classifier.hasProperty(object));
    object.getResource().setResource("test 3");
    assertTrue(classifier.hasProperty(object));
  }

  @Test
  void testHasLiteralProperty() {
    final ContextualClassesClassifier classifier = spy(new ContextualClassesClassifier());

    // Test with null or empty list
    assertFalse(classifier.hasLiteralProperty(null));
    assertFalse(classifier.hasLiteralProperty(Collections.emptyList()));

    // Test with objects
    final LiteralType object1 = new LiteralType();
    final LiteralType object2 = new LiteralType();
    doReturn(false).when(classifier).hasProperty(object1);
    doReturn(true).when(classifier).hasProperty(object2);
    assertFalse(classifier.hasLiteralProperty(Arrays.asList(object1, object1)));
    assertTrue(classifier.hasLiteralProperty(Arrays.asList(object1, object2)));
    assertTrue(classifier.hasLiteralProperty(Arrays.asList(object2, object1)));
    assertTrue(classifier.hasLiteralProperty(Arrays.asList(object2, object2)));
  }

  @Test
  void testHasResourceOrLiteralProperty() {
    final ContextualClassesClassifier classifier = spy(new ContextualClassesClassifier());

    // Test with null or empty list
    assertFalse(classifier.hasResourceOrLiteralProperty(null));
    assertFalse(classifier.hasResourceOrLiteralProperty(Collections.emptyList()));

    // Test with objects
    final ResourceOrLiteralType object1 = new ResourceOrLiteralType();
    final ResourceOrLiteralType object2 = new ResourceOrLiteralType();
    doReturn(false).when(classifier).hasProperty(object1);
    doReturn(true).when(classifier).hasProperty(object2);
    assertFalse(classifier.hasResourceOrLiteralProperty(Arrays.asList(object1, object1)));
    assertTrue(classifier.hasResourceOrLiteralProperty(Arrays.asList(object1, object2)));
    assertTrue(classifier.hasResourceOrLiteralProperty(Arrays.asList(object2, object1)));
    assertTrue(classifier.hasResourceOrLiteralProperty(Arrays.asList(object2, object2)));
  }

  @Test
  void testEntityQualifiesForAgent() {

    // Create objects
    final ContextualClassesClassifier classifier = spy(new ContextualClassesClassifier());
    final AgentType agent = new AgentType();
    final List<PrefLabel> prefLabelList = new ArrayList<>();
    final Begin begin = new Begin();
    final DateOfBirth dateOfBirth = new DateOfBirth();
    final List<PlaceOfBirth> placeOfBirthList = new ArrayList<>();
    final End end = new End();
    final DateOfDeath dateOfDeath = new DateOfDeath();
    final List<PlaceOfDeath> placeOfDeathList = new ArrayList<>();
    final List<ProfessionOrOccupation> professionOrOccupationList = new ArrayList<>();

    // Test empty agent
    assertFalse(classifier.entityQualifies(agent));

    // Set values
    agent.setPrefLabelList(prefLabelList);
    agent.setBegin(begin);
    agent.setDateOfBirth(dateOfBirth);
    agent.setPlaceOfBirthList(placeOfBirthList);
    agent.setEnd(end);
    agent.setDateOfDeath(dateOfDeath);
    agent.setPlaceOfDeathList(placeOfDeathList);
    agent.setProfessionOrOccupationList(professionOrOccupationList);

    // Test prefLabel absent (and rest present)
    doReturn(false).when(classifier).hasLiteralProperty(prefLabelList);
    doReturn(true).when(classifier).hasProperty(begin);
    doReturn(true).when(classifier).hasProperty(dateOfBirth);
    doReturn(true).when(classifier).hasResourceOrLiteralProperty(placeOfBirthList);
    doReturn(true).when(classifier).hasProperty(end);
    doReturn(true).when(classifier).hasProperty(dateOfDeath);
    doReturn(true).when(classifier).hasResourceOrLiteralProperty(placeOfDeathList);
    doReturn(true).when(classifier).hasResourceOrLiteralProperty(professionOrOccupationList);
    assertFalse(classifier.entityQualifies(agent));

    // Test prefLabel present (and rest absent)
    doReturn(true).when(classifier).hasLiteralProperty(prefLabelList);
    doReturn(false).when(classifier).hasProperty(begin);
    doReturn(false).when(classifier).hasProperty(dateOfBirth);
    doReturn(false).when(classifier).hasResourceOrLiteralProperty(placeOfBirthList);
    doReturn(false).when(classifier).hasProperty(end);
    doReturn(false).when(classifier).hasProperty(dateOfDeath);
    doReturn(false).when(classifier).hasResourceOrLiteralProperty(placeOfDeathList);
    doReturn(false).when(classifier).hasResourceOrLiteralProperty(professionOrOccupationList);
    assertFalse(classifier.entityQualifies(agent));

    // Test prefLabel and begin present
    doReturn(true).when(classifier).hasProperty(begin);
    assertTrue(classifier.entityQualifies(agent));
    doReturn(false).when(classifier).hasProperty(begin);
    assertFalse(classifier.entityQualifies(agent));

    // Test prefLabel and dateOfBirth present
    doReturn(true).when(classifier).hasProperty(dateOfBirth);
    assertTrue(classifier.entityQualifies(agent));
    doReturn(false).when(classifier).hasProperty(dateOfBirth);
    assertFalse(classifier.entityQualifies(agent));

    // Test prefLabel and placeOfBirth present
    doReturn(true).when(classifier).hasResourceOrLiteralProperty(placeOfBirthList);
    assertTrue(classifier.entityQualifies(agent));
    doReturn(false).when(classifier).hasResourceOrLiteralProperty(placeOfBirthList);
    assertFalse(classifier.entityQualifies(agent));

    // Test prefLabel and end present
    doReturn(true).when(classifier).hasProperty(end);
    assertTrue(classifier.entityQualifies(agent));
    doReturn(false).when(classifier).hasProperty(end);
    assertFalse(classifier.entityQualifies(agent));

    // Test prefLabel and dateOfDeath present
    doReturn(true).when(classifier).hasProperty(dateOfDeath);
    assertTrue(classifier.entityQualifies(agent));
    doReturn(false).when(classifier).hasProperty(dateOfDeath);
    assertFalse(classifier.entityQualifies(agent));

    // Test prefLabel and placeOfDeath present
    doReturn(true).when(classifier).hasResourceOrLiteralProperty(placeOfDeathList);
    assertTrue(classifier.entityQualifies(agent));
    doReturn(false).when(classifier).hasResourceOrLiteralProperty(placeOfDeathList);
    assertFalse(classifier.entityQualifies(agent));

    // Test prefLabel and professionOrOccupation present
    doReturn(true).when(classifier).hasResourceOrLiteralProperty(professionOrOccupationList);
    assertTrue(classifier.entityQualifies(agent));
  }

  @Test
  void testEntityQualifiesForConcept() {

    // Create objects
    final ContextualClassesClassifier classifier = spy(new ContextualClassesClassifier());
    final Concept concept = new Concept();
    final PrefLabel prefLabel = new PrefLabel();
    final Note note = new Note();
    final Broader broader = new Broader();
    final Narrower narrower = new Narrower();
    final ExactMatch exactMatch = new ExactMatch();
    final CloseMatch closeMatch = new CloseMatch();
    final Related related = new Related();

    // Test empty concept
    concept.setChoiceList(null);
    assertFalse(classifier.entityQualifies(concept));
    concept.setChoiceList(new ArrayList<>());
    assertFalse(classifier.entityQualifies(concept));
    concept.getChoiceList().add(new Choice());
    assertFalse(classifier.entityQualifies(concept));

    // Set values
    concept.setChoiceList(
        IntStream.range(0, 7).mapToObj(index -> new Choice()).toList());
    concept.getChoiceList().get(0).setPrefLabel(prefLabel);
    concept.getChoiceList().get(1).setNote(note);
    concept.getChoiceList().get(2).setBroader(broader);
    concept.getChoiceList().get(3).setNarrower(narrower);
    concept.getChoiceList().get(4).setExactMatch(exactMatch);
    concept.getChoiceList().get(5).setCloseMatch(closeMatch);
    concept.getChoiceList().get(6).setRelated(related);

    // Test prefLabel absent (and rest present)
    doReturn(false).when(classifier).hasProperty(prefLabel);
    doReturn(true).when(classifier).hasProperty(note);
    doReturn(true).when(classifier).hasProperty(broader);
    doReturn(true).when(classifier).hasProperty(narrower);
    doReturn(true).when(classifier).hasProperty(exactMatch);
    doReturn(true).when(classifier).hasProperty(closeMatch);
    doReturn(true).when(classifier).hasProperty(related);
    assertFalse(classifier.entityQualifies(concept));

    // Test prefLabel present (and rest absent)
    doReturn(true).when(classifier).hasProperty(prefLabel);
    doReturn(false).when(classifier).hasProperty(note);
    doReturn(false).when(classifier).hasProperty(broader);
    doReturn(false).when(classifier).hasProperty(narrower);
    doReturn(false).when(classifier).hasProperty(exactMatch);
    doReturn(false).when(classifier).hasProperty(closeMatch);
    doReturn(false).when(classifier).hasProperty(related);
    assertFalse(classifier.entityQualifies(concept));

    // Test prefLabel and note present
    doReturn(true).when(classifier).hasProperty(note);
    assertTrue(classifier.entityQualifies(concept));
    doReturn(false).when(classifier).hasProperty(note);
    assertFalse(classifier.entityQualifies(concept));

    // Test prefLabel and broader present
    doReturn(true).when(classifier).hasProperty(broader);
    assertTrue(classifier.entityQualifies(concept));
    doReturn(false).when(classifier).hasProperty(broader);
    assertFalse(classifier.entityQualifies(concept));

    // Test prefLabel and narrower present
    doReturn(true).when(classifier).hasProperty(narrower);
    assertTrue(classifier.entityQualifies(concept));
    doReturn(false).when(classifier).hasProperty(narrower);
    assertFalse(classifier.entityQualifies(concept));

    // Test prefLabel and exactMatch present
    doReturn(true).when(classifier).hasProperty(exactMatch);
    assertTrue(classifier.entityQualifies(concept));
    doReturn(false).when(classifier).hasProperty(exactMatch);
    assertFalse(classifier.entityQualifies(concept));

    // Test prefLabel and closeMatch present
    doReturn(true).when(classifier).hasProperty(closeMatch);
    assertTrue(classifier.entityQualifies(concept));
    doReturn(false).when(classifier).hasProperty(closeMatch);
    assertFalse(classifier.entityQualifies(concept));

    // Test prefLabel and related present
    doReturn(true).when(classifier).hasProperty(related);
    assertTrue(classifier.entityQualifies(concept));
    doReturn(false).when(classifier).hasProperty(related);
    assertFalse(classifier.entityQualifies(concept));
  }

  @Test
  void testEntityQualifiesForPlace() {

    // Create objects
    final ContextualClassesClassifier classifier = spy(new ContextualClassesClassifier());
    final PlaceType place = new PlaceType();
    final List<PrefLabel> prefLabelList = new ArrayList<>();

    // Test empty time span
    assertFalse(classifier.entityQualifies(place));

    // Set values
    place.setPrefLabelList(prefLabelList);
    place.setLat(new Lat());
    place.getLat().setLat(0.0F);
    place.setLong(new _Long());
    place.getLong().setLong(0.0F);

    // Test all present
    doReturn(true).when(classifier).hasLiteralProperty(prefLabelList);
    assertTrue(classifier.entityQualifies(place));

    // Test without pref label list
    doReturn(false).when(classifier).hasLiteralProperty(prefLabelList);
    assertFalse(classifier.entityQualifies(place));
    doReturn(true).when(classifier).hasLiteralProperty(prefLabelList);
    assertTrue(classifier.entityQualifies(place));

    // Test without lat
    place.getLat().setLat(null);
    assertFalse(classifier.entityQualifies(place));
    place.setLat(null);
    assertFalse(classifier.entityQualifies(place));
    place.setLat(new Lat());
    place.getLat().setLat(0.0F);
    assertTrue(classifier.entityQualifies(place));

    // Test without lon
    place.getLong().setLong(null);
    assertFalse(classifier.entityQualifies(place));
    place.setLong(null);
    assertFalse(classifier.entityQualifies(place));
  }

  @Test
  void testEntityQualifiesForTimeSpan() {

    // Create objects
    final ContextualClassesClassifier classifier = spy(new ContextualClassesClassifier());
    final TimeSpanType timeSpan = new TimeSpanType();
    final Begin begin = new Begin();
    final End end = new End();

    // Test empty time span
    assertFalse(classifier.entityQualifies(timeSpan));

    // Set values
    timeSpan.setBegin(begin);
    timeSpan.setEnd(end);

    // Test all present
    doReturn(true).when(classifier).hasProperty(begin);
    doReturn(true).when(classifier).hasProperty(end);
    assertTrue(classifier.entityQualifies(timeSpan));

    // Test without begin
    doReturn(false).when(classifier).hasProperty(begin);
    assertFalse(classifier.entityQualifies(timeSpan));
    doReturn(true).when(classifier).hasProperty(begin);
    assertTrue(classifier.entityQualifies(timeSpan));

    // Test withouty end
    doReturn(false).when(classifier).hasProperty(end);
    assertFalse(classifier.entityQualifies(timeSpan));
  }

  @Test
  void countQualifyingContextualClassTypes() {

    // Create mocks of the classifier and test empty object.
    final ContextualClassesClassifier classifier = spy(new ContextualClassesClassifier());
    final RdfWrapper entity = mock(RdfWrapper.class);
    assertEquals(0, classifier.countQualifyingContextualClassTypes(entity).getCompleteContextualResources());

    // Create the contextual objects.
    final List<AgentType> agents = Collections.singletonList(new AgentType());
    final List<Concept> concepts = Collections.singletonList(new Concept());
    final List<PlaceType> places = Arrays.asList(new PlaceType(), new PlaceType());
    final List<TimeSpanType> timeSpans = Collections.singletonList(new TimeSpanType());
    doReturn(agents).when(entity).getAgents();
    doReturn(concepts).when(entity).getConcepts();
    doReturn(places).when(entity).getPlaces();
    doReturn(timeSpans).when(entity).getTimeSpans();

    // Set the about values in the contextual objects
    final String agentAbout = "agentAbout";
    final String conceptAbout = "conceptAbout";
    final String linkedPlaceAbout = "linkedPlaceAbout";
    final String unlinkedPlaceAbout = "unlinkedPlaceAbout";
    final String existingTimespanAbout = "existingTimeSpanAbout";
    final String absentTimespanAbout = "absentTimeSpanAbout";
    agents.getFirst().setAbout(agentAbout);
    concepts.getFirst().setAbout(conceptAbout);
    places.get(0).setAbout(linkedPlaceAbout);
    places.get(1).setAbout(unlinkedPlaceAbout);
    timeSpans.getFirst().setAbout(existingTimespanAbout);

    // Create links to most objects and check that they are indeed obtainable.
    final List<ProxyType> proxies = Arrays.asList(new ProxyType(), new ProxyType());
    doReturn(proxies).when(entity).getProviderProxies();
    proxies.get(0).setSameAList(Arrays.asList(new SameAs(), new SameAs(), new SameAs()));
    proxies.get(1).setSameAList(Arrays.asList(new SameAs(), new SameAs(), new SameAs()));
    proxies.get(0).getSameAList().get(0).setResource(agentAbout);
    proxies.get(0).getSameAList().get(1).setResource(conceptAbout);
    proxies.get(0).getSameAList().get(2).setResource(linkedPlaceAbout);
    proxies.get(1).getSameAList().get(0).setResource(agentAbout);
    proxies.get(1).getSameAList().get(1).setResource(existingTimespanAbout);
    proxies.get(1).getSameAList().get(2).setResource(absentTimespanAbout);
    assertEquals(new HashSet<>(Arrays.asList(agentAbout, conceptAbout, linkedPlaceAbout)),
        ResourceLinkFromProxy.SAME_AS.getLinkAndValueGetter().getLinks(proxies.get(0))
                                     .collect(Collectors.toSet()));
    assertEquals(new HashSet<>(Arrays.asList(agentAbout, existingTimespanAbout, absentTimespanAbout)),
        ResourceLinkFromProxy.SAME_AS.getLinkAndValueGetter().getLinks(proxies.get(1))
                                     .collect(Collectors.toSet()));

    // Do the tests for no qualifying entities (except the unlinked one).
    doReturn(false).when(classifier).entityQualifies(agents.getFirst());
    doReturn(false).when(classifier).entityQualifies(concepts.getFirst());
    doReturn(false).when(classifier).entityQualifies(places.get(0));
    doReturn(true).when(classifier).entityQualifies(places.get(1));
    doReturn(false).when(classifier).entityQualifies(timeSpans.getFirst());
    assertEquals(0, classifier.countQualifyingContextualClassTypes(entity).getCompleteContextualResources());

    // Make some of them qualifying and do the tests again.
    doReturn(true).when(classifier).entityQualifies(agents.getFirst());
    assertEquals(1, classifier.countQualifyingContextualClassTypes(entity).getCompleteContextualResources());
    doReturn(true).when(classifier).entityQualifies(concepts.getFirst());
    assertEquals(2, classifier.countQualifyingContextualClassTypes(entity).getCompleteContextualResources());
    doReturn(true).when(classifier).entityQualifies(places.getFirst());
    assertEquals(3, classifier.countQualifyingContextualClassTypes(entity).getCompleteContextualResources());
    doReturn(true).when(classifier).entityQualifies(timeSpans.getFirst());
    assertEquals(4, classifier.countQualifyingContextualClassTypes(entity).getCompleteContextualResources());

    // Make some of them non-qualifying and do the tests again.
    doReturn(false).when(classifier).entityQualifies(agents.getFirst());
    assertEquals(3, classifier.countQualifyingContextualClassTypes(entity).getCompleteContextualResources());
    doReturn(false).when(classifier).entityQualifies(concepts.getFirst());
    assertEquals(2, classifier.countQualifyingContextualClassTypes(entity).getCompleteContextualResources());
    doReturn(false).when(classifier).entityQualifies(places.getFirst());
    assertEquals(1, classifier.countQualifyingContextualClassTypes(entity).getCompleteContextualResources());
    doReturn(false).when(classifier).entityQualifies(timeSpans.getFirst());
    assertEquals(0, classifier.countQualifyingContextualClassTypes(entity).getCompleteContextualResources());
  }

  @Test
  void testClassify() {

    // Create mocks
    final ContextualClassesClassifier classifier = spy(new ContextualClassesClassifier());
    final RdfWrapper entity = mock(RdfWrapper.class);

    // Test for all values
    final Set<ContextualClassGroup> distinctClassesSet = Set.of(ContextualClassGroup.TEMPORAL, ContextualClassGroup.GEOGRAPHICAL);
    doReturn(new ContextualClassesStatistics(0, distinctClassesSet)).when(classifier).countQualifyingContextualClassTypes(entity);
    assertEquals(MetadataTier.TA, classifier.classifyBreakdown(entity).getMetadataTier());
    doReturn(new ContextualClassesStatistics(1, distinctClassesSet)).when(classifier).countQualifyingContextualClassTypes(entity);
    assertEquals(MetadataTier.TB, classifier.classifyBreakdown(entity).getMetadataTier());
    doReturn(new ContextualClassesStatistics(2, distinctClassesSet)).when(classifier).countQualifyingContextualClassTypes(entity);
    assertEquals(MetadataTier.TC, classifier.classifyBreakdown(entity).getMetadataTier());
    doReturn(new ContextualClassesStatistics(3, distinctClassesSet)).when(classifier).countQualifyingContextualClassTypes(entity);
    assertEquals(MetadataTier.TC, classifier.classifyBreakdown(entity).getMetadataTier());
    doReturn(new ContextualClassesStatistics(4, distinctClassesSet)).when(classifier).countQualifyingContextualClassTypes(entity);
    assertEquals(MetadataTier.TC, classifier.classifyBreakdown(entity).getMetadataTier());
  }
}
