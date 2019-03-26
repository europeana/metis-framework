package eu.europeana.metis.dereference.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import eu.europeana.enrichment.api.external.EntityWrapper;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.enrichment.api.external.model.Place;
import eu.europeana.enrichment.rest.client.EnrichmentClient;
import eu.europeana.metis.cache.redis.RedisProvider;
import eu.europeana.metis.dereference.OriginalEntity;
import eu.europeana.metis.dereference.ProcessedEntity;
import eu.europeana.metis.dereference.Vocabulary;
import eu.europeana.metis.dereference.service.dao.CacheDao;
import eu.europeana.metis.dereference.service.dao.EntityDao;
import eu.europeana.metis.dereference.service.dao.VocabularyDao;
import eu.europeana.metis.dereference.service.utils.RdfRetriever;
import eu.europeana.metis.mongo.EmbeddedLocalhostMongo;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.TransformerException;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import redis.clients.jedis.Jedis;

/**
 * Created by ymamakis on 12-2-16.
 */
// TODO: 6-3-19 This test is actually requesting external resource. It should be mocked instead, disabling for now.
@Disabled
class MongoDereferenceServiceTest {

  private MongoDereferenceService service;
  private VocabularyDao vocabularyDao;
  private EntityDao entityDao;
  private RedisProvider redisProvider;
  private Jedis jedis;
  private CacheDao cacheDao;
  private EnrichmentClient enrichmentClient;
  private EmbeddedLocalhostMongo embeddedLocalhostMongo = new EmbeddedLocalhostMongo();

  @BeforeEach
  void prepare() {
    embeddedLocalhostMongo.start();
    String mongoHost = embeddedLocalhostMongo.getMongoHost();
    int mongoPort = embeddedLocalhostMongo.getMongoPort();

    vocabularyDao = new VocabularyDao(new MongoClient(mongoHost, mongoPort), "voctest");
    entityDao = new EntityDao(new MongoClient(mongoHost, mongoPort), "voctest");
    redisProvider = Mockito.mock(RedisProvider.class);
    jedis = Mockito.mock(Jedis.class);
    cacheDao = new CacheDao(redisProvider);

    RdfRetriever retriever = new RdfRetriever(entityDao);

    enrichmentClient = Mockito.mock(EnrichmentClient.class);
    service = new MongoDereferenceService(retriever, cacheDao, vocabularyDao, enrichmentClient);
  }

  @AfterEach
  void destroy() {
    embeddedLocalhostMongo.stop();
  }

  @Test
  void testDereference()
      throws TransformerException, JAXBException, IOException, URISyntaxException {

    Mockito.when(redisProvider.getJedis()).thenReturn(jedis);
    final String entityId = "http://sws.geonames.org/3020251/";

    Vocabulary geonames = new Vocabulary();
    geonames.setUri("http://sws.geonames.org/");
    geonames.setXslt(
        IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("geonames.xsl")));
    geonames.setName("Geonames");
    geonames.setIterations(1);
    String geonamesId = vocabularyDao.save(geonames);

    EntityWrapper wrapper = Mockito.mock(EntityWrapper.class);

    Place place = new Place();

    place.setAbout("http://sws.geonames.org/my");

    Mockito.when(enrichmentClient.getByUri(Mockito.anyString())).thenReturn(null);
    Mockito.when(wrapper.getContextualEntity()).thenReturn(null);

    EnrichmentResultList result = service.dereference(entityId);

    assertNotNull(result);

    OriginalEntity entity = entityDao.get(entityId);

    assertNotNull(entity);
    assertNotNull(entity.getXml());
    assertNotNull(entity.getURI());

    ProcessedEntity entity2 = new ProcessedEntity();
    entity2.setResourceId(entityId);
    entity2.setXml(serialize(place));
    entity2.setVocabularyId(geonamesId);

    Mockito.when(jedis.get(entityId)).thenReturn(new ObjectMapper().writeValueAsString(entity2));

    ProcessedEntity entity1 = cacheDao.get(entityId);
    assertNotNull(entity1);
    assertNotNull(entity1.getResourceId());
    assertNotNull(entity1.getXml());
    assertNotNull(entity1.getVocabularyId());
  }

  private String serialize(EnrichmentBase base) throws JAXBException, IOException {
    JAXBContext contextA = JAXBContext.newInstance(EnrichmentBase.class);

    StringWriter writer = new StringWriter();
    Marshaller marshaller = contextA.createMarshaller();
    marshaller.marshal(base, writer);
    String ret = writer.toString();
    writer.close();
    return ret;
  }
}