package eu.europeana.metis.framework.dao;

import eu.europeana.metis.framework.dataset.Dataset;
import eu.europeana.metis.framework.mongo.MongoProvider;
import eu.europeana.metis.framework.organization.Organization;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Organization DAO
 * Created by ymamakis on 2/17/16.
 */
public class OrganizationDao implements MetisDao<Organization> {


    @Autowired
    private MongoProvider provider;




    @Override
    public void create(Organization organization) {
        provider.getDatastore().save(organization);
    }

    @Override
    public void update(Organization organization) {
        Query<Organization> q = provider.getDatastore().find(Organization.class).filter("organizationId", organization.getOrganizationId());
        UpdateOperations<Organization> ops = provider.getDatastore().createUpdateOperations(Organization.class);
        ops.set("harvestingMetadata", organization.getHarvestingMetadata());
        ops.set("organizationUri",organization.getOrganizationUri());
        ops.set("datasets",organization.getDatasets());
        provider.getDatastore().update(q,ops);
    }

    @Override
    public Organization getById(String id) {
        return provider.getDatastore().find(Organization.class).filter("id",id).get();
    }

    @Override
    public void delete(Organization organization) {
        provider.getDatastore().delete(organization);
    }

    /**
     * Retrieve all the organizations
     * @return A list of all the organizations
     */
    public List<Organization> getAll() {
        return provider.getDatastore().find(Organization.class).asList();
    }


    /**
     * Get an organization by its organization Id
     * @param organizationId The organization id
     * @return The organization
     */
    public Organization getByOrganizationId(String organizationId) {
        return provider.getDatastore().find(Organization.class).filter("organizationId",organizationId).get();
    }

    /**
     * Get all the datasets for an organization
     * @param organizationId The id to retrieve the datasets for
     * @return The datasets for this organization
     */
    public List<Dataset> getAllDatasetsByOrganization(String organizationId){
        Organization org = getByOrganizationId(organizationId);
        if(org!=null){
            return org.getDatasets();
        }
        return null;
    }

}
