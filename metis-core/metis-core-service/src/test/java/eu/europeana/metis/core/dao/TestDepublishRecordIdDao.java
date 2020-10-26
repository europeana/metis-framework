package eu.europeana.metis.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import dev.morphia.Datastore;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.morphia.query.Sort;
import eu.europeana.metis.core.dataset.DepublishRecordId;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProviderImpl;
import eu.europeana.metis.core.util.DepublishRecordIdSortField;
import eu.europeana.metis.core.util.SortDirection;
import eu.europeana.metis.core.utils.TestObjectFactory;
import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.mongo.EmbeddedLocalhostMongo;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestDepublishRecordIdDao {

  private static MorphiaDatastoreProvider provider;
  private static EmbeddedLocalhostMongo embeddedLocalhostMongo;
  private static final long MAX_DEPUBLISH_RECORD_IDS_PER_DATASET = 5L;
  private static DepublishRecordIdDao depublishRecordIdDao;

  @BeforeAll
  static void prepare(){
    embeddedLocalhostMongo = new EmbeddedLocalhostMongo();
    embeddedLocalhostMongo.start();
    String mongoHost = embeddedLocalhostMongo.getMongoHost();
    int mongoPort = embeddedLocalhostMongo.getMongoPort();
    MongoClient mongoClient = MongoClients
        .create(String.format("mongodb://%s:%s", mongoHost, mongoPort));
    provider = spy(new MorphiaDatastoreProviderImpl(mongoClient, "test"));
    depublishRecordIdDao = spy(new DepublishRecordIdDao(provider, MAX_DEPUBLISH_RECORD_IDS_PER_DATASET));
  }

  @AfterAll
  static void destroy() {
    embeddedLocalhostMongo.stop();
  }

//  @Test
//  void createRecordIdsToBeDepublishedTest() throws BadContentException {
//    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
//    final Set<String> setTest = new HashSet<>();
//    setTest.add("1002");
//    final Set<String> setNonExistingTest = new HashSet<>();
//    setNonExistingTest.add("1005");
//
//    doReturn(setNonExistingTest).when(depublishRecordIdDao).getNonExistingRecordIds(datasetId, setTest);
//    depublishRecordIdDao.createRecordIdsToBeDepublished(datasetId, setTest);
//
//    verify(depublishRecordIdDao, times(1)).getNonExistingRecordIds(anyString(), anySet());
//    verify(depublishRecordIdDao, times(1)).countDepublishRecordIdsForDataset(datasetId);
//    verify(depublishRecordIdDao, times(1)).addRecords(anySet(), anyString(), any(), any());
//  }

  @Test
  void deletePendingRecordIdsTest() throws BadContentException {
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    final Set<String> setTest = new HashSet<>();
    setTest.add("1002");
    final Query<DepublishRecordId> mockQuery = mock(Query.class);
    final Datastore mockDatastore = mock(Datastore.class);

    doReturn(mockDatastore).when(provider).getDatastore();
    doReturn(mockQuery).when(mockDatastore).find(DepublishRecordId.class);
    doReturn(1L).when(depublishRecordIdDao).deleteRecords(mockQuery);
    depublishRecordIdDao.deletePendingRecordIds(datasetId, setTest);

    verify(provider, times(3)).getDatastore();
    verify(mockDatastore, times(1)).find(DepublishRecordId.class);
    verify(mockQuery, times(3)).filter(any());
    verify(depublishRecordIdDao, times(1)).deleteRecords(mockQuery);

  }

//  @Test
//  void countDepublishRecordIdsForDatasetTest(){
//    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
//
//    depublishRecordIdDao.countDepublishRecordIdsForDataset(datasetId);
//
//    verify(depublishRecordIdDao, times(1)).countDepublishedRecordsIds(datasetId);
//    verify(provider, times(1)).getDatastore();
//
//  }

  @Test
  void countSuccessfullyDepublishedRecordIdsForDatasetTest(){
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);

    depublishRecordIdDao.countSuccessfullyDepublishedRecordIdsForDataset(datasetId);

    verify(depublishRecordIdDao, times(1)).countSuccessfullyDepublishedRecords(datasetId);
    verify(provider, times(2)).getDatastore();
  }

  @Test
  void getDepublishRecordIdsTest(){
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);

    depublishRecordIdDao.getDepublishRecordIds(datasetId, 1, DepublishRecordIdSortField.RECORD_ID, SortDirection.DESCENDING, "query");

    verify(depublishRecordIdDao, times(1)).prepareQueryForDepublishRecordIds(anyString(), any(), anyString());
    verify(depublishRecordIdDao, times(1)).createFindOptionsObject();

  }

  @Test
  void getAllDepublishRecordIdsWithStatusTest(){

  }

  @Test
  void markRecordIdsWithDepublicationStatusTest(){

  }

  @Test
  void getPageSizeTest(){

  }

}
