package eu.europeana.metis.mapping.persistence;

import com.mongodb.MongoClient;
import eu.europeana.metis.mapping.model.MappingSchema;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.dao.BasicDAO;

/**
 * Created by gmamakis on 15-2-17.
 */
public class MappingSchemaDao extends BasicDAO<MappingSchema, ObjectId> {
    public MappingSchemaDao(Morphia morphia, MongoClient mongo, String database) {
        super(mongo, morphia, database);
    }
}
