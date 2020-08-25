package eu.europeana.enrichment.service.dao;

import static eu.europeana.metis.utils.SonarqubeNullcheckAvoidanceUtils.performFunction;

import com.mongodb.MongoClient;
import dev.morphia.Datastore;
import dev.morphia.Key;
import dev.morphia.Morphia;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.morphia.query.Sort;
import dev.morphia.query.experimental.filters.Filters;
import dev.morphia.query.internal.MorphiaCursor;
import eu.europeana.enrichment.api.external.model.EnrichmentTerm;
import eu.europeana.enrichment.utils.EntityType;
import eu.europeana.metis.utils.ExternalRequestUtil;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;

/**
 * Data Access Object for accessing enrichment entities from Mongo.
 *
 * @author Simon Tzanakis
 * @since 2020-07-07
 */
public class EnrichmentDao {

  private final Datastore datastore;

  public static final String ID_FIELD = "_id";
  public static final String ENTITY_TYPE_FIELD = "entityType";
  public static final String CODE_URI_FIELD = "codeUri";
  private static final String UPDATED_FIELD = "updated";
  public static final String OWL_SAME_AS_FIELD = "owlSameAs";
  public static final String LABEL_FIELD = "labelInfos.lowerCaseLabel";
  public static final String LANG_FIELD = "labelInfos.lang";

  private final MongoClient mongoClient;

  /**
   * Parameter constructor.
   *
   * @param mongoClient the previously initialized mongo client
   * @param databaseName the database name
   */
  public EnrichmentDao(MongoClient mongoClient, String databaseName) {
    this.mongoClient = mongoClient;
    this.datastore = Morphia
        .createDatastore((com.mongodb.client.MongoClient) this.mongoClient, databaseName);
    this.datastore.getMapper().map(EnrichmentTerm.class);
  }

  /**
   * Get the enrichmentTerm by using a provided field name and it's value.
   * <p>Returns the first entity found</p>
   *
   * @param fieldName the field name
   * @param fieldValue the field value
   * @return the retrieved enrichment term
   */
  public Optional<EnrichmentTerm> getEnrichmentTermByField(String fieldName, String fieldValue) {
    return ExternalRequestUtil.retryableExternalRequestConnectionReset(() -> Optional.ofNullable(
        this.datastore.find(EnrichmentTerm.class).filter(Filters.eq(fieldName, fieldValue))
            .first()));
  }

  /**
   * Get the enrichmentTerm {@link ObjectId} by using a provided field name and it's value.
   * <p>Returns the first entity found</p>
   *
   * @param fieldName the field name
   * @param fieldValue the field value
   * @return the retrieved enrichment term object id if present
   */
  public Optional<ObjectId> getEnrichmentTermObjectIdByField(String fieldName, String fieldValue) {
    return ExternalRequestUtil.retryableExternalRequestConnectionReset(() -> {
      final Optional<EnrichmentTerm> enrichmentTerm = Optional.ofNullable(this.datastore
          .find(EnrichmentTerm.class)
          .filter(Filters.eq(fieldName, fieldValue))
          .first(new FindOptions().projection().include("_id")));
      return enrichmentTerm.map(EnrichmentTerm::getId);
    });
  }

  /**
   * Get a list of enrichmentTerm by using a provided pair of field names and values.
   * <p>Convenience method to avoid needless list generation if {@link
   * #getAllEnrichmentTermsByFieldsInList} was used. Order of supplied field pairs matter on the
   * query performance.</p>
   *
   * @param fieldNameAndValues the list of pairs with key being the fieldName and value being the
   * fieldValue
   * @return the retrieved list of enrichmentTerm
   */
  public List<EnrichmentTerm> getAllEnrichmentTermsByFields(
      List<Pair<String, String>> fieldNameAndValues) {
    final Query<EnrichmentTerm> query = datastore.find(EnrichmentTerm.class);
    for (Pair<String, String> fieldNameAndValue : fieldNameAndValues) {
      query.filter(Filters.eq(fieldNameAndValue.getKey(), fieldNameAndValue.getValue()));
    }
    return getListOfQuery(query);
  }

  /**
   * Get a list of enrichmentTerm by using a provided pair of field names and per field name a list
   * of values.
   * <p>Order of supplied field pairs matter on the query performance.</p>
   *
   * @param fieldNameAndValues the list of pairs with key being the fieldName and value being a list
   * of fieldValues
   * @return the retrieved list of enrichmentTerm
   */
  public List<EnrichmentTerm> getAllEnrichmentTermsByFieldsInList(
      List<Pair<String, List<String>>> fieldNameAndValues) {
    final Query<EnrichmentTerm> query = datastore.find(EnrichmentTerm.class);
    for (Pair<String, List<String>> fieldNameAndValue : fieldNameAndValues) {
      query.filter(Filters.in(fieldNameAndValue.getKey(), fieldNameAndValue.getValue());
    }
    return getListOfQuery(query);
  }

  /**
   * Get the date of the latest modified entity in {@link EnrichmentTerm}.
   * <p>The {@code entityType} parameter is used to filter the specific entities</p>
   *
   * @param entityType the entity type
   * @return the date of the latest modified entity
   */
  public Date getDateOfLastUpdatedEnrichmentTerm(EntityType entityType) {
    Query<EnrichmentTerm> query = datastore.find(EnrichmentTerm.class);
    query.filter(Filters.eq(ENTITY_TYPE_FIELD, entityType));
    query.order(Sort.descending(UPDATED_FIELD));
    final EnrichmentTerm enrichmentTerm = ExternalRequestUtil
        .retryableExternalRequestConnectionReset(query::first);

    Date dateUpdated = null;
    if (Objects.nonNull(enrichmentTerm)) {
      dateUpdated = enrichmentTerm.getUpdated();
    }
    return dateUpdated;
  }

  /**
   * Save an enrichmentTerm in the database
   *
   * @param enrichmentTerm the item to save
   * @return the key of the saved item
   */
  public String saveEnrichmentTerm(EnrichmentTerm enrichmentTerm) {
    EnrichmentTerm enrichmentTermSaved = ExternalRequestUtil
        .retryableExternalRequestConnectionReset(
            () -> this.datastore.save(enrichmentTerm));
    return enrichmentTermSaved == null ? StringUtils.EMPTY : enrichmentTermSaved.getId().toString();
  }

  /**
   * Delete enrichmentTerms that match the provided codeUris.
   * <p>Removes entities from the corresponding enrichmentTerm using {@code entityType}.
   * It also removes entities that match with the provided codeUris with owlSameAs.</p>
   *
   * @param entityType the entity type string
   * @param codeUris the code uri to match
   * @return a list of all the removed code uris except the provided one
   */
  public List<String> deleteEnrichmentTerms(EntityType entityType, List<String> codeUris) {
    //Remove from EnrichmentTerm
    deleteEnrichmentTerm(codeUris);

    //Find all TermLists that have owlSameAs equals with codeUri
    final Query<EnrichmentTerm> enrichmentTermsSameAsQuery = this.datastore
        .find(EnrichmentTerm.class).filter(Filters.eq(ENTITY_TYPE_FIELD, entityType))
        .filter(Filters.in(OWL_SAME_AS_FIELD, codeUris);
    final List<EnrichmentTerm> enrichmentTermsOwlSameAs = getListOfQuery(
        enrichmentTermsSameAsQuery);
    final List<String> sameAsCodeUris = enrichmentTermsOwlSameAs.stream()
        .map(EnrichmentTerm::getCodeUri)
        .collect(Collectors.toList());
    //Remove from EnrichmentTerm
    deleteEnrichmentTerm(sameAsCodeUris);
    return sameAsCodeUris;
  }

  private void deleteEnrichmentTerm(List<String> codeUri) {
    ExternalRequestUtil.retryableExternalRequestConnectionReset(() -> this.datastore.delete(
        this.datastore.find(EnrichmentTerm.class).filter(Filters.in(CODE_URI_FIELD, codeUri)));
  }

  private <T> List<T> getListOfQuery(Query<T> query) {
    return ExternalRequestUtil.retryableExternalRequestConnectionReset(() -> {
      try (MorphiaCursor<T> cursor = query.find()) {
        return performFunction(cursor, MorphiaCursor::toList);
      }
    });
  }

  public void close() {
    this.mongoClient.close();
  }
}
