package eu.europeana.metis.core.dao;

import com.mongodb.WriteResult;
import eu.europeana.metis.authentication.user.MetisUser;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.core.rest.RequestLimits;
import eu.europeana.metis.core.workflow.CancelledSystemId;
import eu.europeana.metis.core.workflow.OrderField;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.PluginStatus;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import eu.europeana.metis.utils.ExternalRequestUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.aggregation.AggregationPipeline;
import org.mongodb.morphia.aggregation.Projection;
import org.mongodb.morphia.query.Criteria;
import org.mongodb.morphia.query.CriteriaContainerImpl;
import org.mongodb.morphia.query.FilterOperator;
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
 * Data Access Object for workflow executions using mongo.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-26
 */
@Repository
public class WorkflowExecutionDao implements MetisDao<WorkflowExecution, String> {

  private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowExecutionDao.class);
  private static final String WORKFLOW_STATUS = "workflowStatus";
  private static final String DATASET_ID = "datasetId";
  private static final String METIS_PLUGINS = "metisPlugins";

  private static final String MONGO_COND_OPERATOR = "$cond";

  private static final int INQUEUE_POSITION_IN_OVERVIEW = 1;
  private static final int RUNNING_POSITION_IN_OVERVIEW = 2;
  private static final int DEFAULT_POSITION_IN_OVERVIEW = 3;

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
        .retryableExternalRequestConnectionReset(
            () -> morphiaDatastoreProvider.getDatastore().save(workflowExecution));
    LOGGER.debug("WorkflowExecution for datasetId '{}' created in Mongo",
        workflowExecution.getDatasetId());
    return workflowExecutionKey == null ? null : workflowExecutionKey.getId().toString();
  }

  @Override
  public String update(WorkflowExecution workflowExecution) {
    final Key<WorkflowExecution> workflowExecutionKey = ExternalRequestUtil
        .retryableExternalRequestConnectionReset(() ->
            morphiaDatastoreProvider.getDatastore().save(workflowExecution));
    LOGGER.debug("WorkflowExecution for datasetId '{}' updated in Mongo",
        workflowExecution.getDatasetId());
    return workflowExecutionKey == null ? null : workflowExecutionKey.getId().toString();
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
        updateResults == null ? 0 : updateResults.getUpdatedCount());
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
        updateResults == null ? 0 : updateResults.getUpdatedCount());
  }

  /**
   * Set the cancelling field in the database.
   * <p>Also adds information of the user identifier that cancelled the execution or if it was by a
   * system operation, using {@link CancelledSystemId} values as identifiers. For historical
   * executions the value of the <code>cancelledBy</code> field will remain <code>null</code></p>
   *
   * @param workflowExecution the workflowExecution to be cancelled
   * @param metisUser the user that triggered the cancellation or null if it was the system
   */
  public void setCancellingState(WorkflowExecution workflowExecution, MetisUser metisUser) {
    UpdateOperations<WorkflowExecution> workflowExecutionUpdateOperations = morphiaDatastoreProvider
        .getDatastore().createUpdateOperations(WorkflowExecution.class);
    Query<WorkflowExecution> query = morphiaDatastoreProvider.getDatastore()
        .find(WorkflowExecution.class)
        .filter("_id", workflowExecution.getId());
    workflowExecutionUpdateOperations.set("cancelling", Boolean.TRUE);
    String cancelledBy;
    if (metisUser == null || metisUser.getUserId() == null) {
      cancelledBy = CancelledSystemId.SYSTEM_MINUTE_CAP_EXPIRE.name();
    } else {
      cancelledBy = metisUser.getUserId();
    }
    workflowExecutionUpdateOperations.set("cancelledBy", cancelledBy);
    UpdateResults updateResults = ExternalRequestUtil
        .retryableExternalRequestConnectionReset(() -> morphiaDatastoreProvider.getDatastore()
            .update(query, workflowExecutionUpdateOperations));
    LOGGER.debug(
        "WorkflowExecution cancelling for datasetId '{}' set to true in Mongo. (UpdateResults: {})",
        workflowExecution.getDatasetId(),
        updateResults == null ? 0 : updateResults.getUpdatedCount());
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
        .retryableExternalRequestConnectionReset(
            () -> morphiaDatastoreProvider.getDatastore().find(WorkflowExecution.class)
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

    WorkflowExecution storedWorkflowExecution = ExternalRequestUtil
        .retryableExternalRequestConnectionReset(query::get);
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
      query.or((CriteriaContainerImpl[]) criteriaContainer.toArray(new CriteriaContainerImpl[0]));
    }

    Iterator<WorkflowExecution> metisPluginsIterator = ExternalRequestUtil
        .retryableExternalRequestConnectionReset(
            () -> aggregation.match(query).unwind(METIS_PLUGINS)
                .match(query).sort(firstFinished ? Sort.ascending("metisPlugins.finishedDate")
                    : Sort.descending("metisPlugins.finishedDate"))
                .aggregate(WorkflowExecution.class));

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
   * Get an overview of all WorkflowExecutions. This returns a list of executions ordered to display
   * an overview. First the ones in queue, then those in progress and then those that are finalized.
   * They will be sorted by creation date. This method does support pagination.
   *
   * TODO when we migrate to mongo 3.4 or later, we can do this easier with new aggregation pipeline
   * stages and operators. The main improvements are 1) to try to map the root to the 'execution'
   * variable so that we don't have to look it up afterwards, and 2) to use $addFields with $switch
   * to add the statusIndex instead of having to go through creating and subtracting the two
   * temporary fields.
   *
   * @param datasetIds a set of dataset identifiers to filter, can be empty or null to get all
   * @param nextPage the nextPage token
   * @return a list of all the WorkflowExecutions found
   */
  public List<ExecutionDatasetPair> getWorkflowExecutionsOverview(Set<String> datasetIds,
      int nextPage) {

    // Create the aggregate pipeline
    final AggregationPipeline pipeline = morphiaDatastoreProvider.getDatastore()
        .createAggregation(WorkflowExecution.class);

    // Step 1: match the datasets that are in the list.
    if (datasetIds != null) {
      final Query<WorkflowExecution> query =
          morphiaDatastoreProvider.getDatastore().createQuery(WorkflowExecution.class);
      query.field(DATASET_ID).in(datasetIds);
      pipeline.match(query);
    }

    // Step 2: Add specific positions when the status is INQUEUE or RUNNING.
    final String statusInQueueField = "statusInQueue";
    final String statusRunningField = "statusRunning";
    pipeline.project(
        Projection.projection(statusInQueueField, Projection.expression(MONGO_COND_OPERATOR,
            Projection.expression(FilterOperator.EQUAL.val(), WorkflowStatus.INQUEUE.name(),
                "$" + WORKFLOW_STATUS), INQUEUE_POSITION_IN_OVERVIEW, 0)),
        Projection.projection(statusRunningField, Projection.expression(MONGO_COND_OPERATOR,
            Projection.expression(FilterOperator.EQUAL.val(), WorkflowStatus.RUNNING.name(),
                "$" + WORKFLOW_STATUS), RUNNING_POSITION_IN_OVERVIEW, 0)),
        Projection.projection(OrderField.CREATED_DATE.getOrderFieldName()),
        Projection.projection(DATASET_ID)
    );

    // Step 3: Copy specific positions to final variable: use default position if no position is set.
    final String statusIndexField = "statusIndex";
    final Projection sumExpression = Projection.add("$" + statusInQueueField, "$" + statusRunningField);
    pipeline.project(
        Projection.projection(statusIndexField, Projection.expression(MONGO_COND_OPERATOR,
            Projection.expression(FilterOperator.EQUAL.val(), sumExpression, 0),
            DEFAULT_POSITION_IN_OVERVIEW, sumExpression)),
        Projection.projection(OrderField.CREATED_DATE.getOrderFieldName()),
        Projection.projection(DATASET_ID)
    );

    // Step 4: Sort - first on the status index, then on the createdDate.
    pipeline.sort(Sort.ascending(statusIndexField),
        Sort.descending(OrderField.CREATED_DATE.getOrderFieldName()));

    // Step 5: Apply pagination
    pipeline.skip(nextPage * getWorkflowExecutionsPerRequest())
        .limit(getWorkflowExecutionsPerRequest());

    // Step 6: Join with the dataset and the execution
    final String datasetCollectionName = morphiaDatastoreProvider.getDatastore()
        .getCollection(Dataset.class).getName();
    final String executionCollectionName = morphiaDatastoreProvider.getDatastore()
        .getCollection(WorkflowExecution.class).getName();
    final String datasetListField = "datasetList";
    final String executionListField = "executionList";
    pipeline.lookup(datasetCollectionName, DATASET_ID, DatasetDao.DATASET_ID, datasetListField);
    pipeline.lookup(executionCollectionName, "_id", "_id", executionListField);

    // Step 7: Keep only the first entry in the dataset and execution lists.
    final String datasetField = "dataset";
    final String executionField = "execution";
    pipeline.project(
        Projection.projection(datasetField,
            Projection.expression("$arrayElemAt", "$" + datasetListField, 0)),
        Projection.projection(executionField,
            Projection.expression("$arrayElemAt", "$" + executionListField, 0)),
        Projection.projection("_id").suppress()
    );

    // Done: execute and return result.
    final List<ExecutionDatasetPair> result = new ArrayList<>();
    pipeline.aggregate(ExecutionDatasetPair.class).forEachRemaining(result::add);
    return result;
  }

  /**
   * This object contains a pair consisting of a dataset and an execution. It is meant to be a
   * result of aggregate queries, so the field names cannot easily be changed.
   */
  public static class ExecutionDatasetPair {

    private Dataset dataset;
    private WorkflowExecution execution;

    public ExecutionDatasetPair() {
    }

    /**
     * Constructor.
     *
     * @param dataset The dataset.
     * @param execution The execution.
     */
    public ExecutionDatasetPair(Dataset dataset, WorkflowExecution execution) {
      this.dataset = dataset;
      this.execution = execution;
    }

    public Dataset getDataset() {
      return dataset;
    }

    public WorkflowExecution getExecution() {
      return execution;
    }
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
    WorkflowExecution workflowExecution = ExternalRequestUtil
        .retryableExternalRequestConnectionReset(() ->
            morphiaDatastoreProvider.getDatastore().find(WorkflowExecution.class).field("_id")
                .equal(id)
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
    WorkflowExecution workflowExecution = ExternalRequestUtil
        .retryableExternalRequestConnectionReset(
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
        .retryableExternalRequestConnectionReset(
            () -> morphiaDatastoreProvider.getDatastore().delete(query));
    LOGGER.debug("WorkflowExecution with datasetId: {}, deleted from Mongo", datasetId);
    return (delete == null ? 0 : delete.getN()) >= 1;
  }

  /**
   * This method retrieves the workflow execution of which the task with the given ID is a subtask.
   *
   * @param externalTaskId The external task ID that is to be queried.
   * @return The workflow execution.
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

  /**
   * This method retrieves the workflow execution that contains a subtask satisfying the given
   * parameters.
   *
   * @param startedDate The started date of the subtask.
   * @param pluginType The plugin type of the subtask.
   * @param datasetId The dataset ID of the workflow execution.
   * @return The workflow execution.
   */
  public WorkflowExecution getByTaskExecution(Date startedDate, PluginType pluginType,
      String datasetId) {

    // Create subquery to find the correct plugin.
    final Query<AbstractMetisPlugin> subQuery =
        morphiaDatastoreProvider.getDatastore().createQuery(AbstractMetisPlugin.class);
    subQuery.field("startedDate").equal(startedDate);
    subQuery.field("pluginType").equal(pluginType);

    // Create query to find workflow execution
    final Query<WorkflowExecution> query =
        morphiaDatastoreProvider.getDatastore().createQuery(WorkflowExecution.class);
    query.field(DATASET_ID).equal(datasetId);
    query.field(METIS_PLUGINS).elemMatch(subQuery);

    // Execute query
    final List<WorkflowExecution> resultList = ExternalRequestUtil
        .retryableExternalRequestConnectionReset(() -> query.asList(new FindOptions().limit(1)));
    return CollectionUtils.isEmpty(resultList) ? null : resultList.get(0);
  }
}
