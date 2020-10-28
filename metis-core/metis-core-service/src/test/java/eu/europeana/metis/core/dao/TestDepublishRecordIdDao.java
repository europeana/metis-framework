package eu.europeana.metis.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import dev.morphia.DeleteOptions;
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
  private static MongoClient mongoClient;

  @BeforeEach
  void cleanUp() {

    new MorphiaDatastoreProviderImpl(mongoClient, "test").getDatastore()
        .find(DepublishRecordId.class).delete(new DeleteOptions().multi(true));
    reset(provider, depublishRecordIdDao);
  }

  @BeforeAll
  static void prepare() {
    embeddedLocalhostMongo = new EmbeddedLocalhostMongo();
    embeddedLocalhostMongo.start();
    String mongoHost = embeddedLocalhostMongo.getMongoHost();
    int mongoPort = embeddedLocalhostMongo.getMongoPort();
    mongoClient = MongoClients
        .create(String.format("mongodb://%s:%s", mongoHost, mongoPort));
    provider = spy(new MorphiaDatastoreProviderImpl(mongoClient, "test"));
    depublishRecordIdDao = spy(
        new DepublishRecordIdDao(provider, MAX_DEPUBLISH_RECORD_IDS_PER_DATASET, PAGE_SIZE));
  }

  @AfterAll
  static void destroy() {
    embeddedLocalhostMongo.stop();
  }

  @Test
  void createRecordIdsToBeDepublishedTest() throws BadContentException {
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    final Set<String> setTest = new HashSet<>();
    setTest.add("1001");

    depublishRecordIdDao.createRecordIdsToBeDepublished(datasetId, setTest);

    assertEquals(1, provider.getDatastore().find(DepublishRecordId.class).count());
  }

  @Test
  void deletePendingRecordIdsTest() throws BadContentException {
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    final Set<String> setTest = new HashSet<>();
    setTest.add("1002");
    final Query<DepublishRecordId> mockQuery = mock(Query.class);
    final Datastore mockDatastore = mock(Datastore.class);

    depublishRecordIdDao
        .addRecords(setTest, datasetId, DepublicationStatus.DEPUBLISHED, Instant.now());

    doReturn(mockDatastore).when(provider).getDatastore();
    doReturn(mockQuery).when(mockDatastore).find(DepublishRecordId.class);
    doReturn(1L).when(depublishRecordIdDao).deleteRecords(mockQuery);
    depublishRecordIdDao.deletePendingRecordIds(datasetId, setTest);

    verify(mockDatastore, times(1)).find(DepublishRecordId.class);
    verify(mockQuery, times(3)).filter(any());
    assertEquals(0, provider.getDatastore().find(DepublishRecordId.class).count());
  }


  @Test
  void countSuccessfullyDepublishedRecordIdsForDatasetTest() {
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    final Set<String> setTest = new HashSet<>();
    setTest.add("1003");

    depublishRecordIdDao
        .addRecords(setTest, datasetId, DepublicationStatus.DEPUBLISHED, Instant.now());
    reset(provider);
    long result = depublishRecordIdDao.countSuccessfullyDepublishedRecordIdsForDataset(datasetId);

    verify(provider, times(1)).getDatastore();
    assertEquals(1L, result);
  }

  @Test
  void getDepublishRecordIdsTest() {
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    final Set<String> setTest = new HashSet<>();
    setTest.add("1004");
    setTest.add("1005");

    depublishRecordIdDao
        .addRecords(setTest, datasetId, DepublicationStatus.PENDING_DEPUBLICATION, Instant.now());
    List<DepublishRecordIdView> find1004 = depublishRecordIdDao
        .getDepublishRecordIds(datasetId, 0, DepublishRecordIdSortField.DEPUBLICATION_STATE,
            SortDirection.ASCENDING,
            "1004");

    List<DepublishRecordIdView> findAll = depublishRecordIdDao
        .getDepublishRecordIds(datasetId, 0, DepublishRecordIdSortField.DEPUBLICATION_STATE,
            SortDirection.ASCENDING,
            null);

    assertEquals(1, find1004.size());
    assertEquals(2, findAll.size());
  }

  @Test
  void getAllDepublishRecordIdsWithStatusTest() throws BadContentException {
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    final Set<String> setTest = new HashSet<>();
    setTest.add("1006");

    depublishRecordIdDao
        .addRecords(setTest, datasetId, DepublicationStatus.DEPUBLISHED, Instant.now());
    Set<String> result = depublishRecordIdDao.getAllDepublishRecordIdsWithStatus(datasetId,
        DepublishRecordIdSortField.DEPUBLICATION_STATE, SortDirection.ASCENDING,
        DepublicationStatus.DEPUBLISHED, setTest);

    assertEquals(1, result.size());
  }

  @Test
  void markRecordIdsWithDepublicationStatusTest() {
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    final Set<String> setTest = new HashSet<>();
    setTest.add("1007");

    depublishRecordIdDao
        .addRecords(setTest, datasetId, DepublicationStatus.PENDING_DEPUBLICATION, Instant.now());
    reset(provider, depublishRecordIdDao);
    depublishRecordIdDao
        .markRecordIdsWithDepublicationStatus(datasetId, setTest, DepublicationStatus.DEPUBLISHED,
            Date.from(Instant.now()));

    verify(provider, times(3)).getDatastore();
    verify(depublishRecordIdDao, times(1)).addRecords(anySet(), anyString(), any(), any());
  }

  @Test
  void getPageSizeTest() {
    verifyNoInteractions(provider);
    assertEquals(3, depublishRecordIdDao.getPageSize());
  }

}
