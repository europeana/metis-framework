/*
 * Copyright 2007-2013 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */
package eu.europeana.metis.derefrence.test;

import java.io.IOException;
import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.TransformerException;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import eu.europeana.enrichment.api.external.EntityWrapper;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.enrichment.api.external.model.Place;
import eu.europeana.enrichment.rest.client.EnrichmentClient;
import eu.europeana.metis.dereference.OriginalEntity;
import eu.europeana.metis.dereference.ProcessedEntity;
import eu.europeana.metis.dereference.Vocabulary;
import eu.europeana.metis.dereference.service.MongoDereferenceService;
import eu.europeana.metis.dereference.service.dao.CacheDao;
import eu.europeana.metis.dereference.service.dao.EntityDao;
import eu.europeana.metis.dereference.service.dao.VocabularyDao;
import eu.europeana.metis.dereference.service.utils.RdfRetriever;
import eu.europeana.metis.mongo.EmbeddedLocalhostMongo;
import redis.clients.jedis.Jedis;

/**
 * Created by ymamakis on 12-2-16.
 */
public class MongoDereferenceServiceTest {
    private MongoDereferenceService service;
    private VocabularyDao vocabularyDao;
    private EntityDao entityDao;
    private Jedis jedis;
    private CacheDao cacheDao;
    private EnrichmentClient enrichmentClient;
    private EmbeddedLocalhostMongo embeddedLocalhostMongo = new EmbeddedLocalhostMongo();
    
    @Before
    public void prepare() throws IOException {
        embeddedLocalhostMongo.start();
        String mongoHost = embeddedLocalhostMongo.getMongoHost();
        int mongoPort = embeddedLocalhostMongo.getMongoPort();
    
        vocabularyDao = new VocabularyDao(new MongoClient(mongoHost, mongoPort), "voctest");
        entityDao = new EntityDao(new MongoClient(mongoHost, mongoPort), "voctest");
        jedis = Mockito.mock(Jedis.class);
        cacheDao = new CacheDao(jedis);
        
        RdfRetriever retriever = new RdfRetriever();

        enrichmentClient = Mockito.mock(EnrichmentClient.class);
        service = new MongoDereferenceService(retriever, cacheDao, entityDao, vocabularyDao, enrichmentClient);
    }

    @Test
    public void testDereference() throws IOException {
        Vocabulary geonames = new Vocabulary();
        geonames.setUri("http://sws.geonames.org/");
        geonames.setXslt(IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("geonames.xsl")));
        geonames.setName("Geonames");
        geonames.setIterations(0);
        vocabularyDao.save(geonames);
        
        try {
            EntityWrapper wrapper = Mockito.mock(EntityWrapper.class);
            
            Place place = new Place();
            
            place.setAbout("http://sws.geonames.org/my");
            
            Mockito.when(enrichmentClient.getByUri(Mockito.anyString())).thenReturn(null);
            Mockito.when(wrapper.getContextualEntity()).thenReturn(null);
            
            EnrichmentResultList result = service.dereference("http://sws.geonames.org/3020251");
            
            Assert.assertNotNull(result);
            
            OriginalEntity entity = entityDao.get("http://sws.geonames.org/3020251");
            
            Assert.assertNotNull(entity);
            Assert.assertNotNull(entity.getXml());
            Assert.assertNotNull(entity.getURI());
                                  
            ProcessedEntity entity2 = new ProcessedEntity();
            entity2.setURI("http://sws.geonames.org/3020251");
            entity2.setXml(serialize(place));
                        
            Mockito.when(jedis.get(Mockito.anyString())).thenReturn(new ObjectMapper().writeValueAsString(entity2));
            
            ProcessedEntity entity1 = cacheDao.get("http://sws.geonames.org/3020251");
            Assert.assertNotNull(entity1);
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    @After
    public void destroy() {
        embeddedLocalhostMongo.stop();
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