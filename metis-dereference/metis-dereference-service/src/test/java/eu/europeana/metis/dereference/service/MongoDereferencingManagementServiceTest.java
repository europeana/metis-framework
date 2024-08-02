package eu.europeana.metis.dereference.service;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import dev.morphia.Datastore;
import eu.europeana.metis.dereference.ProcessedEntity;
import eu.europeana.metis.dereference.Vocabulary;
import eu.europeana.metis.dereference.service.dao.ProcessedEntityDao;
import eu.europeana.metis.dereference.service.dao.VocabularyDao;
import eu.europeana.metis.dereference.vocimport.VocabularyCollectionImporter;
import eu.europeana.metis.dereference.vocimport.VocabularyCollectionImporterFactory;
import eu.europeana.metis.dereference.vocimport.exception.VocabularyImportException;
import eu.europeana.metis.mongo.embedded.EmbeddedLocalhostMongo;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link MongoDereferencingManagementService} class
 */
class MongoDereferencingManagementServiceTest {

  private MongoDereferencingManagementService service;
  private final EmbeddedLocalhostMongo embeddedLocalhostMongo = new EmbeddedLocalhostMongo();
  private Datastore vocabularyDaoDatastore;

  private VocabularyCollectionImporterFactory vocabularyCollectionImporterFactory;
  private final ProcessedEntityDao processedEntityDao = mock(ProcessedEntityDao.class);

  @BeforeEach
  void prepare() {
    embeddedLocalhostMongo.start();
    String mongoHost = embeddedLocalhostMongo.getMongoHost();
    int mongoPort = embeddedLocalhostMongo.getMongoPort();

    MongoClient mongoClient = MongoClients
        .create(String.format("mongodb://%s:%s", mongoHost, mongoPort));

    VocabularyDao vocDao = new VocabularyDao(mongoClient, "voctest") {
      {
        vocabularyDaoDatastore = this.getDatastore();
      }
    };
    vocabularyCollectionImporterFactory = mock(VocabularyCollectionImporterFactory.class);

    service = new MongoDereferencingManagementService(vocDao, processedEntityDao, vocabularyCollectionImporterFactory);
  }

  @Test
  void testGetAllVocabularies() {
    Vocabulary voc = new Vocabulary();
    voc.setIterations(0);
    voc.setName("testName");
    voc.setUris(Collections.singleton("http://www.test.uri/"));
    voc.setXslt("testXSLT");
    vocabularyDaoDatastore.save(voc);
    List<Vocabulary> retVoc = service.getAllVocabularies();
    assertEquals(1, retVoc.size());
  }


  @Test
  void purgeAllCache() {
    ProcessedEntity processedEntity = new ProcessedEntity();
    processedEntity.setResourceId("http://www.test.uri/");
    processedEntityDao.save(processedEntity);
    service.emptyCache();
    ProcessedEntity ret = processedEntityDao.getByResourceId("http://www.test.uri/");
    assertNull(ret);
  }

  @Test
  void purgeCacheWithEmptyXML() {
    ProcessedEntity processedEntity = new ProcessedEntity();
    processedEntity.setResourceId("http://www.test.uri/");
    processedEntity.setXml(null);
    processedEntityDao.save(processedEntity);
    service.purgeByNullOrEmptyXml();
    ProcessedEntity ret = processedEntityDao.getByResourceId("http://www.test.uri/");
    assertNull(ret);
  }


  @Test
  void purgeCacheByResourceId() {
    ProcessedEntity processedEntity = new ProcessedEntity();
    processedEntity.setResourceId("http://www.test.uri/");
    processedEntityDao.save(processedEntity);
    service.purgeByResourceId("http://www.test.uri/");
    ProcessedEntity ret = processedEntityDao.getByResourceId("http://www.test.uri/");
    assertNull(ret);
  }

  @Test
  void purgeCacheByVocabularyId() {
    ProcessedEntity processedEntity = new ProcessedEntity();
    processedEntity.setVocabularyId("vocabularyId");
    processedEntityDao.save(processedEntity);
    service.purgeByVocabularyId("vocabularyId");
    ProcessedEntity ret = processedEntityDao.getByVocabularyId("vocabularyId");
    assertNull(ret);
  }

  @Test
  void loadVocabularies_expectSuccess() throws VocabularyImportException, URISyntaxException, IOException {
    final URL vocabularyUrl = this.getClass().getClassLoader().getResource("vocabulary.yml");
    final URL vocabularyTestXslUrl = requireNonNull(getClass().getClassLoader().getResource("vocabulary/voctest.xsl"));
    final String expectedXslt = Files.readString(Paths.get(vocabularyTestXslUrl.toURI())).trim();

    final VocabularyCollectionImporter importer = new VocabularyCollectionImporterFactory().createImporter(vocabularyUrl);
    doReturn(importer).when(vocabularyCollectionImporterFactory).createImporter(any(URL.class));

    service.loadVocabularies(vocabularyUrl);
    Vocabulary vocabulary = vocabularyDaoDatastore.find(Vocabulary.class).first();

    assertNotNull(vocabulary);
    assertEquals("TestWikidata", vocabulary.getName());
    assertEquals(expectedXslt, vocabulary.getXslt());
    assertEquals("http://www.wikidata.org/entity/", vocabulary.getUris().stream().findFirst().orElse(null));
    verify(vocabularyCollectionImporterFactory, times(1)).createImporter(vocabularyUrl);
  }

  @Test
  void loadVocabularies_expectVocabularyImportError() {
    final URL resourceLocation = this.getClass().getClassLoader().getResource("vocabulary-fault.yml");
    final VocabularyCollectionImporter importer = new VocabularyCollectionImporterFactory().createImporter(resourceLocation);
    doReturn(importer).when(vocabularyCollectionImporterFactory).createImporter(any(URL.class));

    VocabularyImportException expectedException = assertThrows(VocabularyImportException.class,
        () -> service.loadVocabularies(resourceLocation));

    assertEquals("An error as occurred while loading the vocabularies", expectedException.getMessage());
    verify(vocabularyCollectionImporterFactory, times(1)).createImporter(resourceLocation);
  }

  @AfterEach
  void destroy() {
    embeddedLocalhostMongo.stop();
  }
}
