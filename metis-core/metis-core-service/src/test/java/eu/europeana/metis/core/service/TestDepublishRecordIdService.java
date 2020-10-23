package eu.europeana.metis.core.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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
import eu.europeana.metis.core.rest.DepublishRecordIdView;
import eu.europeana.metis.core.rest.ResponseListWrapper;
import eu.europeana.metis.core.util.DepublishRecordIdSortField;
import eu.europeana.metis.core.util.SortDirection;
import eu.europeana.metis.core.utils.TestObjectFactory;
import eu.europeana.metis.exception.GenericMetisException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestDepublishRecordIdService {

  private static Authorizer authorizer;
  private static OrchestratorService orchestratorService;
  private static DepublishRecordIdDao depublishRecordIdDao;
  private static DepublishRecordIdService depublishRecordIdService;

  @BeforeAll
  static void setUp() {

    authorizer = mock(Authorizer.class);
    orchestratorService = mock(OrchestratorService.class);
    depublishRecordIdDao = mock(DepublishRecordIdDao.class);

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
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    depublishRecordIdService.addRecordIdsToBeDepublished(metisUser, datasetId,"1002");

    verify(authorizer, times(1)).authorizeWriteExistingDatasetById(metisUser, datasetId);
    verify(depublishRecordIdService, times(1)).checkAndNormalizeRecordIds(any(),any());
    verify(depublishRecordIdDao, times(1)).createRecordIdsToBeDepublished(any(), any());
    verifyNoMoreInteractions(orchestratorService);


  }

  @Test
  void deletePendingRecordIdsTest() throws GenericMetisException {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    depublishRecordIdService.deletePendingRecordIds(metisUser, datasetId,"1002");

    verify(authorizer, times(1)).authorizeWriteExistingDatasetById(metisUser, datasetId);
    verify(depublishRecordIdService, times(2)).checkAndNormalizeRecordIds(any(),any());
    verify(depublishRecordIdDao, times(1)).deletePendingRecordIds(any(), any());
    verifyNoMoreInteractions(orchestratorService);

  }

  @Test
  void getDepublishRecordIdsTest() throws GenericMetisException {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    depublishRecordIdService.getDepublishRecordIds(metisUser, datasetId,1, DepublishRecordIdSortField.RECORD_ID, SortDirection.ASCENDING, "search");

//    doReturn(1).when(depublishRecordIdDao).getPageSize();

    verify(authorizer, times(1)).authorizeReadExistingDatasetById(metisUser, datasetId);
//    verify(depublishRecordIdService, times(1)).createResponseListWrapper(any(), any());
//    verify(depublishRecordIdDao, times(1)).getPageSize();
    verifyNoMoreInteractions(orchestratorService);

  }

  @Test
  void createAndAddInQueueDepublishWorkflowExecutionTest(){

  }

  @Test
  void canTriggerDepublicationTest(){

  }

}
