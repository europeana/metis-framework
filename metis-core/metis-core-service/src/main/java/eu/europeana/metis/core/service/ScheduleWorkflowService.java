package eu.europeana.metis.core.service;

import eu.europeana.metis.authentication.user.MetisUser;
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
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-04-05
 */
@Service
public class ScheduleWorkflowService {

  private final ScheduledWorkflowDao scheduledWorkflowDao;
  private final WorkflowDao workflowDao;
  private final DatasetDao datasetDao;
  private final Authorizer authorizer;

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

  public ScheduledWorkflow getScheduledWorkflowByDatasetId(MetisUser metisUser, String datasetId)
      throws UserUnauthorizedException, NoDatasetFoundException {
    authorizer.authorizeReadExistingDatasetById(metisUser, datasetId);
    return scheduledWorkflowDao.getScheduledWorkflowByDatasetId(datasetId);
  }

  public void scheduleWorkflow(MetisUser metisUser, ScheduledWorkflow scheduledWorkflow)
      throws GenericMetisException {
    authorizer.authorizeWriteExistingDatasetById(metisUser, scheduledWorkflow.getDatasetId());
    checkRestrictionsOnScheduleWorkflow(scheduledWorkflow);
    scheduledWorkflowDao.create(scheduledWorkflow);
  }

  // This method does not require authorization. It is called from a scheduled task.
  public List<ScheduledWorkflow> getAllScheduledWorkflowsWithoutAuthorization(
      ScheduleFrequence scheduleFrequence, int nextPage) {
    return scheduledWorkflowDao.getAllScheduledWorkflows(scheduleFrequence, nextPage);
  }

  public List<ScheduledWorkflow> getAllScheduledWorkflows(MetisUser metisUser,
      ScheduleFrequence scheduleFrequence, int nextPage) throws UserUnauthorizedException {
    authorizer.authorizeReadAllDatasets(metisUser);
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

  public void updateScheduledWorkflow(MetisUser metisUser, ScheduledWorkflow scheduledWorkflow)
      throws GenericMetisException {
    authorizer.authorizeWriteExistingDatasetById(metisUser, scheduledWorkflow.getDatasetId());
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

  public void deleteScheduledWorkflow(MetisUser metisUser, String datasetId)
      throws UserUnauthorizedException, NoDatasetFoundException {
    authorizer.authorizeWriteExistingDatasetById(metisUser, datasetId);
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
