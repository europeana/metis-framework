package eu.europeana.metis.ui.mongo.dao;

import eu.europeana.metis.ui.mongo.domain.DBUser;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;

/**
 * Created by ymamakis on 11/24/16.
 */
public class DBUserDao extends BasicDAO<DBUser, String>{
    public DBUserDao(Class<DBUser> entityClass, Datastore ds) {
        super(entityClass, ds);
    }
}
