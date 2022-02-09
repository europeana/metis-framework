package eu.europeana.metis.mongo.dao;


import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import dev.morphia.Datastore;
import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.definitions.edm.model.metainfo.ImageOrientation;
import eu.europeana.corelib.definitions.edm.model.metainfo.WebResourceMetaInfo;
import eu.europeana.corelib.edm.exceptions.MongoDBException;
import eu.europeana.corelib.edm.model.metainfo.AudioMetaInfoImpl;
import eu.europeana.corelib.edm.model.metainfo.ImageMetaInfoImpl;
import eu.europeana.corelib.edm.model.metainfo.TextMetaInfoImpl;
import eu.europeana.corelib.edm.model.metainfo.VideoMetaInfoImpl;
import eu.europeana.corelib.edm.model.metainfo.WebResourceMetaInfoImpl;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.corelib.solr.entity.AggregationImpl;
import eu.europeana.corelib.solr.entity.BasicProxyImpl;
import eu.europeana.corelib.solr.entity.ConceptImpl;
import eu.europeana.corelib.solr.entity.ConceptSchemeImpl;
import eu.europeana.corelib.solr.entity.EuropeanaAggregationImpl;
import eu.europeana.corelib.solr.entity.EventImpl;
import eu.europeana.corelib.solr.entity.OrganizationImpl;
import eu.europeana.corelib.solr.entity.PhysicalThingImpl;
import eu.europeana.corelib.solr.entity.PlaceImpl;
import eu.europeana.corelib.solr.entity.ProvidedCHOImpl;
import eu.europeana.corelib.solr.entity.ProxyImpl;
import eu.europeana.corelib.solr.entity.TimespanImpl;
import eu.europeana.corelib.solr.entity.WebResourceImpl;
import eu.europeana.corelib.web.exception.EuropeanaException;
import eu.europeana.corelib.web.exception.ProblemType;
import eu.europeana.metis.mongo.embedded.EmbeddedLocalhostMongo;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link RecordDao}
 *
 * @author Jorge Ortiz
 * @since 31-01-2022
 */
class RecordDaoTest {

  private final static String DATABASE_NAME = "dbTest";

  private static RecordDao recordDao;

  private static EmbeddedLocalhostMongo embeddedLocalhostMongo;

  @BeforeAll
  static void setup() {
    embeddedLocalhostMongo = new EmbeddedLocalhostMongo();
    embeddedLocalhostMongo.start();
    final String mongoHost = embeddedLocalhostMongo.getMongoHost();
    final int mongoPort = embeddedLocalhostMongo.getMongoPort();
    final MongoClient mongoClient = MongoClients.create(String.format("mongodb://%s:%s", mongoHost, mongoPort));
    recordDao = new RecordDao(mongoClient, DATABASE_NAME);
  }

  @AfterAll
  static void tearDown() {
    embeddedLocalhostMongo.stop();
  }

  @Test
  void getDatastore() {
    final Datastore datastore = recordDao.getDatastore();

    assertNotNull(datastore);
    assertMappedClasses(datastore);
  }

  @Test
  void getFullBeanHappyPath() throws EuropeanaException {
    recordDao.getDatastore().save(getFullBean());

    final FullBean actualBean = recordDao.getFullBean("fullBeanAbout");

    assertNotNull(actualBean);
    assertFullBean(getFullBean(), actualBean);
  }

  @Test
  void getFullBeanThrowsException() throws EuropeanaException {
    final RecordDao spyRecordDao = spy(recordDao);

    doThrow(new MongoDBException(ProblemType.MONGO_UNREACHABLE)).when(spyRecordDao).getFullBean("");

    assertThrows(EuropeanaException.class, () -> spyRecordDao.getFullBean(""));
  }

  @Test
  void retrieveWebMetaInfosFound() {
    recordDao.getDatastore().save(getWebResourceMetaInfo());

    final Map<String, WebResourceMetaInfoImpl> webResourceMetaInfoMap = recordDao.retrieveWebMetaInfos(
        List.of("51eec080f582833f264dad08"));

    assertEquals(1, webResourceMetaInfoMap.size());
  }

  @Test
  void retrieveWebMetaInfosNotFound() {
    recordDao.getDatastore().save(getWebResourceMetaInfo());

    final Map<String, WebResourceMetaInfoImpl> webResourceMetaInfoMap = recordDao.retrieveWebMetaInfos(
        List.of("51eec080f582833f264dad05"));

    assertEquals(0, webResourceMetaInfoMap.size());
  }

  @Test
  void testToString() {
    final String expectedString = "{ datastore=" + DATABASE_NAME + " }";

    assertEquals(expectedString, recordDao.toString());
  }

  @Test
  void searchByAbout() {
    recordDao.getDatastore().save(getFullBean());

    final FullBean actualBean = recordDao.searchByAbout(FullBeanImpl.class, "fullBeanAbout");

    assertNotNull(actualBean);
    assertFullBean(getFullBean(), actualBean);
  }

  @Test
  void createIndexes() {
    final String mongoHost = embeddedLocalhostMongo.getMongoHost();
    final int mongoPort = embeddedLocalhostMongo.getMongoPort();
    final MongoClient mongoClient = MongoClients.create(String.format("mongodb://%s:%s", mongoHost, mongoPort));

    recordDao = new RecordDao(mongoClient, DATABASE_NAME, true);

    assertNotNull(recordDao);
  }

  private static FullBean getFullBean() {
    final FullBeanImpl fullBean = new FullBeanImpl();
    fullBean.setEuropeanaId(new ObjectId("81eec080f582833f364dad08"));
    fullBean.setLanguage(new String[]{"Nederlands", "Vlams", "Duits", "Frans", "Spaans", "Italians", "Duits", "Portuguese"});
    fullBean.setCountry(
        new String[]{"Nederland", "België", "Duitsland", "Frankrijk", "Spanje", "Italië", "Switzerland", "Portugal"});
    fullBean.setYear(new String[]{"2022", "2023", "2024", "2025", "2026", "2027", "2028", "2029"});
    fullBean.setAbout("fullBeanAbout");
    fullBean.setType("TEXT");
    fullBean.setTimestampCreated(Date.from(Instant.parse("2022-01-26T12:35:12.00Z")));
    fullBean.setTimestampUpdated(Date.from(Instant.parse("2022-01-26T12:35:12.00Z")));
    fullBean.setProvider(new String[]{"Pro1", "Prov2", "Prov3", "Prov4", "Prov5", "Prov6", "Prov7", "Prov8"});
    fullBean.setUserTags(new String[]{"UsrTag1", "UsrTag2", "UsrTag3", "UsrTag4", "UsrTag5", "UsrTag6", "UsrTag7", "UsrTag8"});
    fullBean.setEuropeanaCollectionName(new String[]{"Collection1", "Collection2", "Collection3", "Collection4", "Collection5"});
    fullBean.setEuropeanaCompleteness(100);

    return fullBean;
  }

  private static void assertFullBean(final FullBean expected, final FullBean actual) {
    assertEquals(expected.getId(), actual.getId());
    assertEquals(expected.getAbout(), actual.getAbout());
    assertEquals(expected.getType(), actual.getType());
    assertEquals(expected.getTimestampCreated(), actual.getTimestampCreated());
    assertEquals(expected.getTimestampUpdated(), actual.getTimestampUpdated());
    assertEquals(expected.getEuropeanaCompleteness(), actual.getEuropeanaCompleteness());
    assertArrayEquals(expected.getProvider(), actual.getProvider());
    assertArrayEquals(expected.getUserTags(), actual.getUserTags());
    assertArrayEquals(expected.getYear(), actual.getYear());
    assertArrayEquals(expected.getLanguage(), actual.getLanguage());
    assertArrayEquals(expected.getCountry(), actual.getCountry());
    assertArrayEquals(expected.getEuropeanaCollectionName(), actual.getEuropeanaCollectionName());
  }

  private static WebResourceMetaInfo getWebResourceMetaInfo() {
    final AudioMetaInfoImpl audioMetaInfo = new AudioMetaInfoImpl(12, 24, 80L, "audio/wav", "wave", 80L, 2, 24, "raw");
    final ImageMetaInfoImpl imageMetaInfo = new ImageMetaInfoImpl(256, 256, "image/jpeg", "jpeg", "RGB", 80L,
        new String[]{"1,1,1"}, ImageOrientation.LANDSCAPE);
    final VideoMetaInfoImpl videoMetaInfo = new VideoMetaInfoImpl(256, 256, 3600L, "video/mp4", 50.00, 8194L, "mp4", "1080p", 8);
    final TextMetaInfoImpl textMetaInfo = new TextMetaInfoImpl("docx", 2048L, 600, true, "book");
    return new WebResourceMetaInfoImpl("51eec080f582833f264dad08", imageMetaInfo, audioMetaInfo, videoMetaInfo, textMetaInfo);
  }

  private static void assertMappedClasses(Datastore datastore) {
    assertTrue(datastore.getMapper().isMapped(FullBeanImpl.class));
    assertTrue(datastore.getMapper().isMapped(ProvidedCHOImpl.class));
    assertTrue(datastore.getMapper().isMapped(AgentImpl.class));
    assertTrue(datastore.getMapper().isMapped(AggregationImpl.class));
    assertTrue(datastore.getMapper().isMapped(OrganizationImpl.class));
    assertTrue(datastore.getMapper().isMapped(ConceptImpl.class));
    assertTrue(datastore.getMapper().isMapped(ProxyImpl.class));
    assertTrue(datastore.getMapper().isMapped(PlaceImpl.class));
    assertTrue(datastore.getMapper().isMapped(TimespanImpl.class));
    assertTrue(datastore.getMapper().isMapped(WebResourceImpl.class));
    assertTrue(datastore.getMapper().isMapped(EuropeanaAggregationImpl.class));
    assertTrue(datastore.getMapper().isMapped(EventImpl.class));
    assertTrue(datastore.getMapper().isMapped(PhysicalThingImpl.class));
    assertTrue(datastore.getMapper().isMapped(ConceptSchemeImpl.class));
    assertTrue(datastore.getMapper().isMapped(BasicProxyImpl.class));
    assertTrue(datastore.getMapper().isMapped(WebResourceMetaInfoImpl.class));
  }
}