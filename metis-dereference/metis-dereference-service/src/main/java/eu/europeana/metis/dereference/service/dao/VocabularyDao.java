/*
 * Copyright 2007-2013 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */
package eu.europeana.metis.dereference.service.dao;

import java.util.List;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import com.mongodb.MongoClient;
import eu.europeana.metis.dereference.Vocabulary;

/**
 * Dao for vocabularies
 * Created by ymamakis on 2/11/16.
 */

public class VocabularyDao {

    private Datastore ds;

    public VocabularyDao(MongoClient mongo, String db) {
        Morphia morphia = new Morphia();
        morphia.map(Vocabulary.class);

        ds = morphia.createDatastore(mongo, db);
    }

    /**
     * Retrieve a list of vocabularies for a given URI
     *
     * @param uri The uri to search on
     * @return The list of URIs that conform to that. They need to be further refined by the internal rules
     * after the entity has been retrieved
     */
    public List<Vocabulary> getByUri(String uri) {
        return ds.find(Vocabulary.class).filter("uri", uri).asList();
    }

    /**
     * Save a vocabulary
     *
     * @param entity The vocabulary to save
     */
    public void save(Vocabulary entity) {
        ds.save(entity);
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
        if(entity.getRules()!=null) {
            ops.set("rules", entity.getRules());
        } else {
            ops.unset("rules");
        }
        if(entity.getTypeRules()!=null) {
            ops.set("typeRules", entity.getTypeRules());
        } else {
            ops.unset("typeRules");
        }
        if(entity.getType()!=null) {
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
   * Once the entity has been retrieved decide on the actual vocabulary that you want
   *
   * @param vocabularies The vocabularies to choose from
   * @param incomingDataXml The actual retrieved entity
   * @param uri The uri of the record to check for rules
   * @return The corresponding vocabulary, or null if no such vocabulary is found.
   */
  public static Vocabulary findByEntity(List<Vocabulary> vocabularies, String incomingDataXml,
      String uri) {
    return vocabularies.stream()
        .filter(vocabulary -> vocabularyMatches(vocabulary, incomingDataXml, uri)).findAny()
        .orElse(null);
  }

  private static boolean vocabularyMatches(Vocabulary vocabulary, String incomingDataXml,
      String uri) {

    // Check the rules
    final Set<String> vocabularyRules = vocabulary.getRules();
    if (vocabularyRules != null && !vocabularyRules.isEmpty() && !vocabularyRules.contains(uri)) {
      return false;
    }

    // Check the type rules (more expensive operation: only do when needed).
    final Set<String> typeRules = vocabulary.getTypeRules();
    return typeRules == null || typeRules.isEmpty() || typeRules.stream().anyMatch(typeRule -> StringUtils.contains(incomingDataXml, typeRule));
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
