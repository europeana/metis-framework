package eu.europeana.metis.core.service;

import eu.europeana.metis.authentication.user.MetisUserView;
import eu.europeana.metis.core.dao.DatasetDao;
import eu.europeana.metis.core.dao.ScheduledWorkflowDao;
import eu.europeana.metis.core.dao.WorkflowDao;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.exceptions.NoDatasetFoundException;
import eu.europeana.metis.core.exceptions.NoScheduledWorkflowFoundException;
import eu.europeana.metis.core.exceptions.NoWorkflowFoundException;
import eu.europeana.metis.core.exceptions.ScheduledWorkflowAlreadyExistsException;
import eu.europeana.metis.core.workflow.ScheduleFrequence;
import eu.europeana.metis.core.workflow.ScheduledWorkflow;
import eu.europeana.metis.core.workflow.Workflow;
import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.exception.GenericMetisException;
import eu.europeana.metis.exception.UserUnauthorizedException;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service class that controls the communication between the different DAOs of the system for
 * controlling scheduled workflows.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-04-05
 */
@Service
public class ScheduleWorkflowService {

  private final ScheduledWorkflowDao scheduledWorkflowDao;
  private final WorkflowDao workflowDao;
  private final DatasetDao datasetDao;
  private final Authorizer authorizer;

  /**
   * Constructor with required parameters.
   *
   * @param scheduledWorkflowDao the dao for accessing schedules
   * @param workflowDao the dao for workflows
   * @param datasetDao the dao for datasets
   * @param authorizer the class used for authorizing requests
   */
  @Autowired
  public ScheduleWorkflowService(ScheduledWorkflowDao scheduledWorkflowDao, WorkflowDao workflowDao,
      DatasetDao datasetDao, Authorizer authorizer) {
    this.scheduledWorkflowDao = scheduledWorkflowDao;
    this.workflowDao = workflowDao;
    this.datasetDao = datasetDao;
    this.authorizer = authorizer;
  }

  public int getScheduledWorkflowsPerRequest() {
    return scheduledWorkflowDao.getScheduledWorkflowPerRequest();
  }

  /**
   * Get a scheduled workflow based on datasets identifier.
   *
   * @param metisUserView the metis user trying to access the scheduled workflow
   * @param datasetId the dataset identifier of which a scheduled workflow is to be retrieved
   * @return the scheduled workflow
   * @throws UserUnauthorizedException if user is unauthorized to access the scheduled workflow
   * @throws NoDatasetFoundException if dataset identifier does not exist
   */
  public ScheduledWorkflow getScheduledWorkflowByDatasetId(MetisUserView metisUserView, String datasetId)
      throws UserUnauthorizedException, NoDatasetFoundException {
    authorizer.authorizeReadExistingDatasetById(metisUserView, datasetId);
    return scheduledWorkflowDao.getScheduledWorkflowByDatasetId(datasetId);
  }

  /**
   * Schedules a provided workflow.
   *
   * @param metisUserView the user that tries to submit a scheduled workflow
   * @param scheduledWorkflow the scheduled workflow information
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link NoDatasetFoundException} if the dataset does not exist</li>
   * <li>{@link UserUnauthorizedException} if the user is unauthorized</li>
   * <li>{@link BadContentException} if some content send was not acceptable</li>
   * <li>{@link NoWorkflowFoundException} if the workflow for a dataset was not found</li>
   * <li>{@link ScheduledWorkflowAlreadyExistsException} if a scheduled workflow already exists</li>
   * </ul>
   */
  public void scheduleWorkflow(MetisUserView metisUserView, ScheduledWorkflow scheduledWorkflow)
      throws GenericMetisException {
    authorizer.authorizeWriteExistingDatasetById(metisUserView, scheduledWorkflow.getDatasetId());
    checkRestrictionsOnScheduleWorkflow(scheduledWorkflow);
    scheduledWorkflowDao.create(scheduledWorkflow);
  }

  // This method does not require authorization. It is called from a scheduled task.
  public List<ScheduledWorkflow> getAllScheduledWorkflowsWithoutAuthorization(
      ScheduleFrequence scheduleFrequence, int nextPage) {
    return scheduledWorkflowDao.getAllScheduledWorkflows(scheduleFrequence, nextPage);
  }

  public List<ScheduledWorkflow> getAllScheduledWorkflows(MetisUserView metisUserView,
      ScheduleFrequence scheduleFrequence, int nextPage) throws UserUnauthorizedException {
    authorizer.authorizeReadAllDatasets(metisUserView);
    return getAllScheduledWorkflowsWithoutAuthorization(scheduleFrequence, nextPage);
  }

  // This method does not require authorization. It is called from a scheduled task.
  public List<ScheduledWorkflow> getAllScheduledWorkflowsByDateRangeONCE(
      LocalDateTime lowerBound,
      LocalDateTime upperBound, int nextPage) {
    return scheduledWorkflowDao
        .getAllScheduledWorkflowsByDateRangeONCE(lowerBound, upperBound, nextPage);
  }

  private void checkScheduledWorkflowExistenceForDatasetId(String datasetId)
      throws ScheduledWorkflowAlreadyExistsException {
    String id = scheduledWorkflowDao.existsForDatasetId(datasetId);
    if (id != null) {
      throw new ScheduledWorkflowAlreadyExistsException(String.format(
          "ScheduledWorkflow for datasetId: %s with id %s, already exists",
          datasetId, id));
    }
  }

  public void updateScheduledWorkflow(MetisUserView metisUserView, ScheduledWorkflow scheduledWorkflow)
      throws GenericMetisException {
    authorizer.authorizeWriteExistingDatasetById(metisUserView, scheduledWorkflow.getDatasetId());
    String storedId = checkRestrictionsOnScheduledWorkflowUpdate(scheduledWorkflow);
    scheduledWorkflow.setId(new ObjectId(storedId));
    scheduledWorkflowDao.update(scheduledWorkflow);
  }

  private void checkRestrictionsOnScheduleWorkflow(ScheduledWorkflow scheduledWorkflow)
      throws
      NoWorkflowFoundException, NoDatasetFoundException, ScheduledWorkflowAlreadyExistsException, BadContentException {
    checkDatasetExistence(scheduledWorkflow.getDatasetId());
    checkWorkflowExistence(scheduledWorkflow.getDatasetId());
    checkScheduledWorkflowExistenceForDatasetId(scheduledWorkflow.getDatasetId());
    if (scheduledWorkflow.getPointerDate() == null) {
      throw new BadContentException("PointerDate cannot be null");
    }
    if (scheduledWorkflow.getScheduleFrequence() == null
        || scheduledWorkflow.getScheduleFrequence() == ScheduleFrequence.NULL) {
      throw new BadContentException("NULL or null is not a valid scheduleFrequence");
    }
  }

  private String checkRestrictionsOnScheduledWorkflowUpdate(
      ScheduledWorkflow scheduledWorkflow)
      throws NoScheduledWorkflowFoundException, BadContentException, NoWorkflowFoundException {
    checkWorkflowExistence(scheduledWorkflow.getDatasetId());
    String storedId = scheduledWorkflowDao.existsForDatasetId(scheduledWorkflow.getDatasetId());
    if (StringUtils.isEmpty(storedId)) {
      throw new NoScheduledWorkflowFoundException(String.format(
          "Workflow with datasetId: %s, not found", scheduledWorkflow.getDatasetId()));
    }
    if (scheduledWorkflow.getPointerDate() == null) {
      throw new BadContentException("PointerDate cannot be null");
    }
    if (scheduledWorkflow.getScheduleFrequence() == null
        || scheduledWorkflow.getScheduleFrequence() == ScheduleFrequence.NULL) {
      throw new BadContentException("NULL or null is not a valid scheduleFrequence");
    }
    return storedId;
  }

  public void deleteScheduledWorkflow(MetisUserView metisUserView, String datasetId)
      throws UserUnauthorizedException, NoDatasetFoundException {
    authorizer.authorizeWriteExistingDatasetById(metisUserView, datasetId);
    scheduledWorkflowDao.deleteScheduledWorkflow(datasetId);
  }

  private Dataset checkDatasetExistence(String datasetId) throws NoDatasetFoundException {
    Dataset dataset = datasetDao.getDatasetByDatasetId(datasetId);
    if (dataset == null) {
      throw new NoDatasetFoundException(
          String.format("No dataset found with datasetId: %s, in METIS", datasetId));
    }
    return dataset;
  }

  private Workflow checkWorkflowExistence(String datasetId) throws NoWorkflowFoundException {
    Workflow workflow = workflowDao.getWorkflow(datasetId);
    if (workflow == null) {
      throw new NoWorkflowFoundException(
          String.format("No workflow found with datasetId: %s, in METIS", datasetId));
    }
    return workflow;
  }
}
