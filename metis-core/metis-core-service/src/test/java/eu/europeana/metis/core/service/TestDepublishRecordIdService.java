package eu.europeana.metis.core.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import eu.europeana.metis.authentication.user.MetisUser;
import eu.europeana.metis.core.dao.DepublishRecordIdDao;
import eu.europeana.metis.core.dataset.DatasetExecutionInformation;
import eu.europeana.metis.core.dataset.DatasetExecutionInformation.PublicationStatus;
import eu.europeana.metis.core.dataset.DepublishRecordId;
import eu.europeana.metis.core.rest.DepublishRecordIdView;
import eu.europeana.metis.core.util.DepublishRecordIdSortField;
import eu.europeana.metis.core.util.SortDirection;
import eu.europeana.metis.core.utils.TestObjectFactory;
import eu.europeana.metis.core.workflow.Workflow;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.plugins.DepublishPluginMetadata;
import eu.europeana.metis.exception.GenericMetisException;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestDepublishRecordIdService {

  private static Authorizer authorizer;
  private static OrchestratorService orchestratorService;
  private static DepublishRecordIdDao depublishRecordIdDao;
  private static DepublishRecordIdService depublishRecordIdService;
  private static MetisUser metisUser;
  private static String datasetId;

  @BeforeAll
  static void setUp() {

    authorizer = mock(Authorizer.class);
    orchestratorService = mock(OrchestratorService.class);
    depublishRecordIdDao = mock(DepublishRecordIdDao.class);
    metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    datasetId = Integer.toString(TestObjectFactory.DATASETID);

    depublishRecordIdService = spy(new DepublishRecordIdService(authorizer, orchestratorService,
        depublishRecordIdDao));

  }

  @BeforeEach
  void cleanUp() {
    reset(authorizer);
    reset(orchestratorService);
    reset(depublishRecordIdDao);
    reset(depublishRecordIdService);
  }

  @Test
  void addRecordIdsToBeDepublishedTest() throws GenericMetisException {
    depublishRecordIdService.addRecordIdsToBeDepublished(metisUser, datasetId, "1002");

    verify(authorizer, times(1)).authorizeWriteExistingDatasetById(metisUser, datasetId);
    verify(depublishRecordIdService, times(1)).checkAndNormalizeRecordIds(any(), any());
    verify(depublishRecordIdDao, times(1)).createRecordIdsToBeDepublished(any(), any());
    verifyNoMoreInteractions(orchestratorService);


  }

  @Test
  void deletePendingRecordIdsTest() throws GenericMetisException {
    depublishRecordIdService.deletePendingRecordIds(metisUser, datasetId, "1002");

    verify(authorizer, times(1)).authorizeWriteExistingDatasetById(metisUser, datasetId);
    verify(depublishRecordIdService, times(1)).checkAndNormalizeRecordIds(any(), any());
    verify(depublishRecordIdDao, times(1)).deletePendingRecordIds(any(), any());
    verifyNoMoreInteractions(orchestratorService);

  }

  @Test
  void getDepublishRecordIdsTest() throws GenericMetisException {

    // Mock the DAO
    final DepublishRecordId record = new DepublishRecordId();
    record.setRecordId("RECORD_ID");
    doReturn(List.of(new DepublishRecordIdView(record))).when(depublishRecordIdDao)
        .getDepublishRecordIds(eq(datasetId), anyInt(), any(), any(), anyString());

    // Make the actual call
    final var result = depublishRecordIdService.getDepublishRecordIds(metisUser, datasetId, 1,
            DepublishRecordIdSortField.RECORD_ID, SortDirection.ASCENDING, "search");

    // Verify the interactions
    verify(authorizer, times(1)).authorizeReadExistingDatasetById(metisUser, datasetId);
    verify(depublishRecordIdDao, times(1)).getDepublishRecordIds(datasetId,
            1, DepublishRecordIdSortField.RECORD_ID, SortDirection.ASCENDING, "search");
    verify(depublishRecordIdDao, times(1)).getDepublishRecordIds(anyString(),
            anyInt(), any(), any(), anyString());
    verifyNoMoreInteractions(orchestratorService);

    // verify the result
    assertEquals(1, result.getListSize());
    assertEquals(1, result.getResults().size());
    assertEquals(record.getRecordId(), result.getResults().get(0).getRecordId());
  }

  @Test
  void createAndAddInQueueDepublishWorkflowExecutionTest() throws GenericMetisException {
    final Workflow mockWorkflow = mock(Workflow.class);
    final DepublishPluginMetadata mockDepublishPluginMetadata = mock(DepublishPluginMetadata.class);

    doReturn(mockWorkflow).when(depublishRecordIdService).createNewWorkflow();
    doReturn(mockDepublishPluginMetadata).when(depublishRecordIdService)
        .createNewDepublishPluginMetadata();
    depublishRecordIdService
        .createAndAddInQueueDepublishWorkflowExecution(metisUser, datasetId, true, 1, "search");

    verify(authorizer, times(1)).authorizeReadExistingDatasetById(metisUser, datasetId);
    verify(mockWorkflow, times(1)).setDatasetId(datasetId);
    verify(mockDepublishPluginMetadata, times(1)).setEnabled(true);
    verify(mockDepublishPluginMetadata, times(1)).setDatasetDepublish(anyBoolean());
    verify(mockDepublishPluginMetadata, times(1)).setRecordIdsToDepublish(any());
    verify(mockWorkflow, times(1)).setMetisPluginsMetadata(any());
    verify(orchestratorService, times(1))
        .addWorkflowInQueueOfWorkflowExecutions(any(), anyString(), any(), any(), anyInt());
  }

  @Test
  void canTriggerDepublicationResultTrueTest() throws GenericMetisException {
    final DatasetExecutionInformation mockExecutionInformation = mock(
        DatasetExecutionInformation.class);

    doReturn(mockExecutionInformation).when(orchestratorService)
        .getDatasetExecutionInformation(datasetId);
    doReturn(PublicationStatus.PUBLISHED).when(mockExecutionInformation).getPublicationStatus();
    doReturn(true).when(mockExecutionInformation).isLastPreviewRecordsReadyForViewing();
    doReturn(true).when(mockExecutionInformation).isLastPublishedRecordsReadyForViewing();
    boolean result = depublishRecordIdService.canTriggerDepublication(metisUser, datasetId);

    verify(authorizer, times(1)).authorizeReadExistingDatasetById(metisUser, datasetId);
    verify(orchestratorService, times(1)).getRunningOrInQueueExecution(datasetId);
    verify(orchestratorService, times(1)).getDatasetExecutionInformation(datasetId);
    verify(mockExecutionInformation, times(1)).getPublicationStatus();
    verify(mockExecutionInformation, times(1)).isLastPublishedRecordsReadyForViewing();
    assertTrue(result);
  }

  @Test
  void canTriggerDepublicationResultFalseTest() throws GenericMetisException {
    final WorkflowExecution mockWorkflow = mock(WorkflowExecution.class);

    doReturn(mockWorkflow).when(orchestratorService).getRunningOrInQueueExecution(datasetId);
    boolean result = depublishRecordIdService.canTriggerDepublication(metisUser, datasetId);

    verify(authorizer, times(1)).authorizeReadExistingDatasetById(metisUser, datasetId);
    verify(orchestratorService, times(1)).getRunningOrInQueueExecution(datasetId);
    assertFalse(result);
  }

}
