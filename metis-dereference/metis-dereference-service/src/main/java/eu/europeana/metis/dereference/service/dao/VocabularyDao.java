package eu.europeana.metis.dereference.service.dao;

import static eu.europeana.metis.mongo.utils.MorphiaUtils.getListOfQueryRetryable;
import static eu.europeana.metis.network.ExternalRequestUtil.retryableExternalRequestForNetworkExceptions;

import com.mongodb.client.MongoClient;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.mapping.DiscriminatorFunction;
import dev.morphia.mapping.MapperOptions;
import dev.morphia.mapping.NamingStrategy;
import dev.morphia.query.Query;
import dev.morphia.query.experimental.filters.Filters;
import eu.europeana.metis.dereference.Vocabulary;
import java.util.List;
import java.util.regex.Pattern;
import org.bson.types.ObjectId;

/**
 * Dao for vocabularies Created by ymamakis on 2/11/16.
 */

public class VocabularyDao {

  private final Datastore datastore;

  /**
   * Parameter constructor.
   *
   * @param mongoClient the previously initialized mongo client
   * @param databaseName the database name
   */
  public VocabularyDao(MongoClient mongoClient, String databaseName) {
    final MapperOptions mapperOptions = MapperOptions.builder().discriminatorKey("className")
        .discriminator(DiscriminatorFunction.className())
        .collectionNaming(NamingStrategy.identity()).build();
    datastore = Morphia.createDatastore(mongoClient, databaseName, mapperOptions);
    datastore.getMapper().map(Vocabulary.class);
  }

  /**
   * Retrieve the vocabularies for which the URL contains the given search string.
   *
   * @param searchString The string to search on
   * @return The list of vocabularies. Is not null.
   */
  public List<Vocabulary> getByUriSearch(String searchString) {
    final Pattern pattern = Pattern.compile(Pattern.quote(searchString));
    final Query<Vocabulary> query = datastore.find(Vocabulary.class);
    query.filter(Filters.eq("uris", pattern));
    return getListOfQueryRetryable(query);
  }

  /**
   * Retrieve all the vocabularies
   *
   * @return A list of all the vocabularies
   */
  public List<Vocabulary> getAll() {
    final Query<Vocabulary> query = datastore.find(Vocabulary.class);
    return getListOfQueryRetryable(query);
  }

  /**
   * Retrieve the vocabulary based on its ID.
   *
   * @param vocabularyId The ID of the vocabulary to retrieve.
   * @return A list of all the vocabularies
   */
  public Vocabulary get(String vocabularyId) {
    return retryableExternalRequestForNetworkExceptions(
        () -> datastore.find(Vocabulary.class).filter(Filters.eq("_id", new ObjectId(vocabularyId)))
            .first());
  }

  /**
   * Remove all vocabularies and replace them with the new list.
   *
   * @param vocabularies The new vocabularies.
   */
  public void replaceAll(List<Vocabulary> vocabularies) {
    retryableExternalRequestForNetworkExceptions(() -> datastore.find(Vocabulary.class).delete());
    vocabularies.forEach(vocabulary -> vocabulary.setId(new ObjectId()));
    retryableExternalRequestForNetworkExceptions(() -> datastore.save(vocabularies));
  }

  protected Datastore getDatastore() {
    return datastore;
  }
}
