package eu.europeana.metis.ui.mongo.dao;

import eu.europeana.metis.ui.mongo.domain.User;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;

/**
 * A MongoDB DAO for persisting users
 * Created by ymamakis on 11/24/16.
 */
public class MongoUserDao extends BasicDAO<User, String>{
    /**
     * A DBUser persistence DAO
     * @param entityClass DBUser
     * @param ds The datastore to use for the connection
     */
    public MongoUserDao(Class<User> entityClass, Datastore ds) {
        super(entityClass, ds);
    }
}
