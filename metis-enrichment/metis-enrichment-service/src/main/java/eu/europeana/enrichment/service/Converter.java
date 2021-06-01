package eu.europeana.enrichment.service;

import eu.europeana.enrichment.api.external.model.VcardAddress;
import eu.europeana.enrichment.api.external.model.VcardAddresses;

import eu.europeana.enrichment.api.external.model.Agent;
import eu.europeana.enrichment.api.external.model.Concept;
import eu.europeana.enrichment.api.external.model.Organization;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.Label;
import eu.europeana.enrichment.api.external.model.LabelResource;
import eu.europeana.enrichment.api.external.model.Part;
import eu.europeana.enrichment.api.external.model.Place;
import eu.europeana.enrichment.api.external.model.Resource;
import eu.europeana.enrichment.api.external.model.TimeSpan;
import eu.europeana.enrichment.internal.model.Address;
import eu.europeana.enrichment.internal.model.AgentEnrichmentEntity;
import eu.europeana.enrichment.internal.model.ConceptEnrichmentEntity;
import eu.europeana.enrichment.internal.model.EnrichmentTerm;
import eu.europeana.enrichment.internal.model.OrganizationEnrichmentEntity;
import eu.europeana.enrichment.internal.model.PlaceEnrichmentEntity;
import eu.europeana.enrichment.internal.model.TimespanEnrichmentEntity;
import eu.europeana.enrichment.utils.EntityType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Contains functionality for converting from an incoming Object to a different one.
 */
public final class Converter {

  private Converter() {
  }

  public static List<EnrichmentBase> convert(List<EnrichmentTerm> enrichmentTerms) {
    return enrichmentTerms.stream().map(Converter::convert).collect(Collectors.toList());
  }

  public static EnrichmentBase convert(EnrichmentTerm enrichmentTerm) {
    final EntityType entityType = enrichmentTerm.getEntityType();
    if (entityType == null) {
      return null;
    }
    final EnrichmentBase result;
    switch (entityType) {
      case AGENT:
        result = convertAgent((AgentEnrichmentEntity) enrichmentTerm.getEnrichmentEntity());
        break;
      case CONCEPT:
        result = convertConcept((ConceptEnrichmentEntity) enrichmentTerm.getEnrichmentEntity());
        break;
      case PLACE:
        result = convertPlace((PlaceEnrichmentEntity) enrichmentTerm.getEnrichmentEntity());
        break;
      case TIMESPAN:
        result = convertTimespan((TimespanEnrichmentEntity) enrichmentTerm.getEnrichmentEntity());
        break;
      case ORGANIZATION:
        result = convertOrganization(
            (OrganizationEnrichmentEntity) enrichmentTerm.getEnrichmentEntity());
        break;
      default:
        result = null;
        break;
    }
    return result;
  }

  private static TimeSpan convertTimespan(TimespanEnrichmentEntity timespanEnrichmentEntity) {

    TimeSpan output = new TimeSpan();

    output.setAbout(timespanEnrichmentEntity.getAbout());
    output.setPrefLabelList(convert(timespanEnrichmentEntity.getPrefLabel()));
    output.setAltLabelList(convert(timespanEnrichmentEntity.getAltLabel()));
    output.setBegin(convert(timespanEnrichmentEntity.getBegin()).get(0));
    output.setEnd(convert(timespanEnrichmentEntity.getEnd()).get(0));
    output.setHasPartsList(convertResourceOrLiteral(timespanEnrichmentEntity.getDctermsHasPart()));
    output.setHiddenLabel(convert(timespanEnrichmentEntity.getHiddenLabel()));
    output.setNotes(convert(timespanEnrichmentEntity.getNote()));
    output.setSameAs(convertToPartsList(timespanEnrichmentEntity.getOwlSameAs()));

    if (StringUtils.isNotBlank(timespanEnrichmentEntity.getIsPartOf())) {
      output.setIsPartOf(List.of(new LabelResource(timespanEnrichmentEntity.getIsPartOf())));
    }

    if (StringUtils.isNotBlank(timespanEnrichmentEntity.getIsNextInSequence())) {
      output.setIsNextInSequence(new Part(timespanEnrichmentEntity.getIsNextInSequence()));
    }

    return output;
  }

  private static Concept convertConcept(ConceptEnrichmentEntity conceptEnrichmentEntity) {
    Concept output = new Concept();

    output.setAbout(conceptEnrichmentEntity.getAbout());
    output.setPrefLabelList(convert(conceptEnrichmentEntity.getPrefLabel()));
    output.setAltLabelList(convert(conceptEnrichmentEntity.getAltLabel()));
    output.setHiddenLabel(convert(conceptEnrichmentEntity.getHiddenLabel()));
    output.setNotation(convert(conceptEnrichmentEntity.getNotation()));
    output.setNotes(convert(conceptEnrichmentEntity.getNote()));
    output.setBroader(convertToResourceList(conceptEnrichmentEntity.getBroader()));
    output.setBroadMatch(convertToResourceList(conceptEnrichmentEntity.getBroadMatch()));
    output.setCloseMatch(convertToResourceList(conceptEnrichmentEntity.getCloseMatch()));
    output.setExactMatch(convertToResourceList(conceptEnrichmentEntity.getExactMatch()));
    output.setInScheme(convertToResourceList(conceptEnrichmentEntity.getInScheme()));
    output.setNarrower(convertToResourceList(conceptEnrichmentEntity.getNarrower()));
    output.setNarrowMatch(convertToResourceList(conceptEnrichmentEntity.getNarrowMatch()));
    output.setRelated(convertToResourceList(conceptEnrichmentEntity.getRelated()));
    output.setRelatedMatch(convertToResourceList(conceptEnrichmentEntity.getRelatedMatch()));

    return output;
  }


  private static Place convertPlace(PlaceEnrichmentEntity placeEnrichmentEntity) {

    Place output = new Place();

    output.setAbout(placeEnrichmentEntity.getAbout());
    output.setPrefLabelList(convert(placeEnrichmentEntity.getPrefLabel()));
    output.setAltLabelList(convert(placeEnrichmentEntity.getAltLabel()));

    output.setHasPartsList(convertResourceOrLiteral(placeEnrichmentEntity.getDcTermsHasPart()));
    output.setNotes(convert(placeEnrichmentEntity.getNote()));
    output.setSameAs(convertToPartsList(placeEnrichmentEntity.getOwlSameAs()));

    if (StringUtils.isNotBlank(placeEnrichmentEntity.getIsPartOf())) {
      output.setIsPartOf(List.of(new LabelResource(placeEnrichmentEntity.getIsPartOf())));
    }
    if ((placeEnrichmentEntity.getLatitude() != null && placeEnrichmentEntity.getLatitude() != 0)
        && (placeEnrichmentEntity.getLongitude() != null
        && placeEnrichmentEntity.getLongitude() != 0)) {
      output.setLat(placeEnrichmentEntity.getLatitude().toString());
      output.setLon(placeEnrichmentEntity.getLongitude().toString());
    }

    if (placeEnrichmentEntity.getAltitude() != null && placeEnrichmentEntity.getAltitude() != 0) {
      output.setAlt(placeEnrichmentEntity.getAltitude().toString());
    }
    return output;
  }

  private static Agent convertAgent(AgentEnrichmentEntity agentEntityEnrichment) {

    Agent output = new Agent();

    output.setAbout(agentEntityEnrichment.getAbout());
    output.setPrefLabelList(convert(agentEntityEnrichment.getPrefLabel()));
    output.setAltLabelList(convert(agentEntityEnrichment.getAltLabel()));
    output.setHiddenLabel(convert(agentEntityEnrichment.getHiddenLabel()));
    output.setFoafName(convert(agentEntityEnrichment.getFoafName()));
    output.setNotes(convert(agentEntityEnrichment.getNote()));

    output.setBeginList(convert(agentEntityEnrichment.getBegin()));
    output.setEndList(convert(agentEntityEnrichment.getEnd()));

    output.setIdentifier(convert(agentEntityEnrichment.getDcIdentifier()));
    output.setHasMet(convertToResourceList(agentEntityEnrichment.getEdmHasMet()));
    output.setBiographicalInformation(
        convertResourceOrLiteral(agentEntityEnrichment.getRdaGr2BiographicalInformation()));
    output.setPlaceOfBirth(convertResourceOrLiteral(agentEntityEnrichment.getRdaGr2PlaceOfBirth()));
    output.setPlaceOfDeath(convertResourceOrLiteral(agentEntityEnrichment.getRdaGr2PlaceOfDeath()));
    output.setDateOfBirth(convert(agentEntityEnrichment.getRdaGr2DateOfBirth()));
    output.setDateOfDeath(convert(agentEntityEnrichment.getRdaGr2DateOfDeath()));
    output.setDateOfEstablishment(convert(agentEntityEnrichment.getRdaGr2DateOfEstablishment()));
    output.setDateOfTermination(convert(agentEntityEnrichment.getRdaGr2DateOfTermination()));
    output.setGender(convert(agentEntityEnrichment.getRdaGr2Gender()));

    output.setDate(convertResourceOrLiteral(agentEntityEnrichment.getDcDate()));
    output.setProfessionOrOccupation(
        convertResourceOrLiteral(agentEntityEnrichment.getRdaGr2ProfessionOrOccupation()));

    output.setWasPresentAt(convertToResourceList(agentEntityEnrichment.getEdmWasPresentAt()));
    output.setSameAs(convertToPartsList(agentEntityEnrichment.getOwlSameAs()));

    return output;
  }

  private static Organization convertOrganization(
      OrganizationEnrichmentEntity organizationEnrichmentEntity) {
    Organization output = new Organization();

    output.setAbout(organizationEnrichmentEntity.getAbout());
    output.setPrefLabelList(convert(organizationEnrichmentEntity.getPrefLabel()));
    output.setAltLabelList(convert(organizationEnrichmentEntity.getAltLabel()));
    output.setNotes(convert(organizationEnrichmentEntity.getNote()));
    output.setSameAs(convertToPartsList(organizationEnrichmentEntity.getOwlSameAs()));
    if (MapUtils.isNotEmpty(organizationEnrichmentEntity.getEdmCountry())) {
      output.setCountry(
          organizationEnrichmentEntity.getEdmCountry().entrySet().iterator().next().getValue());
    }
    if (CollectionUtils.isNotEmpty(organizationEnrichmentEntity.getFoafPhone())) {
      output.setPhone(organizationEnrichmentEntity.getFoafPhone().get(0));
    }
    if (CollectionUtils.isNotEmpty(organizationEnrichmentEntity.getFoafMbox())) {
      output.setMbox(organizationEnrichmentEntity.getFoafMbox().get(0));
    }
    output.setHomepage(new Resource(organizationEnrichmentEntity.getFoafHomepage()));
    output.setLogo(new Resource(organizationEnrichmentEntity.getFoafLogo()));
    output.setDepiction(new Resource(organizationEnrichmentEntity.getFoafDepiction()));
    output.setAcronyms(convert(organizationEnrichmentEntity.getEdmAcronym()));
    output.setDescriptions(convertMapToLabels(organizationEnrichmentEntity.getDcDescription()));

    final Address address = organizationEnrichmentEntity.getAddress();
    final VcardAddress vcardAddress = new VcardAddress();
    vcardAddress.setCountryName(address.getVcardCountryName());
    vcardAddress.setLocality(address.getVcardLocality());
    vcardAddress.setPostalCode(address.getVcardPostalCode());
    vcardAddress.setPostOfficeBox(address.getVcardPostOfficeBox());
    vcardAddress.setStreetAddress(address.getVcardStreetAddress());
    vcardAddress.setHasGeo(new Resource(address.getVcardHasGeo()));
    final VcardAddresses vcardAddresses = new VcardAddresses();
    vcardAddresses.setVcardAddressesList(List.of(vcardAddress));
    output.setHasAddress(vcardAddresses);

    return output;
  }

  static EnrichmentTerm organizationImplToEnrichmentTerm(
      OrganizationEnrichmentEntity organizationEnrichmentEntity, Date created, Date updated) {
    final EnrichmentTerm enrichmentTerm = new EnrichmentTerm();
    enrichmentTerm.setEnrichmentEntity(organizationEnrichmentEntity);
    enrichmentTerm.setEntityType(EntityType.ORGANIZATION);
    enrichmentTerm.setCreated(Objects.requireNonNullElseGet(created, Date::new));
    enrichmentTerm.setUpdated(updated);

    return enrichmentTerm;
  }

  private static List<Label> convert(Map<String, List<String>> map) {
    List<Label> labels = new ArrayList<>();
    if (map == null) {
      return labels;
    }
    map.forEach(
        (key, entry) -> entry.stream().map(value -> new Label(key, value)).forEach(labels::add));
    return labels;
  }

  private static List<Label> convertMapToLabels(Map<String, String> map) {
    List<Label> labels = new ArrayList<>();
    if (map == null) {
      return labels;
    }
    map.forEach((key, value) -> labels.add(new Label(key, value)));
    return labels;
  }

  private static List<LabelResource> convertResourceOrLiteral(Map<String, List<String>> map) {
    List<LabelResource> parts = new ArrayList<>();
    if (map == null) {
      return parts;
    }
    map.forEach((key, entry) -> entry.stream()
        .map(value -> (isUri(key) ? new LabelResource(key) : new LabelResource(key, value)))
        .forEach(parts::add));
    return parts;
  }

  private static List<Resource> convertToResourceList(String[] resources) {
    if (resources == null) {
      return new ArrayList<>();
    }
    return Arrays.stream(resources).map(Resource::new).collect(Collectors.toList());
  }

  private static List<Part> convertToPartsList(List<String> resources) {
    if (resources == null) {
      return new ArrayList<>();
    }
    return resources.stream().map(Part::new).collect(Collectors.toList());
  }

  private static boolean isUri(String str) {
    return str.startsWith("http://");
  }
}
