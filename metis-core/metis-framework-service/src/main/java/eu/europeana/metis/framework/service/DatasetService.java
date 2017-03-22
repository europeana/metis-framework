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
package eu.europeana.metis.framework.service;

import eu.europeana.cloud.common.model.DataSet;
import eu.europeana.metis.framework.dao.DatasetDao;
import eu.europeana.metis.framework.dao.OrganizationDao;
import eu.europeana.metis.framework.dao.ecloud.EcloudDatasetDao;
import eu.europeana.metis.framework.dataset.Dataset;
import eu.europeana.metis.framework.exceptions.NoDatasetFoundException;
import eu.europeana.metis.framework.organization.Organization;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Service for storing datasets
 * Created by ymamakis on 2/17/16.
 */
@Component
public class DatasetService {

    @Autowired
    private DatasetDao dsDao;

    @Autowired
    private EcloudDatasetDao ecloudDatasetDao;

    @Autowired
    private OrganizationDao orgDao;

    /**
     * Create a dataset for an organization
     * @param org The organization to assign the dataset to
     * @param ds The dataset to persist
     */
    public void createDataset(Organization org, Dataset ds){
        dsDao.createDatasetForOrganization(org,ds);
        orgDao.update(org);

        //Create in ECloud
        DataSet ecloudDataset = new DataSet();
        ecloudDataset.setId(ds.getName());
        ecloudDataset.setProviderId(ecloudDatasetDao.getEcloudProvider());
        ecloudDataset.setDescription(ds.getDescription());
        ecloudDatasetDao.create(ecloudDataset);
    }

    /**
     * Update a dataset
     * @param ds The dataset to update
     */
    public void updateDataset(Dataset ds){
        dsDao.update(ds);

        //Update in ECloud
        DataSet ecloudDataset = new DataSet();
        ecloudDataset.setId(ds.getName());
        ecloudDataset.setProviderId(ecloudDatasetDao.getEcloudProvider());
        ecloudDataset.setDescription(ds.getDescription());
        ecloudDatasetDao.update(ecloudDataset);
    }

    /**
     * Delete a dataset
     * @param org The organization the dataset is assigned to
     * @param ds The dataset to delete
     */
    public void deleteDataset(Organization org, Dataset ds){
        dsDao.delete(ds);
        List<Dataset> datasetList = org.getDatasets();
        datasetList.remove(ds);
        org.setDatasets(datasetList);
        orgDao.update(org);

        //Delete from ECloud
        DataSet ecloudDataset = new DataSet();
        ecloudDataset.setId(ds.getName());
        ecloudDataset.setProviderId(ecloudDatasetDao.getEcloudProvider());
        ecloudDataset.setDescription(ds.getDescription());
        ecloudDatasetDao.delete(ecloudDataset);
    }

    /**
     * Get Dataset by name
     * @param name The name of the dataset
     * @return The Dataset
     */
    public Dataset getByName(String name) throws NoDatasetFoundException{
        Dataset dataset = dsDao.getByName(name);
        if(dataset==null){
            throw new NoDatasetFoundException(name);
        }
        return dataset;
    }

    /**
     * Get Dataset by datasetKey
     * @param key The key of the dataset
     * @return The Dataset
     */
    public Dataset getByDatasetkey(String key) throws NoDatasetFoundException{
        Dataset dataset = dsDao.getByName(key);
        if(dataset==null){
            throw new NoDatasetFoundException(key);
        }
        return dataset;
    }
    /**
     * Check if a dataset exists
     * @param name The dataset identifier
     * @return true if it exists false otherwise
     */
    public boolean exists(String name){
        return dsDao.exists(name);
    }
}
