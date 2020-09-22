package eu.europeana.metis.dereference.service.dao;

import static eu.europeana.metis.utils.SonarqubeNullcheckAvoidanceUtils.performFunction;

import com.mongodb.client.MongoClient;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.mapping.DiscriminatorFunction;
import dev.morphia.mapping.MapperOptions;
import dev.morphia.mapping.NamingStrategy;
import dev.morphia.query.Query;
import dev.morphia.query.experimental.filters.Filters;
import dev.morphia.query.internal.MorphiaCursor;
import eu.europeana.metis.dereference.Vocabulary;
import java.util.List;
import java.util.regex.Pattern;
import org.bson.types.ObjectId;

/**
 * Dao for vocabularies Created by ymamakis on 2/11/16.
 */

public class VocabularyDao {

  private final Datastore datastore;

  public VocabularyDao(MongoClient mongo, String db) {
    final MapperOptions mapperOptions = MapperOptions.builder().discriminatorKey("className")
        .discriminator(DiscriminatorFunction.className())
        .collectionNaming(NamingStrategy.identity()).build();
    datastore = Morphia.createDatastore(mongo, db, mapperOptions);
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
    try (final MorphiaCursor<Vocabulary> cursor = query.iterator()) {
      return performFunction(cursor, MorphiaCursor::toList);
    }
  }

  /**
   * Retrieve all the vocabularies
   *
   * @return A list of all the vocabularies
   */
  public List<Vocabulary> getAll() {
    final Query<Vocabulary> query = datastore.find(Vocabulary.class);
    try (final MorphiaCursor<Vocabulary> cursor = query.iterator()) {
      return performFunction(cursor, MorphiaCursor::toList);
    }
  }

  /**
   * Retrieve the vocabulary based on its ID.
   *
   * @param vocabularyId The ID of the vocabulary to retrieve.
   * @return A list of all the vocabularies
   */
  public Vocabulary get(String vocabularyId) {
    return datastore.find(Vocabulary.class).filter(Filters.eq("_id", new ObjectId(vocabularyId))).first();
  }

  /**
   * Remove all vocabularies and replace them with the new list.
   *
   * @param vocabularies The new vocabularies.
   */
  public void replaceAll(List<Vocabulary> vocabularies) {
    datastore.find(Vocabulary.class).delete();
    datastore.save(vocabularies);
  }

  protected Datastore getDatastore() {
    return datastore;
  }
}
