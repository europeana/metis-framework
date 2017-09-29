package eu.europeana.metis.core.dao;

import com.mongodb.WriteResult;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.core.workflow.ScheduleFrequence;
import eu.europeana.metis.core.workflow.ScheduledUserWorkflow;
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
public class ScheduledUserWorkflowDao implements MetisDao<ScheduledUserWorkflow, String> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledUserWorkflowDao.class);
  private int scheduledUserWorkflowPerRequest = 5;
  private final MorphiaDatastoreProvider provider;

  @Autowired
  public ScheduledUserWorkflowDao(MorphiaDatastoreProvider provider) {
    this.provider = provider;
  }

  @Override
  public String create(ScheduledUserWorkflow scheduledUserWorkflow) {
    Key<ScheduledUserWorkflow> scheduledUserWorkflowKey = provider.getDatastore().save(
        scheduledUserWorkflow);
    LOGGER.debug(
        "ScheduledUserWorkflow for datasetName: '{}' with workflowName: '{}' and owner: '{}' created in Mongo",
        scheduledUserWorkflow.getDatasetName(), scheduledUserWorkflow.getWorkflowName(),
        scheduledUserWorkflow.getWorkflowOwner());
    return scheduledUserWorkflowKey.getId().toString();
  }

  @Override
  public String update(ScheduledUserWorkflow scheduledUserWorkflow) {
    Key<ScheduledUserWorkflow> scheduledUserWorkflowKey = provider.getDatastore().save(
        scheduledUserWorkflow);
    LOGGER.debug(
        "ScheduledUserWorkflow with datasetName: '{}', workflowName: '{}' and workflowOwner '{}' updated in Mongo",
        scheduledUserWorkflow.getDatasetName(), scheduledUserWorkflow.getWorkflowName(),
        scheduledUserWorkflow.getWorkflowOwner());
    return scheduledUserWorkflowKey.getId().toString();
  }

  @Override
  public ScheduledUserWorkflow getById(String id) {
    Query<ScheduledUserWorkflow> query = provider.getDatastore()
        .find(ScheduledUserWorkflow.class)
        .field("_id").equal(new ObjectId(id));
    return query.get();
  }

  @Override
  public boolean delete(ScheduledUserWorkflow scheduledUserWorkflow) {
    return false;
  }

  public ScheduledUserWorkflow getScheduledUserWorkflow(String datasetName, String workflowOwner,
      String workflowName) {
    return provider.getDatastore()
        .find(ScheduledUserWorkflow.class).field("datasetName")
        .equal(datasetName).field("workflowOwner")
        .equal(workflowOwner).field("workflowName")
        .equal(workflowName).get();
  }

  public ScheduledUserWorkflow getScheduledUserWorkflowByDatasetName(String datasetName) {
    return provider.getDatastore()
        .find(ScheduledUserWorkflow.class).field("datasetName")
        .equal(datasetName).get();
  }

  public String exists(ScheduledUserWorkflow scheduledUserWorkflow) {
    ScheduledUserWorkflow storedScheduledUserWorkflow = provider.getDatastore()
        .find(ScheduledUserWorkflow.class).field("datasetName")
        .equal(scheduledUserWorkflow.getDatasetName()).field("workflowOwner")
        .equal(scheduledUserWorkflow.getWorkflowOwner()).field("workflowName")
        .equal(scheduledUserWorkflow.getWorkflowName())
        .project("_id", true).get();
    return storedScheduledUserWorkflow != null ? storedScheduledUserWorkflow.getId().toString()
        : null;
  }

  public String existsForDatasetName(String datasetName) {
    ScheduledUserWorkflow storedScheduledUserWorkflow = provider.getDatastore()
        .find(ScheduledUserWorkflow.class).field("datasetName")
        .equal(datasetName).project("_id", true).get();
    return storedScheduledUserWorkflow != null ? storedScheduledUserWorkflow.getId().toString()
        : null;
  }

  public boolean deleteScheduledUserWorkflow(String datasetName, String workflowOwner,
      String workflowName) {
    Query<ScheduledUserWorkflow> query = provider.getDatastore()
        .createQuery(ScheduledUserWorkflow.class);
    query.field("datasetName").equal(datasetName);
    query.field("workflowOwner").equal(workflowOwner);
    query.field("workflowName").equal(workflowName);
    WriteResult delete = provider.getDatastore().delete(query);
    LOGGER.debug(
        "ScheduledUserWorkflow with datasetName: {} workflowOwner: {}, and workflowName {}, deleted from Mongo",
        datasetName, workflowOwner, workflowName);
    return delete.getN() == 1;
  }

  public boolean deleteAllByDatasetName(String datasetName) {
    Query<ScheduledUserWorkflow> query = provider.getDatastore()
        .createQuery(ScheduledUserWorkflow.class);
    query.field("datasetName").equal(datasetName);
    WriteResult delete = provider.getDatastore().delete(query);
    LOGGER.debug(
        "ScheduledUserWorkflows with datasetName: {} deleted from Mongo", datasetName);
    return delete.getN() >= 1;
  }

  public void updateAllDatasetNames(String datasetName, String newDatasetName) {
    UpdateOperations<ScheduledUserWorkflow> scheduledUserWorkflowUpdateOperations = provider
        .getDatastore()
        .createUpdateOperations(ScheduledUserWorkflow.class);
    Query<ScheduledUserWorkflow> query = provider.getDatastore().find(ScheduledUserWorkflow.class)
        .filter("datasetName", datasetName);
    scheduledUserWorkflowUpdateOperations.set("datasetName", newDatasetName);
    UpdateResults updateResults = provider.getDatastore()
        .update(query, scheduledUserWorkflowUpdateOperations);
    LOGGER.debug(
        "ScheduledUserWorkflow with datasetName '{}' renamed to '{}'. (UpdateResults: {})",
        datasetName, newDatasetName, updateResults.getUpdatedCount());
  }

  public List<ScheduledUserWorkflow> getAllScheduledUserWorkflows(
      ScheduleFrequence scheduleFrequence, String nextPage) {
    Query<ScheduledUserWorkflow> query = provider.getDatastore()
        .createQuery(ScheduledUserWorkflow.class);
    if (scheduleFrequence != null && scheduleFrequence != ScheduleFrequence.NULL) {
      query.field("scheduleFrequence").equal(scheduleFrequence);
    }
    query.order("_id");
    if (StringUtils.isNotEmpty(nextPage)) {
      query.field("_id").greaterThan(new ObjectId(nextPage));
    }
    return query.asList(new FindOptions().limit(scheduledUserWorkflowPerRequest));
  }

  public List<ScheduledUserWorkflow> getAllScheduledUserWorkflowsByDateRangeONCE(
      LocalDateTime lowBound,
      LocalDateTime highBound, String nextPage) {
    Query<ScheduledUserWorkflow> query = provider.getDatastore()
        .createQuery(ScheduledUserWorkflow.class);
    query.criteria("scheduleFrequence").equal(ScheduleFrequence.ONCE).and(
        query.criteria("pointerDate").greaterThanOrEq(
            Date.from(lowBound.atZone(ZoneId.systemDefault()).toInstant()))).and(
        query.criteria("pointerDate")
            .lessThan(Date.from(highBound.atZone(ZoneId.systemDefault()).toInstant())));
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
