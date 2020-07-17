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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
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

  public List<Pair<String, EnrichmentBase>> findEntitiesBasedOnValues(
      List<InputValue> inputValues) {
    final List<Pair<String, EnrichmentBase>> enrichmentBases = new ArrayList<>();
    try {
      for (InputValue inputValue : inputValues) {
        final String originalField = inputValue.getRdfFieldName();
        final List<EntityType> entityTypes = inputValue.getEntityTypes();
        //Language has to be a valid 2 code, otherwise we do not use it
        final String language =
            ALL_2CODE_LANGUAGES.contains(inputValue.getLanguage()) ? inputValue.getLanguage()
                : null;
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

  public List<EnrichmentBase> getByCodeUriOrOwlSameAs(String uri) {
    final List<EnrichmentBase> enrichmentBases = new ArrayList<>();
    try {
      final List<Pair<String, String>> codeUriFieldValue = new ArrayList<>();
      codeUriFieldValue.add(new ImmutablePair<>(EnrichmentDao.CODE_URI_FIELD, uri));
      final List<Pair<String, String>> owlSameAsFieldValue = new ArrayList<>();
      codeUriFieldValue.add(new ImmutablePair<>(EnrichmentDao.OWL_SAME_AS_FIELD, uri));

      //Create the list of suppliers that we'll use to find first match in order
      final List<FailableSupplier<List<EnrichmentBase>, IOException>> enrichmentBaseSuppliers = Arrays
          .asList(
              () -> getEntitiesAndConvert(EntityType.AGENT, codeUriFieldValue),
              () -> getEntitiesAndConvert(EntityType.CONCEPT, codeUriFieldValue),
              () -> getEntitiesAndConvert(EntityType.TIMESPAN, codeUriFieldValue),
              () -> getEntitiesAndConvert(EntityType.PLACE, codeUriFieldValue),
              () -> getEntitiesAndConvert(EntityType.AGENT, owlSameAsFieldValue),
              () -> getEntitiesAndConvert(EntityType.CONCEPT, owlSameAsFieldValue),
              () -> getEntitiesAndConvert(EntityType.TIMESPAN, owlSameAsFieldValue),
              () -> getEntitiesAndConvert(EntityType.PLACE, owlSameAsFieldValue)
          );
      enrichmentBases.add(getFirstMatch(enrichmentBaseSuppliers));
    } catch (RuntimeException | IOException e) {
      LOGGER.warn("Unable to retrieve entity from id", e);
    }
    return enrichmentBases;
  }

  public List<EnrichmentBase> getByCodeUri(String codeUri) {
    final List<EnrichmentBase> enrichmentBases = new ArrayList<>();
    try {
      final List<Pair<String, String>> codeUriFieldValue = new ArrayList<>();
      codeUriFieldValue.add(new ImmutablePair<>(EnrichmentDao.CODE_URI_FIELD, codeUri));

      //Create the list of suppliers that we'll use to find first match in order
      final List<FailableSupplier<List<EnrichmentBase>, IOException>> enrichmentBaseSuppliers = Arrays
          .asList(
              () -> getEntitiesAndConvert(EntityType.AGENT, codeUriFieldValue),
              () -> getEntitiesAndConvert(EntityType.CONCEPT, codeUriFieldValue),
              () -> getEntitiesAndConvert(EntityType.TIMESPAN, codeUriFieldValue),
              () -> getEntitiesAndConvert(EntityType.PLACE, codeUriFieldValue)
          );

      enrichmentBases.add(getFirstMatch(enrichmentBaseSuppliers));
    } catch (RuntimeException | IOException e) {
      LOGGER.warn("Unable to retrieve entity from codeUri", e);
    }
    return enrichmentBases;
  }

  private EnrichmentBase getFirstMatch(
      List<FailableSupplier<List<EnrichmentBase>, IOException>> suppliers)
      throws IOException {
    for (FailableSupplier<List<EnrichmentBase>, IOException> supplier : suppliers) {
      final List<EnrichmentBase> suppliedItem = supplier.get();
      if (CollectionUtils.isNotEmpty(suppliedItem)) {
        return suppliedItem.iterator().next();
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
      fieldNamesAndValues.clear();
      fieldNamesAndValues
          .add(new ImmutablePair<>(EnrichmentDao.CODE_URI_FIELD, mongoTerm.getCodeUri()));
      final List<? extends MongoTermList<? extends AbstractEdmEntityImpl>> mongoTermLists = enrichmentDao
          .getAllMongoTermListsByFields(
              EntityTypeUtils.getEntityMongoTermListClass(entityType).getMongoTermListClass(),
              fieldNamesAndValues);
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
          .findTermListByField(
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
