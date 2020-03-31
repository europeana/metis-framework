package eu.europeana.metis.dereference.service.dao;

import com.mongodb.MongoClient;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.query.Query;
import dev.morphia.query.UpdateOperations;
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
   * Save a vocabulary
   *
   * @param entity The vocabulary to save
   * @return The ID under which the vocabulary was saved.
   */
  public String save(Vocabulary entity) {
    return (String) ds.save(entity).getId();
  }

  /**
   * Delete a vocabulary by name
   *
   * @param name The name of the vocabulary to delete
   */
  public void delete(String name) {
    ds.delete(ds.createQuery(Vocabulary.class).filter("name", name));
  }

  /**
   * Update the mapping of a vocabulary. It will be created if it does not exist
   *
   * @param entity The Vocabulary to update
   */
  public void update(Vocabulary entity) {
    Query<Vocabulary> query = ds.createQuery(Vocabulary.class).filter("name", entity.getName());
    UpdateOperations<Vocabulary> ops = ds.createUpdateOperations(Vocabulary.class);

    ops.set("iterations", entity.getIterations());
    if (entity.getRules() == null) {
      ops.unset("rules");
    } else {
      ops.set("rules", entity.getRules());
    }
    if (entity.getTypeRules() == null) {
      ops.unset("typeRules");
    } else {
      ops.set("typeRules", entity.getTypeRules());
    }
    if (entity.getType() != null) {
      ops.set("type", entity.getType());
    }
    ops.set("uri", entity.getUri());
    ops.set("xslt", entity.getXslt());
    ops.set("suffix", entity.getSuffix());
    ds.update(query, ops);
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

  /**
   * Return a Vocabulary by name
   *
   * @param name The name to search on
   * @return The Vocabulary with that name
   */
  public Vocabulary findByName(String name) {
    return ds.find(Vocabulary.class).filter("name", name).first();
  }

  public void setDs(Datastore ds) {
    this.ds = ds;
  }
}
