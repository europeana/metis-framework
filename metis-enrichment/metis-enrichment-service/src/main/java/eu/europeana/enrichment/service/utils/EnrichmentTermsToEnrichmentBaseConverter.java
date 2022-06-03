package eu.europeana.enrichment.service.utils;

import static eu.europeana.enrichment.utils.EntityValuesConverter.convertListToPart;
import static eu.europeana.enrichment.utils.EntityValuesConverter.convertMapToLabels;
import static eu.europeana.enrichment.utils.EntityValuesConverter.convertMultilingualMapToLabel;
import static eu.europeana.enrichment.utils.EntityValuesConverter.convertResourceOrLiteral;
import static eu.europeana.enrichment.utils.EntityValuesConverter.convertToResourceList;

import eu.europeana.enrichment.api.external.model.Agent;
import eu.europeana.enrichment.api.external.model.Concept;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.LabelInfo;
import eu.europeana.enrichment.api.external.model.LabelResource;
import eu.europeana.enrichment.api.external.model.Organization;
import eu.europeana.enrichment.api.external.model.Part;
import eu.europeana.enrichment.api.external.model.Place;
import eu.europeana.enrichment.api.external.model.Resource;
import eu.europeana.enrichment.api.external.model.TimeSpan;
import eu.europeana.enrichment.api.external.model.VcardAddress;
import eu.europeana.enrichment.api.external.model.VcardAddresses;
import eu.europeana.enrichment.internal.model.AbstractEnrichmentEntity;
import eu.europeana.enrichment.internal.model.Address;
import eu.europeana.enrichment.internal.model.AgentEnrichmentEntity;
import eu.europeana.enrichment.internal.model.ConceptEnrichmentEntity;
import eu.europeana.enrichment.internal.model.EnrichmentTerm;
import eu.europeana.enrichment.internal.model.OrganizationEnrichmentEntity;
import eu.europeana.enrichment.internal.model.PlaceEnrichmentEntity;
import eu.europeana.enrichment.internal.model.TimespanEnrichmentEntity;
import eu.europeana.enrichment.utils.EntityType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Contains functionality for converting from an incoming Object to a different one.
 */
public final class EnrichmentTermsToEnrichmentBaseConverter {

  private EnrichmentTermsToEnrichmentBaseConverter() {
  }

  /**
   * Converter from list of {@link EnrichmentTerm} to list of {@link EnrichmentBase}.
   *
   * @param enrichmentTerms the enrichment terms to convert
   * @return the converted enrichment bases
   */
  public static List<EnrichmentBase> convert(List<EnrichmentTerm> enrichmentTerms) {
    return enrichmentTerms.stream().map(EnrichmentTermsToEnrichmentBaseConverter::convert).collect(Collectors.toList());
  }

  /**
   * Converter from {@link EnrichmentTerm} to {@link EnrichmentBase}.
   * @param enrichmentTerm the enrichment term to convert
   * @return the converted enrichment base
   */
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
    output.setPrefLabelList(convertMultilingualMapToLabel(timespanEnrichmentEntity.getPrefLabel()));
    output.setAltLabelList(convertMultilingualMapToLabel(timespanEnrichmentEntity.getAltLabel()));
    output.setBegin(convertMultilingualMapToLabel(timespanEnrichmentEntity.getBegin()).get(0));
    output.setEnd(convertMultilingualMapToLabel(timespanEnrichmentEntity.getEnd()).get(0));
    output.setHasPartsList(convertResourceOrLiteral(timespanEnrichmentEntity.getDctermsHasPart()));
    output.setHiddenLabel(convertMultilingualMapToLabel(timespanEnrichmentEntity.getHiddenLabel()));
    output.setNotes(convertMultilingualMapToLabel(timespanEnrichmentEntity.getNote()));
    output.setSameAs(convertListToPart(timespanEnrichmentEntity.getOwlSameAs()));

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
    output.setPrefLabelList(convertMultilingualMapToLabel(conceptEnrichmentEntity.getPrefLabel()));
    output.setAltLabelList(convertMultilingualMapToLabel(conceptEnrichmentEntity.getAltLabel()));
    output.setHiddenLabel(convertMultilingualMapToLabel(conceptEnrichmentEntity.getHiddenLabel()));
    output.setNotation(convertMultilingualMapToLabel(conceptEnrichmentEntity.getNotation()));
    output.setNotes(convertMultilingualMapToLabel(conceptEnrichmentEntity.getNote()));
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
    output.setPrefLabelList(convertMultilingualMapToLabel(placeEnrichmentEntity.getPrefLabel()));
    output.setAltLabelList(convertMultilingualMapToLabel(placeEnrichmentEntity.getAltLabel()));

    output.setHasPartsList(convertResourceOrLiteral(placeEnrichmentEntity.getDcTermsHasPart()));
    output.setNotes(convertMultilingualMapToLabel(placeEnrichmentEntity.getNote()));
    output.setSameAs(convertListToPart(placeEnrichmentEntity.getOwlSameAs()));

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
    output.setPrefLabelList(convertMultilingualMapToLabel(agentEntityEnrichment.getPrefLabel()));
    output.setAltLabelList(convertMultilingualMapToLabel(agentEntityEnrichment.getAltLabel()));
    output.setHiddenLabel(convertMultilingualMapToLabel(agentEntityEnrichment.getHiddenLabel()));
    output.setFoafName(convertMultilingualMapToLabel(agentEntityEnrichment.getFoafName()));
    output.setNotes(convertMultilingualMapToLabel(agentEntityEnrichment.getNote()));

    output.setBeginList(convertMultilingualMapToLabel(agentEntityEnrichment.getBegin()));
    output.setEndList(convertMultilingualMapToLabel(agentEntityEnrichment.getEnd()));

    output.setIdentifier(convertMultilingualMapToLabel(agentEntityEnrichment.getDcIdentifier()));
    output.setHasMet(convertToResourceList(agentEntityEnrichment.getEdmHasMet()));
    output.setBiographicalInformation(
        convertResourceOrLiteral(agentEntityEnrichment.getRdaGr2BiographicalInformation()));
    output.setPlaceOfBirth(convertResourceOrLiteral(agentEntityEnrichment.getRdaGr2PlaceOfBirth()));
    output.setPlaceOfDeath(convertResourceOrLiteral(agentEntityEnrichment.getRdaGr2PlaceOfDeath()));
    output.setDateOfBirth(convertMultilingualMapToLabel(agentEntityEnrichment.getRdaGr2DateOfBirth()));
    output.setDateOfDeath(convertMultilingualMapToLabel(agentEntityEnrichment.getRdaGr2DateOfDeath()));
    output.setDateOfEstablishment(convertMultilingualMapToLabel(agentEntityEnrichment.getRdaGr2DateOfEstablishment()));
    output.setDateOfTermination(convertMultilingualMapToLabel(agentEntityEnrichment.getRdaGr2DateOfTermination()));
    output.setGender(convertMultilingualMapToLabel(agentEntityEnrichment.getRdaGr2Gender()));

    output.setDate(convertResourceOrLiteral(agentEntityEnrichment.getDcDate()));
    output.setProfessionOrOccupation(
        convertResourceOrLiteral(agentEntityEnrichment.getRdaGr2ProfessionOrOccupation()));

    output.setWasPresentAt(convertToResourceList(agentEntityEnrichment.getEdmWasPresentAt()));
    output.setSameAs(convertListToPart(agentEntityEnrichment.getOwlSameAs()));

    return output;
  }

  private static Organization convertOrganization(
      OrganizationEnrichmentEntity organizationEnrichmentEntity) {
    Organization output = new Organization();

    output.setAbout(organizationEnrichmentEntity.getAbout());
    output.setPrefLabelList(convertMultilingualMapToLabel(organizationEnrichmentEntity.getPrefLabel()));
    output.setAltLabelList(convertMultilingualMapToLabel(organizationEnrichmentEntity.getAltLabel()));
    output.setNotes(convertMultilingualMapToLabel(organizationEnrichmentEntity.getNote()));
    output.setSameAs(convertListToPart(organizationEnrichmentEntity.getOwlSameAs()));
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
    output.setAcronyms(convertMultilingualMapToLabel(organizationEnrichmentEntity.getEdmAcronym()));
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

  /**
   * Generates the list of {@link LabelInfo} values.
   * @param abstractEnrichmentEntity the entity to generate them for
   * @return the list of label info objects
   */
  public static List<LabelInfo> createLabelInfoList(
      AbstractEnrichmentEntity abstractEnrichmentEntity) {
    final Map<String, List<String>> combinedLabels = new HashMap<>();

    copyToCombinedLabels(combinedLabels, abstractEnrichmentEntity.getPrefLabel());
    copyToCombinedLabels(combinedLabels, abstractEnrichmentEntity.getAltLabel());
    if (abstractEnrichmentEntity instanceof OrganizationEnrichmentEntity) {
      copyToCombinedLabels(combinedLabels,
          ((OrganizationEnrichmentEntity) abstractEnrichmentEntity).getEdmAcronym());
    }

    return combinedLabels.entrySet().stream()
        .map(entry -> new LabelInfo(entry.getValue(), entry.getKey())).collect(Collectors.toList());
  }

  private static void copyToCombinedLabels(Map<String, List<String>> combinedLabels,
      Map<String, List<String>> prefLabel) {
    if (prefLabel != null) {
      prefLabel.forEach((key, value) -> {
        value = value.stream().map(String::toLowerCase).collect(Collectors.toList());
        combinedLabels.merge(key, value,
            (v1, v2) -> Stream.of(v1, v2).flatMap(List::stream).distinct()
                .collect(Collectors.toList()));
      });
    }
  }
}
