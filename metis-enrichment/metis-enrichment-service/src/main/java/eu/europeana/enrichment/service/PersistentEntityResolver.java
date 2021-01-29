package eu.europeana.enrichment.service;

import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.internal.EntityResolver;
import eu.europeana.enrichment.api.internal.ReferenceTerm;
import eu.europeana.enrichment.api.internal.SearchTerm;
import eu.europeana.enrichment.internal.model.EnrichmentTerm;
import eu.europeana.enrichment.internal.model.OrganizationEnrichmentEntity;
import eu.europeana.enrichment.service.dao.EnrichmentDao;
import eu.europeana.enrichment.utils.EntityType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An entity resolver that works by accessing a persistent database.
 */
public class PersistentEntityResolver implements EntityResolver {

  private static final Logger LOGGER = LoggerFactory.getLogger(PersistentEntityResolver.class);
  private static final Set<String> ALL_2CODE_LANGUAGES;
  private static final Map<String, String> ALL_3CODE_TO_2CODE_LANGUAGES;
  private static final Pattern PATTERN_MATCHING_VERY_BROAD_TIMESPANS = Pattern
      .compile("http://semium.org/time/(ChronologicalPeriod$|Time$|(AD|BC)[1-9]x{3}$)");
  public static final int THREE_CHARACTER_LANGUAGE_LENGTH = 3;
  public static final int TWO_CHARACTER_LANGUAGE_LENGTH = 2;

  static {
    HashSet<String> all2CodeLanguages = new HashSet<>();
    Map<String, String> all3CodeLanguages = new HashMap<>();
    Arrays.stream(Locale.getISOLanguages()).map(Locale::new).forEach(locale -> {
      all2CodeLanguages.add(locale.getLanguage());
      all3CodeLanguages.put(locale.getISO3Language(), locale.getLanguage());
    });
    ALL_2CODE_LANGUAGES = Collections.unmodifiableSet(all2CodeLanguages);
    ALL_3CODE_TO_2CODE_LANGUAGES = Collections.unmodifiableMap(all3CodeLanguages);
  }

  private final EnrichmentDao enrichmentDao;

  /**
   * Constructor with the persistence dao parameter.
   *
   * @param enrichmentDao the enrichment persistence dao
   */
  public PersistentEntityResolver(EnrichmentDao enrichmentDao) {
    this.enrichmentDao = enrichmentDao;
  }

  @Override
  public <T extends SearchTerm> Map<T, List<EnrichmentBase>> resolveByText(Set<T> searchTerms) {
    final Map<T, List<EnrichmentBase>> result = new HashMap<>();
    try {
      for (T searchTerm : searchTerms) {
        findEnrichmentEntitiesBySearchTerm(result, searchTerm);
      }
    } catch (RuntimeException e) {
      LOGGER.warn("Unable to retrieve entity from tag", e);
    }
    return result;
  }

  @Override
  public <T extends ReferenceTerm> Map<T, EnrichmentBase> resolveById(Set<T> referenceTerms) {
    final Map<T, EnrichmentBase> result = new HashMap<>();
    for (T value : referenceTerms) {
      try {
        List<EnrichmentBase> foundEnrichmentBases = getEnrichmentTermsAndConvert(List.of(
            new ImmutablePair<>(EnrichmentDao.ENTITY_ABOUT_FIELD,
                value.getReference().toString())));
        if (!foundEnrichmentBases.isEmpty()) {
          result.put(value, foundEnrichmentBases.get(0));
        }
      } catch (RuntimeException e) {
        LOGGER.warn("Unable to retrieve entity from entityAbout", e);
      }
    }
    return result;
  }

  @Override
  public <T extends ReferenceTerm> Map<T, List<EnrichmentBase>> resolveByUri(
      Set<T> referenceTerms) {
    final Map<T, List<EnrichmentBase>> result = new HashMap<>();

    for (T referenceTerm : referenceTerms) {
      try {
        final Set<EntityType> entityTypes = referenceTerm.getCandidateTypes();
        final List<EnrichmentBase> foundEnrichmentBases = getEnrichmentBases(referenceTerm,
            entityTypes);

        if (CollectionUtils.isNotEmpty(foundEnrichmentBases)) {
          result.put(referenceTerm, foundEnrichmentBases);
        }
      } catch (RuntimeException e) {
        LOGGER.warn("Unable to retrieve entity from id", e);
      }
    }

    return result;
  }

  private <T extends ReferenceTerm> List<EnrichmentBase> getEnrichmentBases(T referenceTerm,
      Set<EntityType> entityTypes) {
    final List<EnrichmentBase> foundEnrichmentBases;
    if (CollectionUtils.isEmpty(entityTypes)) {
      foundEnrichmentBases = searchBasesFirstAboutThenOwlSameAs(
          referenceTerm.getReference().toString(), null);
    } else {
      foundEnrichmentBases = new ArrayList<>();
      for (EntityType entityType : entityTypes) {
        foundEnrichmentBases.addAll(
            searchBasesFirstAboutThenOwlSameAs(referenceTerm.getReference().toString(),
                entityType));
      }
    }
    return foundEnrichmentBases;
  }

  private <T extends SearchTerm> void findEnrichmentEntitiesBySearchTerm(
      Map<T, List<EnrichmentBase>> searchTermListMap, T searchTerm) {
    final String value = searchTerm.getTextValue().toLowerCase(Locale.US);
    if (!StringUtils.isBlank(value)) {
      final Set<EntityType> entityTypes = searchTerm.getCandidateTypes();
      //Language has to be a valid 2 or 3 code, otherwise we do not use it
      final String inputValueLanguage = searchTerm.getLanguage();
      final String language;
      if (inputValueLanguage != null
          && inputValueLanguage.length() == THREE_CHARACTER_LANGUAGE_LENGTH) {
        language = ALL_3CODE_TO_2CODE_LANGUAGES.get(inputValueLanguage);
      } else if (inputValueLanguage != null
          && inputValueLanguage.length() == TWO_CHARACTER_LANGUAGE_LENGTH) {
        language = ALL_2CODE_LANGUAGES.contains(inputValueLanguage) ? inputValueLanguage : null;
      } else {
        language = null;
      }

      if (CollectionUtils.isEmpty(entityTypes)) {
        searchTermListMap.put(searchTerm, findEnrichmentTerms(null, value, language));
      } else {
        final List<EnrichmentBase> enrichmentBases = new ArrayList<>();
        for (EntityType entityType : entityTypes) {
          enrichmentBases.addAll(findEnrichmentTerms(entityType, value, language));
        }
        searchTermListMap.put(searchTerm, enrichmentBases);
      }
    }
  }

  private List<EnrichmentBase> findEnrichmentTerms(EntityType entityType, String termLabel,
      String termLanguage) {

    final HashMap<String, List<Pair<String, String>>> fieldNameMap = new HashMap<>();
    //Find all terms that match label and language. Order of Pairs matter for the query performance.
    final List<Pair<String, String>> labelInfosFields = new ArrayList<>();
    labelInfosFields.add(new ImmutablePair<>(EnrichmentDao.LABEL_FIELD, termLabel));
    //If language not defined we are searching without specifying the language
    if (StringUtils.isNotBlank(termLanguage)) {
      labelInfosFields.add(new ImmutablePair<>(EnrichmentDao.LANG_FIELD, termLanguage));
    }

    final List<Pair<String, String>> enrichmentTermFields = new ArrayList<>();

    if (entityType != null) {
      enrichmentTermFields
          .add(new ImmutablePair<>(EnrichmentDao.ENTITY_TYPE_FIELD, entityType.name()));
    }
    fieldNameMap.put(EnrichmentDao.LABEL_INFOS_FIELD, labelInfosFields);
    fieldNameMap.put(null, enrichmentTermFields);
    final List<EnrichmentTerm> enrichmentTerms = enrichmentDao
        .getAllEnrichmentTermsByFields(fieldNameMap);
    final List<EnrichmentTerm> parentEnrichmentTerms = enrichmentTerms.stream()
        .map(this::findParentEntities).flatMap(List::stream).collect(Collectors.toList());

    final List<EnrichmentBase> enrichmentBases = new ArrayList<>();
    //Convert to EnrichmentBases
    enrichmentBases.addAll(Converter.convert(enrichmentTerms));
    enrichmentBases.addAll(Converter.convert(parentEnrichmentTerms));

    return enrichmentBases;
  }

  private List<EnrichmentTerm> findParentEntities(EnrichmentTerm enrichmentTerm) {
    final Set<String> parentAbouts = new HashSet<>();
    parentAbouts.add(enrichmentTerm.getEnrichmentEntity().getAbout());
    final List<EnrichmentTerm> parentEntities = new ArrayList<>();
    Predicate<String> isTimespanVeryBroad = parent ->
        enrichmentTerm.getEntityType() == EntityType.TIMESPAN
            && PATTERN_MATCHING_VERY_BROAD_TIMESPANS.matcher(parent).matches();
    String parentAbout = enrichmentTerm.getEnrichmentEntity().getIsPartOf();
    while (StringUtils.isNotBlank(parentAbout) && !isTimespanVeryBroad.test(parentAbout)) {
      EnrichmentTerm currentEnrichmentTerm = enrichmentDao
          .getEnrichmentTermByField(EnrichmentDao.ENTITY_ABOUT_FIELD, parentAbout).orElse(null);
      //Break when there is no other parent available or when we have already encountered the
      // same about
      if (currentEnrichmentTerm == null || !parentAbouts.add(parentAbout)) {
        break;
      }
      parentEntities.add(currentEnrichmentTerm);
      parentAbout = currentEnrichmentTerm.getEnrichmentEntity().getIsPartOf();
    }
    return parentEntities;
  }

  private List<EnrichmentBase> searchBasesFirstAboutThenOwlSameAs(String reference,
      EntityType entityType) {
    final Pair<String, String> parameterAbout = new ImmutablePair<>(
        EnrichmentDao.ENTITY_ABOUT_FIELD, reference);
    final Pair<String, String> parameterOwlSameAs = new ImmutablePair<>(
        EnrichmentDao.ENTITY_OWL_SAME_AS_FIELD, reference);

    List<EnrichmentBase> foundEnrichmentBases;
    final List<Pair<String, String>> parametersAbout = new ArrayList<>();
    final List<Pair<String, String>> parametersOwlSameAs = new ArrayList<>();
    if (entityType != null) {
      final ImmutablePair<String, String> parameterEntityType = new ImmutablePair<>(
          EnrichmentDao.ENTITY_TYPE_FIELD, entityType.name());
      parametersAbout.add(parameterEntityType);
      parametersOwlSameAs.add(parameterEntityType);
    }
    // Get by about first
    parametersAbout.add(parameterAbout);
    foundEnrichmentBases = getEnrichmentTermsAndConvert(parametersAbout);
    if (CollectionUtils.isEmpty(foundEnrichmentBases)) {
      // If empty try OwlSameAs
      parametersOwlSameAs.add(parameterOwlSameAs);
      foundEnrichmentBases = getEnrichmentTermsAndConvert(parametersOwlSameAs);
    }
    return foundEnrichmentBases;
  }

  private List<EnrichmentBase> getEnrichmentTermsAndConvert(
      List<Pair<String, String>> fieldNamesAndValues) {
    final List<EnrichmentTerm> enrichmentTerms = getEnrichmentTerms(fieldNamesAndValues);
    return Converter.convert(enrichmentTerms);
  }

  private List<EnrichmentTerm> getEnrichmentTerms(List<Pair<String, String>> fieldNamesAndValues) {
    final HashMap<String, List<Pair<String, String>>> fieldNameMap = new HashMap<>();
    fieldNameMap.put(null, fieldNamesAndValues);
    return enrichmentDao.getAllEnrichmentTermsByFields(fieldNameMap);
  }

  /* --- Organization specific methods, used by the annotations api --- */

  /**
   * Save an organization to the database
   *
   * @param organizationEnrichmentEntity the organization to save
   * @param created the created date to be used
   * @param updated the updated date to be used
   * @return the saved organization
   */
  public OrganizationEnrichmentEntity saveOrganization(
      OrganizationEnrichmentEntity organizationEnrichmentEntity, Date created, Date updated) {

    final EnrichmentTerm enrichmentTerm = EntityConverterUtils
        .organizationImplToEnrichmentTerm(organizationEnrichmentEntity, created, updated);

    final Optional<ObjectId> objectId = enrichmentDao
        .getEnrichmentTermObjectIdByField(EnrichmentDao.ENTITY_ABOUT_FIELD,
            organizationEnrichmentEntity.getAbout());
    objectId.ifPresent(enrichmentTerm::setId);

    //Save term list
    final String id = enrichmentDao.saveEnrichmentTerm(enrichmentTerm);
    return enrichmentDao.getEnrichmentTermByField(EnrichmentDao.ID_FIELD, id)
        .map(EnrichmentTerm::getEnrichmentEntity).map(OrganizationEnrichmentEntity.class::cast)
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
      Optional<OrganizationEnrichmentEntity> organization = getOrganizationByUri(id);
      organization.ifPresent(value -> existingOrganizationIds.add(value.getAbout()));
    }
    return existingOrganizationIds;
  }

  /**
   * Get an organization by uri
   *
   * @param uri The EDM organization uri
   * @return OrganizationImpl object
   */
  public Optional<OrganizationEnrichmentEntity> getOrganizationByUri(String uri) {
    final List<EnrichmentTerm> enrichmentTerm = getEnrichmentTerms(
        Collections.singletonList(new ImmutablePair<>(EnrichmentDao.ENTITY_ABOUT_FIELD, uri)));
    return enrichmentTerm.stream().findFirst().map(EnrichmentTerm::getEnrichmentEntity)
        .map(OrganizationEnrichmentEntity.class::cast);
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
