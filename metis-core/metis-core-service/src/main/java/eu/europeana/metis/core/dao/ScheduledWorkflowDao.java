package eu.europeana.metis.core.dao;

import static eu.europeana.metis.core.common.DaoFieldNames.DATASET_ID;
import static eu.europeana.metis.core.common.DaoFieldNames.ID;
import static eu.europeana.metis.utils.SonarqubeNullcheckAvoidanceUtils.performFunction;

import com.mongodb.WriteResult;
import dev.morphia.Key;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.morphia.query.Sort;
import dev.morphia.query.experimental.filters.Filters;
import dev.morphia.query.internal.MorphiaCursor;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.core.rest.RequestLimits;
import eu.europeana.metis.core.workflow.ScheduleFrequence;
import eu.europeana.metis.core.workflow.ScheduledWorkflow;
import eu.europeana.metis.utils.ExternalRequestUtil;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * DAO class for {@link ScheduledWorkflow}
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-09-25
 */
@Repository
public class ScheduledWorkflowDao implements MetisDao<ScheduledWorkflow, String> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledWorkflowDao.class);
  private int scheduledWorkflowPerRequest = RequestLimits.SCHEDULED_EXECUTIONS_PER_REQUEST
      .getLimit();
  private final MorphiaDatastoreProvider morphiaDatastoreProvider;

  /**
   * Autowired Constructor with required {@link MorphiaDatastoreProvider} parameters.
   *
   * @param morphiaDatastoreProvider the class that handles the connection to the database
   */
  @Autowired
  public ScheduledWorkflowDao(MorphiaDatastoreProvider morphiaDatastoreProvider) {
    this.morphiaDatastoreProvider = morphiaDatastoreProvider;
  }

  @Override
  public String create(ScheduledWorkflow scheduledWorkflow) {
    ScheduledWorkflow scheduledWorkflowSaved = ExternalRequestUtil
        .retryableExternalRequestConnectionReset(
            () -> morphiaDatastoreProvider.getDatastore().save(scheduledWorkflow));
    LOGGER.debug("ScheduledWorkflow for datasetName: '{}' created in Mongo",
        scheduledWorkflow.getDatasetId());
    return scheduledWorkflowSaved == null ? null : scheduledWorkflowSaved.getId().toString();
  }

  @Override
  public String update(ScheduledWorkflow scheduledWorkflow) {
    ScheduledWorkflow scheduledWorkflowSaved =
        ExternalRequestUtil.retryableExternalRequestConnectionReset(
            () -> morphiaDatastoreProvider.getDatastore().save(scheduledWorkflow));
    LOGGER.debug("ScheduledWorkflow with datasetId: '{}' updated in Mongo",
        scheduledWorkflow.getDatasetId());
    return scheduledWorkflowSaved == null ? null : scheduledWorkflowSaved.getId().toString();
  }

  @Override
  public ScheduledWorkflow getById(String id) {
    Query<ScheduledWorkflow> query = morphiaDatastoreProvider.getDatastore()
        .find(ScheduledWorkflow.class)
        .filter(Filters.eq(ID.getFieldName(), new ObjectId(id));
    return ExternalRequestUtil.retryableExternalRequestConnectionReset(query::first);
  }

  @Override
  public boolean delete(ScheduledWorkflow scheduledWorkflow) {
    return false;
  }

  /**
   * Get a shceduled workflow with {@code datasetId}.
   *
   * @param datasetId the dataset identifier
   * @return the found ScheduledWorkflow or null
   */
  public ScheduledWorkflow getScheduledWorkflow(String datasetId) {
    return ExternalRequestUtil.retryableExternalRequestConnectionReset(
        () -> morphiaDatastoreProvider.getDatastore().find(ScheduledWorkflow.class)
            .filter(Filters.eq(DATASET_ID.getFieldName(), datasetId)).first());
  }

  /**
   * Get a ScheduledWorkflow using a datasetId.
   *
   * @param datasetId the dataset identifier
   * @return the found ScheduledWorkflow or null
   */
  public ScheduledWorkflow getScheduledWorkflowByDatasetId(String datasetId) {
    return ExternalRequestUtil.retryableExternalRequestConnectionReset(
        () -> morphiaDatastoreProvider.getDatastore().find(ScheduledWorkflow.class)
            .filter(Filters.eq(DATASET_ID.getFieldName(), datasetId)).first());
  }

  /**
   * Check if a ScheduledWorkflow exists using {@link ScheduledWorkflow#getDatasetId()}.
   *
   * @param scheduledWorkflow the provided ScheduledWorkflow
   * @return true if exist, otherwise false
   */
  public boolean exists(ScheduledWorkflow scheduledWorkflow) {
    return ExternalRequestUtil.retryableExternalRequestConnectionReset(
        () -> morphiaDatastoreProvider.getDatastore()
            .find(ScheduledWorkflow.class)
            .filter(Filters.eq(DATASET_ID.getFieldName(), scheduledWorkflow.getDatasetId()))
            .first(new FindOptions().projection().include(ID.getFieldName()))) != null;
  }

  /**
   * Checks if a ScheduledWorkflow exists by datasetId.
   *
   * @param datasetId the dataset identifier
   * @return the String representation of the ScheduledWorkflow identifier
   */
  public String existsForDatasetId(String datasetId) {
    ScheduledWorkflow storedScheduledWorkflow = ExternalRequestUtil
        .retryableExternalRequestConnectionReset(
            () -> morphiaDatastoreProvider.getDatastore().find(ScheduledWorkflow.class)
                .filter(
                    Filters.eq(DATASET_ID.getFieldName(), datasetId))
                .first(new FindOptions().projection().include(ID.getFieldName())));
    return storedScheduledWorkflow == null ? null : storedScheduledWorkflow.getId().toString();
  }

  /**
   * Delete a ScheduledWorkflow using a datasetId.
   *
   * @param datasetId the dataset identifier
   * @return true if one was deleted, false if none was deleted
   */
  public boolean deleteScheduledWorkflow(String datasetId) {
    Query<ScheduledWorkflow> query = morphiaDatastoreProvider.getDatastore()
        .createQuery(ScheduledWorkflow.class);
    query.filter(Filters.eq(DATASET_ID.getFieldName(), datasetId);
    WriteResult delete = ExternalRequestUtil
        .retryableExternalRequestConnectionReset(
            () -> morphiaDatastoreProvider.getDatastore().delete(query));
    LOGGER.debug(
        "ScheduledWorkflow with datasetId: {} deleted from Mongo",
        datasetId);
    return (delete == null ? 0 : delete.getN()) == 1;
  }

  /**
   * Deletes all ScheduledWorkflows using a datasetId.
   *
   * @param datasetId the dataset identifier
   * @return true if at least one was deleted, false if none
   */
  public boolean deleteAllByDatasetId(String datasetId) {
    Query<ScheduledWorkflow> query = morphiaDatastoreProvider.getDatastore()
        .find(ScheduledWorkflow.class);
    query.filter(Filters.eq(DATASET_ID.getFieldName(), datasetId);
    WriteResult delete = ExternalRequestUtil
        .retryableExternalRequestConnectionReset(
            () -> morphiaDatastoreProvider.getDatastore().delete(query));
    LOGGER.debug(
        "ScheduledWorkflows with datasetId: {} deleted from Mongo", datasetId);
    return (delete == null ? 0 : delete.getN()) >= 1;
  }

  /**
   * Get all ScheduledWorkflows using a {@link ScheduleFrequence} filter paged.
   *
   * @param scheduleFrequence the frequence used to filter the results
   * @param nextPage the nextPage positive number
   * @return a list of ScheduledWorkflows
   */
  public List<ScheduledWorkflow> getAllScheduledWorkflows(
      ScheduleFrequence scheduleFrequence, int nextPage) {
    Query<ScheduledWorkflow> query = morphiaDatastoreProvider.getDatastore()
        .find(ScheduledWorkflow.class);
    if (scheduleFrequence != null && scheduleFrequence != ScheduleFrequence.NULL) {
      query.filter(Filters.eq("scheduleFrequence", scheduleFrequence);
    }
    query.order(Sort.ascending(ID.getFieldName()));
    final FindOptions findOptions = new FindOptions()
        .skip(nextPage * getScheduledWorkflowPerRequest())
        .limit(getScheduledWorkflowPerRequest());
    return getListOfQuery(query, findOptions);
  }

  /**
   * Get all ScheduledWorkflows using a date range check.
   *
   * @param lowerBound the lower edge of the date range with a check of greater or equal
   * @param upperBound the upper edge of the date range with a check of lower than
   * @param nextPage the nextPage positive number
   * @return a list of ScheduledWorkflows
   */
  public List<ScheduledWorkflow> getAllScheduledWorkflowsByDateRangeONCE(
      LocalDateTime lowerBound,
      LocalDateTime upperBound, int nextPage) {
    Query<ScheduledWorkflow> query = morphiaDatastoreProvider.getDatastore()
        .find(ScheduledWorkflow.class);
    query.criteria("scheduleFrequence").equal(ScheduleFrequence.ONCE).and(
        query.criteria("pointerDate").greaterThanOrEq(
            Date.from(lowerBound.atZone(ZoneId.systemDefault()).toInstant()))).and(
        query.criteria("pointerDate")
            .lessThan(Date.from(upperBound.atZone(ZoneId.systemDefault()).toInstant())));
    query.order(Sort.ascending(ID.getFieldName()));
    final FindOptions findOptions = new FindOptions()
        .skip(nextPage * getScheduledWorkflowPerRequest())
        .limit(getScheduledWorkflowPerRequest());
    return getListOfQuery(query, findOptions);

  }

  private <T> List<T> getListOfQuery(Query<T> query, FindOptions findOptions) {
    return ExternalRequestUtil.retryableExternalRequestConnectionReset(() -> {
      try (MorphiaCursor<T> cursor = query.find(findOptions)) {
        return performFunction(cursor, MorphiaCursor::toList);
      }
    });
  }

  public int getScheduledWorkflowPerRequest() {
    synchronized (this) {
      return scheduledWorkflowPerRequest;
    }
  }

  public void setScheduledWorkflowPerRequest(int scheduledWorkflowPerRequest) {
    synchronized (this) {
      this.scheduledWorkflowPerRequest = scheduledWorkflowPerRequest;
    }
  }
}
