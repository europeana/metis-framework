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
import eu.europeana.corelib.definitions.jibx.EuropeanaType;
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
import eu.europeana.corelib.definitions.jibx.ProxyType;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.definitions.jibx.Related;
import eu.europeana.corelib.definitions.jibx.RelatedMatch;
import eu.europeana.corelib.definitions.jibx.ResourceOrLiteralType;
import eu.europeana.corelib.definitions.jibx.ResourceType;
import eu.europeana.corelib.definitions.jibx.SameAs;
import eu.europeana.corelib.definitions.jibx.TimeSpanType;
import eu.europeana.corelib.definitions.jibx._Long;
import eu.europeana.enrichment.api.external.model.Agent;
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
public final class EntityMergeUtils {

  private EntityMergeUtils() {}

  static ProxyType getProviderProxy(RDF rdf) {
    for (ProxyType proxyType : rdf.getProxyList()) {
      if (proxyType.getEuropeanaProxy() == null
          || !proxyType.getEuropeanaProxy().isEuropeanaProxy()) {
        return proxyType;
      }
    }
    return null;
  }

  private static RDF appendToProxy(RDF rdf, AboutType about, String fieldName) {
    ProxyType europeanaProxy = getEuropeanaProxy(rdf);
    appendToProxy(europeanaProxy, EnrichmentFields.valueOf(fieldName), about.getAbout());
    return replaceProxy(rdf, europeanaProxy);
  }

  private static void appendToProxy(ProxyType europeanaProxy, EnrichmentFields enrichmentFields,
      String about) {
    List<EuropeanaType.Choice> choices = europeanaProxy.getChoiceList();
    choices.add(enrichmentFields.createChoice(about));
    europeanaProxy.setChoiceList(choices);
  }

  private static ProxyType getEuropeanaProxy(RDF rdf) {
    for (ProxyType proxyType : rdf.getProxyList()) {
      if (proxyType.getEuropeanaProxy() != null
          && proxyType.getEuropeanaProxy().isEuropeanaProxy()) {
        return proxyType;
      }
    }
    throw new IllegalArgumentException("Could not find Europeana proxy.");
  }

  private static RDF replaceProxy(RDF rdf, ProxyType europeanaProxy) {
    List<ProxyType> proxyTypeList = new ArrayList<>();
    proxyTypeList.add(europeanaProxy);
    for (ProxyType proxyType : rdf.getProxyList()) {
      if (!StringUtils.equals(proxyType.getAbout(), europeanaProxy.getAbout())) {
        proxyTypeList.add(proxyType);
      }
    }
    rdf.setProxyList(proxyTypeList);
    return rdf;
  }

  private static RDF mergePlace(RDF rdf, Place place, String fieldName) {

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

    if (rdf.getPlaceList() == null) {
      rdf.setPlaceList(new ArrayList<PlaceType>());
    }
    rdf.getPlaceList().add(placeType);

    if (StringUtils.isNotEmpty(fieldName)) {
      return appendToProxy(rdf, placeType, fieldName);
    } else {
      return rdf;
    }
  }

  private static RDF mergeAgent(RDF rdf, Agent agent, String fieldName) {

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

    if (rdf.getAgentList() == null) {
      rdf.setAgentList(new ArrayList<AgentType>());
    }
    rdf.getAgentList().add(agentType);

    if (StringUtils.isNotEmpty(fieldName)) {
      return appendToProxy(rdf, agentType, fieldName);
    } else {
      return rdf;
    }
  }

  private static RDF mergeConcept(RDF rdf,
      eu.europeana.enrichment.api.external.model.Concept baseConcept, String fieldName) {
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
    rdf.setConceptList(mutableSingletonList(concept));

    if (StringUtils.isNotEmpty(fieldName)) {
      return appendToProxy(rdf, concept, fieldName);
    } else {
      return rdf;
    }
  }

  private static RDF mergeTimespan(RDF rdf, Timespan timespan, String fieldName) {
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

    if (rdf.getTimeSpanList() == null) {
      rdf.setTimeSpanList(new ArrayList<TimeSpanType>());
    }
    rdf.getTimeSpanList().add(timeSpanType);

    if (StringUtils.isNotEmpty(fieldName)) {
      return appendToProxy(rdf, timeSpanType, fieldName);
    } else {
      return rdf;
    }
  }

  public static RDF mergeEntity(RDF rdf, List<EnrichmentBase> enrichmentBaseList,
      String fieldName) {
    for (EnrichmentBase enrichmentBase : enrichmentBaseList) {
      if (enrichmentBase.getClass() == Place.class) {
        return mergePlace(rdf, (Place) enrichmentBase, fieldName);
      }
      if (enrichmentBase.getClass() == Agent.class) {
        return mergeAgent(rdf, (Agent) enrichmentBase, fieldName);
      }
      if (enrichmentBase.getClass() == eu.europeana.enrichment.api.external.model.Concept.class) {
        return mergeConcept(rdf,
            (eu.europeana.enrichment.api.external.model.Concept) enrichmentBase, fieldName);
      }
      if (enrichmentBase.getClass() == Timespan.class) {
        return mergeTimespan(rdf, (Timespan) enrichmentBase, fieldName);
      }
    }
    return rdf;
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
