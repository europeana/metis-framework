package eu.europeana.metis.framework.dao;

import eu.europeana.metis.framework.dataset.Dataset;
import eu.europeana.metis.framework.mongo.MongoProvider;
import eu.europeana.metis.framework.organization.Organization;
import org.bson.types.ObjectId;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO for the datasets
 * Created by ymamakis on 2/17/16.
 */
public class DatasetDao implements MetisDao<Dataset> {

    @Autowired
    private MongoProvider provider;



    @Override
    public void create(Dataset dataset) {

        provider.getDatastore().save(dataset);

    }

    @Override
    public void update(Dataset dataset) {
        UpdateOperations<Dataset> ops = provider.getDatastore().createUpdateOperations(Dataset.class);
        Query<Dataset> q = provider.getDatastore().find(Dataset.class).filter("name",dataset.getName());
        ops.set("assignedToLdapId",dataset.getAssignedToLdapId());
        ops.set("createdByLdapId",dataset.getCreatedByLdapId());
        ops.set("created",dataset.getCreated());
        ops.set("country",dataset.getCountry());
        ops.set("dataProviders",dataset.getDataProviders());
        ops.set("description",dataset.getDescription());
        ops.set("DQA",dataset.getDQA());
        ops.set("firstPublished",dataset.getFirstPublished());
        ops.set("harvestedAt",dataset.getHarvestedAt());
        ops.set("language",dataset.getLanguage());
        ops.set("lastPublished",dataset.getLastPublished());
        ops.set("metadata",dataset.getMetadata());
        ops.set("notes",dataset.getNotes());
        ops.set("recordsPublished",dataset.getRecordsPublished());
        ops.set("recordsSubmitted",dataset.getRecordsSubmitted());
        ops.set("replacedBy",dataset.getReplacedBy());
        ops.set("source",dataset.getSource());
        ops.set("subject",dataset.getSubject());
        ops.set("submittedAt",dataset.getSubmittedAt());
        ops.set("updated",dataset.getUpdated());
        ops.set("workflowStatus",dataset.getWorkflowStatus());
        ops.set("accepted",dataset.isAccepted());
        ops.set("deaSigned",dataset.isDeaSigned());
        provider.getDatastore().update(q,ops);
    }

    @Override
    public Dataset getById(String id) {
        return provider.getDatastore().find(Dataset.class).filter("_id",new ObjectId(id)).get();
    }

    @Override
    public void delete(Dataset dataset) {
        provider.getDatastore().delete(provider.getDatastore().createQuery(Dataset.class).filter("name",dataset.getName()));
    }

    /**
     * Retrieve a dataset by name
     * @param name The name of the dataset
     * @return The dataset with the specific name
     */
    public Dataset getByName(String name) {
        return provider.getDatastore().find(Dataset.class).filter("name",name).get();
    }

    /**
     * Create a dataset for an organization
     * @param organization The organization to assign the dataset to
     * @param dataset The dataset to persist
     * @return The organization to update
     */
    public Organization createDatasetForOrganization(Organization organization, Dataset dataset){
        create(dataset);
        List<Dataset> ds = organization.getDatasets();
        if(ds==null){
            ds = new ArrayList<>();
        }
        ds.add(dataset);
        organization.setDatasets(ds);
        return organization;
    }

}
