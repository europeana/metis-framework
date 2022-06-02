package eu.europeana.enrichment.service;

import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.internal.EntityResolver;
import eu.europeana.enrichment.api.internal.ReferenceTerm;
import eu.europeana.enrichment.api.internal.SearchTerm;
import eu.europeana.enrichment.internal.model.EnrichmentTerm;
import eu.europeana.enrichment.service.dao.EnrichmentDao;
import eu.europeana.enrichment.utils.EntityType;
import eu.europeana.enrichment.utils.LanguageCodeConverter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An entity resolver that works by accessing a persistent database.
 */
public class PersistentEntityResolver implements EntityResolver {

  private static final Logger LOGGER = LoggerFactory.getLogger(PersistentEntityResolver.class);
  private static final Pattern PATTERN_MATCHING_VERY_BROAD_TIMESPANS = Pattern
      .compile("http://semium.org/time/(ChronologicalPeriod$|Time$|(AD|BC)[1-9]x{3}$)");

  private final EnrichmentDao enrichmentDao;
  private final LanguageCodeConverter languageCodeConverter;

  /**
   * Constructor with the persistence dao parameter.
   *
   * @param enrichmentDao the enrichment persistence dao
   */
  public PersistentEntityResolver(EnrichmentDao enrichmentDao) {
    this.enrichmentDao = enrichmentDao;
    languageCodeConverter = new LanguageCodeConverter();
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
      final String language = languageCodeConverter.convertLanguageCode(searchTerm.getLanguage());

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

}
