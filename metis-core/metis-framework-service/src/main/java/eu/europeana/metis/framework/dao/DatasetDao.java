/*
 * Copyright 2007-2013 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */
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
        if(dataset.getAssignedToLdapId()!=null) {
            ops.set("assignedToLdapId", dataset.getAssignedToLdapId());
        } else {
            ops.unset("assignedToLdapId");
        }
        ops.set("createdByLdapId",dataset.getCreatedByLdapId());
        ops.set("created",dataset.getCreated());
        ops.set("country",dataset.getCountry());
        ops.set("dataProvider",dataset.getDataProvider());
        ops.set("description",dataset.getDescription());
        if(dataset.getDQA()!=null) {
            ops.set("DQA", dataset.getDQA());
        } else {
            ops.unset("DQA");
        }
        if(dataset.getFirstPublished()!=null) {
            ops.set("firstPublished", dataset.getFirstPublished());
        } else {
            ops.unset("firstPublished");
        }
        if (dataset.getHarvestedAt()!=null) {
            ops.set("harvestedAt", dataset.getHarvestedAt());
        } else {
            ops.unset("harvestedAt");
        }
        ops.set("language",dataset.getLanguage());
        if(dataset.getLastPublished()!=null) {
            ops.set("lastPublished", dataset.getLastPublished());
        } else {
            ops.unset("lastPublished");
        }
        ops.set("metadata",dataset.getMetadata());
        ops.set("notes",dataset.getNotes());
        ops.set("recordsPublished",dataset.getRecordsPublished());
        ops.set("recordsSubmitted",dataset.getRecordsSubmitted());
        if(dataset.getReplacedBy()!=null) {
            ops.set("replacedBy", dataset.getReplacedBy());
        } else {
            ops.unset("replacedBy");
        }

        if(dataset.getSource()!=null) {
            ops.set("source", dataset.getSource());
        } else {
            ops.unset("source");
        }
        if(dataset.getSubject()!=null) {
            ops.set("subject", dataset.getSubject());
        } else {
            ops.unset("subject");
        }
        if(dataset.getSubmittedAt()!=null) {
            ops.set("submittedAt", dataset.getSubmittedAt());
        } else {
            ops.unset("submittedAt");
        }

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
        ds.add(getByName(dataset.getName()));
        organization.setDatasets(ds);
        return organization;
    }

}
