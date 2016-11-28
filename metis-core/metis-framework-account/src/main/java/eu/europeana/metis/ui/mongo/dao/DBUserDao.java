package eu.europeana.metis.ui.mongo.dao;

import eu.europeana.metis.ui.mongo.domain.DBUser;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;

/**
 * A MongoDB DAO for persisting users
 * Created by ymamakis on 11/24/16.
 */
public class DBUserDao extends BasicDAO<DBUser, String>{
    /**
     * A DBUser persistence DAO
     * @param entityClass DBUser
     * @param ds The datastore to use for the connection
     */
    public DBUserDao(Class<DBUser> entityClass, Datastore ds) {
        super(entityClass, ds);
    }
}
