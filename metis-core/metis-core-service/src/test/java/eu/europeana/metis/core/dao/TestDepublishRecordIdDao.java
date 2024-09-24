package eu.europeana.metis.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verifyNoInteractions;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import dev.morphia.DeleteOptions;
import eu.europeana.metis.core.dataset.DepublishRecordId;
import eu.europeana.metis.core.dataset.DepublishRecordId.DepublicationStatus;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProviderImpl;
import eu.europeana.metis.core.rest.DepublishRecordIdView;
import eu.europeana.metis.core.util.DepublishRecordIdSortField;
import eu.europeana.metis.core.util.SortDirection;
import eu.europeana.metis.core.utils.TestObjectFactory;
import eu.europeana.metis.utils.DepublicationReason;
import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.mongo.embedded.EmbeddedLocalhostMongo;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestDepublishRecordIdDao {

  private static MorphiaDatastoreProvider provider;
  private static EmbeddedLocalhostMongo embeddedLocalhostMongo;
  private static final long MAX_DEPUBLISH_RECORD_IDS_PER_DATASET = 5L;
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
    mongoClient = MongoClients.create(String.format("mongodb://%s:%s", mongoHost, mongoPort));
    provider = spy(new MorphiaDatastoreProviderImpl(mongoClient, "test"));
    depublishRecordIdDao = spy(
        new DepublishRecordIdDao(provider, MAX_DEPUBLISH_RECORD_IDS_PER_DATASET));
  }

  @AfterAll
  static void destroy() {
    embeddedLocalhostMongo.stop();
  }

  @Test
  void createRecordIdsToBeDepublishedHappyScenarioTest() throws BadContentException {
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    final Set<String> setTest = Set.of("1001");

    depublishRecordIdDao.createRecordIdsToBeDepublished(datasetId, setTest);

    assertEquals(1, provider.getDatastore().find(DepublishRecordId.class).count());
    assertEquals("1001",
        provider.getDatastore().find(DepublishRecordId.class).first().getRecordId());
  }

  @Test
  void createRecordIdsToBeDepublishedBigNumberOfCandidateRecordIdsTest() {
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    final Set<String> setTest = Set.of("1008", "1009", "1010", "1011", "1012", "1013");

    Throwable exception = assertThrows(BadContentException.class,
        () -> depublishRecordIdDao.createRecordIdsToBeDepublished(datasetId, setTest));

    assertEquals(
        "Can't add these records: this would violate the maximum number of records per dataset.",
        exception.getMessage());
  }

  @Test
  void createRecordIdsToBeDepublishedBigNumberOfDepublishedRecord() {
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    final Set<String> setTest = Set.of("1014");

    doReturn(6L).when(depublishRecordIdDao).countDepublishRecordIdsForDataset(datasetId);

    Throwable exception = assertThrows(BadContentException.class,
        () -> depublishRecordIdDao.createRecordIdsToBeDepublished(datasetId, setTest));

    assertEquals(
        "Can't add these records: this would violate the maximum number of records per dataset.",
        exception.getMessage());
  }

  @Test
  void deletePendingRecordIdsTest() throws BadContentException {
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    final Set<String> setTest = Set.of("1");
    final Set<String> biggerThanAllowedSet = IntStream
        .range(0, (int) MAX_DEPUBLISH_RECORD_IDS_PER_DATASET + 1).mapToObj(Integer::toString)
        .collect(Collectors.toSet());

    //Big list should fail
    assertThrows(BadContentException.class,
        () -> depublishRecordIdDao.deletePendingRecordIds(datasetId, biggerThanAllowedSet));

    depublishRecordIdDao
        .addRecords(setTest, datasetId, DepublicationStatus.PENDING_DEPUBLICATION, Instant.now(), DepublicationReason.GENERIC);
    assertEquals(1, provider.getDatastore().find(DepublishRecordId.class).count());

    depublishRecordIdDao.deletePendingRecordIds(datasetId, setTest);
    assertEquals(0, provider.getDatastore().find(DepublishRecordId.class).count());
  }


  @Test
  void countSuccessfullyDepublishedRecordIdsForDatasetTest() {
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    final Set<String> setTest = Set.of("1003");

    depublishRecordIdDao
        .addRecords(setTest, datasetId, DepublicationStatus.DEPUBLISHED, Instant.now(), DepublicationReason.GENERIC);
    long result = depublishRecordIdDao.countSuccessfullyDepublishedRecordIdsForDataset(datasetId);
    assertEquals(1L, result);
  }

  @Test
  void getDepublishRecordIdsTest() {
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    final Set<String> setTest = Set.of("1004", "1005");

    depublishRecordIdDao
        .addRecords(setTest, datasetId, DepublicationStatus.PENDING_DEPUBLICATION, Instant.now(), DepublicationReason.GENERIC);
    List<DepublishRecordIdView> find1004 = depublishRecordIdDao
        .getDepublishRecordIds(datasetId, 0, DepublishRecordIdSortField.DEPUBLICATION_STATE,
            SortDirection.ASCENDING, "1004");

    List<DepublishRecordIdView> findAll = depublishRecordIdDao
        .getDepublishRecordIds(datasetId, 0, DepublishRecordIdSortField.DEPUBLICATION_STATE,
            SortDirection.ASCENDING, null);

    assertEquals(1, find1004.size());
    assertEquals("1004", find1004.getFirst().getRecordId());
    assertEquals(2, findAll.size());
  }

  @Test
  void getAllDepublishRecordIdsWithStatusTest() throws BadContentException {
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    final Set<String> setTest = Set.of("1");
    final Set<String> biggerThanAllowedSet = IntStream
        .range(0, (int) MAX_DEPUBLISH_RECORD_IDS_PER_DATASET + 1).mapToObj(Integer::toString)
        .collect(Collectors.toSet());

    //Big list should fail
    assertThrows(BadContentException.class,
        () -> depublishRecordIdDao.getAllDepublishRecordIdsWithStatus(datasetId,
            DepublishRecordIdSortField.DEPUBLICATION_STATE, SortDirection.ASCENDING,
            DepublicationStatus.DEPUBLISHED, biggerThanAllowedSet));

    depublishRecordIdDao
        .addRecords(setTest, datasetId, DepublicationStatus.DEPUBLISHED, Instant.now(), DepublicationReason.GENERIC);
    Set<String> result = depublishRecordIdDao.getAllDepublishRecordIdsWithStatus(datasetId,
        DepublishRecordIdSortField.DEPUBLICATION_STATE, SortDirection.ASCENDING,
        DepublicationStatus.DEPUBLISHED, setTest);

    assertEquals(1, result.size());

    //Check also when requesting without recordIds set parameter
    result = depublishRecordIdDao.getAllDepublishRecordIdsWithStatus(datasetId,
        DepublishRecordIdSortField.DEPUBLICATION_STATE, SortDirection.ASCENDING,
        DepublicationStatus.DEPUBLISHED);

    assertEquals(1, result.size());
  }

  @Test
  void markRecordIdsWithDepublicationStatus_wrong_parametersTest() {
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    final Set<String> recordIdsSet = Set.of("1", "2");
    Date date = Date.from(Instant.now());

    //Null depublication status
    assertThrows(IllegalArgumentException.class, () -> depublishRecordIdDao
        .markRecordIdsWithDepublicationStatus(datasetId, recordIdsSet, null, date, DepublicationReason.GENERIC));

    //Blank dataset id
    assertThrows(IllegalArgumentException.class, () -> depublishRecordIdDao
        .markRecordIdsWithDepublicationStatus(null, recordIdsSet,
            DepublicationStatus.PENDING_DEPUBLICATION, date, DepublicationReason.GENERIC));

    //Depublished status but date null
    assertThrows(IllegalArgumentException.class, () -> depublishRecordIdDao
        .markRecordIdsWithDepublicationStatus(datasetId, recordIdsSet,
            DepublicationStatus.DEPUBLISHED, null, null));
  }

  @Test
  void markRecordIdsWithDepublicationStatus_all_recordIds_set_depublished_and_then_pendingTest()
      throws BadContentException {
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    final Set<String> recordIdsSet = Set.of("1", "2");
    Date date = Date.from(Instant.now());

    //Create recordIds
    depublishRecordIdDao.createRecordIdsToBeDepublished(datasetId, recordIdsSet);
    //Check stored recordIds
    List<DepublishRecordIdView> findAll = depublishRecordIdDao
        .getDepublishRecordIds(datasetId, 0, DepublishRecordIdSortField.DEPUBLICATION_STATE,
            SortDirection.ASCENDING, null);
    assertTrue(findAll.stream().allMatch(depublishRecordIdView ->
        DepublishRecordIdView.DepublicationStatus.PENDING == depublishRecordIdView
            .getDepublicationStatus() && null == depublishRecordIdView.getDepublicationDate()));
    //Set to DEPUBLISHED
    depublishRecordIdDao
        .markRecordIdsWithDepublicationStatus(datasetId, null, DepublicationStatus.DEPUBLISHED,
            date, DepublicationReason.GENERIC);
    //Check stored recordIds
    findAll = depublishRecordIdDao
        .getDepublishRecordIds(datasetId, 0, DepublishRecordIdSortField.DEPUBLICATION_STATE,
            SortDirection.ASCENDING, null);
    assertTrue(findAll.stream().allMatch(depublishRecordIdView ->
        DepublishRecordIdView.DepublicationStatus.DEPUBLISHED == depublishRecordIdView
            .getDepublicationStatus() && date
            .equals(Date.from(depublishRecordIdView.getDepublicationDate()))));
    //Set to PENDING_DEPUBLICATION
    depublishRecordIdDao.markRecordIdsWithDepublicationStatus(datasetId, null,
        DepublicationStatus.PENDING_DEPUBLICATION, date, DepublicationReason.GENERIC);
    //Check stored recordIds
    findAll = depublishRecordIdDao
        .getDepublishRecordIds(datasetId, 0, DepublishRecordIdSortField.DEPUBLICATION_STATE,
            SortDirection.ASCENDING, null);
    assertTrue(findAll.stream().allMatch(depublishRecordIdView ->
        DepublishRecordIdView.DepublicationStatus.PENDING == depublishRecordIdView
            .getDepublicationStatus() && null == depublishRecordIdView.getDepublicationDate()));
  }

  @Test
  void markRecordIdsWithDepublicationStatus_specified_recordIds_set_depublished_and_then_pendingTest()
      throws BadContentException {
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    final Set<String> recordIdsToCreate = Set.of("1", "2", "3");
    final Set<String> recordIdsToUpdate = Set.of("1", "2");
    Date date = Date.from(Instant.now());

    //Create recordIds
    depublishRecordIdDao.createRecordIdsToBeDepublished(datasetId, recordIdsToCreate);
    //Check stored recordIds
    List<DepublishRecordIdView> findAll = depublishRecordIdDao
        .getDepublishRecordIds(datasetId, 0, DepublishRecordIdSortField.DEPUBLICATION_STATE,
            SortDirection.ASCENDING, null);
    assertTrue(findAll.stream().allMatch(depublishRecordIdView ->
        DepublishRecordIdView.DepublicationStatus.PENDING == depublishRecordIdView
            .getDepublicationStatus() && null == depublishRecordIdView.getDepublicationDate()));
    //Set to DEPUBLISHED
    depublishRecordIdDao.markRecordIdsWithDepublicationStatus(datasetId, recordIdsToUpdate,
        DepublicationStatus.DEPUBLISHED, date, DepublicationReason.GENERIC);
    //Check stored recordIds
    findAll = depublishRecordIdDao
        .getDepublishRecordIds(datasetId, 0, DepublishRecordIdSortField.DEPUBLICATION_STATE,
            SortDirection.ASCENDING, null);
    assertEquals(2, findAll.stream().filter(depublishRecordIdView ->
        DepublishRecordIdView.DepublicationStatus.DEPUBLISHED == depublishRecordIdView
            .getDepublicationStatus() && date
            .equals(Date.from(depublishRecordIdView.getDepublicationDate()))).count());
    //Set to PENDING_DEPUBLICATION
    depublishRecordIdDao.markRecordIdsWithDepublicationStatus(datasetId, recordIdsToUpdate,
        DepublicationStatus.PENDING_DEPUBLICATION, date, DepublicationReason.GENERIC);
    //Check stored recordIds
    findAll = depublishRecordIdDao
        .getDepublishRecordIds(datasetId, 0, DepublishRecordIdSortField.DEPUBLICATION_STATE,
            SortDirection.ASCENDING, null);
    assertEquals(3, findAll.stream().filter(depublishRecordIdView ->
        DepublishRecordIdView.DepublicationStatus.PENDING == depublishRecordIdView
            .getDepublicationStatus() && null == depublishRecordIdView.getDepublicationDate())
        .count());
  }

  @Test
  void markRecordIdsWithDepublicationStatus_depublish_non_already_existing_recordIdsTest()
      throws BadContentException {
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    final Set<String> recordIdsToCreate = Set.of("1", "2", "3");
    final Set<String> recordIdsToUpdate = Set.of("4", "5");
    Date date = Date.from(Instant.now());

    //Create recordIds
    depublishRecordIdDao.createRecordIdsToBeDepublished(datasetId, recordIdsToCreate);
    //Set to DEPUBLISHED
    depublishRecordIdDao.markRecordIdsWithDepublicationStatus(datasetId, recordIdsToUpdate,
        DepublicationStatus.DEPUBLISHED, date, DepublicationReason.GENERIC);

    //Check stored recordIds
    List<DepublishRecordIdView> findAll = depublishRecordIdDao
        .getDepublishRecordIds(datasetId, 0, DepublishRecordIdSortField.DEPUBLICATION_STATE,
            SortDirection.ASCENDING, null);
    final long pendingCount = findAll.stream().filter(depublishRecordIdView ->
        DepublishRecordIdView.DepublicationStatus.PENDING == depublishRecordIdView
            .getDepublicationStatus() && null == depublishRecordIdView.getDepublicationDate())
        .count();
    final long depublishedCount = findAll.stream().filter(depublishRecordIdView ->
        DepublishRecordIdView.DepublicationStatus.DEPUBLISHED == depublishRecordIdView
            .getDepublicationStatus() && date
            .equals(Date.from(depublishRecordIdView.getDepublicationDate()))).count();
    assertEquals(3, pendingCount);
    assertEquals(2, depublishedCount);
    assertEquals(recordIdsToCreate.size() + recordIdsToUpdate.size(),
        pendingCount + depublishedCount);
  }

  @Test
  void getPageSizeTest() {
    final DepublishRecordIdDao depublishRecordIdDaoWithPageSize = new DepublishRecordIdDao(provider,
        MAX_DEPUBLISH_RECORD_IDS_PER_DATASET, 3);
    verifyNoInteractions(provider);
    assertEquals(3, depublishRecordIdDaoWithPageSize.getPageSize());
  }
}
