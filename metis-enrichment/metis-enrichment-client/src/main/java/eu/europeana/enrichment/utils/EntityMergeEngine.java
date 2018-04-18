package eu.europeana.enrichment.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
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
import eu.europeana.corelib.definitions.jibx.LiteralType;
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
import eu.europeana.corelib.definitions.jibx.ResourceOrLiteralType;
import eu.europeana.corelib.definitions.jibx.ResourceType;
import eu.europeana.corelib.definitions.jibx.SameAs;
import eu.europeana.corelib.definitions.jibx.TimeSpanType;
import eu.europeana.corelib.definitions.jibx._Long;
import eu.europeana.enrichment.api.external.model.Agent;
import eu.europeana.enrichment.api.external.model.Concept;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.Label;
import eu.europeana.enrichment.api.external.model.LabelResource;
import eu.europeana.enrichment.api.external.model.Part;
import eu.europeana.enrichment.api.external.model.Place;
import eu.europeana.enrichment.api.external.model.Resource;
import eu.europeana.enrichment.api.external.model.Timespan;

public class EntityMergeEngine {

  private static void setAbout(EnrichmentBase source, AboutType destination) {
    destination.setAbout(source.getAbout());
  }

  private static PlaceType convertAndAddPlace(Place place, List<PlaceType> destination) {

    PlaceType placeType = new PlaceType();

    // about
    setAbout(place, placeType);

    // alt
    if (place.getAlt() != null) {
      Alt alt = new Alt();
      alt.setAlt(Float.valueOf(place.getAlt()));
      placeType.setAlt(alt);
    }

    // altlabels
    placeType.setAltLabelList(extractLabels(place.getAltLabelList(), AltLabel::new));

    // hasPartList
    placeType.setHasPartList(extractParts(place.getHasPartsList(), HasPart::new));

    // isPartOfList
    placeType.setIsPartOfList(extractParts(place.getIsPartOfList(), IsPartOf::new));

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
    placeType.setNoteList(extractLabels(place.getNotes(), Note::new));

    // prefLabelList
    placeType.setPrefLabelList(extractLabels(place.getPrefLabelList(), PrefLabel::new));

    // sameAsList
    placeType.setSameAList(extractAsResources(place.getSameAs(), SameAs::new, Part::getResource));

    // isNextInSequence: not available

    // Done
    destination.add(placeType);
    return placeType;
  }

  private static AgentType convertAndAddAgent(Agent agent, List<AgentType> destination) {

    AgentType agentType = new AgentType();

    // about
    setAbout(agent, agentType);

    // altLabelList
    agentType.setAltLabelList(extractLabels(agent.getAltLabelList(), AltLabel::new));

    // begin
    agentType.setBegin(extractFirstLabel(agent.getBeginList(), Begin::new));

    // biographicalInformation
    agentType.setBiographicalInformation(
        extractFirstLabel(agent.getBiographicaInformation(), BiographicalInformation::new));

    // dateList
    agentType.setDateList(extractLabelResources(agent.getDate(), Date::new));

    // dateOfBirth
    agentType.setDateOfBirth(extractFirstLabel(agent.getDateOfBirth(), DateOfBirth::new));

    // dateofDeath
    agentType.setDateOfDeath(extractFirstLabel(agent.getDateOfDeath(), DateOfDeath::new));

    // dateOfEstablishment
    agentType.setDateOfEstablishment(
        extractFirstLabel(agent.getDateOfEstablishment(), DateOfEstablishment::new));

    // dateofTermination
    agentType.setDateOfTermination(
        extractFirstLabel(agent.getDateOfTermination(), DateOfTermination::new));

    // end
    agentType.setEnd(extractFirstLabel(agent.getEndList(), End::new));

    // gender
    agentType.setGender(extractFirstLabel(agent.getGender(), Gender::new));

    // hasMetList
    agentType.setHasMetList(extractAsResources(agent.getHasMet(), HasMet::new, Label::getValue));

    // hasPartList: not available

    // identifierList
    agentType.setIdentifierList(extractLabels(agent.getIdentifier(), Identifier::new));

    // isPartOfList: not available

    // isRelatedToList
    agentType.setIsRelatedToList(extractLabelResources(agent.getIsRelatedTo(), IsRelatedTo::new));

    // nameList: not available

    // noteList
    agentType.setNoteList(extractLabels(agent.getNotes(), Note::new));

    // placeofBirth: not available

    // placeofDeath: not available

    // prefLabelList
    agentType.setPrefLabelList(extractLabels(agent.getPrefLabelList(), PrefLabel::new));

    // professionOrOccupationList
    agentType.setProfessionOrOccupationList(
        extractLabelResources(agent.getProfessionOrOccupation(), ProfessionOrOccupation::new));

    // sameAsList
    agentType.setSameAList(extractAsResources(agent.getSameAs(), SameAs::new, Part::getResource));

    destination.add(agentType);
    return agentType;
  }

  private static eu.europeana.corelib.definitions.jibx.Concept convertAndAddConcept(
      Concept baseConcept, List<eu.europeana.corelib.definitions.jibx.Concept> destination) {
    eu.europeana.corelib.definitions.jibx.Concept concept =
        new eu.europeana.corelib.definitions.jibx.Concept();

    // about
    setAbout(baseConcept, concept);

    // choiceList
    final List<Choice> choices = new ArrayList<>();

    final List<AltLabel> altLabels = extractLabels(baseConcept.getAltLabelList(), AltLabel::new);
    toChoices(altLabels, (choice, content) -> choice.setAltLabel(content), choices);

    final List<BroadMatch> broadMatches =
        extractResources(baseConcept.getBroadMatch(), BroadMatch::new);
    toChoices(broadMatches, (choice, content) -> choice.setBroadMatch(content), choices);

    final List<Broader> broaders = extractResources(baseConcept.getBroader(), Broader::new);
    toChoices(broaders, (choice, content) -> choice.setBroader(content), choices);

    final List<CloseMatch> closeMatches =
        extractResources(baseConcept.getCloseMatch(), CloseMatch::new);
    toChoices(closeMatches, (choice, content) -> choice.setCloseMatch(content), choices);

    final List<ExactMatch> exactMatches =
        extractResources(baseConcept.getExactMatch(), ExactMatch::new);
    toChoices(exactMatches, (choice, content) -> choice.setExactMatch(content), choices);

    final List<InScheme> inSchemes = extractResources(baseConcept.getInScheme(), InScheme::new);
    toChoices(inSchemes, (choice, content) -> choice.setInScheme(content), choices);

    final List<Narrower> narrowers = extractResources(baseConcept.getNarrower(), Narrower::new);
    toChoices(narrowers, (choice, content) -> choice.setNarrower(content), choices);

    final List<NarrowMatch> narrowMatches =
        extractResources(baseConcept.getNarrowMatch(), NarrowMatch::new);
    toChoices(narrowMatches, (choice, content) -> choice.setNarrowMatch(content), choices);

    final List<Notation> notations = extractLabels(baseConcept.getNotation(), Notation::new);
    toChoices(notations, (choice, content) -> choice.setNotation(content), choices);

    final List<Note> notes = extractLabels(baseConcept.getNotes(), Note::new);
    toChoices(notes, (choice, content) -> choice.setNote(content), choices);

    final List<PrefLabel> prefLabels =
        extractLabels(baseConcept.getPrefLabelList(), PrefLabel::new);
    toChoices(prefLabels, (choice, content) -> choice.setPrefLabel(content), choices);

    final List<Related> relateds = extractResources(baseConcept.getRelated(), Related::new);
    toChoices(relateds, (choice, content) -> choice.setRelated(content), choices);

    final List<RelatedMatch> relatedMatches =
        extractResources(baseConcept.getRelatedMatch(), RelatedMatch::new);
    toChoices(relatedMatches, (choice, content) -> choice.setRelatedMatch(content), choices);

    concept.setChoiceList(choices);

    destination.add(concept);
    return concept;
  }

  private static <T> void toChoices(List<T> inputList, BiConsumer<Choice, T> propertySetter,
      List<Choice> destination) {
    for (T input : inputList) {
      final Choice choice = new Choice();
      propertySetter.accept(choice, input);
      destination.add(choice);
    }
  }

  private static TimeSpanType convertAndAddTimespan(Timespan timespan,
      List<TimeSpanType> destination) {
    TimeSpanType timeSpanType = new TimeSpanType();

    // about
    setAbout(timespan, timeSpanType);

    // altLabelList
    timeSpanType.setAltLabelList(extractLabels(timespan.getAltLabelList(), AltLabel::new));

    // begin
    timeSpanType.setBegin(extractFirstLabel(timespan.getBeginList(), Begin::new));

    // end
    timeSpanType.setEnd(extractFirstLabel(timespan.getEndList(), End::new));

    // hasPartList
    timeSpanType.setHasPartList(extractParts(timespan.getHasPartsList(), HasPart::new));

    // isNextInSequence: not available

    // isPartOfList
    timeSpanType.setIsPartOfList(extractParts(timespan.getIsPartOfList(), IsPartOf::new));

    // noteList
    timeSpanType.setNoteList(extractLabels(timespan.getNotes(), Note::new));

    // prefLabelList
    timeSpanType.setPrefLabelList(extractLabels(timespan.getPrefLabelList(), PrefLabel::new));

    // sameAsList
    timeSpanType
        .setSameAList(extractAsResources(timespan.getSameAs(), SameAs::new, Part::getResource));

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
    if (StringUtils.isNotEmpty(fieldName)) {
      RdfProxyUtils.appendToProxy(rdf, entity, fieldName);
    }
  }

  /**
   * Merge entities in a record. Wrapper for {@link #mergeEntities(RDF, List, String)} without a field
   * name.
   * 
   * @param rdf The RDF to enrich
   * @param enrichmentBaseList The information to append
   */
  public void mergeEntities(RDF rdf, List<EnrichmentBase> enrichmentBaseList) {
    mergeEntities(rdf, enrichmentBaseList, null);
  }

  /**
   * TODO JV This method is NEVER called with non-null (nonempty) field name? I would expect
   * that particularly during enrichment (given the history of this method) there may be a need to
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

  private static <S, T> List<T> extractItems(List<S> sourceList, Function<S, T> converter) {
    final List<T> result;
    if (sourceList != null) {
      result = sourceList.stream().filter(Objects::nonNull).map(converter::apply)
          .collect(Collectors.toList());
    } else {
      result = new ArrayList<>();
    }
    return result;
  }

  private static <T extends LiteralType> List<T> extractLabels(List<Label> sourceList,
      Supplier<T> newInstanceProvider) {
    return extractItems(sourceList, label -> extractLabel(label, newInstanceProvider));
  }

  private static <T extends ResourceOrLiteralType> List<T> extractParts(List<Part> sourceList,
      Supplier<T> newInstanceProvider) {
    return extractItems(sourceList, part -> extractPart(part, newInstanceProvider));
  }

  private static <S, T extends ResourceType> List<T> extractAsResources(List<S> sourceList,
      Supplier<T> newInstanceProvider, Function<S, String> resourceProvider) {
    return extractItems(sourceList,
        item -> extractAsResource(item, newInstanceProvider, resourceProvider));
  }

  private static <T extends ResourceType> List<T> extractResources(List<Resource> sourceList,
      Supplier<T> newInstanceProvider) {
    return extractAsResources(sourceList, newInstanceProvider, Resource::getResource);
  }

  private static <T extends ResourceOrLiteralType> List<T> extractLabelResources(
      List<LabelResource> sourceList, Supplier<T> newInstanceProvider) {
    return extractItems(sourceList,
        labelResource -> extractLabelResource(labelResource, newInstanceProvider));
  }

  private static <T extends LiteralType> T extractFirstLabel(List<Label> sourceList,
      Supplier<T> newInstanceProvider) {
    final Label firstLabel;
    if (sourceList == null) {
      firstLabel = null;
    } else {
      firstLabel = sourceList.stream().filter(Objects::nonNull).findFirst().orElse(null);
    }
    return firstLabel != null ? extractLabel(firstLabel, newInstanceProvider) : null;
  }

  private static <T extends LiteralType> T extractLabel(Label label,
      Supplier<T> newInstanceProvider) {
    final T result = newInstanceProvider.get();
    if (label.getLang() != null) {
      final LiteralType.Lang lang = new LiteralType.Lang();
      lang.setLang(label.getLang());
      result.setLang(lang);
    }
    result.setString(label.getValue() != null ? label.getValue() : "");
    return result;
  }

  private static <T extends ResourceOrLiteralType> T extractLabelResource(
      LabelResource labelResource, Supplier<T> newInstanceProvider) {
    final T result = newInstanceProvider.get();
    if (labelResource.getLang() != null) {
      final ResourceOrLiteralType.Lang lang = new ResourceOrLiteralType.Lang();
      lang.setLang(labelResource.getLang());
      result.setLang(lang);
    }
    if (labelResource.getResource() != null) {
      ResourceOrLiteralType.Resource resrc = new ResourceOrLiteralType.Resource();
      resrc.setResource(labelResource.getResource());
      result.setResource(resrc);
    }
    result.setString(labelResource.getValue() != null ? labelResource.getValue() : "");
    return result;
  }

  private static <T extends ResourceOrLiteralType> T extractPart(Part part,
      Supplier<T> newInstanceProvider) {
    final T result = newInstanceProvider.get();
    if (part.getResource() != null) {
      ResourceOrLiteralType.Resource resrc = new ResourceOrLiteralType.Resource();
      resrc.setResource(part.getResource());
      result.setResource(resrc);
    }
    result.setString("");
    return result;
  }

  private static <S, T extends ResourceType> T extractAsResource(S input,
      Supplier<T> newInstanceProvider, Function<S, String> resourceProvider) {
    final T result = newInstanceProvider.get();
    final String inputString = resourceProvider.apply(input);
    result.setResource(inputString != null ? inputString : "");
    return result;
  }
}
