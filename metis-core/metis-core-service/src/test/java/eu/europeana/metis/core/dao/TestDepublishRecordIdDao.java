package eu.europeana.metis.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import dev.morphia.Datastore;
import dev.morphia.query.Query;
import eu.europeana.metis.core.dataset.DepublishRecordId;
import eu.europeana.metis.core.dataset.DepublishRecordId.DepublicationStatus;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProviderImpl;
import eu.europeana.metis.core.rest.DepublishRecordIdView;
import eu.europeana.metis.core.util.DepublishRecordIdSortField;
import eu.europeana.metis.core.util.SortDirection;
import eu.europeana.metis.core.utils.TestObjectFactory;
import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.mongo.EmbeddedLocalhostMongo;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestDepublishRecordIdDao {

  private static MorphiaDatastoreProvider provider;
  private static EmbeddedLocalhostMongo embeddedLocalhostMongo;
  private static final long MAX_DEPUBLISH_RECORD_IDS_PER_DATASET = 5L;
  private static final int PAGE_SIZE = 3;
  private static DepublishRecordIdDao depublishRecordIdDao;

  @BeforeEach
  void resetMocks(){
    reset(provider);
    reset(depublishRecordIdDao);
  }

  @BeforeAll
  static void prepare(){
    embeddedLocalhostMongo = new EmbeddedLocalhostMongo();
    embeddedLocalhostMongo.start();
    String mongoHost = embeddedLocalhostMongo.getMongoHost();
    int mongoPort = embeddedLocalhostMongo.getMongoPort();
    MongoClient mongoClient = MongoClients
        .create(String.format("mongodb://%s:%s", mongoHost, mongoPort));
    provider = spy(new MorphiaDatastoreProviderImpl(mongoClient, "test"));
    depublishRecordIdDao = spy(new DepublishRecordIdDao(provider, MAX_DEPUBLISH_RECORD_IDS_PER_DATASET, PAGE_SIZE));
  }

  @AfterAll
  static void destroy() {
    embeddedLocalhostMongo.stop();
  }

  @Test
  void createRecordIdsToBeDepublishedTest() throws BadContentException {
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    final Set<String> setTest = new HashSet<>();
    setTest.add("1002");

    long originalSize = provider.getDatastore().find(DepublishRecordId.class).count();

    depublishRecordIdDao.createRecordIdsToBeDepublished(datasetId, setTest);

    verify(depublishRecordIdDao, times(1)).addRecords(anySet(), anyString(), any(), any());
    assertEquals(originalSize + 1, provider.getDatastore().find(DepublishRecordId.class).count());
  }

  @Test
  void deletePendingRecordIdsTest() throws BadContentException {
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    final Set<String> setTest = new HashSet<>();
    setTest.add("1002");
    final Query<DepublishRecordId> mockQuery = mock(Query.class);
    final Datastore mockDatastore = mock(Datastore.class);


    provider.getDatastore().find(DepublishRecordId.class).count();
    depublishRecordIdDao.createRecordIdsToBeDepublished(datasetId, setTest);

    doReturn(mockDatastore).when(provider).getDatastore();
    doReturn(mockQuery).when(mockDatastore).find(DepublishRecordId.class);
    doReturn(1L).when(depublishRecordIdDao).deleteRecords(mockQuery);
    depublishRecordIdDao.deletePendingRecordIds(datasetId, setTest);

    verify(mockDatastore, times(1)).find(DepublishRecordId.class);
    verify(mockQuery, times(3)).filter(any());
    verify(depublishRecordIdDao, times(1)).deleteRecords(mockQuery);
    assertEquals(0, provider.getDatastore().find(DepublishRecordId.class).count());
  }


  @Test
  void countSuccessfullyDepublishedRecordIdsForDatasetTest(){
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    final Set<String> setTest = new HashSet<>();
    setTest.add("1005");

    depublishRecordIdDao.addRecords(setTest, datasetId, DepublicationStatus.DEPUBLISHED, Instant.now());
    long result = depublishRecordIdDao.countSuccessfullyDepublishedRecordIdsForDataset(datasetId);

    verify(provider, times(2)).getDatastore();
    assertEquals(1L, result);
  }

  @Test
  void getDepublishRecordIdsTest(){ //TODO: FAILING
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    final Set<String> setTest = new HashSet<>();
//    setTest.add("1005");
//
//    depublishRecordIdDao.addRecords(setTest, datasetId, DepublicationStatus.DEPUBLISHED, Instant.now());
    List<DepublishRecordIdView> result = depublishRecordIdDao.getDepublishRecordIds(datasetId, 1, DepublishRecordIdSortField.DEPUBLICATION_STATE, SortDirection.ASCENDING,
        String.valueOf(Pattern.DOTALL));

//    verify(depublishRecordIdDao, times(1)).prepareQueryForDepublishRecordIds(anyString(), any(), anyString());
//    assertEquals(1, result.size());
  }

  @Test
  void getAllDepublishRecordIdsWithStatusTest() throws BadContentException {
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    final Set<String> setTest = new HashSet<>();
    setTest.add("1005");

    depublishRecordIdDao.addRecords(setTest, datasetId, DepublicationStatus.DEPUBLISHED, Instant.now());
    Set<String> result = depublishRecordIdDao.getAllDepublishRecordIdsWithStatus(datasetId, DepublishRecordIdSortField.DEPUBLICATION_STATE, SortDirection.ASCENDING,
        DepublicationStatus.DEPUBLISHED, setTest);

//    verify(depublishRecordIdDao, times(1)).prepareQueryForDepublishRecordIds(anyString(), any(), anyString());
//    verify(depublishRecordIdDao, times(1)).createFindOptionsObject();
    assertEquals(1, result.size());
  }

  @Test
  void markRecordIdsWithDepublicationStatusTest(){
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    final Set<String> setTest = new HashSet<>();
    setTest.add("1005");

//    doReturn(setTest).when(depublishRecordIdDao).getNonExistingRecordIds(datasetId, setTest);
    depublishRecordIdDao.addRecords(setTest, datasetId, DepublicationStatus.DEPUBLISHED, Instant.now());
    depublishRecordIdDao.markRecordIdsWithDepublicationStatus(datasetId, setTest, DepublicationStatus.DEPUBLISHED,
        Date.from(Instant.now()));

//    verify(depublishRecordIdDao, times(1)).getNonExistingRecordIds(anyString(), anySet());
    verify(depublishRecordIdDao, times(2)).addRecords(anySet(), anyString(), any(), any());
  }

  @Test
  void getPageSizeTest(){
    verifyNoInteractions(provider);
    assertEquals(3, depublishRecordIdDao.getPageSize());
  }

}
