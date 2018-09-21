package eu.europeana.enrichment.utils;

import eu.europeana.corelib.definitions.jibx.AboutType;
import eu.europeana.corelib.definitions.jibx.AgentType;
import eu.europeana.corelib.definitions.jibx.Alt;
import eu.europeana.corelib.definitions.jibx.AltLabel;
import eu.europeana.corelib.definitions.jibx.Begin;
import eu.europeana.corelib.definitions.jibx.BiographicalInformation;
import eu.europeana.corelib.definitions.jibx.BroadMatch;
import eu.europeana.corelib.definitions.jibx.Broader;
import eu.europeana.corelib.definitions.jibx.CloseMatch;
import eu.europeana.corelib.definitions.jibx.Concept.Choice;
import eu.europeana.corelib.definitions.jibx.Date;
import eu.europeana.corelib.definitions.jibx.DateOfBirth;
import eu.europeana.corelib.definitions.jibx.DateOfDeath;
import eu.europeana.corelib.definitions.jibx.DateOfEstablishment;
import eu.europeana.corelib.definitions.jibx.DateOfTermination;
import eu.europeana.corelib.definitions.jibx.End;
import eu.europeana.corelib.definitions.jibx.ExactMatch;
import eu.europeana.corelib.definitions.jibx.Gender;
import eu.europeana.corelib.definitions.jibx.HasMet;
import eu.europeana.corelib.definitions.jibx.HasPart;
import eu.europeana.corelib.definitions.jibx.Identifier;
import eu.europeana.corelib.definitions.jibx.InScheme;
import eu.europeana.corelib.definitions.jibx.IsPartOf;
import eu.europeana.corelib.definitions.jibx.IsRelatedTo;
import eu.europeana.corelib.definitions.jibx.Lat;
import eu.europeana.corelib.definitions.jibx.NarrowMatch;
import eu.europeana.corelib.definitions.jibx.Narrower;
import eu.europeana.corelib.definitions.jibx.Notation;
import eu.europeana.corelib.definitions.jibx.Note;
import eu.europeana.corelib.definitions.jibx.PlaceType;
import eu.europeana.corelib.definitions.jibx.PrefLabel;
import eu.europeana.corelib.definitions.jibx.ProfessionOrOccupation;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.definitions.jibx.Related;
import eu.europeana.corelib.definitions.jibx.RelatedMatch;
import eu.europeana.corelib.definitions.jibx.SameAs;
import eu.europeana.corelib.definitions.jibx.TimeSpanType;
import eu.europeana.corelib.definitions.jibx._Long;
import eu.europeana.enrichment.api.external.model.Agent;
import eu.europeana.enrichment.api.external.model.Concept;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.Label;
import eu.europeana.enrichment.api.external.model.Part;
import eu.europeana.enrichment.api.external.model.Place;
import eu.europeana.enrichment.api.external.model.Timespan;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class EntityMergeEngine {

  private static PlaceType convertAndAddPlace(Place place, List<PlaceType> destination) {
    //Check if Entity already exists in the list
    if (ItemExtractorUtils.isEntityAlreadyInList(place, destination)) {
      return null;
    }
    PlaceType placeType = new PlaceType();

    // about
    ItemExtractorUtils.setAbout(place, placeType);

    // alt
    if (place.getAlt() != null) {
      Alt alt = new Alt();
      alt.setAlt(Float.valueOf(place.getAlt()));
      placeType.setAlt(alt);
    }

    // altlabels
    placeType
        .setAltLabelList(ItemExtractorUtils.extractLabels(place.getAltLabelList(), AltLabel::new));

    // hasPartList
    placeType
        .setHasPartList(ItemExtractorUtils.extractParts(place.getHasPartsList(), HasPart::new));

    // isPartOfList
    placeType
        .setIsPartOfList(ItemExtractorUtils.extractParts(place.getIsPartOfList(), IsPartOf::new));

    // lat
    if (place.getLat() != null) {
      Lat lat = new Lat();
      lat.setLat(Float.valueOf(place.getLat()));
      placeType.setLat(lat);
    }

    // _long
    if (place.getLon() != null) {
      _Long longitude = new _Long();
      longitude.setLong(Float.valueOf(place.getLon()));
      placeType.setLong(longitude);
    }

    // noteList
    placeType.setNoteList(ItemExtractorUtils.extractLabels(place.getNotes(), Note::new));

    // prefLabelList
    placeType.setPrefLabelList(
        ItemExtractorUtils.extractLabels(place.getPrefLabelList(), PrefLabel::new));

    // sameAsList
    placeType.setSameAList(
        ItemExtractorUtils.extractAsResources(place.getSameAs(), SameAs::new, Part::getResource));

    // isNextInSequence: not available

    // Done
    destination.add(placeType);
    return placeType;
  }

  private static AgentType convertAndAddAgent(Agent agent, List<AgentType> destination) {
    //Check if Entity already exists in the list
    if (ItemExtractorUtils.isEntityAlreadyInList(agent, destination)) {
      return null;
    }

    AgentType agentType = new AgentType();

    // about
    ItemExtractorUtils.setAbout(agent, agentType);

    // altLabelList
    agentType
        .setAltLabelList(ItemExtractorUtils.extractLabels(agent.getAltLabelList(), AltLabel::new));

    // begin
    agentType.setBegin(ItemExtractorUtils.extractFirstLabel(agent.getBeginList(), Begin::new));

    // biographicalInformation
    agentType.setBiographicalInformationList(
        ItemExtractorUtils.extractLabelsToResourceOrLiteralList(agent.getBiographicaInformation(),
            BiographicalInformation::new));
    agentType.setProfessionOrOccupationList(
        ItemExtractorUtils
            .extractLabelResources(agent.getProfessionOrOccupation(), ProfessionOrOccupation::new));

    // dateList
    agentType.setDateList(ItemExtractorUtils.extractLabelResources(agent.getDate(), Date::new));

    // dateOfBirth
    agentType.setDateOfBirth(
        ItemExtractorUtils.extractFirstLabel(agent.getDateOfBirth(), DateOfBirth::new));

    // dateofDeath
    agentType.setDateOfDeath(
        ItemExtractorUtils.extractFirstLabel(agent.getDateOfDeath(), DateOfDeath::new));

    // dateOfEstablishment
    agentType.setDateOfEstablishment(
        ItemExtractorUtils
            .extractFirstLabel(agent.getDateOfEstablishment(), DateOfEstablishment::new));

    // dateofTermination
    agentType.setDateOfTermination(
        ItemExtractorUtils.extractFirstLabel(agent.getDateOfTermination(), DateOfTermination::new));

    // end
    agentType.setEnd(ItemExtractorUtils.extractFirstLabel(agent.getEndList(), End::new));

    // gender
    agentType.setGender(ItemExtractorUtils.extractFirstLabel(agent.getGender(), Gender::new));

    // hasMetList
    agentType.setHasMetList(
        ItemExtractorUtils.extractAsResources(agent.getHasMet(), HasMet::new, Label::getValue));

    // hasPartList: not available

    // identifierList
    agentType.setIdentifierList(
        ItemExtractorUtils.extractLabels(agent.getIdentifier(), Identifier::new));

    // isPartOfList: not available

    // isRelatedToList
    agentType.setIsRelatedToList(
        ItemExtractorUtils.extractLabelResources(agent.getIsRelatedTo(), IsRelatedTo::new));

    // nameList: not available

    // noteList
    agentType.setNoteList(ItemExtractorUtils.extractLabels(agent.getNotes(), Note::new));

    // placeofBirth: not available

    // placeofDeath: not available

    // prefLabelList
    agentType.setPrefLabelList(
        ItemExtractorUtils.extractLabels(agent.getPrefLabelList(), PrefLabel::new));

    // professionOrOccupationList
    agentType.setProfessionOrOccupationList(
        ItemExtractorUtils
            .extractLabelResources(agent.getProfessionOrOccupation(), ProfessionOrOccupation::new));

    // sameAsList
    agentType.setSameAList(
        ItemExtractorUtils.extractAsResources(agent.getSameAs(), SameAs::new, Part::getResource));

    destination.add(agentType);
    return agentType;
  }

  private static eu.europeana.corelib.definitions.jibx.Concept convertAndAddConcept(
      Concept baseConcept, List<eu.europeana.corelib.definitions.jibx.Concept> destination) {
    //Check if Entity already exists in the list
    if (ItemExtractorUtils.isEntityAlreadyInList(baseConcept, destination)) {
      return null;
    }
    eu.europeana.corelib.definitions.jibx.Concept concept =
        new eu.europeana.corelib.definitions.jibx.Concept();

    // about
    ItemExtractorUtils.setAbout(baseConcept, concept);

    // choiceList
    final List<Choice> choices = new ArrayList<>();

    final List<AltLabel> altLabels = ItemExtractorUtils
        .extractLabels(baseConcept.getAltLabelList(), AltLabel::new);
    ItemExtractorUtils.toChoices(altLabels, Choice::setAltLabel, choices);

    final List<BroadMatch> broadMatches =
        ItemExtractorUtils.extractResources(baseConcept.getBroadMatch(), BroadMatch::new);
    ItemExtractorUtils.toChoices(broadMatches, Choice::setBroadMatch, choices);

    final List<Broader> broaders = ItemExtractorUtils
        .extractResources(baseConcept.getBroader(), Broader::new);
    ItemExtractorUtils.toChoices(broaders, Choice::setBroader, choices);

    final List<CloseMatch> closeMatches =
        ItemExtractorUtils.extractResources(baseConcept.getCloseMatch(), CloseMatch::new);
    ItemExtractorUtils.toChoices(closeMatches, Choice::setCloseMatch, choices);

    final List<ExactMatch> exactMatches =
        ItemExtractorUtils.extractResources(baseConcept.getExactMatch(), ExactMatch::new);
    ItemExtractorUtils.toChoices(exactMatches, Choice::setExactMatch, choices);

    final List<InScheme> inSchemes = ItemExtractorUtils
        .extractResources(baseConcept.getInScheme(), InScheme::new);
    ItemExtractorUtils.toChoices(inSchemes, Choice::setInScheme, choices);

    final List<Narrower> narrowers = ItemExtractorUtils
        .extractResources(baseConcept.getNarrower(), Narrower::new);
    ItemExtractorUtils.toChoices(narrowers, Choice::setNarrower, choices);

    final List<NarrowMatch> narrowMatches =
        ItemExtractorUtils.extractResources(baseConcept.getNarrowMatch(), NarrowMatch::new);
    ItemExtractorUtils.toChoices(narrowMatches, Choice::setNarrowMatch, choices);

    final List<Notation> notations = ItemExtractorUtils
        .extractLabels(baseConcept.getNotation(), Notation::new);
    ItemExtractorUtils.toChoices(notations, Choice::setNotation, choices);

    final List<Note> notes = ItemExtractorUtils.extractLabels(baseConcept.getNotes(), Note::new);
    ItemExtractorUtils.toChoices(notes, Choice::setNote, choices);

    final List<PrefLabel> prefLabels =
        ItemExtractorUtils.extractLabels(baseConcept.getPrefLabelList(), PrefLabel::new);
    ItemExtractorUtils.toChoices(prefLabels, Choice::setPrefLabel, choices);

    final List<Related> relateds = ItemExtractorUtils
        .extractResources(baseConcept.getRelated(), Related::new);
    ItemExtractorUtils.toChoices(relateds, Choice::setRelated, choices);

    final List<RelatedMatch> relatedMatches =
        ItemExtractorUtils.extractResources(baseConcept.getRelatedMatch(), RelatedMatch::new);
    ItemExtractorUtils.toChoices(relatedMatches, Choice::setRelatedMatch, choices);

    concept.setChoiceList(choices);

    destination.add(concept);
    return concept;
  }

  private static TimeSpanType convertAndAddTimespan(Timespan timespan,
      List<TimeSpanType> destination) {
    //Check if Entity already exists in the list
    if (ItemExtractorUtils.isEntityAlreadyInList(timespan, destination)) {
      return null;
    }

    TimeSpanType timeSpanType = new TimeSpanType();

    // about
    ItemExtractorUtils.setAbout(timespan, timeSpanType);

    // altLabelList
    timeSpanType.setAltLabelList(
        ItemExtractorUtils.extractLabels(timespan.getAltLabelList(), AltLabel::new));

    // begin
    timeSpanType
        .setBegin(ItemExtractorUtils.extractFirstLabel(timespan.getBeginList(), Begin::new));

    // end
    timeSpanType.setEnd(ItemExtractorUtils.extractFirstLabel(timespan.getEndList(), End::new));

    // hasPartList
    timeSpanType
        .setHasPartList(ItemExtractorUtils.extractParts(timespan.getHasPartsList(), HasPart::new));

    // isNextInSequence: not available

    // isPartOfList
    timeSpanType.setIsPartOfList(
        ItemExtractorUtils.extractParts(timespan.getIsPartOfList(), IsPartOf::new));

    // noteList
    timeSpanType.setNoteList(ItemExtractorUtils.extractLabels(timespan.getNotes(), Note::new));

    // prefLabelList
    timeSpanType.setPrefLabelList(
        ItemExtractorUtils.extractLabels(timespan.getPrefLabelList(), PrefLabel::new));

    // sameAsList
    timeSpanType
        .setSameAList(ItemExtractorUtils
            .extractAsResources(timespan.getSameAs(), SameAs::new, Part::getResource));

    // done
    destination.add(timeSpanType);
    return timeSpanType;
  }

  private static void convertAndAddEntity(RDF rdf, EnrichmentBase enrichmentBase,
      String fieldName) {

    // Convert the entity.
    final AboutType entity;
    if (enrichmentBase instanceof Place) {
      entity = convertAndAddPlace((Place) enrichmentBase, rdf.getPlaceList());
    } else if (enrichmentBase instanceof Agent) {
      entity = convertAndAddAgent((Agent) enrichmentBase, rdf.getAgentList());
    } else if (enrichmentBase instanceof Concept) {
      entity = convertAndAddConcept((Concept) enrichmentBase, rdf.getConceptList());
    } else if (enrichmentBase instanceof Timespan) {
      entity = convertAndAddTimespan((Timespan) enrichmentBase, rdf.getTimeSpanList());
    } else {
      throw new IllegalArgumentException("Unknown entity type: " + enrichmentBase.getClass());
    }

    // Append it to the proxy if needed.
    if (StringUtils.isNotEmpty(fieldName) && entity != null) {
      RdfProxyUtils.appendToProxy(rdf, entity, fieldName);
    }
  }

  /**
   * Merge entities in a record. Wrapper for {@link #mergeEntities(RDF, List, String)} without a
   * field name.
   *
   * @param rdf The RDF to enrich
   * @param enrichmentBaseList The information to append
   */
  public void mergeEntities(RDF rdf, List<EnrichmentBase> enrichmentBaseList) {
    mergeEntities(rdf, enrichmentBaseList, null);
  }

  /**
   * TODO JV This method is NEVER called with non-null (nonempty) field name? I would expect that
   * particularly during enrichment (given the history of this method) there may be a need to
   * provide this parameter.
   *
   * Merge entities in a record
   *
   * @param rdf The RDF to enrich
   * @param enrichmentBaseList The information to append
   * @param fieldName The name of the field so that it can be connected to Europeana Proxy
   */
  private void mergeEntities(final RDF rdf, List<EnrichmentBase> enrichmentBaseList,
      String fieldName) {

    // Ensure that there are lists for all four types.
    if (rdf.getAgentList() == null) {
      rdf.setAgentList(new ArrayList<>());
    }
    if (rdf.getConceptList() == null) {
      rdf.setConceptList(new ArrayList<>());
    }
    if (rdf.getPlaceList() == null) {
      rdf.setPlaceList(new ArrayList<>());
    }
    if (rdf.getTimeSpanList() == null) {
      rdf.setTimeSpanList(new ArrayList<>());
    }

    // Go by all input data.
    for (EnrichmentBase enrichmentBase : enrichmentBaseList) {
      convertAndAddEntity(rdf, enrichmentBase, fieldName);
    }
  }
}
