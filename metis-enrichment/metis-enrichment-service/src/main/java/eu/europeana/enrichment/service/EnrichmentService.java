package eu.europeana.enrichment.service;

import eu.europeana.corelib.solr.entity.AbstractEdmEntityImpl;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.internal.MongoTerm;
import eu.europeana.enrichment.api.internal.MongoTermList;
import eu.europeana.enrichment.service.dao.EnrichmentDao;
import eu.europeana.enrichment.utils.EntityType;
import eu.europeana.enrichment.utils.EntityTypeUtils;
import eu.europeana.enrichment.utils.InputValue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import org.apache.commons.lang3.Functions.FailableSupplier;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
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
  public List<Pair<String, EnrichmentBase>> enrichByInputValueList(
      List<InputValue> inputValues) {
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
          findEntities(entityType, value, language).stream()
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
   * Get an enrichment by providing a list of URIs, might match owl:sameAs.
   *
   * @param uri The URI to check for match
   * @return the structured result of the enrichment
   */
  public List<EnrichmentBase> enrichByCodeUriOrOwlSameAs(String uri) {
    final List<EnrichmentBase> enrichmentBases = new ArrayList<>();
    try {
      //Create the list of suppliers that we'll use to find first match in order
      final List<FailableSupplier<List<EnrichmentBase>, IOException>> enrichmentBaseSuppliers = getEnrichmentBaseSuppliers(
          new ImmutablePair<>(EnrichmentDao.CODE_URI_FIELD, uri));
      enrichmentBaseSuppliers.addAll(
          getEnrichmentBaseSuppliers(new ImmutablePair<>(EnrichmentDao.OWL_SAME_AS_FIELD, uri)));
      Optional.ofNullable(getFirstMatch(enrichmentBaseSuppliers)).ifPresent(enrichmentBases::add);
    } catch (RuntimeException | IOException e) {
      LOGGER.warn("Unable to retrieve entity from id", e);
    }
    return enrichmentBases;
  }

  /**
   * Get an enrichment by providing a list of URIs.
   *
   * @param codeUri The URI to check for match
   * @return the structured result of the enrichment
   */
  public List<EnrichmentBase> enrichByCodeUri(String codeUri) {
    final List<EnrichmentBase> enrichmentBases = new ArrayList<>();
    try {
      //Create the list of suppliers that we'll use to find first match in order
      final List<FailableSupplier<List<EnrichmentBase>, IOException>> enrichmentBaseSuppliers = getEnrichmentBaseSuppliers(
          new ImmutablePair<>(EnrichmentDao.CODE_URI_FIELD, codeUri));
      Optional.ofNullable(getFirstMatch(enrichmentBaseSuppliers)).ifPresent(enrichmentBases::add);
    } catch (RuntimeException | IOException e) {
      LOGGER.warn("Unable to retrieve entity from codeUri", e);
    }
    return enrichmentBases;
  }

  private List<FailableSupplier<List<EnrichmentBase>, IOException>> getEnrichmentBaseSuppliers(
      Pair<String, String> fieldNamesAndValues) {
    return Arrays
        .asList(
            () -> getEntitiesAndConvert(EntityType.AGENT,
                Collections.singletonList(fieldNamesAndValues)),
            () -> getEntitiesAndConvert(EntityType.CONCEPT,
                Collections.singletonList(fieldNamesAndValues)),
            () -> getEntitiesAndConvert(EntityType.TIMESPAN,
                Collections.singletonList(fieldNamesAndValues)),
            () -> getEntitiesAndConvert(EntityType.PLACE,
                Collections.singletonList(fieldNamesAndValues))
        );
  }

  private EnrichmentBase getFirstMatch(
      List<FailableSupplier<List<EnrichmentBase>, IOException>> suppliers)
      throws IOException {
    for (FailableSupplier<List<EnrichmentBase>, IOException> supplier : suppliers) {
      final List<EnrichmentBase> suppliedItem = supplier.get();
      if (CollectionUtils.isNotEmpty(suppliedItem)) {
        return suppliedItem.get(0);
      }
    }
    return null;
  }

  private List<EnrichmentBase> getEntitiesAndConvert(EntityType entityType,
      List<Pair<String, String>> fieldNamesAndValues) {
    final List<? extends MongoTermList<? extends AbstractEdmEntityImpl>> mongoTermLists = enrichmentDao
        .getAllMongoTermListsByFields(
            EntityTypeUtils.getEntityMongoTermListClass(entityType).getMongoTermListClass(),
            fieldNamesAndValues);
    return Converter.convert(mongoTermLists);
  }

  private List<EnrichmentBase> findEntities(EntityType entityType, String termLabel,
      String termLanguage) {

    //Find all terms that match label and language
    final List<Pair<String, String>> fieldNamesAndValues = new ArrayList<>();
    fieldNamesAndValues.add(new ImmutablePair<>(EnrichmentDao.LABEL_FIELD, termLabel));
    //If language not defined we are searching without specifying the language
    if (StringUtils.isNotBlank(termLanguage)) {
      fieldNamesAndValues.add(new ImmutablePair<>(EnrichmentDao.LANG_FIELD, termLanguage));
    }
    final List<MongoTerm> mongoTerms = enrichmentDao
        .getAllMongoTermsByFields(entityType, fieldNamesAndValues);

    final List<EnrichmentBase> enrichmentBases = new ArrayList<>();
    for (MongoTerm mongoTerm : mongoTerms) {
      //Find mongoTermLists by codeUri
      final List<? extends MongoTermList<? extends AbstractEdmEntityImpl>> mongoTermLists = enrichmentDao
          .getAllMongoTermListsByFields(
              EntityTypeUtils.getEntityMongoTermListClass(entityType).getMongoTermListClass(),
              List.of(new ImmutablePair<>(EnrichmentDao.CODE_URI_FIELD, mongoTerm.getCodeUri())));
      //Find parent mongoTermLists
      final List<? extends MongoTermList<? extends AbstractEdmEntityImpl>> parentMongoTermLists = mongoTermLists
          .stream().map(mongoTermList -> findParentEntities(entityType, mongoTermList))
          .flatMap(List::stream).collect(Collectors.toList());

      //Convert to EnrichmentBases
      enrichmentBases.addAll(Converter.convert(mongoTermLists));
      enrichmentBases.addAll(Converter.convert(parentMongoTermLists));
    }

    return enrichmentBases;
  }

  private List<? extends MongoTermList<? extends AbstractEdmEntityImpl>> findParentEntities(
      EntityType entityType, MongoTermList<? extends AbstractEdmEntityImpl> mongoTermList) {
    Set<String> parentCodeUris = findParentCodeUris(entityType, mongoTermList);
    //Do not get entities for very broad TIMESPAN
    if (entityType == EntityType.TIMESPAN) {
      parentCodeUris = parentCodeUris.stream()
          .filter(parentCodeUri -> PATTERN_MATCHING_VERY_BROAD_TIMESPANS
              .matcher(parentCodeUri).matches()).collect(Collectors.toSet());
    }

    final List<Pair<String, List<String>>> fieldNamesAndValues = new ArrayList<>();
    fieldNamesAndValues
        .add(new ImmutablePair<>(EnrichmentDao.CODE_URI_FIELD, new ArrayList<>(parentCodeUris)));
    return enrichmentDao.getAllMongoTermListsByFieldsInList(
        EntityTypeUtils.getEntityMongoTermListClass(entityType).getMongoTermListClass(),
        fieldNamesAndValues);
  }

  private Set<String> findParentCodeUris(EntityType entityType,
      MongoTermList<? extends AbstractEdmEntityImpl> termList) {
    final Set<String> parentEntities = new HashSet<>();
    MongoTermList<? extends AbstractEdmEntityImpl> currentMongoTermList = termList;
    while (StringUtils.isNotBlank(currentMongoTermList.getParent())) {
      currentMongoTermList = enrichmentDao
          .getTermListByField(
              EntityTypeUtils.getEntityMongoTermListClass(entityType).getMongoTermListClass(),
              EnrichmentDao.CODE_URI_FIELD, currentMongoTermList.getParent());
      //Break when there is no other parent available or when we have already encountered the codeUri
      if (currentMongoTermList == null || !parentEntities.add(currentMongoTermList.getCodeUri())) {
        break;
      }
    }
    return parentEntities;
  }
}
