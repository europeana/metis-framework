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

import com.mongodb.MongoClient;
import eu.europeana.metis.dereference.ContextualClass;
import eu.europeana.metis.dereference.OriginalEntity;
import eu.europeana.metis.dereference.Vocabulary;
import eu.europeana.metis.dereference.service.MongoDereferencingManagementService;
import eu.europeana.metis.dereference.service.dao.CacheDao;
import eu.europeana.metis.dereference.service.dao.EntityDao;
import eu.europeana.metis.dereference.service.dao.VocabularyDao;
import eu.europeana.metis.mongo.MongoProvider;
import eu.europeana.metis.utils.NetworkUtil;
import java.io.IOException;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;
import redis.clients.jedis.Jedis;

/**
 * Created by ymamakis on 2/22/16.
 */
public class MongoDereferencingManagementServiceTest {

    private MongoDereferencingManagementService service;
    private Jedis jedis;
    private EntityDao entityDao;
    private MongoProvider mongoProvider = new MongoProvider();
    @Before
    public void prepare() throws IOException {
        int port = NetworkUtil.getAvailableLocalPort();
        mongoProvider.start(port);
        service = new MongoDereferencingManagementService();
        jedis = Mockito.mock(Jedis.class);
        CacheDao cacheDao = new CacheDao(jedis);
        MongoClient mongo = new MongoClient("localhost",port);
        VocabularyDao vocDao = new VocabularyDao(mongo,"voctest");
        entityDao = new EntityDao(mongo,"voctest");
        ReflectionTestUtils.setField(service,"cacheDao",cacheDao);
        ReflectionTestUtils.setField(service,"vocabularyDao",vocDao);
        ReflectionTestUtils.setField(service,"entityDao",entityDao);
    }


    @Test
    public void testCreateRetrieveVocabulary(){
        Vocabulary voc = new Vocabulary();
        voc.setIterations(0);
        voc.setName("testName");
        voc.setRules("testRules");
        voc.setType(ContextualClass.AGENT);
        voc.setTypeRules("testTypeRules");
        voc.setURI("testURI");
        voc.setXslt("testXSLT");
        service.saveVocabulary(voc);
        Vocabulary retVoc = service.findByName(voc.getName());
        Assert.assertEquals(voc.getName(),retVoc.getName());
        Assert.assertEquals(voc.getIterations(),retVoc.getIterations());
        Assert.assertEquals(voc.getRules(),retVoc.getRules());
        Assert.assertEquals(voc.getType(),retVoc.getType());
        Assert.assertEquals(voc.getTypeRules(),retVoc.getTypeRules());
        Assert.assertEquals(voc.getURI(),retVoc.getURI());
    }

    @Test
    public void testCreateUpdateRetrieveVocabulary(){
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return null;
            }
        }).when(jedis).flushAll();
        Vocabulary voc = new Vocabulary();
        voc.setIterations(0);
        voc.setName("testName");
        voc.setRules("testRules");
        voc.setType(ContextualClass.AGENT);
        voc.setTypeRules("testTypeRules");
        voc.setURI("testURI");
        voc.setXslt("testXSLT");
        service.saveVocabulary(voc);
        voc.setURI("testUri2");
        service.updateVocabulary(voc);
        Vocabulary retVoc = service.findByName(voc.getName());
        Assert.assertEquals(voc.getName(),retVoc.getName());
        Assert.assertEquals(voc.getIterations(),retVoc.getIterations());
        Assert.assertEquals(voc.getRules(),retVoc.getRules());
        Assert.assertEquals(voc.getType(),retVoc.getType());
        Assert.assertEquals(voc.getTypeRules(),retVoc.getTypeRules());
        Assert.assertEquals(voc.getURI(),retVoc.getURI());
    }

    @Test
    public void testGetAllVocabularies(){
        Vocabulary voc = new Vocabulary();
        voc.setIterations(0);
        voc.setName("testName");
        voc.setRules("testRules");
        voc.setType(ContextualClass.AGENT);
        voc.setTypeRules("testTypeRules");
        voc.setURI("testURI");
        voc.setXslt("testXSLT");
        service.saveVocabulary(voc);
        List<Vocabulary> retVoc = service.getAllVocabularies();
        Assert.assertEquals(1,retVoc.size());
    }

    @Test
    public void testDeleteVocabularies(){
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return null;
            }
        }).when(jedis).flushAll();
        Vocabulary voc = new Vocabulary();
        voc.setIterations(0);
        voc.setName("testName");
        voc.setRules("testRules");
        voc.setType(ContextualClass.AGENT);
        voc.setTypeRules("testTypeRules");
        voc.setURI("testURI");
        voc.setXslt("testXSLT");
        service.saveVocabulary(voc);
        List<Vocabulary> retVoc = service.getAllVocabularies();
        Assert.assertEquals(1,retVoc.size());
        service.deleteVocabulary(voc.getName());
        List<Vocabulary> retVoc2 = service.getAllVocabularies();
        Assert.assertEquals(0,retVoc2.size());
    }

    @Test
    public void removeEntity(){
        OriginalEntity entity = new OriginalEntity();
        entity.setURI("testUri");
        entity.setXml("testXml");
        entityDao.save(entity);

        service.removeEntity(entity.getURI());

        Assert.assertEquals(null,entityDao.getByUri(entity.getURI()));

    }

    @Test
    public void updateEntity(){
        OriginalEntity entity = new OriginalEntity();
        entity.setURI("testUri");
        entity.setXml("testXml");
        entityDao.save(entity);

        service.updateEntity(entity.getURI(),"testXml2");
        OriginalEntity entity1 = entityDao.getByUri(entity.getURI());
        Assert.assertEquals(entity1.getXml(),"testXml2");

    }

    @After
    public void destroy(){
        mongoProvider.stop();
    }


}
