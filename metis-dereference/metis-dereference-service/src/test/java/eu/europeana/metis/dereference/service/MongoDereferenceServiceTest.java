package eu.europeana.metis.dereference.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import dev.morphia.Datastore;
import eu.europeana.enrichment.api.external.DereferenceResultStatus;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.Place;
import eu.europeana.metis.dereference.RdfRetriever;
import eu.europeana.metis.dereference.Vocabulary;
import eu.europeana.metis.dereference.service.dao.ProcessedEntityDao;
import eu.europeana.metis.dereference.service.dao.VocabularyDao;
import eu.europeana.metis.mongo.embedded.EmbeddedLocalhostMongo;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MongoDereferenceServiceTest {

  private MongoDereferenceService service;
  private Datastore vocabularyDaoDatastore;
  private final EmbeddedLocalhostMongo embeddedLocalhostMongo = new EmbeddedLocalhostMongo();

  @BeforeEach
  void prepare() {
    embeddedLocalhostMongo.start();
    String mongoHost = embeddedLocalhostMongo.getMongoHost();
    int mongoPort = embeddedLocalhostMongo.getMongoPort();

    MongoClient mongoClient = MongoClients
        .create(String.format("mongodb://%s:%s", mongoHost, mongoPort));
    VocabularyDao vocabularyDao = new VocabularyDao(mongoClient, "voctest") {
      {
        vocabularyDaoDatastore = this.getDatastore();
      }
    };

    ProcessedEntityDao processedEntityDao = mock(ProcessedEntityDao.class);

    service = spy(
        new MongoDereferenceService(new RdfRetriever(), processedEntityDao, vocabularyDao));
  }

  @AfterEach
  void destroy() {
    embeddedLocalhostMongo.stop();
  }

  @Test
  void testDereference()
      throws TransformerException, JAXBException, IOException, URISyntaxException {

    // Create vocabulary for geonames and save it.
    final Vocabulary geonames = new Vocabulary();
    geonames.setUris(Collections.singleton("http://sws.geonames.org/"));
    geonames.setXslt(IOUtils
        .toString(this.getClass().getClassLoader().getResourceAsStream("geonames.xsl"),
            StandardCharsets.UTF_8));
    geonames.setName("Geonames");
    geonames.setIterations(0);
    vocabularyDaoDatastore.save(geonames);

    // Create geonames entity
    final Place place = new Place();
    final String entityId = "http://sws.geonames.org/3020251/";
    place.setAbout(entityId);

    // Mock the service
    doReturn(new ImmutableTriple<>(place, geonames,DereferenceResultStatus.SUCCESS)).when(service)
                                                                                  .computeEnrichmentBaseVocabularyTriple(entityId);

    // Test the method
    final Pair<List<EnrichmentBase>, DereferenceResultStatus> result = service.dereference(entityId);
    assertNotNull(result);
    assertEquals(1, result.getLeft().size());
    assertSame(place, result.getLeft().get(0));

    // Test null argument
    assertThrows(IllegalArgumentException.class, () -> service.dereference(null));

    // Test absent object
    doReturn(new ImmutableTriple<>(null, null, DereferenceResultStatus.SUCCESS))
        .when(service).computeEnrichmentBaseVocabularyTriple(entityId);
    final Pair<List<EnrichmentBase>, DereferenceResultStatus> emptyResult = service.dereference(entityId);
    assertNotNull(emptyResult);
    assertTrue(emptyResult.getLeft().isEmpty());
    assertTrue(emptyResult.getRight().equals(DereferenceResultStatus.NO_VOCABULARY_MATCHING));
  }
}
