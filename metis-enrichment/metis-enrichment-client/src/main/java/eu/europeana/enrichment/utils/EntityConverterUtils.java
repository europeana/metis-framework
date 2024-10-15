package eu.europeana.enrichment.utils;

import eu.europeana.enrichment.api.external.model.Agent;
import eu.europeana.enrichment.api.external.model.Concept;
import eu.europeana.enrichment.api.external.model.Organization;
import eu.europeana.enrichment.api.external.model.Part;
import eu.europeana.enrichment.api.external.model.Place;
import eu.europeana.enrichment.api.external.model.TimeSpan;
import eu.europeana.metis.schema.jibx.AgentType;
import eu.europeana.metis.schema.jibx.Alt;
import eu.europeana.metis.schema.jibx.AltLabel;
import eu.europeana.metis.schema.jibx.Begin;
import eu.europeana.metis.schema.jibx.BiographicalInformation;
import eu.europeana.metis.schema.jibx.BroadMatch;
import eu.europeana.metis.schema.jibx.Broader;
import eu.europeana.metis.schema.jibx.CloseMatch;
import eu.europeana.metis.schema.jibx.Concept.Choice;
import eu.europeana.metis.schema.jibx.Date;
import eu.europeana.metis.schema.jibx.DateOfBirth;
import eu.europeana.metis.schema.jibx.DateOfDeath;
import eu.europeana.metis.schema.jibx.DateOfEstablishment;
import eu.europeana.metis.schema.jibx.DateOfTermination;
import eu.europeana.metis.schema.jibx.End;
import eu.europeana.metis.schema.jibx.ExactMatch;
import eu.europeana.metis.schema.jibx.Gender;
import eu.europeana.metis.schema.jibx.HasMet;
import eu.europeana.metis.schema.jibx.HasPart;
import eu.europeana.metis.schema.jibx.HiddenLabel;
import eu.europeana.metis.schema.jibx.Identifier;
import eu.europeana.metis.schema.jibx.InScheme;
import eu.europeana.metis.schema.jibx.IsNextInSequence;
import eu.europeana.metis.schema.jibx.IsPartOf;
import eu.europeana.metis.schema.jibx.IsRelatedTo;
import eu.europeana.metis.schema.jibx.Lat;
import eu.europeana.metis.schema.jibx.NarrowMatch;
import eu.europeana.metis.schema.jibx.Narrower;
import eu.europeana.metis.schema.jibx.Notation;
import eu.europeana.metis.schema.jibx.Note;
import eu.europeana.metis.schema.jibx.PlaceOfBirth;
import eu.europeana.metis.schema.jibx.PlaceOfDeath;
import eu.europeana.metis.schema.jibx.PlaceType;
import eu.europeana.metis.schema.jibx.PrefLabel;
import eu.europeana.metis.schema.jibx.ProfessionOrOccupation;
import eu.europeana.metis.schema.jibx.Related;
import eu.europeana.metis.schema.jibx.RelatedMatch;
import eu.europeana.metis.schema.jibx.SameAs;
import eu.europeana.metis.schema.jibx.TimeSpanType;
import eu.europeana.metis.schema.jibx._Long;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains methods for converting enrichment entity classes to jibx entity classes
 */
public final class EntityConverterUtils {

  private EntityConverterUtils() {
  }

  static eu.europeana.metis.schema.jibx.Organization convertOrganization(
      Organization organization) {
    final eu.europeana.metis.schema.jibx.Organization organizationType = new eu.europeana.metis.schema.jibx.Organization();
    organizationType.setAbout(organization.getAbout());
    organizationType.setPrefLabelList(
        ItemExtractorUtils.extractLabels(organization.getPrefLabelList(), PrefLabel::new));
    return organizationType;
  }

  static TimeSpanType convertTimeSpan(TimeSpan timespan) {

    TimeSpanType timeSpanType = new TimeSpanType();

    ItemExtractorUtils.setAbout(timespan, timeSpanType);
    timeSpanType.setAltLabelList(
        ItemExtractorUtils.extractLabels(timespan.getAltLabelList(), AltLabel::new));
    timeSpanType.setBegin(ItemExtractorUtils.extractLabel(timespan.getBegin(), Begin::new));
    timeSpanType.setEnd(ItemExtractorUtils.extractLabel(timespan.getEnd(), End::new));
    timeSpanType.setHasPartList(
        ItemExtractorUtils.extractLabelResources(timespan.getHasPartsList(), HasPart::new));
    if (timespan.getIsNextInSequence() != null) {
      timeSpanType.setIsNextInSequence(ItemExtractorUtils
          .extractAsResource(timespan.getIsNextInSequence(), IsNextInSequence::new,
              Part::getResource));
    }
    if (timespan.getIsPartOf() != null) {
      timeSpanType.setIsPartOfList(
          ItemExtractorUtils.extractLabelResources(timespan.getIsPartOf(), IsPartOf::new));
    }
    timeSpanType.setNoteList(ItemExtractorUtils.extractLabels(timespan.getNotes(), Note::new));
    timeSpanType.setPrefLabelList(
        ItemExtractorUtils.extractLabels(timespan.getPrefLabelList(), PrefLabel::new));
    timeSpanType
        .setSameAList(ItemExtractorUtils.extractResources(timespan.getSameAs(), SameAs::new));
    timeSpanType.setHiddenLabelList(
        ItemExtractorUtils.extractLabels(timespan.getHiddenLabel(), HiddenLabel::new));

    return timeSpanType;
  }

  static eu.europeana.metis.schema.jibx.Concept convertConcept(Concept baseConcept) {

    eu.europeana.metis.schema.jibx.Concept concept = new eu.europeana.metis.schema.jibx.Concept();

    ItemExtractorUtils.setAbout(baseConcept, concept);
    final List<Choice> choices = new ArrayList<>();

    final List<AltLabel> altLabels = ItemExtractorUtils
        .extractLabels(baseConcept.getAltLabelList(), AltLabel::new);
    ItemExtractorUtils.toChoices(altLabels, Choice::setAltLabel, choices);

    final List<BroadMatch> broadMatches = ItemExtractorUtils
        .extractResources(baseConcept.getBroadMatch(), BroadMatch::new);
    ItemExtractorUtils.toChoices(broadMatches, Choice::setBroadMatch, choices);

    final List<Broader> broaders = ItemExtractorUtils
        .extractResources(baseConcept.getBroader(), Broader::new);
    ItemExtractorUtils.toChoices(broaders, Choice::setBroader, choices);

    final List<CloseMatch> closeMatches = ItemExtractorUtils
        .extractResources(baseConcept.getCloseMatch(), CloseMatch::new);
    ItemExtractorUtils.toChoices(closeMatches, Choice::setCloseMatch, choices);

    final List<ExactMatch> exactMatches = ItemExtractorUtils
        .extractResources(baseConcept.getExactMatch(), ExactMatch::new);
    ItemExtractorUtils.toChoices(exactMatches, Choice::setExactMatch, choices);

    final List<InScheme> inSchemes = ItemExtractorUtils
        .extractResources(baseConcept.getInScheme(), InScheme::new);
    ItemExtractorUtils.toChoices(inSchemes, Choice::setInScheme, choices);

    final List<Narrower> narrowers = ItemExtractorUtils
        .extractResources(baseConcept.getNarrower(), Narrower::new);
    ItemExtractorUtils.toChoices(narrowers, Choice::setNarrower, choices);

    final List<NarrowMatch> narrowMatches = ItemExtractorUtils
        .extractResources(baseConcept.getNarrowMatch(), NarrowMatch::new);
    ItemExtractorUtils.toChoices(narrowMatches, Choice::setNarrowMatch, choices);

    final List<Notation> notations = ItemExtractorUtils
        .extractLabels(baseConcept.getNotation(), Notation::new);
    ItemExtractorUtils.toChoices(notations, Choice::setNotation, choices);

    final List<Note> notes = ItemExtractorUtils.extractLabels(baseConcept.getNotes(), Note::new);
    ItemExtractorUtils.toChoices(notes, Choice::setNote, choices);

    final List<PrefLabel> prefLabels = ItemExtractorUtils
        .extractLabels(baseConcept.getPrefLabelList(), PrefLabel::new);
    ItemExtractorUtils.toChoices(prefLabels, Choice::setPrefLabel, choices);

    final List<Related> relateds = ItemExtractorUtils
        .extractResources(baseConcept.getRelated(), Related::new);
    ItemExtractorUtils.toChoices(relateds, Choice::setRelated, choices);

    final List<RelatedMatch> relatedMatches = ItemExtractorUtils
        .extractResources(baseConcept.getRelatedMatch(), RelatedMatch::new);
    ItemExtractorUtils.toChoices(relatedMatches, Choice::setRelatedMatch, choices);

    concept.setChoiceList(choices);

    return concept;
  }

  static AgentType convertAgent(Agent agent) {

    AgentType agentType = new AgentType();

    ItemExtractorUtils.setAbout(agent, agentType);
    agentType.setAltLabelList(ItemExtractorUtils.extractLabels(agent.getAltLabelList(), AltLabel::new));
    agentType.setBegin(ItemExtractorUtils.extractFirstLabel(agent.getBeginList(), Begin::new));
    agentType.setBiographicalInformationList(ItemExtractorUtils.extractLabelResources(agent.getBiographicalInformation(),
        BiographicalInformation::new));
    agentType.setProfessionOrOccupationList(
        ItemExtractorUtils.extractLabelResources(agent.getProfessionOrOccupation(), ProfessionOrOccupation::new));
    agentType.setDateList(ItemExtractorUtils.extractLabelResources(agent.getDate(), Date::new));
    agentType.setPlaceOfBirthList(
        ItemExtractorUtils.extractLabelResources(agent.getPlaceOfBirth(), PlaceOfBirth::new));
    agentType.setPlaceOfDeathList(
        ItemExtractorUtils.extractLabelResources(agent.getPlaceOfDeath(), PlaceOfDeath::new));
    agentType.setDateOfBirth(ItemExtractorUtils.extractFirstLabel(agent.getDateOfBirth(), DateOfBirth::new));
    agentType.setDateOfDeath(ItemExtractorUtils.extractFirstLabel(agent.getDateOfDeath(), DateOfDeath::new));
    agentType.setDateOfEstablishment(ItemExtractorUtils
        .extractFirstLabel(agent.getDateOfEstablishment(), DateOfEstablishment::new));
    agentType.setDateOfTermination(
        ItemExtractorUtils.extractFirstLabel(agent.getDateOfTermination(), DateOfTermination::new));
    agentType.setEnd(ItemExtractorUtils.extractFirstLabel(agent.getEndList(), End::new));
    agentType.setGender(ItemExtractorUtils.extractFirstLabel(agent.getGender(), Gender::new));
    agentType.setHasMetList(ItemExtractorUtils.extractResources(agent.getHasMet(), HasMet::new));

    // hasPartList: not available

    agentType.setIdentifierList(
        ItemExtractorUtils.extractLabels(agent.getIdentifier(), Identifier::new));

    // isPartOfList: not available

    agentType.setIsRelatedToList(
        ItemExtractorUtils.extractLabelResources(agent.getIsRelatedTo(), IsRelatedTo::new));

    // nameList: not available

    agentType.setNoteList(ItemExtractorUtils.extractLabels(agent.getNotes(), Note::new));
    agentType.setPrefLabelList(
        ItemExtractorUtils.extractLabels(agent.getPrefLabelList(), PrefLabel::new));
    agentType.setProfessionOrOccupationList(ItemExtractorUtils
        .extractLabelResources(agent.getProfessionOrOccupation(), ProfessionOrOccupation::new));
    agentType.setSameAList(ItemExtractorUtils.extractResources(agent.getSameAs(), SameAs::new));

    return agentType;
  }

  static PlaceType convertPlace(Place place) {

    PlaceType placeType = new PlaceType();

    ItemExtractorUtils.setAbout(place, placeType);
    if (place.getAlt() != null) {
      Alt alt = new Alt();
      alt.setAlt(Float.valueOf(place.getAlt()));
      placeType.setAlt(alt);
    }
    placeType.setAltLabelList(ItemExtractorUtils.extractLabels(place.getAltLabelList(), AltLabel::new));
    placeType.setHasPartList(ItemExtractorUtils.extractLabelResources(place.getHasPartsList(), HasPart::new));
    if (place.getIsPartOf() != null) {
      placeType.setIsPartOfList(
          ItemExtractorUtils.extractLabelResources(place.getIsPartOf(), IsPartOf::new));
    }
    if (place.getLat() != null) {
      Lat lat = new Lat();
      lat.setLat(Float.valueOf(place.getLat()));
      placeType.setLat(lat);
    }
    if (place.getLon() != null) {
      _Long longitude = new _Long();
      longitude.setLong(Float.valueOf(place.getLon()));
      placeType.setLong(longitude);
    }
    placeType.setNoteList(ItemExtractorUtils.extractLabels(place.getNotes(), Note::new));
    placeType.setPrefLabelList(ItemExtractorUtils.extractLabels(place.getPrefLabelList(), PrefLabel::new));
    placeType.setSameAList(ItemExtractorUtils.extractResources(place.getSameAs(), SameAs::new));

    // isNextInSequence: not available
    return placeType;
  }
}
