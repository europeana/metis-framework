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
package eu.europeana.metis.core.dao;

import com.mongodb.WriteResult;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.query.FindOptions;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.mongodb.morphia.query.UpdateResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * DAO for the datasets
 * Created by ymamakis on 2/17/16.
 */
public class DatasetDao implements MetisDao<Dataset, String> {

  private final Logger LOGGER = LoggerFactory.getLogger(DatasetDao.class);
  private int datasetsPerRequest = 5;

  private final MorphiaDatastoreProvider provider;

  @Autowired
  public DatasetDao(MorphiaDatastoreProvider provider) {
    this.provider = provider;
  }

  @Override
  public String create(Dataset dataset) {
    Key<Dataset> datasetKey = provider.getDatastore().save(dataset);
    LOGGER.info("Dataset '" + dataset.getDatasetName() + "' created with OrganizationId '" + dataset
        .getOrganizationId() + "' and Provider '" + dataset.getDataProvider()
        + "' and Description '" + dataset.getDescription() + "' in Mongo");
    return datasetKey.getId().toString();
  }

  @Override
  public String update(Dataset dataset) {
    UpdateOperations<Dataset> ops = provider.getDatastore().createUpdateOperations(Dataset.class);
    Query<Dataset> q = provider.getDatastore().find(Dataset.class)
        .filter("datasetName", dataset.getDatasetName());
    if (dataset.getAssignedToLdapId() != null) {
      ops.set("assignedToLdapId", dataset.getAssignedToLdapId());
    } else {
      ops.unset("assignedToLdapId");
    }
    ops.set("createdByLdapId", dataset.getCreatedByLdapId());
//    ops.set("createdDate", dataset.getCreated());
    ops.set("country", dataset.getCountry());
    ops.set("dataProvider", dataset.getDataProvider());
    ops.set("description", dataset.getDescription());
    if (dataset.getDqas() != null) {
      ops.set("dqas", dataset.getDqas());
    } else {
      ops.unset("dqas");
    }
    if (dataset.getFirstPublished() != null) {
      ops.set("firstPublished", dataset.getFirstPublished());
    } else {
      ops.unset("firstPublished");
    }
    if (dataset.getHarvestedAt() != null) {
      ops.set("harvestedAt", dataset.getHarvestedAt());
    } else {
      ops.unset("harvestedAt");
    }
    ops.set("language", dataset.getLanguage());
    if (dataset.getLastPublished() != null) {
      ops.set("lastPublished", dataset.getLastPublished());
    } else {
      ops.unset("lastPublished");
    }
    ops.set("metadata", dataset.getHarvestingMetadata());
    ops.set("notes", dataset.getNotes());
    ops.set("publishedRecords", dataset.getPublishedRecords());
    ops.set("submittedRecords", dataset.getSubmittedRecords());
    if (dataset.getReplacedBy() != null) {
      ops.set("replacedBy", dataset.getReplacedBy());
    } else {
      ops.unset("replacedBy");
    }

    if (dataset.getSources() != null) {
      ops.set("sources", dataset.getSources());
    } else {
      ops.unset("sources");
    }
    if (dataset.getSubjects() != null) {
      ops.set("subjects", dataset.getSubjects());
    } else {
      ops.unset("subjects");
    }
    if (dataset.getSubmissionDate() != null) {
      ops.set("submissionDate", dataset.getSubmissionDate());
    } else {
      ops.unset("submissionDate");
    }

    ops.set("updatedDate", dataset.getUpdatedDate());
    ops.set("workflowStatus", dataset.getDatasetStatus());
    ops.set("accepted", dataset.isAccepted());
    ops.set("deaSigned", dataset.isDeaSigned());
    UpdateResults updateResults = provider.getDatastore().update(q, ops);

    LOGGER.info(
        "Dataset '" + dataset.getDatasetName() + "' updated with Provider '" + dataset
            .getDataProvider()
            + "' and Description '" + dataset.getDescription() + "' in Mongo");
    return String.valueOf(updateResults.getUpdatedCount());
  }

  @Override
  public Dataset getById(String id) {
    return provider.getDatastore().find(Dataset.class).filter("_id", new ObjectId(id)).get();
  }

  @Override
  public boolean delete(Dataset dataset) {
    provider.getDatastore().delete(
        provider.getDatastore().createQuery(Dataset.class).field("datasetName")
            .equal(dataset.getDatasetName()));
    LOGGER.info("Dataset '" + dataset.getDatasetName() + "' deleted with organizationId '" + dataset
        .getOrganizationId() + "' from Mongo");
    return true;
  }

  public void updateDatasetName(String datasetName, String newDatasetName) {
    UpdateOperations<Dataset> datasetUpdateOperations = provider.getDatastore()
        .createUpdateOperations(Dataset.class);
    Query<Dataset> query = provider.getDatastore().find(Dataset.class)
        .filter("datasetName", datasetName);
    datasetUpdateOperations.set("datasetName", newDatasetName);
    UpdateResults updateResults = provider.getDatastore().update(query, datasetUpdateOperations);
    LOGGER.info(
        "Dataset with datasetName '" + datasetName + "' renamed to '" + newDatasetName
            + "'. (UpdateResults: " + updateResults.getUpdatedCount() + ")");
  }

  public boolean deleteDatasetByDatasetName(String datasetName) {
    Query<Dataset> query = provider.getDatastore().createQuery(Dataset.class);
    query.field("datasetName").equal(datasetName);
    WriteResult delete = provider.getDatastore().delete(query);
    LOGGER.info("Dataset '" + datasetName + "' deleted from Mongo");
    return delete.getN() == 1;
  }

  public Dataset getDatasetByDatasetName(String datasetName) {
    return provider.getDatastore().find(Dataset.class).field("datasetName").equal(datasetName)
        .get();
  }

  public boolean existsDatasetByDatasetName(String datasetName) {
    return provider.getDatastore().find(Dataset.class).field("datasetName").equal(datasetName)
        .project("_id", true).get() != null;
  }

  public List<Dataset> getAllDatasetsByDataProvider(String dataProvider, String nextPage) {
    Query<Dataset> query = provider.getDatastore().createQuery(Dataset.class);
    query.field("dataProvider").equal(dataProvider).order("_id");
    if (StringUtils.isNotEmpty(nextPage)) {
      query.field("_id").greaterThan(new ObjectId(nextPage));
    }
    return query.asList(new FindOptions().limit(datasetsPerRequest));
  }

  public List<Dataset> getAllDatasetsByOrganizationId(String organizationId, String nextPage) {
    Query<Dataset> query = provider.getDatastore().createQuery(Dataset.class);
    query.field("organizationId").equal(organizationId).order("_id");
    if (StringUtils.isNotEmpty(nextPage)) {
      query.field("_id").greaterThan(new ObjectId(nextPage));
    }
    return query.asList(new FindOptions().limit(datasetsPerRequest));
  }

  public int getDatasetsPerRequest() {
    return datasetsPerRequest;
  }

  public void setDatasetsPerRequest(int datasetsPerRequest) {
    this.datasetsPerRequest = datasetsPerRequest;
  }
}
