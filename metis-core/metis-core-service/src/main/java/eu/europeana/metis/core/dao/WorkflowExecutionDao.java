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
import eu.europeana.metis.utils.ExternalRequestUtil;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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
import org.springframework.util.CollectionUtils;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-26
 */
@Repository
public class WorkflowExecutionDao implements MetisDao<WorkflowExecution, String> {

  private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowExecutionDao.class);
  private static final String WORKFLOW_STATUS = "workflowStatus";
  private static final String DATASET_ID = "datasetId";
  private static final String METIS_PLUGINS = "metisPlugins";
  private final MorphiaDatastoreProvider morphiaDatastoreProvider;
  private int workflowExecutionsPerRequest = RequestLimits.WORKFLOW_EXECUTIONS_PER_REQUEST
      .getLimit();

  /**
   * Constructs the DAO
   *
   * @param morphiaDatastoreProvider {@link MorphiaDatastoreProvider} used to access Mongo
   */
  @Autowired
  public WorkflowExecutionDao(MorphiaDatastoreProvider morphiaDatastoreProvider) {
    this.morphiaDatastoreProvider = morphiaDatastoreProvider;
  }

  @Override
  public String create(WorkflowExecution workflowExecution) {
    final Key<WorkflowExecution> workflowExecutionKey = ExternalRequestUtil
        .retryableExternalRequestConnectionReset(() -> morphiaDatastoreProvider.getDatastore().save(workflowExecution));
    LOGGER.debug("WorkflowExecution for datasetId '{}' created in Mongo",
        workflowExecution.getDatasetId());
    return workflowExecutionKey != null ? workflowExecutionKey.getId().toString() : null;
  }

  @Override
  public String update(WorkflowExecution workflowExecution) {
    final Key<WorkflowExecution> workflowExecutionKey = ExternalRequestUtil.retryableExternalRequestConnectionReset(() ->
        morphiaDatastoreProvider.getDatastore().save(workflowExecution));
    LOGGER.debug("WorkflowExecution for datasetId '{}' updated in Mongo",
        workflowExecution.getDatasetId());
    return workflowExecutionKey != null ? workflowExecutionKey.getId().toString() : null;
  }

  /**
   * Overwrites only the portion of the WorkflowExecution that contains the plugins.
   *
   * @param workflowExecution the WorkflowExecution to update
   */
  public void updateWorkflowPlugins(WorkflowExecution workflowExecution) {
    UpdateOperations<WorkflowExecution> workflowExecutionUpdateOperations = morphiaDatastoreProvider
        .getDatastore()
        .createUpdateOperations(WorkflowExecution.class);
    Query<WorkflowExecution> query = morphiaDatastoreProvider.getDatastore()
        .find(WorkflowExecution.class)
        .filter("_id", workflowExecution.getId());
    workflowExecutionUpdateOperations
        .set(METIS_PLUGINS, workflowExecution.getMetisPlugins());
    UpdateResults updateResults = ExternalRequestUtil
        .retryableExternalRequestConnectionReset(() -> morphiaDatastoreProvider.getDatastore()
            .update(query, workflowExecutionUpdateOperations));
    LOGGER.debug(
        "WorkflowExecution metisPlugins for datasetId '{}' updated in Mongo. (UpdateResults: {})",
        workflowExecution.getDatasetId(),
        updateResults != null ? updateResults.getUpdatedCount() : 0);
  }

  /**
   * Overwrites only the portion of the WorkflowExecution that contains the monitor
   * information(plugins, started date, updated date).
   *
   * @param workflowExecution the WorkflowExecution to update
   */
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
    UpdateResults updateResults = ExternalRequestUtil
        .retryableExternalRequestConnectionReset(() -> morphiaDatastoreProvider.getDatastore()
            .update(query, workflowExecutionUpdateOperations));
    LOGGER.debug(
        "WorkflowExecution monitor information for datasetId '{}' updated in Mongo. (UpdateResults: {})",
        workflowExecution.getDatasetId(),
        updateResults != null ? updateResults.getUpdatedCount() : 0);
  }

  public void setCancellingState(WorkflowExecution workflowExecution) {
    UpdateOperations<WorkflowExecution> workflowExecutionUpdateOperations = morphiaDatastoreProvider
        .getDatastore().createUpdateOperations(WorkflowExecution.class);
    Query<WorkflowExecution> query = morphiaDatastoreProvider.getDatastore()
        .find(WorkflowExecution.class)
        .filter("_id", workflowExecution.getId());
    workflowExecutionUpdateOperations.set("cancelling", Boolean.TRUE);
    UpdateResults updateResults = ExternalRequestUtil
        .retryableExternalRequestConnectionReset(() -> morphiaDatastoreProvider.getDatastore()
            .update(query, workflowExecutionUpdateOperations));
    LOGGER.debug(
        "WorkflowExecution cancelling for datasetId '{}' set to true in Mongo. (UpdateResults: {})",
        workflowExecution.getDatasetId(),
        updateResults != null ? updateResults.getUpdatedCount() : 0);
  }

  @Override
  public WorkflowExecution getById(String id) {
    Query<WorkflowExecution> query = morphiaDatastoreProvider.getDatastore()
        .find(WorkflowExecution.class)
        .field("_id").equal(new ObjectId(id));
    return ExternalRequestUtil.retryableExternalRequestConnectionReset(query::get);
  }

  @Override
  public boolean delete(WorkflowExecution workflowExecution) {
    return false;
  }

  /**
   * Get the WorkflowExecution for a dataset identifier that is {@link WorkflowStatus#INQUEUE} or
   * {@link WorkflowStatus#RUNNING}
   *
   * @param datasetId the dataset identifier
   * @return the WorkflowExecution if found
   */
  public WorkflowExecution getRunningOrInQueueExecution(String datasetId) {
    Query<WorkflowExecution> query = morphiaDatastoreProvider.getDatastore()
        .find(WorkflowExecution.class)
        .field(DATASET_ID).equal(
            datasetId);
    query.or(query.criteria(WORKFLOW_STATUS).equal(WorkflowStatus.INQUEUE),
        query.criteria(WORKFLOW_STATUS).equal(WorkflowStatus.RUNNING));
    return ExternalRequestUtil.retryableExternalRequestConnectionReset(query::get);
  }

  /**
   * Check the existence of a WorkflowExecution in the database.
   *
   * @param workflowExecution the WorkflowExecution to check upon
   * @return true if it exist, false if it does not exist
   */
  public boolean exists(WorkflowExecution workflowExecution) {
    return ExternalRequestUtil
        .retryableExternalRequestConnectionReset(() -> morphiaDatastoreProvider.getDatastore().find(WorkflowExecution.class)
            .field(DATASET_ID).equal(workflowExecution.getDatasetId())
            .project("_id", true).get()) != null;
  }

  /**
   * Check if a WorkflowExecution exists for a dataset identifier and has not completed it's
   * execution.
   *
   * @param datasetId the dataset identifier
   * @return the identifier of the execution if found, otherwise null
   */
  public String existsAndNotCompleted(String datasetId) {
    Query<WorkflowExecution> query = morphiaDatastoreProvider.getDatastore()
        .find(WorkflowExecution.class).field(DATASET_ID).equal(datasetId);
    query.or(query.criteria(WORKFLOW_STATUS).equal(WorkflowStatus.INQUEUE),
        query.criteria(WORKFLOW_STATUS).equal(WorkflowStatus.RUNNING));
    query.project("_id", true);
    query.project(WORKFLOW_STATUS, true);

    WorkflowExecution storedWorkflowExecution = ExternalRequestUtil.retryableExternalRequestConnectionReset(query::get);
    if (storedWorkflowExecution != null) {
      return storedWorkflowExecution.getId().toString();
    }
    return null;
  }

  /**
   * Get the first successful Plugin of a WorkflowExecution for a dataset identifier and a set of
   * plugin types
   *
   * @param datasetId the dataset identifier
   * @param pluginTypes the set of plugin types to check for
   * @return the first plugin found
   */
  public AbstractMetisPlugin getFirstFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(
      String datasetId, Set<PluginType> pluginTypes) {
    return getFirstOrLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(datasetId,
        pluginTypes, true);
  }

  /**
   * Get the last successful Plugin of a WorkflowExecution for a dataset identifier and a set of
   * plugin types
   *
   * @param datasetId the dataset identifier
   * @param pluginTypes the set of plugin types to check for
   * @return the last plugin found
   */
  public AbstractMetisPlugin getLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(
      String datasetId, Set<PluginType> pluginTypes) {
    return getFirstOrLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(datasetId,
        pluginTypes, false);
  }

  private AbstractMetisPlugin getFirstOrLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(
      String datasetId, Set<PluginType> pluginTypes, boolean firstFinished) {
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
      query.or((CriteriaContainerImpl[]) criteriaContainer
          .toArray(new CriteriaContainerImpl[criteriaContainer.size()]));
    }

    Iterator<WorkflowExecution> metisPluginsIterator = ExternalRequestUtil
        .retryableExternalRequestConnectionReset(() -> aggregation.match(query).unwind(METIS_PLUGINS)
            .match(query).sort(firstFinished ? Sort.ascending("metisPlugins.finishedDate")
                : Sort.descending("metisPlugins.finishedDate")).aggregate(WorkflowExecution.class));

    if (metisPluginsIterator != null && metisPluginsIterator.hasNext()) {
      return metisPluginsIterator.next().getMetisPlugins().get(0);
    }
    return null;
  }

  /**
   * Get all WorkflowExecutions paged.
   *
   * @param datasetIds a set of dataset identifiers to filter, can be empty or null to get all
   * @param workflowStatuses a set of workflow statuses to filter, can be empty or null
   * @param orderField the field to be used to sort the results
   * @param ascending a boolean value to request the ordering to ascending or descending
   * @param nextPage the nextPage token
   * @return a list of all the WorkflowExecutions found
   */
  public List<WorkflowExecution> getAllWorkflowExecutions(Set<String> datasetIds,
      Set<WorkflowStatus> workflowStatuses, OrderField orderField, boolean ascending,
      int nextPage) {
    Query<WorkflowExecution> query =
        morphiaDatastoreProvider.getDatastore().createQuery(WorkflowExecution.class);

    if (datasetIds != null && !datasetIds.isEmpty()) {
      query.field(DATASET_ID).in(datasetIds);
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
    return ExternalRequestUtil.retryableExternalRequestConnectionReset(
        () -> query.asList(new FindOptions().skip(nextPage * getWorkflowExecutionsPerRequest())
            .limit(getWorkflowExecutionsPerRequest())));
  }

  /**
   * The number of WorkflowExecutions that would be returned if a get all request would be
   * performed.
   *
   * @return the number representing the size during a get all request
   */
  public int getWorkflowExecutionsPerRequest() {
    synchronized (this) {
      return workflowExecutionsPerRequest;
    }
  }

  /**
   * Set the number of WorkflowExecutions that would be returned if a get all request would be
   * performed.
   *
   * @param workflowExecutionsPerRequest the number to set to
   */
  public void setWorkflowExecutionsPerRequest(int workflowExecutionsPerRequest) {
    synchronized (this) {
      this.workflowExecutionsPerRequest = workflowExecutionsPerRequest;
    }
  }

  /**
   * Check if a WorkflowExecution using an execution identifier is {@link WorkflowStatus#CANCELLED}
   *
   * @param id the execution identifier
   * @return true for cancelled, false for not cancelled
   */
  public boolean isCancelled(ObjectId id) {
    WorkflowExecution workflowExecution = ExternalRequestUtil.retryableExternalRequestConnectionReset(() ->
        morphiaDatastoreProvider.getDatastore().find(WorkflowExecution.class).field("_id").equal(id)
            .project(WORKFLOW_STATUS, true).get());
    return workflowExecution != null
        && workflowExecution.getWorkflowStatus() == WorkflowStatus.CANCELLED;
  }

  /**
   * Check if a WorkflowExecution using an execution identifier is in a cancelling state. The state
   * before finally being {@link WorkflowStatus#CANCELLED}
   *
   * @param id the execution identifier
   * @return true for cancelling, false for not cancelling
   */
  public boolean isCancelling(ObjectId id) {
    WorkflowExecution workflowExecution = ExternalRequestUtil.retryableExternalRequestConnectionReset(
        () -> morphiaDatastoreProvider.getDatastore().find(WorkflowExecution.class).field("_id")
            .equal(id).project("cancelling", true).get());
    return workflowExecution != null && workflowExecution.isCancelling();
  }

  /**
   * Delete all WorkflowExecutions for a dataset identifier
   *
   * @param datasetId the dataset identifier
   * @return true if at least one was removed
   */
  public boolean deleteAllByDatasetId(String datasetId) {
    Query<WorkflowExecution> query = morphiaDatastoreProvider.getDatastore()
        .createQuery(WorkflowExecution.class);
    query.field(DATASET_ID).equal(datasetId);
    WriteResult delete = ExternalRequestUtil
        .retryableExternalRequestConnectionReset(() -> morphiaDatastoreProvider.getDatastore().delete(query));
    LOGGER.debug("WorkflowExecution with datasetId: {}, deleted from Mongo", datasetId);
    return (delete != null ? delete.getN() : 0) >= 1;
  }

  /**
   * This method retrieves the workflow execution of which the task with the given ID is a subtask.
   *
   * @param externalTaskId The external task ID that is to be queried.
   * @return The dataset ID.
   */
  public WorkflowExecution getByExternalTaskId(long externalTaskId) {
    final Query<AbstractMetisPlugin> subQuery =
        morphiaDatastoreProvider.getDatastore().createQuery(AbstractMetisPlugin.class);
    subQuery.field("externalTaskId").equal(Long.toString(externalTaskId));
    final Query<WorkflowExecution> query =
        morphiaDatastoreProvider.getDatastore().createQuery(WorkflowExecution.class);
    query.field(METIS_PLUGINS).elemMatch(subQuery);
    final List<WorkflowExecution> resultList = ExternalRequestUtil
        .retryableExternalRequestConnectionReset(() -> query.asList(new FindOptions().limit(1)));
    return CollectionUtils.isEmpty(resultList) ? null : resultList.get(0);
  }
}
