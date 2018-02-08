package eu.europeana.enrichment.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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

/**
 * Created by erikkonijnenburg on 28/07/2017.
 */
public final class EntityMergeEngine {

  private static PlaceType convertAndAddPlace(Place place, List<PlaceType> destination) {

    PlaceType placeType = new PlaceType();

    // about
    if (place.getAbout() != null) {
      placeType.setAbout(place.getAbout());
    } else {
      placeType.setAbout("");
    }

    // alt
    String placeAlt = place.getAlt();
    Alt alt = new Alt();
    if (placeAlt != null) {
      alt.setAlt(Float.valueOf(placeAlt));
    }
    placeType.setAlt(alt);

    // altlabels
    placeType.setAltLabelList(extractLabels(place.getAltLabelList(), AltLabel::new));

    // hasPartList
    placeType.setHasPartList(extractParts(place.getHasPartsList(), HasPart::new));

    // isPartOfList
    placeType.setIsPartOfList(extractParts(place.getIsPartOfList(), IsPartOf::new));

    // lat
    Lat lat = new Lat();
    if (place.getLat() != null) {
      lat.setLat(Float.valueOf(place.getLat()));
    }
    placeType.setLat(lat);

    // _long
    _Long longitude = new _Long();
    if (place.getLon() != null) {
      longitude.setLong(Float.valueOf(place.getLon()));
    }
    placeType.setLong(longitude);

    // noteList
    placeType.setNoteList(extractLabels(place.getNotes(), Note::new));

    // prefLabelList
    placeType.setPrefLabelList(extractLabels(place.getPrefLabelList(), PrefLabel::new));

    // sameAsList
    placeType
        .setSameAList(extractPartsAsResources(place.getSameAs(), SameAs::new, Part::getResource));

    // isNextInSequence: not available

    // Done
    destination.add(placeType);
    return placeType;
  }

  private static AgentType convertAndAddAgent(Agent agent, List<AgentType> destination) {

    AgentType agentType = new AgentType();

    // about
    if (agent.getAbout() != null) {
      agentType.setAbout(agent.getAbout());
    } else {
      agentType.setAbout("");
    }

    // altLabelList
    agentType.setAltLabelList(extractLabels(agent.getAltLabelList(), AltLabel::new));

    // begin
    agentType.setBegin(extractFirstLabel(agent.getBeginList(), Begin::new));

    // biographicalInformation
    agentType.setBiographicalInformation(
        extractFirstLabel(agent.getBiographicaInformation(), BiographicalInformation::new));

    // dateList
    agentType.setDateList(
        extractFirstLabelEmptyResourceToList(agent.getBiographicaInformation(), Date::new));

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
    agentType.setHasMetList(
        extractFirstAsResourceToList(agent.getHasMet(), HasMet::new, Label::getValue));

    // hasPartList: not available

    // identifierList
    agentType.setIdentifierList(extractFirstLabelToList(agent.getIdentifier(), Identifier::new));

    // isPartOfList: not available

    // isRelatedToList
    agentType.setIsRelatedToList(
        extractFirstLabelResourceToList(agent.getIsRelatedTo(), IsRelatedTo::new));

    // nameList: not available

    // noteList
    agentType.setNoteList(extractFirstLabelToList(agent.getNotes(), Note::new));

    // placeofBirth: not available

    // placeofDeath: not available

    // prefLabelList
    agentType.setPrefLabelList(extractFirstLabelToList(agent.getPrefLabelList(), PrefLabel::new));

    // professionOrOccupationList
    agentType.setProfessionOrOccupationList(extractFirstLabelResourceToList(
        agent.getProfessionOrOccupation(), ProfessionOrOccupation::new));

    // sameAsList
    agentType.setSameAList(
        extractFirstAsResourceToList(agent.getSameAs(), SameAs::new, Part::getResource));

    destination.add(agentType);
    return agentType;
  }

  private static eu.europeana.corelib.definitions.jibx.Concept convertAndAddConcept(
      Concept baseConcept, List<eu.europeana.corelib.definitions.jibx.Concept> destination) {
    eu.europeana.corelib.definitions.jibx.Concept concept =
        new eu.europeana.corelib.definitions.jibx.Concept();

    // about
    if (baseConcept.getAbout() != null) {
      concept.setAbout(baseConcept.getAbout());
    } else {
      concept.setAbout("");
    }

    // choiceList
    Choice choice = new Choice();

    choice.setAltLabel(extractFirstLabel(baseConcept.getAltLabelList(), AltLabel::new));

    choice.setBroadMatch(extractFirstResource(baseConcept.getBroadMatch(), BroadMatch::new));

    choice.setBroader(extractFirstResource(baseConcept.getBroader(), Broader::new));

    choice.setCloseMatch(extractFirstResource(baseConcept.getCloseMatch(), CloseMatch::new));

    choice.setExactMatch(extractFirstResource(baseConcept.getExactMatch(), ExactMatch::new));

    choice.setInScheme(extractFirstResource(baseConcept.getInScheme(), InScheme::new));

    choice.setNarrower(extractFirstResource(baseConcept.getNarrower(), Narrower::new));

    choice.setNarrowMatch(extractFirstResource(baseConcept.getNarrowMatch(), NarrowMatch::new));

    choice.setNotation(extractFirstLabel(baseConcept.getNotation(), Notation::new));

    choice.setNote(extractFirstLabel(baseConcept.getNotes(), Note::new));

    choice.setPrefLabel(extractFirstLabel(baseConcept.getPrefLabelList(), PrefLabel::new));

    choice.setRelated(extractFirstResource(baseConcept.getRelated(), Related::new));

    choice.setRelatedMatch(extractFirstResource(baseConcept.getRelatedMatch(), RelatedMatch::new));

    concept.setChoiceList(mutableSingletonList(choice));
    
    destination.add(concept);
    return concept;
  }

  private static TimeSpanType convertAndAddTimespan(Timespan timespan, List<TimeSpanType> destination) {
    TimeSpanType timeSpanType = new TimeSpanType();

    // about
    if (timespan.getAbout() != null) {
      timeSpanType.setAbout(timespan.getAbout());
    } else {
      timeSpanType.setAbout("");
    }

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
    timeSpanType.setSameAList(
        extractPartsAsResources(timespan.getSameAs(), SameAs::new, Part::getResource));

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
      throw new IllegalStateException("Unknown entity type: " + enrichmentBase.getClass());
    }

    // Append it to the proxy if needed.
    if (StringUtils.isNotEmpty(fieldName)) {
      RdfProxyUtils.appendToProxy(rdf, entity, fieldName);
    }
  }

  /**
   * Merge entities in a record. Wrapper for {@link #mergeEntity(RDF, List, String)} without a field
   * name.
   * 
   * @param rdf The RDF to enrich
   * @param enrichmentBaseList The information to append
   * @return An RDF object with the merged entities
   */
  public void mergeEntity(RDF rdf, List<EnrichmentBase> enrichmentBaseList) {
    mergeEntity(rdf, enrichmentBaseList, null);
  }

  /**
   * TODO JOCHEN This method is NEVER called with non-null (nonempty) field name? I would expect
   * that particularly during enrichment (given the history of this method) there may be a need to
   * provide this parameter.
   * 
   * Merge entities in a record
   * 
   * @param rdf The RDF to enrich
   * @param enrichmentBaseList The information to append
   * @param fieldName The name of the field so that it can be connected to Europeana Proxy
   */
  public void mergeEntity(final RDF rdf, List<EnrichmentBase> enrichmentBaseList, String fieldName) {
    
    // Ensure that there are lists for all four types.
    if (rdf.getAgentList() == null) {
      rdf.setAgentList(new ArrayList<AgentType>());
    }
    if (rdf.getConceptList() == null) {
      rdf.setConceptList(new ArrayList<eu.europeana.corelib.definitions.jibx.Concept>());
    }
    if (rdf.getPlaceList() == null) {
      rdf.setPlaceList(new ArrayList<PlaceType>());
    }
    if (rdf.getTimeSpanList() == null) {
      rdf.setTimeSpanList(new ArrayList<TimeSpanType>());
    }
    
    // Go by all input data.
    for (EnrichmentBase enrichmentBase : enrichmentBaseList) {
      convertAndAddEntity(rdf, enrichmentBase, fieldName);
    }
  }

  private static <S, T> List<T> extractItems(List<S> sourceList, Function<S, T> converter) {
    final List<T> result;
    if (sourceList != null && !sourceList.isEmpty()) {
      result = sourceList.stream().filter(Objects::nonNull).map(converter::apply)
          .collect(Collectors.toList());
    } else {
      result = mutableSingletonList(converter.apply(null));
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

  private static <S, T extends ResourceType> List<T> extractPartsAsResources(List<S> sourceList,
      Supplier<T> newInstanceProvider, Function<S, String> resourceProvider) {
    return extractItems(sourceList,
        part -> extractAsResource(part, newInstanceProvider, resourceProvider));
  }

  private static <T extends LiteralType> List<T> extractFirstLabelToList(List<Label> sourceList,
      Supplier<T> newInstanceProvider) {
    return mutableSingletonList(extractFirstLabel(sourceList, newInstanceProvider));
  }

  private static <T extends ResourceOrLiteralType> List<T> extractFirstLabelResourceToList(
      List<LabelResource> sourceList, Supplier<T> newInstanceProvider) {
    final LabelResource input = sourceList.stream().findFirst().orElse(null);
    return mutableSingletonList(extractLabelResource(input, newInstanceProvider));
  }

  private static <T extends ResourceOrLiteralType> List<T> extractFirstLabelEmptyResourceToList(
      List<Label> sourceList, Supplier<T> newInstanceProvider) {
    final Label input = sourceList.stream().findFirst().orElse(null);
    return mutableSingletonList(extractLabelEmptyResource(input, newInstanceProvider));
  }

  private static <S, T extends ResourceType> List<T> extractFirstAsResourceToList(
      List<S> sourceList, Supplier<T> newInstanceProvider, Function<S, String> resourceProvider) {
    final S input = sourceList.stream().findFirst().orElse(null);
    return mutableSingletonList(extractAsResource(input, newInstanceProvider, resourceProvider));
  }

  private static <T extends LiteralType> T extractFirstLabel(List<Label> sourceList,
      Supplier<T> newInstanceProvider) {
    final Label input = sourceList.stream().findFirst().orElse(null);
    return extractLabel(input, newInstanceProvider);
  }

  private static <T extends ResourceType> T extractFirstResource(List<Resource> sourceList,
      Supplier<T> newInstanceProvider) {
    final Resource input = sourceList.stream().findFirst().orElse(null);
    return extractAsResource(input, newInstanceProvider, Resource::getResource);
  }

  private static <T extends LiteralType> T extractLabel(Label label,
      Supplier<T> newInstanceProvider) {
    final T result = newInstanceProvider.get();
    final LiteralType.Lang lang = new LiteralType.Lang();
    lang.setLang(label != null ? label.getLang() : "");
    result.setLang(lang);
    result.setString(label != null ? label.getValue() : "");
    return result;
  }

  private static <T extends ResourceOrLiteralType> T extractLabelEmptyResource(Label label,
      Supplier<T> newInstanceProvider) {
    final T result = newInstanceProvider.get();
    final ResourceOrLiteralType.Lang lang = new ResourceOrLiteralType.Lang();
    lang.setLang(label != null ? label.getLang() : "");
    result.setLang(lang);
    ResourceOrLiteralType.Resource resrc = new ResourceOrLiteralType.Resource();
    resrc.setResource("");
    result.setResource(resrc);
    result.setString(label != null ? label.getValue() : "");
    return result;
  }

  private static <T extends ResourceOrLiteralType> T extractLabelResource(
      LabelResource labelResource, Supplier<T> newInstanceProvider) {
    final T result = newInstanceProvider.get();
    ResourceOrLiteralType.Lang lang = new ResourceOrLiteralType.Lang();
    lang.setLang(labelResource != null ? labelResource.getLang() : "");
    result.setLang(lang);
    ResourceOrLiteralType.Resource resrc = new ResourceOrLiteralType.Resource();
    resrc.setResource(labelResource != null ? labelResource.getResource() : "");
    result.setResource(resrc);
    result.setString(labelResource != null ? labelResource.getValue() : "");
    return result;
  }

  private static <T extends ResourceOrLiteralType> T extractPart(Part part,
      Supplier<T> newInstanceProvider) {
    final T result = newInstanceProvider.get();
    ResourceOrLiteralType.Lang lang = new ResourceOrLiteralType.Lang();
    lang.setLang("");
    result.setLang(lang);
    ResourceOrLiteralType.Resource resrc = new ResourceOrLiteralType.Resource();
    resrc.setResource(part != null ? part.getResource() : "");
    result.setResource(resrc);
    result.setString("");
    return result;
  }

  private static <S, T extends ResourceType> T extractAsResource(S input,
      Supplier<T> newInstanceProvider, Function<S, String> resourceProvider) {
    final T result = newInstanceProvider.get();
    result.setResource(input != null ? resourceProvider.apply(input) : "");
    return result;
  }

  /**
   * Creates a list with one entry. Note that this is different than
   * {@link Collections#singletonList(Object)} because the list created here is mutable.
   * 
   * @param entry The single entry to be contained in the list.
   * @return The list with the entry.
   */
  private static <T> List<T> mutableSingletonList(T entry) {
    final List<T> result = new ArrayList<>();
    result.add(entry);
    return result;
  }
}
