package eu.europeana.enrichment.service;

import eu.europeana.corelib.solr.entity.AbstractEdmEntityImpl;
import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.corelib.solr.entity.ConceptImpl;
import eu.europeana.corelib.solr.entity.PlaceImpl;
import eu.europeana.corelib.solr.entity.TimespanImpl;
import eu.europeana.enrichment.api.external.model.Agent;
import eu.europeana.enrichment.api.external.model.Concept;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.Label;
import eu.europeana.enrichment.api.external.model.LabelResource;
import eu.europeana.enrichment.api.external.model.Part;
import eu.europeana.enrichment.api.external.model.Place;
import eu.europeana.enrichment.api.external.model.Resource;
import eu.europeana.enrichment.api.external.model.Timespan;
import eu.europeana.enrichment.api.internal.MongoTermList;
import eu.europeana.enrichment.utils.EntityType;
import eu.europeana.enrichment.utils.EntityTypeUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Contains functionality for converting from an incoming Object to a different one.
 */
@Service
public class Converter {

  public List<EnrichmentBase> convert(
      List<? extends MongoTermList<? extends AbstractEdmEntityImpl>> mongoTermLists) {
    final List<EnrichmentBase> enrichmentBases = new ArrayList<>();

    for (MongoTermList<? extends AbstractEdmEntityImpl> mongoTermList : mongoTermLists) {
      enrichmentBases.add(convert(mongoTermList));
    }
    return enrichmentBases;

  }

  public EnrichmentBase convert(MongoTermList<? extends AbstractEdmEntityImpl> mongoTermList) {
    final EnrichmentBase result;
    final EntityType entityType = EntityTypeUtils.getEntityTypeFromClassImpl(mongoTermList.getEntityType());
    if (entityType == null) {
      return null;
    }
    switch (entityType) {
      case AGENT:
        result = convertAgent((AgentImpl) mongoTermList.getRepresentation());
        break;
      case CONCEPT:
        result = convertConcept((ConceptImpl) mongoTermList.getRepresentation());
        break;
      case PLACE:
        result = convertPlace((PlaceImpl) mongoTermList.getRepresentation());
        break;
      case TIMESPAN:
        result = convertTimespan((TimespanImpl) mongoTermList.getRepresentation());
        break;
      default:
        result = null;
        break;
    }
    return result;
  }

  private Timespan convertTimespan(TimespanImpl timespanImpl) {

    Timespan output = new Timespan();

    output.setAbout(timespanImpl.getAbout());
    output.setPrefLabelList(convert(timespanImpl.getPrefLabel()));
    output.setAltLabelList(convert(timespanImpl.getAltLabel()));
    output.setBeginList(convert(timespanImpl.getBegin()));
    output.setEndList(convert(timespanImpl.getEnd()));
    output.setHasPartsList(convertPart(timespanImpl.getDctermsHasPart()));
    output.setHiddenLabel(convert(timespanImpl.getHiddenLabel()));
    output.setIsPartOfList(convertPart(timespanImpl.getIsPartOf()));
    output.setNotes(convert(timespanImpl.getNote()));
    output.setSameAs(convertToPartsList(timespanImpl.getOwlSameAs()));

    return output;
  }

  private Concept convertConcept(ConceptImpl conceptImpl) {
    Concept output = new Concept();

    output.setAbout(conceptImpl.getAbout());
    output.setPrefLabelList(convert(conceptImpl.getPrefLabel()));
    output.setAltLabelList(convert(conceptImpl.getAltLabel()));
    output.setHiddenLabel(convert(conceptImpl.getHiddenLabel()));
    output.setNotation(convert(conceptImpl.getNotation()));
    output.setNotes(convert(conceptImpl.getNote()));
    output.setBroader(convertToResourceList(conceptImpl.getBroader()));
    output.setBroadMatch(convertToResourceList(conceptImpl.getBroadMatch()));
    output.setCloseMatch(convertToResourceList(conceptImpl.getCloseMatch()));
    output.setExactMatch(convertToResourceList(conceptImpl.getExactMatch()));
    output.setInScheme(convertToResourceList(conceptImpl.getInScheme()));
    output.setNarrower(convertToResourceList(conceptImpl.getNarrower()));
    output.setNarrowMatch(convertToResourceList(conceptImpl.getNarrowMatch()));
    output.setRelated(convertToResourceList(conceptImpl.getRelated()));
    output.setRelatedMatch(convertToResourceList(conceptImpl.getRelatedMatch()));

    return output;
  }


  private Place convertPlace(PlaceImpl placeImpl) {

    Place output = new Place();

    output.setAbout(placeImpl.getAbout());
    output.setPrefLabelList(convert(placeImpl.getPrefLabel()));
    output.setAltLabelList(convert(placeImpl.getAltLabel()));

    output.setHasPartsList(convertPart(placeImpl.getDcTermsHasPart()));
    output.setIsPartOfList(convertPart(placeImpl.getIsPartOf()));
    output.setNotes(convert(placeImpl.getNote()));
    output.setSameAs(convertToPartsList(placeImpl.getOwlSameAs()));

    if ((placeImpl.getLatitude() != null && placeImpl.getLatitude() != 0) &&
        (placeImpl.getLongitude() != null && placeImpl.getLongitude() != 0)) {
      output.setLat(placeImpl.getLatitude().toString());
      output.setLon(placeImpl.getLongitude().toString());
    }

    if (placeImpl.getAltitude() != null && placeImpl.getAltitude() != 0) {
      output.setAlt(placeImpl.getAltitude().toString());
    }
    return output;
  }

  private Agent convertAgent(AgentImpl agentImpl) {

    Agent output = new Agent();

    output.setAbout(agentImpl.getAbout());
    output.setPrefLabelList(convert(agentImpl.getPrefLabel()));
    output.setAltLabelList(convert(agentImpl.getAltLabel()));
    output.setHiddenLabel(convert(agentImpl.getHiddenLabel()));
    output.setFoafName(convert(agentImpl.getFoafName()));
    output.setNotes(convert(agentImpl.getNote()));

    output.setBeginList(convert(agentImpl.getBegin()));
    output.setEndList(convert(agentImpl.getEnd()));

    output.setIdentifier(convert(agentImpl.getDcIdentifier()));
    output.setHasMet(convert(agentImpl.getEdmHasMet()));
    output.setBiographicaInformation(convert(agentImpl.getRdaGr2BiographicalInformation()));
    output.setPlaceOfBirth(convertResourceOrLiteral(agentImpl.getRdaGr2PlaceOfBirth()));
    output.setPlaceOfDeath(convertResourceOrLiteral(agentImpl.getRdaGr2PlaceOfDeath()));
    output.setDateOfBirth(convert(agentImpl.getRdaGr2DateOfBirth()));
    output.setDateOfDeath(convert(agentImpl.getRdaGr2DateOfDeath()));
    output.setDateOfEstablishment(convert(agentImpl.getRdaGr2DateOfEstablishment()));
    output.setDateOfTermination(convert(agentImpl.getRdaGr2DateOfTermination()));
    output.setGender(convert(agentImpl.getRdaGr2Gender()));

    output.setDate(convertResourceOrLiteral(agentImpl.getDcDate()));
    output.setProfessionOrOccupation(
        convertResourceOrLiteral(agentImpl.getRdaGr2ProfessionOrOccupation()));

    output.setWasPresentAt(convertToResourceList(agentImpl.getEdmWasPresentAt()));
    output.setSameAs(convertToPartsList(agentImpl.getOwlSameAs()));

    return output;
  }

  private List<Label> convert(Map<String, List<String>> map) {
    List<Label> labels = new ArrayList<>();
    if (map == null) {
      return labels;
    }
    map.forEach((key, entry) ->
        entry.stream().map(
            value -> new Label(key, value)
        ).forEach(labels::add)
    );
    return labels;
  }

  private List<Part> convertPart(Map<String, List<String>> map) {
    List<Part> parts = new ArrayList<>();
    if (map == null) {
      return parts;
    }
    map.forEach((key, entry) ->
        entry.stream().map(Part::new).forEach(parts::add)
    );
    return parts;
  }

  private List<LabelResource> convertResourceOrLiteral(Map<String, List<String>> map) {
    List<LabelResource> parts = new ArrayList<>();
    if (map == null) {
      return parts;
    }
    map.forEach((key, entry) ->
        entry.stream().map(
            value ->
                (isUri(key) ? new LabelResource(key) : new LabelResource(key, value))
        ).forEach(parts::add)
    );
    return parts;
  }

  private List<Resource> convertToResourceList(String[] resources) {
    if (resources == null) {
      return new ArrayList<>();
    }
    return Arrays.stream(resources).map(Resource::new).collect(Collectors.toList());
  }

  private List<Part> convertToPartsList(String[] resources) {
    if (resources == null) {
      return new ArrayList<>();
    }
    return Arrays.stream(resources).map(Part::new).collect(Collectors.toList());
  }

  private boolean isUri(String str) {
    return str.startsWith("http://");
  }
}
