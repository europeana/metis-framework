package eu.europeana.metis.core.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import eu.europeana.metis.authentication.user.MetisUser;
import eu.europeana.metis.core.common.DepublishRecordIdUtils;
import eu.europeana.metis.core.dao.DepublishRecordIdDao;
import eu.europeana.metis.core.dataset.DatasetExecutionInformation;
import eu.europeana.metis.core.dataset.DatasetExecutionInformation.PublicationStatus;
import eu.europeana.metis.core.rest.DepublishRecordIdView;
import eu.europeana.metis.core.rest.ResponseListWrapper;
import eu.europeana.metis.core.util.DepublishRecordIdSortField;
import eu.europeana.metis.core.util.SortDirection;
import eu.europeana.metis.core.utils.TestObjectFactory;
import eu.europeana.metis.core.workflow.Workflow;
import eu.europeana.metis.core.workflow.plugins.DepublishPluginMetadata;
import eu.europeana.metis.exception.GenericMetisException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestDepublishRecordIdService {

  private static Authorizer authorizer;
  private static OrchestratorService orchestratorService;
  private static DepublishRecordIdDao depublishRecordIdDao;
  private static DepublishRecordIdService depublishRecordIdService;
  private static MetisUser metisUser ;
  private static String datasetId ;

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

  @AfterEach
  void cleanUp() {
    reset(authorizer);
    reset(orchestratorService);
    reset(depublishRecordIdDao);

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
    verify(depublishRecordIdService, times(2)).checkAndNormalizeRecordIds(any(), any());
    verify(depublishRecordIdDao, times(1)).deletePendingRecordIds(any(), any());
    verifyNoMoreInteractions(orchestratorService);

  }

  @Test
  void getDepublishRecordIdsTest() throws GenericMetisException {
    final ResponseListWrapper<DepublishRecordIdView> mockResponseListWrapper = mock(
        ResponseListWrapper.class);
    final List<DepublishRecordIdView> mockResultList = new ArrayList<>();
    final DepublishRecordIdView mockDepublishRecordIdView = mock(DepublishRecordIdView.class);
    mockResultList.add(mockDepublishRecordIdView);

    doReturn(mockResponseListWrapper).when(depublishRecordIdService)
        .createResponseListWrapper(anyList(), anyInt());
    doReturn(mockResultList).when(depublishRecordIdDao)
        .getDepublishRecordIds(anyString(), anyInt(), any(), any(), anyString());
    doReturn(1).when(depublishRecordIdDao).getPageSize();

    depublishRecordIdService
        .getDepublishRecordIds(metisUser, datasetId, 1, DepublishRecordIdSortField.RECORD_ID,
            SortDirection.ASCENDING, "search");

    verify(authorizer, times(1)).authorizeReadExistingDatasetById(metisUser, datasetId);
    verify(depublishRecordIdDao, times(1))
        .getDepublishRecordIds(anyString(), anyInt(), any(), any(), anyString());
    verify(depublishRecordIdService, times(1)).createResponseListWrapper(anyList(), anyInt());
//    verify(depublishRecordIdDao, times(1)).getPageSize();
    verifyNoMoreInteractions(orchestratorService);

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
  void canTriggerDepublicationTest() throws GenericMetisException {
    final DatasetExecutionInformation mockExecutionInformation = mock(DatasetExecutionInformation.class);

    doReturn(mockExecutionInformation).when(orchestratorService).getDatasetExecutionInformation(datasetId);
    doReturn(PublicationStatus.PUBLISHED).when(mockExecutionInformation).getPublicationStatus();
    doReturn(true).when(mockExecutionInformation).isLastPreviewRecordsReadyForViewing();
    depublishRecordIdService.canTriggerDepublication(metisUser, datasetId);

    verify(authorizer, times(1)).authorizeReadExistingDatasetById(metisUser, datasetId);
    verify(orchestratorService, times(1)).getRunningOrInQueueExecution(datasetId);
    verify(orchestratorService, times(1)).getDatasetExecutionInformation(datasetId);
    verify(mockExecutionInformation, times(1)).getPublicationStatus();
    verify(mockExecutionInformation, times(1)).isLastPublishedRecordsReadyForViewing();
  }

}
