package eu.europeana.metis.dereference.service.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import dev.morphia.DeleteOptions;
import eu.europeana.metis.dereference.Vocabulary;
import eu.europeana.metis.mongo.embedded.EmbeddedLocalhostMongo;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * Unit tests for {@link VocabularyDao} class
 */
class VocabularyDaoTest {

  private static EmbeddedLocalhostMongo embeddedLocalhostMongo;

  private static VocabularyDao vocabularyDao;

  @BeforeAll
  static void prepare() {
    embeddedLocalhostMongo = new EmbeddedLocalhostMongo();

    embeddedLocalhostMongo.start();
    final String mongoHost = embeddedLocalhostMongo.getMongoHost();
    final int mongoPort = embeddedLocalhostMongo.getMongoPort();

    final MongoClient mongoClient = MongoClients.create(String.format("mongodb://%s:%s", mongoHost, mongoPort));
    vocabularyDao = new VocabularyDao(mongoClient, "metis-dereference");
  }

  @AfterAll
  static void destroy() {
    embeddedLocalhostMongo.stop();
  }

  @BeforeEach
  void setupDb() {
    initDatabaseWithEntities();
  }

  @AfterEach
  void tearDownDb() {
    vocabularyDao.getDatastore().find(Vocabulary.class).delete(new DeleteOptions().multi(true));
  }

  @Test
  void getByUriSearch() {
    assertEquals(5, vocabularyDao.size());

    List<Vocabulary> vocabularies = vocabularyDao.getByUriSearch("http://domain2.uri");

    assertEquals(2, vocabularies.size());
    assertNotEquals(Optional.empty(),
        vocabularies.stream().filter(vocabulary -> vocabulary.getName().equals("vocabulary2")).findFirst());
    assertNotEquals(Optional.empty(),
        vocabularies.stream().filter(vocabulary -> vocabulary.getName().equals("vocabulary4")).findFirst());
    assertEquals(Optional.empty(),
        vocabularies.stream().filter(vocabulary -> vocabulary.getName().equals("vocabulary1")).findFirst());
    assertEquals(Optional.empty(),
        vocabularies.stream().filter(vocabulary -> vocabulary.getName().equals("vocabulary3")).findFirst());
    assertEquals(Optional.empty(),
        vocabularies.stream().filter(vocabulary -> vocabulary.getName().equals("vocabulary5")).findFirst());
  }

  @Test
  void getAll() {
    assertEquals(5, vocabularyDao.size());

    List<Vocabulary> vocabularies = vocabularyDao.getAll();

    assertEquals(5, vocabularies.size());
    assertNotEquals(Optional.empty(),
        vocabularies.stream().filter(vocabulary -> vocabulary.getName().equals("vocabulary1")).findFirst());
    assertNotEquals(Optional.empty(),
        vocabularies.stream().filter(vocabulary -> vocabulary.getName().equals("vocabulary2")).findFirst());
    assertNotEquals(Optional.empty(),
        vocabularies.stream().filter(vocabulary -> vocabulary.getName().equals("vocabulary3")).findFirst());
    assertNotEquals(Optional.empty(),
        vocabularies.stream().filter(vocabulary -> vocabulary.getName().equals("vocabulary4")).findFirst());
    assertNotEquals(Optional.empty(),
        vocabularies.stream().filter(vocabulary -> vocabulary.getName().equals("vocabulary5")).findFirst());
  }

  @Test
  void get() {
    assertEquals(5, vocabularyDao.size());

    Vocabulary expectedVocabulary = vocabularyDao.getDatastore().find(Vocabulary.class).first();

    Vocabulary vocabulary = vocabularyDao.get(expectedVocabulary.getId().toString());

    assertEquals(expectedVocabulary.getName(), vocabulary.getName());
    assertEquals(expectedVocabulary.getSuffix(), vocabulary.getSuffix());
    assertEquals(expectedVocabulary.getXslt(), vocabulary.getXslt());
    assertEquals(expectedVocabulary.getUris().stream().findFirst().get(), vocabulary.getUris().stream().findFirst().get());
    assertEquals(expectedVocabulary.getIterations(), vocabulary.getIterations());
  }

  @Test
  void replaceAll() {
    assertEquals(5, vocabularyDao.size());

    Vocabulary vocabulary = new Vocabulary();
    vocabulary.setXslt("xlst");
    vocabulary.setSuffix("suffix");
    vocabulary.setUris(List.of("uri"));
    vocabulary.setIterations(0);
    vocabulary.setName("vocabularyName");

    vocabularyDao.replaceAll(List.of(vocabulary));

    assertEquals(1, vocabularyDao.size());
  }

  @Test
  void getDatastore() {
    assertNotNull(vocabularyDao.getDatastore());
  }

  void initDatabaseWithEntities() {
    for (int i = 1; i <= 5; i++) {
      Vocabulary vocabulary = new Vocabulary();
      vocabulary.setName("vocabulary" + i);
      vocabulary.setSuffix("suffix" + i);
      if (i % 2 == 0) {
        vocabulary.setUris(List.of("http://domain2.uri"));
      } else {
        vocabulary.setUris(List.of("http://domain1.uri"));
      }
      vocabulary.setXslt("xlst" + i);
      vocabulary.setIterations(0);
      vocabularyDao.getDatastore().save(vocabulary);
    }
  }
}
