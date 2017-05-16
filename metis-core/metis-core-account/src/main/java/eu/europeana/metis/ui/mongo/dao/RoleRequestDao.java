package eu.europeana.metis.ui.mongo.dao;

import eu.europeana.metis.ui.mongo.domain.RoleRequest;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;

/**
 * A OrganizationRole REquest DAO
 * Created by ymamakis on 11/24/16.
 */
public class RoleRequestDao extends BasicDAO<RoleRequest, String> {

    /**
     * The DAO for the OrganizationRole Request
     * @param entityClass RoleRequest
     * @param ds The datastore to use for the connection
     */
    public RoleRequestDao(Class<RoleRequest> entityClass, Datastore ds) {
        super(entityClass, ds);
    }
}
