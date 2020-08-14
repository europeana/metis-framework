package eu.europeana.enrichment.service;

import eu.europeana.corelib.solr.entity.OrganizationImpl;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentTerm;
import eu.europeana.enrichment.service.dao.EnrichmentDao;
import eu.europeana.enrichment.utils.EntityType;
import eu.europeana.enrichment.utils.InputValue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Contains functionality for accessing entities from the enrichment database using {@link
 * EnrichmentDao}.
 *
 * @author Simon Tzanakis
 * @since 2020-07-16
 */
@Service
public class EnrichmentService {

  private static final Logger LOGGER = LoggerFactory.getLogger(EnrichmentService.class);
  private static final Set<String> ALL_2CODE_LANGUAGES = all2CodeLanguages();
  private static final Pattern PATTERN_MATCHING_VERY_BROAD_TIMESPANS = Pattern
      .compile("http://semium.org/time/(ChronologicalPeriod$|Time$|(AD|BC)[1-9]x{3}$)");
  private final EnrichmentDao enrichmentDao;

  @Autowired
  public EnrichmentService(EnrichmentDao enrichmentDao) {
    this.enrichmentDao = enrichmentDao;
  }

  private static Set<String> all2CodeLanguages() {
    return Arrays.stream(Locale.getISOLanguages()).map(Locale::new).map(Locale::toString)
        .collect(Collectors.toCollection(TreeSet::new));
  }

  /**
   * Get an enrichment by providing a list of {@link InputValue}s.
   *
   * @param inputValues a list of structured input values with parameters
   * @return the enrichment values in a wrapped structured list
   */
  public List<Pair<String, EnrichmentBase>> enrichByInputValueList(List<InputValue> inputValues) {
    final List<Pair<String, EnrichmentBase>> enrichmentBases = new ArrayList<>();
    try {
      for (InputValue inputValue : inputValues) {
        final String originalField = inputValue.getRdfFieldName();
        final List<EntityType> entityTypes = inputValue.getEntityTypes();
        //Language has to be a valid 2 code, otherwise we do not use it
        final String inputValueLanguage = inputValue.getLanguage();
        final String language = (StringUtils.isNotBlank(inputValueLanguage) && ALL_2CODE_LANGUAGES
            .contains(inputValueLanguage)) ? inputValueLanguage : null;
        final String value = inputValue.getValue().toLowerCase(Locale.US);

        if (CollectionUtils.isEmpty(entityTypes) || StringUtils.isBlank(value)) {
          continue;
        }
        for (EntityType entityType : entityTypes) {
          findEnrichmentTerms(entityType, value, language).stream()
              .map(enrichmentBase -> new ImmutablePair<>(originalField, enrichmentBase))
              .forEach(enrichmentBases::add);
        }
      }
    } catch (RuntimeException e) {
      LOGGER.warn("Unable to retrieve entity from tag", e);
    }
    return enrichmentBases;
  }

  /**
   * Get an enrichment by providing a URI, might match owl:sameAs.
   *
   * @param uri The URI to check for match
   * @return the structured result of the enrichment
   */
  public EnrichmentBase enrichByCodeUriOrOwlSameAs(String uri) {
    try {
      //First check codeUri, otherwise OwlSameAs
      List<EnrichmentBase> foundEnrichmentBases = getEnrichmentTermsAndConvert(
          Collections.singletonList(new ImmutablePair<>(EnrichmentDao.CODE_URI_FIELD, uri)));
      if (CollectionUtils.isEmpty(foundEnrichmentBases)) {
        foundEnrichmentBases = getEnrichmentTermsAndConvert(
            Collections.singletonList(new ImmutablePair<>(EnrichmentDao.OWL_SAME_AS_FIELD, uri)));
      }
      if (CollectionUtils.isNotEmpty(foundEnrichmentBases)) {
        return foundEnrichmentBases.get(0);
      }
    } catch (RuntimeException e) {
      LOGGER.warn("Unable to retrieve entity from id", e);
    }
    return null;
  }

  /**
   * Get an enrichment by providing a URI.
   *
   * @param codeUri The URI to check for match
   * @return the structured result of the enrichment
   */
  public EnrichmentBase enrichByCodeUri(String codeUri) {
    try {
      List<EnrichmentBase> foundEnrichmentBases = getEnrichmentTermsAndConvert(
          Collections.singletonList(new ImmutablePair<>(EnrichmentDao.CODE_URI_FIELD, codeUri)));
      if (CollectionUtils.isNotEmpty(foundEnrichmentBases)) {
        return foundEnrichmentBases.get(0);
      }
    } catch (RuntimeException e) {
      LOGGER.warn("Unable to retrieve entity from codeUri", e);
    }
    return null;
  }

  private List<EnrichmentBase> getEnrichmentTermsAndConvert(
      List<Pair<String, String>> fieldNamesAndValues) {
    final List<EnrichmentTerm> enrichmentTerms = getEnrichmentTerms(fieldNamesAndValues);
    return Converter.convert(enrichmentTerms);
  }

  public List<EnrichmentTerm> getEnrichmentTerms(List<Pair<String, String>> fieldNamesAndValues) {
    return enrichmentDao.getAllEnrichmentTermsByFields(fieldNamesAndValues);
  }

  private List<EnrichmentBase> findEnrichmentTerms(EntityType entityType, String termLabel,
      String termLanguage) {

    //Find all terms that match label and language. Order of Pairs matter for the query performance.
    final List<Pair<String, String>> fieldNamesAndValues = new ArrayList<>();
    fieldNamesAndValues.add(new ImmutablePair<>(EnrichmentDao.LABEL_FIELD, termLabel));
    //If language not defined we are searching without specifying the language
    if (StringUtils.isNotBlank(termLanguage)) {
      fieldNamesAndValues.add(new ImmutablePair<>(EnrichmentDao.LANG_FIELD, termLanguage));
    }
    fieldNamesAndValues
        .add(new ImmutablePair<>(EnrichmentDao.ENTITY_TYPE_FIELD, entityType.name()));
    final List<EnrichmentTerm> enrichmentTerms = enrichmentDao
        .getAllEnrichmentTermsByFields(fieldNamesAndValues);
    final List<EnrichmentTerm> parentEnrichmentTerms = enrichmentTerms.stream()
        .map(enrichmentTerm -> findParentEntities(entityType, enrichmentTerm)).flatMap(List::stream)
        .collect(Collectors.toList());

    final List<EnrichmentBase> enrichmentBases = new ArrayList<>();
    //Convert to EnrichmentBases
    enrichmentBases.addAll(Converter.convert(enrichmentTerms));
    enrichmentBases.addAll(Converter.convert(parentEnrichmentTerms));

    return enrichmentBases;
  }

  private List<EnrichmentTerm> findParentEntities(
      EntityType entityType, EnrichmentTerm enrichmentTerm) {
    Set<String> parentCodeUris = findParentCodeUris(enrichmentTerm);
    //Do not get entities for very broad TIMESPAN
    if (entityType == EntityType.TIMESPAN) {
      parentCodeUris = parentCodeUris.stream()
          .filter(parentCodeUri -> !PATTERN_MATCHING_VERY_BROAD_TIMESPANS
              .matcher(parentCodeUri).matches()).collect(Collectors.toSet());
    }

    final List<Pair<String, List<String>>> fieldNamesAndValues = new ArrayList<>();
    fieldNamesAndValues
        .add(new ImmutablePair<>(EnrichmentDao.CODE_URI_FIELD, new ArrayList<>(parentCodeUris)));
    return enrichmentDao.getAllEnrichmentTermsByFieldsInList(fieldNamesAndValues);
  }

  private Set<String> findParentCodeUris(EnrichmentTerm enrichmentTerm) {
    final Set<String> parentEntities = new HashSet<>();
    EnrichmentTerm currentEnrichmentTerm = enrichmentTerm;
    while (StringUtils.isNotBlank(currentEnrichmentTerm.getParent())) {
      currentEnrichmentTerm = enrichmentDao
          .getEnrichmentTermByField(EnrichmentDao.CODE_URI_FIELD,
              currentEnrichmentTerm.getParent()).orElse(null);
      //Break when there is no other parent available or when we have already encountered the codeUri
      if (currentEnrichmentTerm == null || !parentEntities
          .add(currentEnrichmentTerm.getCodeUri())) {
        break;
      }
    }
    return parentEntities;
  }

  /* --- Organization specific methods, used by the annotations api --- */

  /**
   * Save an organization to the database
   *
   * @param organization the organization to save
   * @param created the created date to be used
   * @param updated the updated date to be used
   * @return the saved organization
   */
  public OrganizationImpl saveOrganization(OrganizationImpl organization,
      Date created, Date updated) {

    final EnrichmentTerm enrichmentTerm = EntityConverterUtils
        .organizationImplToEnrichmentTerm(organization, created,
            updated);

    final Optional<ObjectId> objectId = enrichmentDao
        .getEnrichmentTermObjectIdByField(EnrichmentDao.CODE_URI_FIELD,
            organization.getAbout());
    objectId.ifPresent(enrichmentTerm::setId);

    //Save term list
    final String id = enrichmentDao.saveEnrichmentTerm(enrichmentTerm);
    return enrichmentDao.getEnrichmentTermByField(EnrichmentDao.ID_FIELD, id)
        .map(EnrichmentTerm::getContextualEntity).map(OrganizationImpl.class::cast)
        .orElse(null);
  }

  /**
   * Return the list of ids for existing organizations from database
   *
   * @param organizationIds The organization ids to check existence
   * @return list of ids of existing organizations
   */
  public List<String> findExistingOrganizations(List<String> organizationIds) {
    List<String> existingOrganizationIds = new ArrayList<>();
    for (String id : organizationIds) {
      Optional<OrganizationImpl> organization = getOrganizationByUri(id);
      organization.ifPresent(value -> existingOrganizationIds.add(value.getAbout()));
    }
    return existingOrganizationIds;
  }

  /**
   * Get an organization by uri
   *
   * @param uri The EDM organization uri (codeUri)
   * @return OrganizationImpl object
   */
  public Optional<OrganizationImpl> getOrganizationByUri(String uri) {
    final List<EnrichmentTerm> enrichmentTerm = getEnrichmentTerms(
        Collections.singletonList(new ImmutablePair<>(EnrichmentDao.CODE_URI_FIELD, uri)));
    return enrichmentTerm.stream().findFirst().map(EnrichmentTerm::getContextualEntity)
        .map(OrganizationImpl.class::cast);
  }

  /**
   * Delete organizations from database by given organization ids
   *
   * @param organizationIds The organization ids
   */
  public void deleteOrganizations(List<String> organizationIds) {
    enrichmentDao.deleteEnrichmentTerms(EntityType.ORGANIZATION, organizationIds);
  }

  /**
   * This method removes organization from database by given organization id.
   *
   * @param organizationId The organization id
   */
  public void deleteOrganization(String organizationId) {
    deleteOrganizations(Collections.singletonList(organizationId));
  }


  /**
   * Get the date of the latest updated organization.
   *
   * @return the date of the latest updated organization
   */
  public Date getDateOfLastUpdatedOrganization() {
    return enrichmentDao.getDateOfLastUpdatedEnrichmentTerm(EntityType.ORGANIZATION);
  }
}
