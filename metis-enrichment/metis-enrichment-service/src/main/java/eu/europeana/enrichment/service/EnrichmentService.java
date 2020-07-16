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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Simon Tzanakis
 * @since 2020-07-16
 */
@Service
public class EnrichmentService {

  private static final int LANGUAGE_TAG_LENGTH = 2;
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final Pattern PATTERN_MATCHING_VERY_BROAD_TIMESPANS = Pattern
      .compile("http://semium.org/time/(ChronologicalPeriod$|Time$|(AD|BC)[1-9]x{3}$)");
  private final EntityDao entityDao;

  // TODO: 7/16/20 Try to eliminate EntityWrapper
  // TODO: 7/16/20 Check if everything has to be list or single values

  @Autowired
  public EnrichmentService(EntityDao entityDao) {
    this.entityDao = entityDao;
  }

  public List<EntityWrapper> findEntitiesBasedOnValues(List<InputValue> inputValues)
      throws JsonProcessingException {
    List<EntityWrapper> entityWrappers = new ArrayList<>();
    for (InputValue inputValue : inputValues) {
      if (inputValue.getVocabularies() == null) {
        continue;
      }
      for (EntityType entityType : inputValue.getVocabularies()) {
        entityWrappers.addAll(findEntities(entityType, inputValue.getValue().toLowerCase(Locale.US),
            inputValue.getLanguage()));
      }
    }
    return entityWrappers;
  }

  public EntityWrapper getByCodeUriOrOwlSameAs(String uri) throws IOException {
    final List<Pair<String, String>> codeUriFieldValue = new ArrayList<>();
    codeUriFieldValue.add(new ImmutablePair<>(EntityDao.CODE_URI_FIELD, uri));
    final List<Pair<String, String>> owlSameAsFieldValue = new ArrayList<>();
    codeUriFieldValue.add(new ImmutablePair<>(EntityDao.OWL_SAME_AS_FIELD, uri));

    // Get the result providers - this is constant and does not depend on the input.
    final List<GetterByUri> getters = Arrays.asList(
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
  }

  public EntityWrapper getByCodeUri(String codeUri) throws IOException {
    final List<Pair<String, String>> codeUriFieldValue = new ArrayList<>();
    codeUriFieldValue.add(new ImmutablePair<>(EntityDao.CODE_URI_FIELD, codeUri));

    // Get the result providers - this is constant and does not depend on the input.
    final List<GetterByUri> getters = Arrays.asList(
        () -> getEntitiesAndConvert(EntityType.AGENT, codeUriFieldValue),
        () -> getEntitiesAndConvert(EntityType.CONCEPT, codeUriFieldValue),
        () -> getEntitiesAndConvert(EntityType.TIMESPAN, codeUriFieldValue),
        () -> getEntitiesAndConvert(EntityType.PLACE, codeUriFieldValue)
    );

    return getFirstMatch(getters);
  }

  private Set<EntityWrapper> getEntitiesAndConvert(EntityType entityType,
      List<Pair<String, String>> fieldNamesAndValues)
      throws JsonProcessingException {
    final List<? extends MongoTermList<? extends AbstractEdmEntityImpl>> mongoTermLists = entityDao
        .getAllMongoTermListsByFields(getEntityMongoTermListClass(entityType), fieldNamesAndValues);
    return convertToEntityWrappers(entityType, null, mongoTermLists);
  }

  private EntityWrapper getFirstMatch(List<GetterByUri> getters)
      throws IOException {
    for (GetterByUri getter : getters) {
      final Set<EntityWrapper> byUri = getter.getByUri();
      if (CollectionUtils.isNotEmpty(byUri)) {
        return byUri.iterator().next();
      }
    }
    return null;
  }

  @FunctionalInterface
  private interface GetterByUri {

    Set<EntityWrapper> getByUri() throws IOException;
  }

  private List<EntityWrapper> findEntities(EntityType entityType, String termLabel,
      String termLanguage) throws JsonProcessingException {
    Set<EntityWrapper> entityWrapperSet = new HashSet<>();

    //Find all terms that match language and label
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
          .getAllMongoTermListsByFields(getEntityMongoTermListClass(entityType),
              fieldNamesAndValues);
      //Find parent mongoTermLists
      final List<? extends MongoTermList<? extends AbstractEdmEntityImpl>> parentMongoTermLists = findParentEntities(
          entityType, (MongoTermList<?>) mongoTermLists);

      //Convert to EntityWrappers
      entityWrapperSet = convertToEntityWrappers(entityType, mongoTerm.getOriginalLabel(),
          mongoTermLists);
      entityWrapperSet.addAll(
          convertToEntityWrappers(entityType, mongoTerm.getOriginalLabel(), parentMongoTermLists));
    }

    return new ArrayList<>(entityWrapperSet);
  }

  private List<? extends MongoTermList<? extends AbstractEdmEntityImpl>> findParentEntities(
      EntityType entityType,
      MongoTermList<?> mongoTermLists) {
    // TODO: 7/16/20 optimize query in mongo to check on an array of core uris
    final Set<String> parentCodeUris = findParentCodeUris(entityType, mongoTermLists);
    final List<Pair<String, String>> fieldNamesAndValues = new ArrayList<>();
    final List<MongoTermList<?>> parentMongoTermLists = new ArrayList<>();
    for (String parentCodeUri : parentCodeUris) {
      //For timespans, do not get entities for very broad timespans
      if (entityType == EntityType.TIMESPAN && PATTERN_MATCHING_VERY_BROAD_TIMESPANS
          .matcher(parentCodeUri).matches()) {
        continue;
      }
      fieldNamesAndValues.clear();
      fieldNamesAndValues.add(new ImmutablePair<>(EntityDao.CODE_URI_FIELD, parentCodeUri));
      parentMongoTermLists.addAll(entityDao
          .getAllMongoTermListsByFields(getEntityMongoTermListClass(entityType),
              fieldNamesAndValues));
    }
    return parentMongoTermLists;
  }

  private Set<String> findParentCodeUris(EntityType entityType, MongoTermList<?> termList) {
    final Set<String> parentEntities = new HashSet<>();
    MongoTermList<?> currentMongoTermList = termList;
    while (StringUtils.isNotBlank(currentMongoTermList.getParent())) {
      currentMongoTermList = entityDao.findTermListByField(getEntityMongoTermListClass(entityType),
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

  private static Class<? extends MongoTermList<? extends AbstractEdmEntityImpl>> getEntityMongoTermListClass(
      EntityType entityType) {
    final Class<? extends MongoTermList<? extends AbstractEdmEntityImpl>> termListClass;
    switch (entityType) {
      case ORGANIZATION:
        termListClass = OrganizationTermList.class;
        break;
      case TIMESPAN:
        termListClass = TimespanTermList.class;
        break;
      case AGENT:
        termListClass = AgentTermList.class;
        break;
      case PLACE:
        termListClass = PlaceTermList.class;
        break;
      case CONCEPT:
        termListClass = ConceptTermList.class;
        break;
      default:
        throw new IllegalStateException("Unexpected value: " + entityType);
    }
    return termListClass;
  }

  private ObjectMapper getObjectMapper() {
    return OBJECT_MAPPER;
  }

}
