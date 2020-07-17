package eu.europeana.enrichment.service.dao;

import com.mongodb.MongoClient;
import dev.morphia.AdvancedDatastore;
import dev.morphia.Key;
import dev.morphia.Morphia;
import dev.morphia.query.Query;
import dev.morphia.query.Sort;
import dev.morphia.query.internal.MorphiaCursor;
import eu.europeana.corelib.solr.entity.AbstractEdmEntityImpl;
import eu.europeana.corelib.solr.entity.ContextualClassImpl;
import eu.europeana.enrichment.api.internal.AgentTermList;
import eu.europeana.enrichment.api.internal.ConceptTermList;
import eu.europeana.enrichment.api.internal.MongoTerm;
import eu.europeana.enrichment.api.internal.MongoTermList;
import eu.europeana.enrichment.api.internal.OrganizationTermList;
import eu.europeana.enrichment.api.internal.PlaceTermList;
import eu.europeana.enrichment.api.internal.TimespanTermList;
import eu.europeana.enrichment.utils.EntityType;
import eu.europeana.metis.utils.ExternalRequestUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Data Acces Object for accessing enrichment entities from Mongo.
 *
 * @author Simon Tzanakis
 * @since 2020-07-07
 */
public class EnrichmentDao {

  private final AdvancedDatastore datastore;

  public static final String ID_FIELD = "_id";
  private static final String ENTITY_TYPE_FIELD = "entityType";
  public static final String CODE_URI_FIELD = "codeUri";
  private static final String MODIFIED_FIELD = "modified";
  public static final String OWL_SAME_AS_FIELD = "owlSameAs";
  public static final String LABEL_FIELD = "label";
  public static final String LANG_FIELD = "lang";

  //Entity types
  private static final String CONCEPT_TYPE = "ConceptImpl";
  private static final String PLACE_TYPE = "PlaceImpl";
  private static final String AGENT_TYPE = "AgentImpl";
  private static final String TIMESPAN_TYPE = "TimespanImpl";
  public static final String ORGANIZATION_TYPE = "OrganizationImpl";

  //Table names
  private static final String AGENT_TABLE = "people";
  private static final String CONCEPT_TABLE = "concept";
  private static final String PLACE_TABLE = "place";
  private static final String TIMESPAN_TABLE = "period";
  public static final String ORGANIZATION_TABLE = "organization";
  private final MongoClient mongoClient;


  /**
   * Parameter constructor.
   *
   * @param mongoClient the previously initialized mongo client
   * @param databaseName the database name
   */
  public EnrichmentDao(MongoClient mongoClient, String databaseName) {
    this.mongoClient = mongoClient;

    // Register the mappings and set up the data store.
    final Morphia morphia = new Morphia();
    morphia.map(MongoTermList.class);
    morphia.map(AgentTermList.class);
    morphia.map(ConceptTermList.class);
    morphia.map(PlaceTermList.class);
    morphia.map(TimespanTermList.class);
    morphia.map(OrganizationTermList.class);
    this.datastore = (AdvancedDatastore) morphia.createDatastore(this.mongoClient, databaseName);
  }

  /**
   * Get the mongoTermList by using a provided field name and it's value.
   * <p>Returns the first entity found</p>
   * <p>The {@code mongoTermListType} class type has to be provided so that the correct retrieval
   * from the database would occur. Those are {@link AgentTermList}, {@link ConceptTermList}, {@link
   * PlaceTermList}, {@link TimespanTermList}, {@link OrganizationTermList}.</p>
   *
   * @param mongoTermListType the type of mongo term list
   * @param fieldName the field name
   * @param fieldValue the field value
   * @param <T> the subclass of {@link MongoTermList}
   * @param <S> the subclass of {@link AbstractEdmEntityImpl} which is used from {@link T}
   * @return the retrieved mongo term list
   */
  public <T extends MongoTermList<S>, S extends AbstractEdmEntityImpl> MongoTermList<S> getTermListByField(
      Class<T> mongoTermListType, String fieldName, String fieldValue) {
    return ExternalRequestUtil.retryableExternalRequestConnectionReset(
        () -> this.datastore.find(mongoTermListType).filter(fieldName, fieldValue).first());
  }

  /**
   * Get a list of mongoTermList by using a provided pair of field names and values.
   * <p>The {@code mongoTermListType} class type has to be provided so that the correct retrieval
   * from the database would occur. Those are {@link AgentTermList}, {@link ConceptTermList}, {@link
   * PlaceTermList}, {@link TimespanTermList}, {@link OrganizationTermList}.</p>
   * <p>Convenience method to avoid needless list generation if {@link
   * #getAllMongoTermListsByFieldsInList} was used.</p>
   *
   * @param mongoTermListType the type of mongo term list
   * @param fieldNameAndValues the list of pairs with key being the fieldName and value being the
   * fieldValue
   * @param <T> the subclass of {@link MongoTermList}
   * @param <S> the subclass of {@link AbstractEdmEntityImpl} which is used from {@link T}
   * @return the retrieved list of mongoTermList
   */
  public <T extends MongoTermList<S>, S extends AbstractEdmEntityImpl> List<T> getAllMongoTermListsByFields(
      Class<T> mongoTermListType, List<Pair<String, String>> fieldNameAndValues) {
    final Query<T> query = datastore.createQuery(mongoTermListType);
    for (Pair<String, String> fieldNameAndValue : fieldNameAndValues) {
      query.filter(fieldNameAndValue.getKey(), fieldNameAndValue.getValue());
    }
    return getListOfQuery(query);
  }

  /**
   * Get a list of mongoTermList by using a provided pair of field names and per field name a list
   * of values.
   * <p>The {@code mongoTermListType} class type has to be provided so that the correct retrieval
   * from the database would occur. Those are {@link AgentTermList}, {@link ConceptTermList}, {@link
   * PlaceTermList}, {@link TimespanTermList}, {@link OrganizationTermList}.</p>
   *
   * @param mongoTermListType the type of mongo term list
   * @param fieldNameAndValues the list of pairs with key being the fieldName and value being a list
   * of fieldValues
   * @param <T> the subclass of {@link MongoTermList}
   * @param <S> the subclass of {@link AbstractEdmEntityImpl} which is used from {@link T}
   * @return the retrieved list of mongoTermList
   */
  public <T extends MongoTermList<S>, S extends AbstractEdmEntityImpl> List<T> getAllMongoTermListsByFieldsInList(
      Class<T> mongoTermListType, List<Pair<String, List<String>>> fieldNameAndValues) {
    final Query<T> query = datastore.createQuery(mongoTermListType);
    for (Pair<String, List<String>> fieldNameAndValue : fieldNameAndValues) {
      query.field(fieldNameAndValue.getKey()).in(fieldNameAndValue.getValue());
    }
    return getListOfQuery(query);
  }

  private MongoTerm findMongoTermByField(String entityType, String fieldName, String fieldValue) {
    return ExternalRequestUtil.retryableExternalRequestConnectionReset(
        () -> this.datastore.find(entityType, MongoTerm.class).filter(fieldName, fieldValue)
            .first());
  }

  /**
   * Get a list of all MongoTerms that match the criteria.
   * <p>The {@code entityType} parameter is required because it's used to access the correct
   * database collection.</p>
   *
   * @param entityType the entity type
   * @param fieldNameAndValues the list of pairs with key being the fieldName and value being the
   * fieldValue
   * @return the retrieved list of MongoTerm
   */
  public List<MongoTerm> getAllMongoTermsByFields(EntityType entityType,
      List<Pair<String, String>> fieldNameAndValues) {
    Query<MongoTerm> query = this.datastore.createQuery(getTableName(entityType), MongoTerm.class);
    for (Pair<String, String> fieldNameAndValue : fieldNameAndValues) {
      query.filter(fieldNameAndValue.getKey(), fieldNameAndValue.getValue());
    }
    return getListOfQuery(query);
  }

  /**
   * Get the date of the latest modified entity in {@link MongoTermList}.
   * <p>The {@code entityType} parameter is used to filter the specific entities</p>
   *
   * @param entityType the entity type
   * @return the date of the latest modified entity
   */
  public Date getDateOfLastModifiedEntity(EntityType entityType) {
    Query<MongoTermList> query = datastore.createQuery(MongoTermList.class);
    query.filter(ENTITY_TYPE_FIELD, getEntityType(entityType));
    query.order(Sort.descending(MODIFIED_FIELD));
    final MongoTermList mongoTermList = ExternalRequestUtil
        .retryableExternalRequestConnectionReset(query::first);

    Date dateModified = null;
    if (Objects.nonNull(mongoTermList)) {
      dateModified = mongoTermList.getModified();
    }
    return dateModified;
  }

  /**
   * Save a MongoTermList in the database
   *
   * @param mongoTermList the item to save
   * @return the key of the saved item
   */
  public String saveTermList(MongoTermList<? extends AbstractEdmEntityImpl> mongoTermList) {
    Key<MongoTermList<? extends AbstractEdmEntityImpl>> datasetKey = ExternalRequestUtil
        .retryableExternalRequestConnectionReset(
            () -> this.datastore.save(mongoTermList));
    return datasetKey == null ? StringUtils.EMPTY : datasetKey.getId().toString();
  }

  /**
   * Save a list of MongoTerms in the database.
   * <p>The {@code entityType} is used to store to the correct database collection.</p>
   *
   * @param mongoTerms the list of items
   * @param entityType the entity type
   */
  public void saveMongoTerms(List<MongoTerm> mongoTerms, EntityType entityType) {
    ExternalRequestUtil.retryableExternalRequestConnectionReset(
        () -> this.datastore.save(getTableName(entityType), mongoTerms));
  }

  /**
   * Save a mongoTermls from a contextual entity.
   *
   * @param entity the item to save
   * @param entityType the entity type
   * @return the total items saved in the database
   */
  public int saveMongoTermsFromEntity(ContextualClassImpl entity, EntityType entityType) {
    List<MongoTerm> mongoTerms = createListOfMongoTerms(entity);
    saveMongoTerms(mongoTerms, entityType);
    return mongoTerms.size();
  }


  private static List<MongoTerm> createListOfMongoTerms(ContextualClassImpl entity) {
    MongoTerm mongoTerm;
    List<MongoTerm> mongoTerms = new ArrayList<>();

    for (Map.Entry<String, List<String>> prefLabel : entity.getPrefLabel().entrySet()) {
      for (String label : prefLabel.getValue()) {
        mongoTerm = new MongoTerm();
        mongoTerm.setCodeUri(entity.getAbout());
        mongoTerm.setLabel(label.toLowerCase());
        mongoTerm.setOriginalLabel(label);
        mongoTerm.setLang(prefLabel.getKey());
        mongoTerms.add(mongoTerm);
      }
    }
    return mongoTerms;
  }

  /**
   * Delete entities that match the provided code uris.
   * <p>Removes entities from all MongoTerm collections</p>
   *
   * @param codeUris the list of code uris
   * @return the list of code uris that a removal was performed
   */
  public List<String> deleteAllEntitiesMatching(List<String> codeUris) {
    List<String> removedCodeUris = new ArrayList<>();
    for (String codeUri : codeUris) {
      removedCodeUris.add(codeUri);
      removedCodeUris.addAll(deleteEntities(getTableName(EntityType.PLACE), PLACE_TYPE, codeUri));
      removedCodeUris
          .addAll(deleteEntities(getTableName(EntityType.CONCEPT), CONCEPT_TYPE, codeUri));
      removedCodeUris.addAll(deleteEntities(getTableName(EntityType.AGENT), AGENT_TYPE, codeUri));
      removedCodeUris
          .addAll(deleteEntities(getTableName(EntityType.TIMESPAN), TIMESPAN_TYPE, codeUri));
      removedCodeUris.addAll(
          deleteEntities(getTableName(EntityType.ORGANIZATION), ORGANIZATION_TYPE, codeUri));
    }
    return removedCodeUris;
  }

  /**
   * Delete entities that match the provided code uri.
   * <p>Removes entities from the corresponding MongoTermList using {@code entityType} and from the
   * corresponding MongoTerm collection using {@code entityTable}</p>.
   *
   * @param entityTable the entity table name
   * @param entityType the entity type string
   * @param codeUri the code uri to match
   * @return a list of all the removed code uris except the provided one
   */
  public List<String> deleteEntities(String entityTable, String entityType, String codeUri) {
    List<String> extraUrisRemoved = new ArrayList<>();
    //Remove from Term List
    deleteMongoTermList(codeUri);
    //Remove from specific collection
    deleteMongoTerm(entityTable, codeUri);

    //Find all TermLists that have owlSameAs equals with codeUri
    final Query<MongoTermList> termListsSameAsQuery = this.datastore
        .createQuery(MongoTermList.class).filter(ENTITY_TYPE_FIELD, entityType)
        .filter(OWL_SAME_AS_FIELD, codeUri);
    final List<MongoTermList> allTermListsSameAs = getListOfQuery(termListsSameAsQuery);
    for (MongoTermList mongoTermList : allTermListsSameAs) {
      final String sameAsCodeUri = mongoTermList.getCodeUri();
      extraUrisRemoved.add(sameAsCodeUri);
      //Remove from Term List
      deleteMongoTermList(codeUri);
      //Remove from specific collection
      deleteMongoTerm(entityTable, sameAsCodeUri);
    }
    return extraUrisRemoved;
  }

  /**
   * Delete a MongoTerm using a code uri.
   *
   * @param entityTable the entity table name
   * @param codeUri the code uri to match
   */
  public void deleteMongoTerm(String entityTable, String codeUri) {
    final MongoTerm mongoTermCoreUri = findMongoTermByField(entityTable, CODE_URI_FIELD, codeUri);
    ExternalRequestUtil.retryableExternalRequestConnectionReset(
        () -> this.datastore.delete(entityTable, MongoTerm.class, mongoTermCoreUri.getId()));
  }

  private void deleteMongoTermList(String codeUri) {
    ExternalRequestUtil.retryableExternalRequestConnectionReset(
        () -> this.datastore.delete(
            this.datastore.createQuery(MongoTermList.class).filter(CODE_URI_FIELD, codeUri)));
  }

  private String getTableName(EntityType entityType) {
    final String result;
    switch (entityType) {
      case AGENT:
        result = AGENT_TABLE;
        break;
      case CONCEPT:
        result = CONCEPT_TABLE;
        break;
      case PLACE:
        result = PLACE_TABLE;
        break;
      case TIMESPAN:
        result = TIMESPAN_TABLE;
        break;
      case ORGANIZATION:
        result = ORGANIZATION_TABLE;
        break;
      default:
        throw new IllegalStateException("Unknown entity: " + entityType);
    }
    return result;
  }

  private String getEntityType(EntityType entityType) {
    final String result;
    switch (entityType) {
      case AGENT:
        result = AGENT_TYPE;
        break;
      case CONCEPT:
        result = CONCEPT_TYPE;
        break;
      case PLACE:
        result = PLACE_TYPE;
        break;
      case TIMESPAN:
        result = TIMESPAN_TYPE;
        break;
      case ORGANIZATION:
        result = ORGANIZATION_TYPE;
        break;
      default:
        throw new IllegalStateException("Unknown entity: " + entityType);
    }
    return result;
  }

  private <T> List<T> getListOfQuery(Query<T> query) {
    return ExternalRequestUtil.retryableExternalRequestConnectionReset(() -> {
      try (MorphiaCursor<T> cursor = query.find()) {
        return cursor.toList();
      }
    });
  }

  public void close() {
    this.mongoClient.close();
  }

}
