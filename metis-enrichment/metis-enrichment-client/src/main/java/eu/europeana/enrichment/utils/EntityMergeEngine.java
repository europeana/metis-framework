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
import eu.europeana.corelib.definitions.jibx.PlaceOfBirth;
import eu.europeana.corelib.definitions.jibx.PlaceOfDeath;
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
import eu.europeana.enrichment.api.external.model.EnrichmentBaseWrapper;
import eu.europeana.enrichment.api.external.model.Label;
import eu.europeana.enrichment.api.external.model.Part;
import eu.europeana.enrichment.api.external.model.Place;
import eu.europeana.enrichment.api.external.model.Timespan;
import eu.europeana.enrichment.api.external.model.WebResource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

/**
 * Class that contains logic for converting class entity types and/or merging entities to {@link
 * RDF}
 */
public class EntityMergeEngine {

  private static PlaceType convertPlace(Place place) {

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
    return placeType;
  }

  private static AgentType convertAgent(Agent agent) {

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

    // placeOfBirth
    agentType.setPlaceOfBirthList(
            ItemExtractorUtils.extractLabelResources(agent.getPlaceOfBirth(), PlaceOfBirth::new));

    // placeOfDeath
    agentType.setPlaceOfDeathList(
            ItemExtractorUtils.extractLabelResources(agent.getPlaceOfDeath(), PlaceOfDeath::new));

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

    return agentType;
  }

  private static eu.europeana.corelib.definitions.jibx.Concept convertConcept(Concept baseConcept) {

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

    return concept;
  }

  private static TimeSpanType convertTimeSpan(Timespan timespan) {

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
    return timeSpanType;
  }

  private static <I extends EnrichmentBase, T extends AboutType> T convertAndAddEntity(
          I inputEntity, Function<I, T> converter, Supplier<List<T>> listGetter,
          Consumer<List<T>> listSetter) {

    // Check if Entity already exists in the list. If so, return it. We don't overwrite.
    final T existingEntity = Optional.ofNullable(listGetter.get()).map(List::stream)
            .orElseGet(Stream::empty)
            .filter(candidate -> inputEntity.getAbout().equals(candidate.getAbout())).findAny()
            .orElse(null);
    if (existingEntity != null) {
      return existingEntity;
    }

    // Convert and add the new entity.
    final T convertedEntity = converter.apply(inputEntity);
    if (listGetter.get() == null) {
      listSetter.accept(new ArrayList<>());
    }
    listGetter.get().add(convertedEntity);
    return convertedEntity;
  }

  private static void convertAndAddEntity(RDF rdf, EnrichmentBase enrichmentBase,
          Set<EnrichmentFields> proxyLinkTypes) {

    // Convert the entity and add it to the RDF.
    final AboutType entity;
    if (enrichmentBase instanceof Place) {
      entity = convertAndAddEntity((Place) enrichmentBase, EntityMergeEngine::convertPlace,
              rdf::getPlaceList, rdf::setPlaceList);
    } else if (enrichmentBase instanceof Agent) {
      entity = convertAndAddEntity((Agent) enrichmentBase, EntityMergeEngine::convertAgent,
              rdf::getAgentList, rdf::setAgentList);
    } else if (enrichmentBase instanceof Concept) {
      entity = convertAndAddEntity((Concept) enrichmentBase, EntityMergeEngine::convertConcept,
              rdf::getConceptList, rdf::setConceptList);
    } else if (enrichmentBase instanceof Timespan) {
      entity = convertAndAddEntity((Timespan) enrichmentBase, EntityMergeEngine::convertTimeSpan,
              rdf::getTimeSpanList, rdf::setTimeSpanList);
    } else {
      throw new IllegalArgumentException("Unknown entity type: " + enrichmentBase.getClass());
    }

    // Append it to the europeana proxy if needed, regardless of whether entity is new or existing.
    if (!CollectionUtils.isEmpty(proxyLinkTypes)) {
      RdfProxyUtils.appendLinkToEuropeanaProxy(rdf, entity.getAbout(), proxyLinkTypes);
    }
  }

  /**
   * Merge entities in a record.
   *
   * @param rdf The RDF to enrich
   * @param enrichmentBaseWrapperList The information to append
   */
  public void mergeEntities(RDF rdf, List<EnrichmentBaseWrapper> enrichmentBaseWrapperList) {
    for (EnrichmentBaseWrapper enrichmentBaseWrapper : enrichmentBaseWrapperList) {
      final Set<EnrichmentFields> proxyLinkTypes = Optional
              .ofNullable(enrichmentBaseWrapper.getOriginalField()).filter(StringUtils::isNotBlank)
              .map(EnrichmentFields::valueOf).map(Collections::singleton).orElse(null);
      convertAndAddEntity(rdf, enrichmentBaseWrapper.getEnrichmentBase(), proxyLinkTypes);
    }
  }

  /**
   * Merge entities in a record.
   *
   * @param rdf The RDF to enrich.
   * @param contextualEntities The objects to append.
   * @param proxyLinkTypes Lookup of the link types to create in the europeana proxy. The keys are
   * the about values of the entities to add.
   */
  public void mergeEntities(RDF rdf, List<EnrichmentBase> contextualEntities,
          Map<String, Set<EnrichmentFields>> proxyLinkTypes) {
    for (EnrichmentBase entity : contextualEntities) {
      final Set<String> links = getSameAsLinks(entity);
      links.add(entity.getAbout());
      final Set<EnrichmentFields> fields = links.stream().map(proxyLinkTypes::get).filter(Objects::nonNull)
              .flatMap(Set::stream).collect(Collectors.toSet());
      convertAndAddEntity(rdf, entity, fields);
    }
  }


  private static Set<String> getSameAsLinks(EnrichmentBase contextualClass) {
    final List<? extends WebResource> result;
    if (contextualClass instanceof Agent) {
      result = ((Agent) contextualClass).getSameAs();
    } else if (contextualClass instanceof Concept) {
      result= ((Concept)contextualClass).getExactMatch();
    } else if (contextualClass instanceof Place) {
      result = ((Place) contextualClass).getSameAs();
    } else if (contextualClass instanceof Timespan) {
      result = ((Timespan) contextualClass).getSameAs();
    } else {
      result = null;
    }
    return Optional.ofNullable(result).orElseGet(Collections::emptyList).stream()
            .filter(Objects::nonNull).map(WebResource::getResourceUri).filter(StringUtils::isNotBlank)
            .collect(Collectors.toSet());
  }
}
