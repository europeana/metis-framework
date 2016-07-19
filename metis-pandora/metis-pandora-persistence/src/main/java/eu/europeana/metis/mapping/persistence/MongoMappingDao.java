package eu.europeana.metis.mapping.persistence;

import com.mongodb.MongoClient;
import eu.europeana.metis.mapping.model.Mapping;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.dao.BasicDAO;

/**
 * A Mongo persistence DAO
 * Created by ymamakis on 4/7/16.
 */
public class MongoMappingDao extends BasicDAO<Mapping, ObjectId> {
    /**
     * Default constructor
     *
     * @param morphia  The Morphia wrapper to use
     * @param mongo    The Mongo connection settings
     * @param database The database to connect to
     */
    public MongoMappingDao(Morphia morphia, MongoClient mongo, String database) {
        super(mongo, morphia, database);
    }
}

