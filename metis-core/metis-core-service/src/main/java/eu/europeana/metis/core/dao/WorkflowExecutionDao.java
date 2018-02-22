package eu.europeana.metis.core.dao;

import com.mongodb.WriteResult;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.core.rest.RequestLimits;
import eu.europeana.metis.core.workflow.OrderField;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.PluginStatus;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.aggregation.AggregationPipeline;
import org.mongodb.morphia.query.Criteria;
import org.mongodb.morphia.query.CriteriaContainerImpl;
import org.mongodb.morphia.query.FindOptions;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.Sort;
import org.mongodb.morphia.query.UpdateOperations;
import org.mongodb.morphia.query.UpdateResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-26
 */
@Repository
public class WorkflowExecutionDao implements MetisDao<WorkflowExecution, String> {

  private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowExecutionDao.class);
  private static final String WORKFLOW_STATUS = "workflowStatus";
  private static final String WORKFLOW_OWNER = "workflowOwner";
  private static final String WORKFLOW_NAME = "workflowName";
  private static final String DATASET_ID = "datasetId";
  private static final String METIS_PLUGINS = "metisPlugins";
  private static final int MULTIPLIER_FOR_MONITOR_CHECK_IN_SECS = 2;
  private final MorphiaDatastoreProvider morphiaDatastoreProvider;
  private int workflowExecutionsPerRequest = RequestLimits.WORKFLOW_EXECUTIONS_PER_REQUEST
      .getLimit();

  @Autowired
  public WorkflowExecutionDao(MorphiaDatastoreProvider morphiaDatastoreProvider) {
    this.morphiaDatastoreProvider = morphiaDatastoreProvider;
  }

  @Override
  public String create(WorkflowExecution workflowExecution) {
    Key<WorkflowExecution> workflowExecutionKey = morphiaDatastoreProvider.getDatastore().save(
        workflowExecution);
    LOGGER.debug(
        "WorkflowExecution for datasetId '{}' with workflowOwner '{}' and workflowName '{}' created in Mongo",
        workflowExecution.getDatasetId(), workflowExecution.getWorkflowOwner(),
        workflowExecution.getWorkflowName());
    return workflowExecutionKey.getId().toString();
  }

  @Override
  public String update(WorkflowExecution workflowExecution) {
    Key<WorkflowExecution> workflowExecutionKey = morphiaDatastoreProvider.getDatastore().save(
        workflowExecution);
    LOGGER.debug(
        "WorkflowExecution for datasetId '{}' with workflowOwner '{}' and workflowName '{}' updated in Mongo",
        workflowExecution.getDatasetId(), workflowExecution.getWorkflowOwner(),
        workflowExecution.getWorkflowName());
    return workflowExecutionKey.getId().toString();
  }

  public void updateWorkflowPlugins(WorkflowExecution workflowExecution) {
    UpdateOperations<WorkflowExecution> workflowExecutionUpdateOperations = morphiaDatastoreProvider
        .getDatastore()
        .createUpdateOperations(WorkflowExecution.class);
    Query<WorkflowExecution> query = morphiaDatastoreProvider.getDatastore()
        .find(WorkflowExecution.class)
        .filter("_id", workflowExecution.getId());
    workflowExecutionUpdateOperations
        .set(METIS_PLUGINS, workflowExecution.getMetisPlugins());
    UpdateResults updateResults = morphiaDatastoreProvider.getDatastore()
        .update(query, workflowExecutionUpdateOperations);
    LOGGER.debug(
        "WorkflowExecution metisPlugins for datasetId '{}' with workflowOwner '{}' and workflowName '{}' updated in Mongo. (UpdateResults: {})",
        workflowExecution.getDatasetId(), workflowExecution.getWorkflowOwner(),
        workflowExecution.getWorkflowName(), updateResults.getUpdatedCount());
  }

  public void updateMonitorInformation(WorkflowExecution workflowExecution) {
    UpdateOperations<WorkflowExecution> workflowExecutionUpdateOperations = morphiaDatastoreProvider
        .getDatastore()
        .createUpdateOperations(WorkflowExecution.class);
    Query<WorkflowExecution> query = morphiaDatastoreProvider.getDatastore()
        .find(WorkflowExecution.class)
        .filter("_id", workflowExecution.getId());
    workflowExecutionUpdateOperations
        .set(WORKFLOW_STATUS, workflowExecution.getWorkflowStatus());
    if (workflowExecution.getStartedDate() != null) {
      workflowExecutionUpdateOperations
          .set("startedDate", workflowExecution.getStartedDate());
    }
    if (workflowExecution.getUpdatedDate() != null) {
      workflowExecutionUpdateOperations
          .set("updatedDate", workflowExecution.getUpdatedDate());
    }
    workflowExecutionUpdateOperations
        .set(METIS_PLUGINS, workflowExecution.getMetisPlugins());
    UpdateResults updateResults = morphiaDatastoreProvider.getDatastore()
        .update(query, workflowExecutionUpdateOperations);
    LOGGER.debug(
        "WorkflowExecution monitor information for datasetId '{}' with workflowOwner '{}' and workflowName '{}' updated in Mongo. (UpdateResults: {})",
        workflowExecution.getDatasetId(), workflowExecution.getWorkflowOwner(),
        workflowExecution.getWorkflowName(), updateResults.getUpdatedCount());
  }

  public void setCancellingState(WorkflowExecution workflowExecution) {
    UpdateOperations<WorkflowExecution> workflowExecutionUpdateOperations = morphiaDatastoreProvider
        .getDatastore()
        .createUpdateOperations(WorkflowExecution.class);
    Query<WorkflowExecution> query = morphiaDatastoreProvider.getDatastore()
        .find(WorkflowExecution.class)
        .filter("_id", workflowExecution.getId());
    workflowExecutionUpdateOperations.set("cancelling", Boolean.TRUE);
    UpdateResults updateResults = morphiaDatastoreProvider.getDatastore()
        .update(query, workflowExecutionUpdateOperations);
    LOGGER.debug(
        "WorkflowExecution cancelling for datasetId '{}' with workflowOwner '{}' and workflowName '{}' set to true in Mongo. (UpdateResults: {})",
        workflowExecution.getDatasetId(), workflowExecution.getWorkflowOwner(),
        workflowExecution.getWorkflowName(), updateResults.getUpdatedCount());
  }

  @Override
  public WorkflowExecution getById(String id) {
    Query<WorkflowExecution> query = morphiaDatastoreProvider.getDatastore()
        .find(WorkflowExecution.class)
        .field("_id").equal(new ObjectId(id));
    return query.get();
  }

  @Override
  public boolean delete(WorkflowExecution workflowExecution) {
    return false;
  }

  public WorkflowExecution getRunningOrInQueueExecution(int datasetId) {
    Query<WorkflowExecution> query = morphiaDatastoreProvider.getDatastore()
        .find(WorkflowExecution.class)
        .field(DATASET_ID).equal(
            datasetId);
    query.or(query.criteria(WORKFLOW_STATUS).equal(WorkflowStatus.INQUEUE),
        query.criteria(WORKFLOW_STATUS).equal(WorkflowStatus.RUNNING));
    return query.get();
  }

  public boolean exists(WorkflowExecution workflowExecution) {
    return morphiaDatastoreProvider.getDatastore().find(WorkflowExecution.class)
        .field(DATASET_ID).equal(
            workflowExecution.getDatasetId()).field(WORKFLOW_OWNER).equal(
            workflowExecution.getWorkflowOwner()).field(WORKFLOW_NAME)
        .equal(workflowExecution.getWorkflowName())
        .project("_id", true).get() != null;
  }

  public String existsAndNotCompleted(int datasetId) {
    Query<WorkflowExecution> query = morphiaDatastoreProvider.getDatastore()
        .find(WorkflowExecution.class).field(DATASET_ID).equal(datasetId);
    query.or(query.criteria(WORKFLOW_STATUS).equal(WorkflowStatus.INQUEUE),
        query.criteria(WORKFLOW_STATUS).equal(WorkflowStatus.RUNNING));
    query.project("_id", true);
    query.project(WORKFLOW_STATUS, true);

    WorkflowExecution storedWorkflowExecution = query.get();
    if (storedWorkflowExecution != null) {
      return storedWorkflowExecution.getId().toString();
    }
    return null;
  }

  public AbstractMetisPlugin getLatestFinishedWorkflowExecutionByDatasetIdAndPluginType(
      int datasetId,
      Set<PluginType> pluginTypes) {
    Query<WorkflowExecution> query = morphiaDatastoreProvider.getDatastore()
        .createQuery(WorkflowExecution.class);

    AggregationPipeline aggregation = morphiaDatastoreProvider.getDatastore()
        .createAggregation(WorkflowExecution.class);

    Criteria[] criteria = {
        query.criteria(DATASET_ID).equal(datasetId),
        query.criteria("metisPlugins.pluginStatus").equal(PluginStatus.FINISHED)};
    query.and(criteria);

    List<CriteriaContainerImpl> criteriaContainer = new ArrayList<>();
    if (pluginTypes != null) {
      for (PluginType pluginType : pluginTypes) {
        if (pluginType != null) {
          criteriaContainer.add(query.criteria("metisPlugins.pluginType").equal(pluginType));
        }
      }
    }
    if (!criteriaContainer.isEmpty()) {
      query.or((CriteriaContainerImpl[]) criteriaContainer.toArray(new CriteriaContainerImpl[0]));
    }

    Iterator<WorkflowExecution> metisPluginsIterator = aggregation.match(query)
        .unwind(METIS_PLUGINS)
        .match(query).sort(Sort.descending("metisPlugins.finishedDate"))
        .aggregate(WorkflowExecution.class);

    if (metisPluginsIterator.hasNext()) {
      return metisPluginsIterator.next().getMetisPlugins().get(0);
    }
    return null;
  }

  public List<WorkflowExecution> getAllWorkflowExecutions(int datasetId,
      String workflowOwner, String workflowName, Set<WorkflowStatus> workflowStatuses,
      OrderField orderField, boolean ascending, int nextPage) {
    Query<WorkflowExecution> query = morphiaDatastoreProvider.getDatastore()
        .createQuery(WorkflowExecution.class);
    if (datasetId > 0) {
      query.field(DATASET_ID).equal(datasetId);
    }
    if (StringUtils.isNotEmpty(workflowOwner)) {
      query.field(WORKFLOW_OWNER).equal(workflowOwner);
    }
    if (StringUtils.isNotEmpty(workflowName)) {
      query.field(WORKFLOW_NAME).equal(workflowName);
    }

    List<CriteriaContainerImpl> criteriaContainer = new ArrayList<>();
    if (workflowStatuses != null) {
      for (WorkflowStatus workflowStatus : workflowStatuses) {
        if (workflowStatus != null) {
          criteriaContainer.add(query.criteria(WORKFLOW_STATUS).equal(workflowStatus));
        }
      }
    }
    if (!criteriaContainer.isEmpty()) {
      query.or((CriteriaContainerImpl[]) criteriaContainer.toArray(new CriteriaContainerImpl[0]));
    }

    if (orderField != null) {
      if (ascending) {
        query.order(orderField.getOrderFieldName());
      } else {
        query.order("-" + orderField.getOrderFieldName());
      }
    }
    return query.asList(new FindOptions().skip(nextPage * workflowExecutionsPerRequest)
        .limit(workflowExecutionsPerRequest));
  }

  public int getWorkflowExecutionsPerRequest() {
    return workflowExecutionsPerRequest;
  }

  public void setWorkflowExecutionsPerRequest(int workflowExecutionsPerRequest) {
    this.workflowExecutionsPerRequest = workflowExecutionsPerRequest;
  }

  public boolean isCancelled(ObjectId id) {
    return
        morphiaDatastoreProvider.getDatastore().find(WorkflowExecution.class).field("_id").equal(id)
            .project(WORKFLOW_STATUS, true).get().getWorkflowStatus() == WorkflowStatus.CANCELLED;
  }

  public boolean isCancelling(ObjectId id) {
    return morphiaDatastoreProvider.getDatastore().find(WorkflowExecution.class).field("_id")
        .equal(id)
        .project("cancelling", true).get().isCancelling();
  }

  public boolean isExecutionActive(WorkflowExecution workflowExecutionToCheck,
      int monitorCheckInSecs) {
    try {
      Date updatedDateBefore = workflowExecutionToCheck.getUpdatedDate();
      Thread.sleep(
          TimeUnit.SECONDS.toMillis(MULTIPLIER_FOR_MONITOR_CHECK_IN_SECS) * monitorCheckInSecs);
      WorkflowExecution workflowExecution = this
          .getById(workflowExecutionToCheck.getId().toString());
      return hasUpdatedDateChanged(updatedDateBefore, workflowExecution.getUpdatedDate())
          || workflowExecution.getFinishedDate() != null;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();  // set interrupt flag
      LOGGER.warn("Thread was interrupted", e);
      return true;
    }
  }

  private boolean hasUpdatedDateChanged(Date updatedDateBefore, Date updatedDateAfter) {
    return (updatedDateBefore != null && updatedDateBefore.compareTo(updatedDateAfter) < 0) ||
        (updatedDateBefore == null && updatedDateAfter != null);
  }

  public void removeActiveExecutionsFromList(List<WorkflowExecution> workflowExecutions,
      int monitorCheckInSecs) {
    try {
      Thread.sleep(
          TimeUnit.SECONDS.toMillis(MULTIPLIER_FOR_MONITOR_CHECK_IN_SECS) * monitorCheckInSecs);
      for (Iterator<WorkflowExecution> iterator = workflowExecutions.iterator();
          iterator.hasNext(); ) {
        WorkflowExecution workflowExecutionToCheck = iterator.next();
        WorkflowExecution workflowExecution = this
            .getById(workflowExecutionToCheck.getId().toString());
        if (workflowExecutionToCheck.getUpdatedDate() != null
            && workflowExecutionToCheck.getUpdatedDate()
            .compareTo(workflowExecution.getUpdatedDate()) < 0) {
          iterator.remove();
        }
      }

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();  // set interrupt flag
      LOGGER.warn("Thread was interruped", e);
    }
  }

  public boolean deleteAllByDatasetId(int datasetId) {
    Query<WorkflowExecution> query = morphiaDatastoreProvider.getDatastore()
        .createQuery(WorkflowExecution.class);
    query.field(DATASET_ID).equal(datasetId);
    WriteResult delete = morphiaDatastoreProvider.getDatastore().delete(query);
    LOGGER.debug("WorkflowExecution with datasetId: {}, deleted from Mongo", datasetId);
    return delete.getN() >= 1;
  }
}
