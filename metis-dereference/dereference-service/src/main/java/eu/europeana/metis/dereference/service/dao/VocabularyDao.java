package eu.europeana.metis.dereference.service.dao;

import com.mongodb.MongoClient;
import eu.europeana.metis.dereference.Vocabulary;
import org.apache.commons.lang.StringUtils;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import java.util.List;

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
        return ds.find(Vocabulary.class).filter("URI", uri).asList();
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
        ds.delete(ds.createQuery(Vocabulary.class).filter("URI", name));
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
        ops.set("rules", entity.getRules());
        ops.set("typeRules", entity.getTypeRules());
        ops.set("type", entity.getType());
        ops.set("URI", entity.getURI());
        ops.set("xslt", entity.getXslt());
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
     * @param vocs The vocabularies to choose from
     * @param xml  The actual retrieved entity
     * @param uri  The uri of the record to check for rules
     * @return The corresponding vocabulary
     */
    public Vocabulary findByEntity(List<Vocabulary> vocs, String xml, String uri) {
        for (Vocabulary vocabulary : vocs) {
            if (StringUtils.equals(vocabulary.getRules(), "*")
                    || StringUtils.contains(uri, vocabulary.getRules()) || StringUtils.contains(xml, vocabulary.getTypeRules())) {
                return vocabulary;
            }
        }
        return null;
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
