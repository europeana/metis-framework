import static org.mockito.ArgumentMatchers.any;

import eu.europeana.corelib.lookup.impl.CollectionMongoServerImpl;
import eu.europeana.corelib.lookup.impl.EuropeanaIdMongoServerImpl;
import eu.europeana.corelib.tools.lookuptable.CollectionMongoServer;
import eu.europeana.corelib.tools.lookuptable.EuropeanaId;
import eu.europeana.corelib.tools.lookuptable.EuropeanaIdMongoServer;
import eu.europeana.redirects.model.RedirectRequest;
import eu.europeana.redirects.model.RedirectRequestList;
import eu.europeana.redirects.model.RedirectResponse;
import eu.europeana.redirects.model.RedirectResponseList;
import eu.europeana.redirects.params.ControlledParams;
import eu.europeana.redirects.params.QualifiedFieldName;
import eu.europeana.redirects.service.mongo.MongoRedirectService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.test.util.ReflectionTestUtils;


/**
 * Created by ymamakis on 1/15/16.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({CollectionMongoServerImpl.class,
        CloudSolrServer.class,QueryResponse.class,EuropeanaIdMongoServer.class,SolrDocumentList.class,
        SolrDocument.class,ModifiableSolrParams.class})
@PowerMockIgnore({"org.apache.logging.log4j.*"})
public class MongoRedirectServiceTest {
    private CollectionMongoServer collectionMongo;
    private EuropeanaIdMongoServer mongoServer;
    private CloudSolrServer solrServer;

    @Before
    public void prepare(){
        collectionMongo = PowerMockito.mock(CollectionMongoServerImpl.class);
        mongoServer = PowerMockito.mock(EuropeanaIdMongoServerImpl.class);
        solrServer = PowerMockito.mock(CloudSolrServer.class);
    }

    @Test
    public void testTransformationsOnly() throws Exception{

        MongoRedirectService service = new MongoRedirectService();
        ReflectionTestUtils.setField(service,"mongoServer",mongoServer);
        ReflectionTestUtils.setField(service,"productionSolrServer",solrServer);
        ReflectionTestUtils.setField(service,"collectionMongoServer",collectionMongo);
        RedirectRequest request = new RedirectRequest();
        request.setCollection("test_collection");
        request.setEuropeanaId("id1");
        Map<String,String> params = new HashMap<>();
        params.put(ControlledParams.REDIRECT_USE_CUSTOM_FUNCTIONS.toString(),"replace(1,2)");
        request.setParameters(params);

        SolrDocumentList list = PowerMockito.mock(SolrDocumentList.class);
        SolrDocument doc = PowerMockito.mock (SolrDocument.class);

        QueryResponse resp = PowerMockito.mock(QueryResponse.class);
        Object response = Mockito.mock(Object.class);
        Mockito.when(collectionMongo.findOldCollectionId("test_collection")).thenReturn(null);
        Mockito.when(solrServer.query(any(ModifiableSolrParams.class))).thenReturn(resp);
        Mockito.when(resp.getResults()).thenReturn(list);
        Mockito.when(list.getNumFound()).thenReturn(new Long(1));
        Mockito.when(list.get(0)).thenReturn(doc);
        Mockito.when(doc.getFieldValue("europeana_id")).thenReturn(response);
        Mockito.when(response.toString()).thenReturn("id2");
        Mockito.when(mongoServer.retrieveEuropeanaIdFromOld("id2")).thenReturn(null);


        RedirectResponse redirectResponse = service.createRedirect(request);
        Assert.assertTrue("New id different", StringUtils.equals(redirectResponse.getNewId(),"id1"));
        Assert.assertTrue("Old id different", StringUtils.equals(redirectResponse.getOldId(),"id2"));
    }

    @Test
    public void testCollectionChange() throws Exception{
        MongoRedirectService service = new MongoRedirectService();
        ReflectionTestUtils.setField(service,"mongoServer",mongoServer);
        ReflectionTestUtils.setField(service,"productionSolrServer",solrServer);
        ReflectionTestUtils.setField(service,"collectionMongoServer",collectionMongo);
        RedirectRequest request = new RedirectRequest();
        request.setCollection("test_collection");
        request.setEuropeanaId("/id1");
        Map<String,String> params = new HashMap<>();
        params.put(ControlledParams.REDIRECT_USE_CUSTOM_FUNCTIONS.toString(),"replace(1,2)");
        request.setParameters(params);

        SolrDocumentList list = PowerMockito.mock(SolrDocumentList.class);
        SolrDocument doc = PowerMockito.mock (SolrDocument.class);

        QueryResponse resp = PowerMockito.mock(QueryResponse.class);
        Object response = Mockito.mock(Object.class);
        Mockito.when(collectionMongo.findOldCollectionId("test_collection")).thenReturn("test_collection2");
        Mockito.when(solrServer.query(any(ModifiableSolrParams.class))).thenReturn(resp);
        Mockito.when(resp.getResults()).thenReturn(list);
        Mockito.when(list.getNumFound()).thenReturn(new Long(1));
        Mockito.when(list.get(0)).thenReturn(doc);
        Mockito.when(doc.getFieldValue("europeana_id")).thenReturn(response);
        Mockito.when(response.toString()).thenReturn("id2");
        Mockito.when(mongoServer.retrieveEuropeanaIdFromOld("id2")).thenReturn(null);


        RedirectResponse redirectResponse = service.createRedirect(request);
        Assert.assertTrue("New id different", StringUtils.equals(redirectResponse.getNewId(),"/id1"));
        Assert.assertTrue("Old id different " + redirectResponse.getOldId(), StringUtils.equals(redirectResponse.getOldId(),"/test_collection2/id2"));
    }

    @Test
    public void testCustomField() throws Exception{
        MongoRedirectService service = new MongoRedirectService();
        ReflectionTestUtils.setField(service,"mongoServer",mongoServer);
        ReflectionTestUtils.setField(service,"productionSolrServer",solrServer);
        ReflectionTestUtils.setField(service,"collectionMongoServer",collectionMongo);
        RedirectRequest request = new RedirectRequest();
        request.setCollection("test_collection");
        request.setEuropeanaId("/id1");
        Map<String,String> params = new HashMap<>();
        params.put(ControlledParams.REDIRECT_USE_CUSTOM_FUNCTIONS.toString(),"replace(1,2)");
        request.setParameters(params);
        request.setFieldName("edm:isShownAt");
        request.setFieldValue("testId");
        SolrDocumentList list = PowerMockito.mock(SolrDocumentList.class);
        SolrDocument doc = PowerMockito.mock (SolrDocument.class);

        QueryResponse resp = PowerMockito.mock(QueryResponse.class);
        Object response = Mockito.mock(Object.class);
        Mockito.when(collectionMongo.findOldCollectionId("test_collection")).thenReturn(null);
        Mockito.when(solrServer.query(any(ModifiableSolrParams.class))).thenReturn(resp);
        Mockito.when(resp.getResults()).thenReturn(list);
        Mockito.when(list.getNumFound()).thenReturn(new Long(1));
        Mockito.when(list.get(0)).thenReturn(doc);
        Mockito.when(doc.getFieldValue("europeana_id")).thenReturn(response);
        Mockito.when(response.toString()).thenReturn("/id2");
        Mockito.when(mongoServer.retrieveEuropeanaIdFromOld("/id2")).thenReturn(null);


        RedirectResponse redirectResponse = service.createRedirect(request);
        Assert.assertTrue("New id different", StringUtils.equals(redirectResponse.getNewId(),"/id1"));
        Assert.assertTrue("Old id different " + redirectResponse.getOldId(), StringUtils.equals(redirectResponse.getOldId(),"/id2"));
    }

    @Test
    public void testCollectionChangeNoTransform() throws Exception{
        MongoRedirectService service = new MongoRedirectService();
        ReflectionTestUtils.setField(service,"mongoServer",mongoServer);
        ReflectionTestUtils.setField(service,"productionSolrServer",solrServer);
        ReflectionTestUtils.setField(service,"collectionMongoServer",collectionMongo);
        RedirectRequest request = new RedirectRequest();
        request.setCollection("test_collection");
        request.setEuropeanaId("/id1");


        SolrDocumentList list = PowerMockito.mock(SolrDocumentList.class);
        SolrDocument doc = PowerMockito.mock (SolrDocument.class);

        QueryResponse resp = PowerMockito.mock(QueryResponse.class);
        Object response = Mockito.mock(Object.class);
        Mockito.when(collectionMongo.findOldCollectionId("test_collection")).thenReturn("test_collection2");
        Mockito.when(solrServer.query(any(ModifiableSolrParams.class))).thenReturn(resp);
        Mockito.when(resp.getResults()).thenReturn(list);
        Mockito.when(list.getNumFound()).thenReturn(new Long(1));
        Mockito.when(list.get(0)).thenReturn(doc);
        Mockito.when(doc.getFieldValue("europeana_id")).thenReturn(response);
        Mockito.when(response.toString()).thenReturn("id1");
        Mockito.when(mongoServer.retrieveEuropeanaIdFromOld("id1")).thenReturn(null);

        RedirectResponse redirectResponse = service.createRedirect(request);
        Assert.assertTrue("New id different", StringUtils.equals(redirectResponse.getNewId(),"/id1"));
        Assert.assertTrue("Old id different " + redirectResponse.getOldId(), StringUtils.equals(redirectResponse.getOldId(),"/test_collection2/id1"));
    }

    @Test
    public void testCustomFieldNoTransform() throws Exception{
        MongoRedirectService service = new MongoRedirectService();
        ReflectionTestUtils.setField(service,"mongoServer",mongoServer);
        ReflectionTestUtils.setField(service,"productionSolrServer",solrServer);
        ReflectionTestUtils.setField(service,"collectionMongoServer",collectionMongo);
        RedirectRequest request = new RedirectRequest();
        request.setCollection("test_collection");
        request.setEuropeanaId("/id1");

        request.setFieldName("edm:isShownAt");
        request.setFieldValue("testId");
        SolrDocumentList list = PowerMockito.mock(SolrDocumentList.class);
        SolrDocument doc = PowerMockito.mock (SolrDocument.class);

        QueryResponse resp = PowerMockito.mock(QueryResponse.class);
        Object response = Mockito.mock(Object.class);
        Mockito.when(collectionMongo.findOldCollectionId("test_collection")).thenReturn(null);
        Mockito.when(solrServer.query(any(ModifiableSolrParams.class))).thenReturn(resp);
        Mockito.when(resp.getResults()).thenReturn(list);
        Mockito.when(list.getNumFound()).thenReturn(new Long(1));
        Mockito.when(list.get(0)).thenReturn(doc);
        Mockito.when(doc.getFieldValue("europeana_id")).thenReturn(response);
        Mockito.when(response.toString()).thenReturn("/id2");
        Mockito.when(mongoServer.retrieveEuropeanaIdFromOld("/id2")).thenReturn(null);

        RedirectResponse redirectResponse = service.createRedirect(request);
        Assert.assertTrue("New id different", StringUtils.equals(redirectResponse.getNewId(),"/id1"));
        Assert.assertTrue("Old id different " + redirectResponse.getOldId(), StringUtils.equals(redirectResponse.getOldId(),"/id2"));
    }

    @Test
    public void testIdNotExists() throws Exception{
        MongoRedirectService service = new MongoRedirectService();
        ReflectionTestUtils.setField(service,"mongoServer",mongoServer);
        ReflectionTestUtils.setField(service,"productionSolrServer",solrServer);
        ReflectionTestUtils.setField(service,"collectionMongoServer",collectionMongo);
        RedirectRequest request = new RedirectRequest();
        request.setCollection("test_collection");
        request.setEuropeanaId("/id1");
        Map<String,String> params = new HashMap<>();
        params.put(ControlledParams.REDIRECT_USE_CUSTOM_FUNCTIONS.toString(),"replace(1,2)");
        request.setParameters(params);
        request.setFieldName(QualifiedFieldName.ISSHOWNAT);
        request.setFieldValue("testId");
        SolrDocumentList list = PowerMockito.mock(SolrDocumentList.class);

        QueryResponse resp = PowerMockito.mock(QueryResponse.class);
        Mockito.when(collectionMongo.findOldCollectionId("test_collection")).thenReturn(null);
        Mockito.when(solrServer.query(any(ModifiableSolrParams.class))).thenReturn(resp);
        Mockito.when(resp.getResults()).thenReturn(list);
        Mockito.when(list.getNumFound()).thenReturn(new Long(0));



        RedirectResponse redirectResponse = service.createRedirect(request);
        Assert.assertTrue("New id different", StringUtils.equals(redirectResponse.getNewId(),"/id1"));
        Assert.assertTrue("Old id different " + redirectResponse.getOldId(), StringUtils.equals(redirectResponse.getOldId(),null));
    }

    @Test
    public void testGenerateIdFailsNotNull() throws Exception{
        MongoRedirectService service = new MongoRedirectService();
        ReflectionTestUtils.setField(service,"mongoServer",mongoServer);
        ReflectionTestUtils.setField(service,"productionSolrServer",solrServer);
        ReflectionTestUtils.setField(service,"collectionMongoServer",collectionMongo);
        RedirectRequest request = new RedirectRequest();
        request.setCollection("test_collection");
        request.setEuropeanaId("/id1");
        Map<String,String> params = new HashMap<>();
        params.put(ControlledParams.REDIRECT_USE_CUSTOM_FUNCTIONS.toString(),"replace(1,2)");
        request.setParameters(params);
        request.setFieldName("edm:isShownAt");
        request.setFieldValue("testId");
        SolrDocumentList list = PowerMockito.mock(SolrDocumentList.class);
        SolrDocument doc = PowerMockito.mock (SolrDocument.class);
        EuropeanaId id = new EuropeanaId();
        id.setNewId(request.getEuropeanaId());
        QueryResponse resp = PowerMockito.mock(QueryResponse.class);
        Object response = Mockito.mock(Object.class);
        Mockito.when(collectionMongo.findOldCollectionId("test_collection")).thenReturn(null);
        Mockito.when(solrServer.query(any(ModifiableSolrParams.class))).thenReturn(resp);
        Mockito.when(resp.getResults()).thenReturn(list);
        Mockito.when(list.getNumFound()).thenReturn(new Long(1));
        Mockito.when(list.get(0)).thenReturn(doc);
        Mockito.when(doc.getFieldValue("europeana_id")).thenReturn(response);
        Mockito.when(response.toString()).thenReturn("/id2");
        Mockito.when(mongoServer.retrieveEuropeanaIdFromOld(any(List.class))).thenReturn(id);


        RedirectResponse redirectResponse = service.createRedirect(request);
        Assert.assertTrue("New id different", StringUtils.equals(redirectResponse.getNewId(),"/id1"));
        Assert.assertTrue("Old id different " + redirectResponse.getOldId(), StringUtils.equals(redirectResponse.getOldId(),null));
    }


    @Test
    public void testBatchRedirects() throws Exception{
        MongoRedirectService service = new MongoRedirectService();
        ReflectionTestUtils.setField(service,"mongoServer",mongoServer);
        ReflectionTestUtils.setField(service,"productionSolrServer",solrServer);
        ReflectionTestUtils.setField(service,"collectionMongoServer",collectionMongo);
        RedirectRequestList lst = new RedirectRequestList();
        List<RedirectRequest> list = new ArrayList<>();
        RedirectRequest req = new RedirectRequest();
        req.setCollection("test_collection");
        req.setEuropeanaId("id1");
        Map<String,String> params = new HashMap<>();
        params.put(ControlledParams.REDIRECT_USE_CUSTOM_FUNCTIONS.toString(),"replace(1,2)");
        req.setParameters(params);
        list.add(req);
        lst.setRequestList(list);
        SolrDocumentList docList = PowerMockito.mock(SolrDocumentList.class);
        SolrDocument doc = PowerMockito.mock (SolrDocument.class);

        QueryResponse resp = PowerMockito.mock(QueryResponse.class);
        Object response = Mockito.mock(Object.class);
        Mockito.when(collectionMongo.findOldCollectionId("test_collection")).thenReturn(null);
        Mockito.when(solrServer.query(any(ModifiableSolrParams.class))).thenReturn(resp);
        Mockito.when(resp.getResults()).thenReturn(docList);
        Mockito.when(docList.getNumFound()).thenReturn(new Long(1));
        Mockito.when(docList.get(0)).thenReturn(doc);
        Mockito.when(doc.getFieldValue("europeana_id")).thenReturn(response);
        Mockito.when(response.toString()).thenReturn("id2");
        Mockito.when(mongoServer.retrieveEuropeanaIdFromOld("id2")).thenReturn(null);

        RedirectResponseList responseList = service.createRedirects(lst);
        Assert.assertTrue(responseList.getResponseList().size()==lst.getRequestList().size());
        Assert.assertTrue("New id different", StringUtils.equals(responseList.getResponseList().get(0).getNewId(),"id1"));
        Assert.assertTrue("Old id different", StringUtils.equals(responseList.getResponseList().get(0).getOldId(),"id2"));
    }

}
