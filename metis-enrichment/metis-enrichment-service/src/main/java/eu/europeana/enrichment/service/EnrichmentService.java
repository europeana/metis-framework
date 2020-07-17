package eu.europeana.enrichment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.corelib.solr.entity.AbstractEdmEntityImpl;
import eu.europeana.enrichment.api.external.EntityWrapper;
import eu.europeana.enrichment.api.internal.AgentTermList;
import eu.europeana.enrichment.api.internal.ConceptTermList;
import eu.europeana.enrichment.api.internal.MongoTerm;
import eu.europeana.enrichment.api.internal.MongoTermList;
import eu.europeana.enrichment.api.internal.OrganizationTermList;
import eu.europeana.enrichment.api.internal.PlaceTermList;
import eu.europeana.enrichment.api.internal.TimespanTermList;
import eu.europeana.enrichment.utils.EntityDao;
import eu.europeana.enrichment.utils.EntityType;
import eu.europeana.enrichment.utils.InputValue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
  private static final List<EntityInfo<?, ?>> ENTITY_INFOS = createEntityTypeList();
  private static final Set<String> ALL_2CODE_LANGUAGES = all2CodeLanguages();
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final Pattern PATTERN_MATCHING_VERY_BROAD_TIMESPANS = Pattern
      .compile("http://semium.org/time/(ChronologicalPeriod$|Time$|(AD|BC)[1-9]x{3}$)");
  private final EntityDao entityDao;

  // TODO: 7/16/20 Try to eliminate EntityWrapper

  @Autowired
  public EnrichmentService(EntityDao entityDao) {
    this.entityDao = entityDao;
  }

  private static Set<String> all2CodeLanguages() {
    return Arrays.stream(Locale.getISOLanguages()).map(Locale::new).map(Locale::toString)
        .collect(Collectors.toCollection(TreeSet::new));
  }

  private static List<EntityInfo<?, ?>> createEntityTypeList() {
    final ArrayList<EntityInfo<?, ?>> entityInfos = new ArrayList<>();
    entityInfos.add(new EntityInfo<>(EntityType.AGENT, AgentTermList.class));
    entityInfos.add(new EntityInfo<>(EntityType.CONCEPT, ConceptTermList.class));
    entityInfos.add(new EntityInfo<>(EntityType.PLACE, PlaceTermList.class));
    entityInfos.add(new EntityInfo<>(EntityType.TIMESPAN, TimespanTermList.class));
    entityInfos.add(new EntityInfo<>(EntityType.ORGANIZATION, OrganizationTermList.class));
    return entityInfos;
  }

  public List<EntityWrapper> findEntitiesBasedOnValues(List<InputValue> inputValues) {
    try {
      List<EntityWrapper> entityWrappers = new ArrayList<>();
      for (InputValue inputValue : inputValues) {
        final List<EntityType> entityTypes = inputValue.getEntityTypes();
        final String language =
            ALL_2CODE_LANGUAGES.contains(inputValue.getLanguage()) ? inputValue.getLanguage()
                : null;
        final String value = inputValue.getValue().toLowerCase(Locale.US);

        if (CollectionUtils.isEmpty(entityTypes) || StringUtils.isBlank(value)) {
          continue;
        }
        for (EntityType entityType : entityTypes) {
          entityWrappers.addAll(findEntities(entityType, value, language));
        }
      }
      return entityWrappers;
    } catch (RuntimeException | JsonProcessingException e) {
      LOGGER.warn("Unable to retrieve entity from tag", e);
    }
    return Collections.emptyList();
  }

  public EntityWrapper getByCodeUriOrOwlSameAs(String uri) {
    try {
      final List<Pair<String, String>> codeUriFieldValue = new ArrayList<>();
      codeUriFieldValue.add(new ImmutablePair<>(EntityDao.CODE_URI_FIELD, uri));
      final List<Pair<String, String>> owlSameAsFieldValue = new ArrayList<>();
      codeUriFieldValue.add(new ImmutablePair<>(EntityDao.OWL_SAME_AS_FIELD, uri));

      //Create the list of suppliers that we'll use to find first match in order
      final List<FailableSupplier<Set<EntityWrapper>, IOException>> getters = Arrays.asList(
          () -> getEntitiesAndConvert(EntityType.AGENT, codeUriFieldValue),
          () -> getEntitiesAndConvert(EntityType.CONCEPT, codeUriFieldValue),
          () -> getEntitiesAndConvert(EntityType.TIMESPAN, codeUriFieldValue),
          () -> getEntitiesAndConvert(EntityType.PLACE, codeUriFieldValue),
          () -> getEntitiesAndConvert(EntityType.AGENT, owlSameAsFieldValue),
          () -> getEntitiesAndConvert(EntityType.CONCEPT, owlSameAsFieldValue),
          () -> getEntitiesAndConvert(EntityType.TIMESPAN, owlSameAsFieldValue),
          () -> getEntitiesAndConvert(EntityType.PLACE, owlSameAsFieldValue)
      );

      return getFirstMatch(getters);
    } catch (RuntimeException | IOException e) {
      LOGGER.warn("Unable to retrieve entity from id", e);
    }
    return null;
  }

  public EntityWrapper getByCodeUri(String codeUri) {
    try {
      final List<Pair<String, String>> codeUriFieldValue = new ArrayList<>();
      codeUriFieldValue.add(new ImmutablePair<>(EntityDao.CODE_URI_FIELD, codeUri));

      //Create the list of suppliers that we'll use to find first match in order
      final List<FailableSupplier<Set<EntityWrapper>, IOException>> entityWrapperSuppliers = Arrays
          .asList(
              () -> getEntitiesAndConvert(EntityType.AGENT, codeUriFieldValue),
              () -> getEntitiesAndConvert(EntityType.CONCEPT, codeUriFieldValue),
              () -> getEntitiesAndConvert(EntityType.TIMESPAN, codeUriFieldValue),
              () -> getEntitiesAndConvert(EntityType.PLACE, codeUriFieldValue)
          );

      return getFirstMatch(entityWrapperSuppliers);
    } catch (RuntimeException | IOException e) {
      LOGGER.warn("Unable to retrieve entity from codeUri", e);
    }
    return null;
  }

  private EntityWrapper getFirstMatch(
      List<FailableSupplier<Set<EntityWrapper>, IOException>> suppliers)
      throws IOException {
    for (FailableSupplier<Set<EntityWrapper>, IOException> supplier : suppliers) {
      final Set<EntityWrapper> entityWrappers = supplier.get();
      if (CollectionUtils.isNotEmpty(entityWrappers)) {
        return entityWrappers.iterator().next();
      }
    }
    return null;
  }

  private Set<EntityWrapper> getEntitiesAndConvert(EntityType entityType,
      List<Pair<String, String>> fieldNamesAndValues)
      throws JsonProcessingException {
    final List<? extends MongoTermList<? extends AbstractEdmEntityImpl>> mongoTermLists = entityDao
        .getAllMongoTermListsByFields(getEntityMongoTermListClass(entityType).mongoTermListClass,
            fieldNamesAndValues);
    return convertToEntityWrappers(entityType, null, mongoTermLists);
  }

  private List<EntityWrapper> findEntities(EntityType entityType, String termLabel,
      String termLanguage) throws JsonProcessingException {
    Set<EntityWrapper> entityWrapperSet = new HashSet<>();

    //Find all terms that match label and language
    final List<Pair<String, String>> fieldNamesAndValues = new ArrayList<>();
    fieldNamesAndValues.add(new ImmutablePair<>(EntityDao.LABEL_FIELD, termLabel));
    //If language not defined we are searching without specifying the language
    if (StringUtils.isNotBlank(termLanguage)) {
      fieldNamesAndValues.add(new ImmutablePair<>(EntityDao.LANG_FIELD, termLanguage));
    }
    final List<MongoTerm> mongoTerms = entityDao
        .getAllMongoTermsByFields(entityType, fieldNamesAndValues);

    for (MongoTerm mongoTerm : mongoTerms) {
      //Find mongoTermLists by codeUri
      fieldNamesAndValues.clear();
      fieldNamesAndValues
          .add(new ImmutablePair<>(EntityDao.CODE_URI_FIELD, mongoTerm.getCodeUri()));
      final List<? extends MongoTermList<? extends AbstractEdmEntityImpl>> mongoTermLists = entityDao
          .getAllMongoTermListsByFields(getEntityMongoTermListClass(entityType).mongoTermListClass,
              fieldNamesAndValues);
      //Find parent mongoTermLists
      final List<? extends MongoTermList<? extends AbstractEdmEntityImpl>> parentMongoTermLists = mongoTermLists
          .stream().map(mongoTermList -> findParentEntities(entityType, mongoTermList))
          .flatMap(List::stream).collect(Collectors.toList());

      //Convert to EntityWrappers
      entityWrapperSet = convertToEntityWrappers(entityType, mongoTerm.getOriginalLabel(),
          mongoTermLists);
      entityWrapperSet.addAll(
          convertToEntityWrappers(entityType, mongoTerm.getOriginalLabel(), parentMongoTermLists));
    }

    return new ArrayList<>(entityWrapperSet);
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
        .add(new ImmutablePair<>(EntityDao.CODE_URI_FIELD, new ArrayList<>(parentCodeUris)));
    return entityDao.getAllMongoTermListsByFieldsInList(
        getEntityMongoTermListClass(entityType).mongoTermListClass, fieldNamesAndValues);
  }

  private Set<String> findParentCodeUris(EntityType entityType,
      MongoTermList<? extends AbstractEdmEntityImpl> termList) {
    final Set<String> parentEntities = new HashSet<>();
    MongoTermList<? extends AbstractEdmEntityImpl> currentMongoTermList = termList;
    while (StringUtils.isNotBlank(currentMongoTermList.getParent())) {
      currentMongoTermList = entityDao
          .findTermListByField(getEntityMongoTermListClass(entityType).mongoTermListClass,
              EntityDao.CODE_URI_FIELD, currentMongoTermList.getParent());
      //Break when there is no other parent available or when we have already encountered the codeUri
      if (currentMongoTermList == null || !parentEntities.add(currentMongoTermList.getCodeUri())) {
        break;
      }
    }
    return parentEntities;
  }

  private Set<EntityWrapper> convertToEntityWrappers(EntityType entityType, String originalLabel,
      List<? extends MongoTermList<? extends AbstractEdmEntityImpl>> mongoTermLists)
      throws JsonProcessingException {
    Set<EntityWrapper> entityWrapperSet = new HashSet<>();
    for (MongoTermList<? extends AbstractEdmEntityImpl> mongoTermList : mongoTermLists) {
      EntityWrapper entityWrapper = new EntityWrapper();
      entityWrapper.setEntityType(entityType);
      entityWrapper.setUrl(mongoTermList.getCodeUri());
      entityWrapper.setOriginalValue(originalLabel);
      entityWrapper.setContextualEntity(
          this.getObjectMapper().writeValueAsString(mongoTermList.getRepresentation()));
      entityWrapperSet.add(entityWrapper);
    }
    return entityWrapperSet;
  }

  private static class EntityInfo<T extends MongoTermList<S>, S extends AbstractEdmEntityImpl> {

    protected final EntityType entityType;
    protected final Class<T> mongoTermListClass;

    EntityInfo(EntityType entityType, Class<T> mongoTermListClass) {
      this.entityType = entityType;
      this.mongoTermListClass = mongoTermListClass;
    }
  }

  private static EntityInfo<?, ?> getEntityMongoTermListClass(EntityType entityType) {
    return ENTITY_INFOS.stream().filter((entityInfo) -> entityType == entityInfo.entityType)
        .findFirst().orElse(null);

  }

  private ObjectMapper getObjectMapper() {
    return OBJECT_MAPPER;
  }

}
