package eu.europeana.metis.core.dao;

import com.mongodb.WriteResult;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.core.workflow.ScheduleFrequence;
import eu.europeana.metis.core.workflow.ScheduledWorkflow;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
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
import org.springframework.stereotype.Repository;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-09-25
 */
@Repository
public class ScheduledWorkflowDao implements MetisDao<ScheduledWorkflow, String> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledWorkflowDao.class);
  private static final String DATASET_NAME = "datasetName";
  private int scheduledUserWorkflowPerRequest = 5;
  private final MorphiaDatastoreProvider morphiaDatastoreProvider;

  @Autowired
  public ScheduledWorkflowDao(MorphiaDatastoreProvider morphiaDatastoreProvider) {
    this.morphiaDatastoreProvider = morphiaDatastoreProvider;
  }

  @Override
  public String create(ScheduledWorkflow scheduledWorkflow) {
    Key<ScheduledWorkflow> scheduledUserWorkflowKey = morphiaDatastoreProvider.getDatastore().save(
        scheduledWorkflow);
    LOGGER.debug(
        "ScheduledWorkflow for datasetName: '{}' with workflowName: '{}' and owner: '{}' created in Mongo",
        scheduledWorkflow.getDatasetName(), scheduledWorkflow.getWorkflowName(),
        scheduledWorkflow.getWorkflowOwner());
    return scheduledUserWorkflowKey.getId().toString();
  }

  @Override
  public String update(ScheduledWorkflow scheduledWorkflow) {
    Key<ScheduledWorkflow> scheduledUserWorkflowKey = morphiaDatastoreProvider.getDatastore().save(
        scheduledWorkflow);
    LOGGER.debug(
        "ScheduledWorkflow with datasetName: '{}', workflowName: '{}' and workflowOwner '{}' updated in Mongo",
        scheduledWorkflow.getDatasetName(), scheduledWorkflow.getWorkflowName(),
        scheduledWorkflow.getWorkflowOwner());
    return scheduledUserWorkflowKey.getId().toString();
  }

  @Override
  public ScheduledWorkflow getById(String id) {
    Query<ScheduledWorkflow> query = morphiaDatastoreProvider.getDatastore()
        .find(ScheduledWorkflow.class)
        .field("_id").equal(new ObjectId(id));
    return query.get();
  }

  @Override
  public boolean delete(ScheduledWorkflow scheduledWorkflow) {
    return false;
  }

  public ScheduledWorkflow getScheduledUserWorkflow(String datasetName, String workflowOwner,
      String workflowName) {
    return morphiaDatastoreProvider.getDatastore()
        .find(ScheduledWorkflow.class).field(DATASET_NAME)
        .equal(datasetName).field("workflowOwner")
        .equal(workflowOwner).field("workflowName")
        .equal(workflowName).get();
  }

  public ScheduledWorkflow getScheduledUserWorkflowByDatasetName(String datasetName) {
    return morphiaDatastoreProvider.getDatastore()
        .find(ScheduledWorkflow.class).field(DATASET_NAME)
        .equal(datasetName).get();
  }

  public boolean exists(ScheduledWorkflow scheduledWorkflow) {
    return morphiaDatastoreProvider.getDatastore()
        .find(ScheduledWorkflow.class).field(DATASET_NAME)
        .equal(scheduledWorkflow.getDatasetName()).field("workflowOwner")
        .equal(scheduledWorkflow.getWorkflowOwner()).field("workflowName")
        .equal(scheduledWorkflow.getWorkflowName())
        .project("_id", true).get() != null;
  }

  public String existsForDatasetName(String datasetName) {
    ScheduledWorkflow storedScheduledWorkflow = morphiaDatastoreProvider.getDatastore()
        .find(ScheduledWorkflow.class).field(DATASET_NAME)
        .equal(datasetName).project("_id", true).get();
    return storedScheduledWorkflow != null ? storedScheduledWorkflow.getId().toString()
        : null;
  }

  public boolean deleteScheduledUserWorkflow(String datasetName) {
    Query<ScheduledWorkflow> query = morphiaDatastoreProvider.getDatastore()
        .createQuery(ScheduledWorkflow.class);
    query.field(DATASET_NAME).equal(datasetName);
    WriteResult delete = morphiaDatastoreProvider.getDatastore().delete(query);
    LOGGER.debug(
        "ScheduledWorkflow with datasetName: {} deleted from Mongo",
        datasetName);
    return delete.getN() == 1;
  }

  public boolean deleteAllByDatasetName(String datasetName) {
    Query<ScheduledWorkflow> query = morphiaDatastoreProvider.getDatastore()
        .createQuery(ScheduledWorkflow.class);
    query.field(DATASET_NAME).equal(datasetName);
    WriteResult delete = morphiaDatastoreProvider.getDatastore().delete(query);
    LOGGER.debug(
        "ScheduledUserWorkflows with datasetName: {} deleted from Mongo", datasetName);
    return delete.getN() >= 1;
  }

  public void updateAllDatasetNames(String datasetName, String newDatasetName) {
    UpdateOperations<ScheduledWorkflow> scheduledUserWorkflowUpdateOperations = morphiaDatastoreProvider
        .getDatastore()
        .createUpdateOperations(ScheduledWorkflow.class);
    Query<ScheduledWorkflow> query = morphiaDatastoreProvider.getDatastore().find(ScheduledWorkflow.class)
        .filter(DATASET_NAME, datasetName);
    scheduledUserWorkflowUpdateOperations.set(DATASET_NAME, newDatasetName);
    UpdateResults updateResults = morphiaDatastoreProvider.getDatastore()
        .update(query, scheduledUserWorkflowUpdateOperations);
    LOGGER.debug(
        "ScheduledWorkflow with datasetName '{}' renamed to '{}'. (UpdateResults: {})",
        datasetName, newDatasetName, updateResults.getUpdatedCount());
  }

  public List<ScheduledWorkflow> getAllScheduledUserWorkflows(
      ScheduleFrequence scheduleFrequence, String nextPage) {
    Query<ScheduledWorkflow> query = morphiaDatastoreProvider.getDatastore()
        .createQuery(ScheduledWorkflow.class);
    if (scheduleFrequence != null && scheduleFrequence != ScheduleFrequence.NULL) {
      query.field("scheduleFrequence").equal(scheduleFrequence);
    }
    query.order("_id");
    if (StringUtils.isNotEmpty(nextPage)) {
      query.field("_id").greaterThan(new ObjectId(nextPage));
    }
    return query.asList(new FindOptions().limit(scheduledUserWorkflowPerRequest));
  }

  public List<ScheduledWorkflow> getAllScheduledUserWorkflowsByDateRangeONCE(
      LocalDateTime lowerBound,
      LocalDateTime upperBound, String nextPage) {
    Query<ScheduledWorkflow> query = morphiaDatastoreProvider.getDatastore()
        .createQuery(ScheduledWorkflow.class);
    query.criteria("scheduleFrequence").equal(ScheduleFrequence.ONCE).and(
        query.criteria("pointerDate").greaterThanOrEq(
            Date.from(lowerBound.atZone(ZoneId.systemDefault()).toInstant()))).and(
        query.criteria("pointerDate")
            .lessThan(Date.from(upperBound.atZone(ZoneId.systemDefault()).toInstant())));
    query.order("_id");
    if (StringUtils.isNotEmpty(nextPage)) {
      query.field("_id").greaterThan(new ObjectId(nextPage));
    }
    return query.asList(new FindOptions().limit(scheduledUserWorkflowPerRequest));
  }

  public int getScheduledUserWorkflowPerRequest() {
    return scheduledUserWorkflowPerRequest;
  }

  public void setScheduledUserWorkflowPerRequest(int scheduledUserWorkflowPerRequest) {
    this.scheduledUserWorkflowPerRequest = scheduledUserWorkflowPerRequest;
  }
}
