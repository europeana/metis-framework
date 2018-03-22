package eu.europeana.metis.dereference.service.dao;

import java.util.List;
import java.util.regex.Pattern;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import com.mongodb.MongoClient;
import eu.europeana.metis.dereference.Vocabulary;

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
    return ds.find(Vocabulary.class).filter("uri", pattern).asList();
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
    if (entity.getRules() != null) {
      ops.set("rules", entity.getRules());
    } else {
      ops.unset("rules");
    }
    if (entity.getTypeRules() != null) {
      ops.set("typeRules", entity.getTypeRules());
    } else {
      ops.unset("typeRules");
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
    return ds.find(Vocabulary.class).asList();
  }

  /**
   * Retrieve the vocabulary based on its ID.
   * 
   * @param vocabularyId The ID of the vocabulary to retrieve.
   * @return A list of all the vocabularies
   */
  public Vocabulary get(String vocabularyId) {
    return ds.find(Vocabulary.class).filter("id", vocabularyId).get();
  }

  /**
   * Return a Vocabulary by name
   *
   * @param name The name to search on
   * @return The Vocabulary with that name
   */
  public Vocabulary findByName(String name) {
    return ds.find(Vocabulary.class).filter("name", name).get();
  }

  public void setDs(Datastore ds) {
    this.ds = ds;
  }
}
