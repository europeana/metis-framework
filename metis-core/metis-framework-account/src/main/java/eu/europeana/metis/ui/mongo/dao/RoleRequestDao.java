package eu.europeana.metis.ui.mongo.dao;

import eu.europeana.metis.ui.mongo.domain.RoleRequest;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;

/**
 * Created by ymamakis on 11/24/16.
 */
public class RoleRequestDao extends BasicDAO<RoleRequest, String> {

    public RoleRequestDao(Class<RoleRequest> entityClass, Datastore ds) {
        super(entityClass, ds);
    }
}
