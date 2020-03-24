package eu.europeana.metis.dereference.service.dao;

import com.mongodb.MongoClient;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.query.Query;
import dev.morphia.query.internal.MorphiaCursor;
import eu.europeana.metis.dereference.Vocabulary;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Dao for vocabularies Created by ymamakis on 2/11/16.
 */

public class VocabularyDao {

  private Datastore ds;

  public VocabularyDao(MongoClient mongo, String db) {
    Morphia morphia = new Morphia();
    morphia.map(Vocabulary.class);

    ds = morphia.createDatastore(mongo, db);
  }

  /**
   * Retrieve the vocabularies for which the URL contains the given search string.
   *
   * @param searchString The string to search on
   * @return The list of vocabularies. Is not null.
   */
  public List<Vocabulary> getByUriSearch(String searchString) {
    final Pattern pattern = Pattern.compile(Pattern.quote(searchString));
    final Query<Vocabulary> query = ds.createQuery(Vocabulary.class);
    query.field("uri").equal(pattern);
    try (final MorphiaCursor<Vocabulary> cursor = query.find()) {
      return cursor.toList();
    }
  }

  /**
   * Retrieve all the vocabularies
   *
   * @return A list of all the vocabularies
   */
  public List<Vocabulary> getAll() {
    final Query<Vocabulary> query = ds.createQuery(Vocabulary.class);
    try (final MorphiaCursor<Vocabulary> cursor = query.find()) {
      return cursor.toList();
    }
  }

  /**
   * Retrieve the vocabulary based on its ID.
   *
   * @param vocabularyId The ID of the vocabulary to retrieve.
   * @return A list of all the vocabularies
   */
  public Vocabulary get(String vocabularyId) {
    return ds.find(Vocabulary.class).filter("id", vocabularyId).first();
  }

  protected Datastore getDatastore() {
    return ds;
  }
}
