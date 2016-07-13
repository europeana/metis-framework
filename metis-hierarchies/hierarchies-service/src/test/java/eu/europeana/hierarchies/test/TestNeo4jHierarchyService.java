package eu.europeana.hierarchies.test;

import eu.europeana.hierarchies.service.cache.CacheDAO;
import eu.europeana.hierarchies.service.cache.CacheEntry;
import eu.europeana.hierarchies.service.cache.RedisDAO;
import eu.europeana.hierarchies.service.impl.Neo4jHierarchyService;
import eu.europeana.hierarchy.ParentNode;
import eu.europeana.hierarchy.ParentNodeList;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;

/**
 * Service Unit tests
 * Created by ymamakis on 1/29/16.
 */
public class TestNeo4jHierarchyService {

    CacheDAO dao = Mockito.mock(RedisDAO.class);
    Client clientMock = Mockito.mock(Client.class);
    Neo4jHierarchyService service;
    @Before
    public void prepare(){
        service = new Neo4jHierarchyService(dao,clientMock);
    }

    /**
     * Test the creation of one node
     * @throws IOException
     */
    @Test
    public void testCreateNode() throws IOException {
        ParentNode pNode = new ParentNode();
        pNode.setId("test");
        Map<String,Object> params = new HashMap<>();
        params.put("string","string");
        WebTarget targetMock = Mockito.mock(WebTarget.class);
        WebTarget pathMock = Mockito.mock(WebTarget.class);
        Invocation.Builder builderMock = Mockito.mock(Invocation.Builder.class);
        Response postResponseMock = Mockito.mock(Response.class);
        Mockito.when(clientMock.target(Mockito.anyString())).thenReturn(targetMock);
        Mockito.when(targetMock.path(Mockito.anyString())).thenReturn(pathMock);
        Mockito.when(pathMock.request()).thenReturn(builderMock);
        Mockito.when(builderMock.post(Mockito.any(Entity.class))).thenReturn(postResponseMock);
        Mockito.when(postResponseMock.readEntity(ParentNode.class)).thenReturn(pNode);
        Mockito.doNothing().when(dao).addParentToSet("collection","string");
        ParentNode parentNode = service.createNode("collection",params);
        Assert.assertNotNull(parentNode);
        Assert.assertTrue(StringUtils.equals(parentNode.getId(),pNode.getId()));
    }

    /**
     * Test the creation of multiple node
     * @throws IOException
     */
    @Test
    public void testCreateNodes() throws IOException {
        ParentNodeList pNodeList = new ParentNodeList();
        Set<ParentNode> set = new HashSet<>();
        ParentNode pNode = new ParentNode();
        pNode.setId("test");
        set.add(pNode);
        pNodeList.setParentNodeList(set);
        Map<String,Object> params = new HashMap<>();
        params.put("string","string");
        List<Map<String,Object>> paramsList = new ArrayList<>();
        paramsList.add(params);
        WebTarget targetMock = Mockito.mock(WebTarget.class);
        WebTarget pathMock = Mockito.mock(WebTarget.class);
        Invocation.Builder builderMock = Mockito.mock(Invocation.Builder.class);
        Response postResponseMock = Mockito.mock(Response.class);
        Mockito.when(clientMock.target(Mockito.anyString())).thenReturn(targetMock);
        Mockito.when(targetMock.path(Mockito.anyString())).thenReturn(pathMock);
        Mockito.when(pathMock.request()).thenReturn(builderMock);
        Mockito.when(builderMock.post(Mockito.any(Entity.class))).thenReturn(postResponseMock);
        Mockito.when(postResponseMock.readEntity(ParentNodeList.class)).thenReturn(pNodeList);
        Mockito.doNothing().when(dao).addParentToSet("collection","string");
        Set<ParentNode> parentNodes = service.createNodes("collection",paramsList);
        Assert.assertNotNull(parentNodes);
        Assert.assertTrue(StringUtils.equals(parentNodes.iterator().next().getId(),pNode.getId()));
    }
    /**
     * Test that nodes are not created when empty parameters are sent
     * @throws IOException
     */
    @Test
    public void testCreateNodeNull() throws IOException {
        Map<String,Object> params = new HashMap<>();
        ParentNode parentNode = service.createNode("collection",params);
        Assert.assertNull(parentNode);
    }

    /**
     * Test of removal of relations for a single node
     */
    @Test
    public void createRelationsForNodeTest(){
        WebTarget targetMock = Mockito.mock(WebTarget.class);
        Invocation.Builder builderMock = Mockito.mock(Invocation.Builder.class);
        Response postResponseMock = Mockito.mock(Response.class);
        Mockito.when(clientMock.target("/relations/generate/%2Fid")).thenReturn(targetMock);
        Mockito.when(targetMock.request()).thenReturn(builderMock);
        Mockito.when(builderMock.get()).thenReturn(postResponseMock);
        service.createRelationsForNode("/id");
        Mockito.verify(targetMock).request();
        Mockito.verify(builderMock).post(null);
    }

    /**
     * Test of removal of relations for a list of nodes
     */
    @Test
    public void createRelationsForNodesTest(){
        WebTarget targetMock = Mockito.mock(WebTarget.class);
        Invocation.Builder builderMock = Mockito.mock(Invocation.Builder.class);
        Response postResponseMock = Mockito.mock(Response.class);
        Mockito.when(clientMock.target("/relations/generate/%2Fid")).thenReturn(targetMock);
        Mockito.when(targetMock.request()).thenReturn(builderMock);
        Mockito.when(builderMock.get()).thenReturn(postResponseMock);
        List<String>ids = new ArrayList<>();
        ids.add("/id");
        service.createRelationsForNodes(ids);
        Mockito.verify(targetMock).request();
        Mockito.verify(builderMock).post(null);
    }

    /**
     * Test retrieval of cache entry from Cache
     * @throws IOException
     */
    @Test
    public void retrieveParentsByCollectionTest() throws IOException {
        CacheEntry entry = new CacheEntry();
        Set<String> parents = new HashSet<>();
        parents.add("test1");
        parents.add("test2");
        entry.setParents(parents);
        Mockito.when(dao.getByCollection("collection")).thenReturn(entry);
        CacheEntry retEntry = service.retrieveParentsByCollection("collection");
        Assert.assertEquals(entry,retEntry);

    }
    /**
     * Test that the REST calls are invoked for the ordered and unordered relations for a single node
     */
    @Test
    public void createOrderedAndUnorderedRelationsForNodeTest(){
        WebTarget targetMock = Mockito.mock(WebTarget.class);
        Invocation.Builder builderMock = Mockito.mock(Invocation.Builder.class);
        Response orderedPostResponseMock = Mockito.mock(Response.class);
        WebTarget unorderedMock = Mockito.mock(WebTarget.class);
        Invocation.Builder builderUnorderedMock = Mockito.mock(Invocation.Builder.class);
        Response unorderedPostResponseMock = Mockito.mock(Response.class);
        Mockito.when(clientMock.target("/relations/generate/order/%2Fid")).thenReturn(targetMock);
        Mockito.when(targetMock.request()).thenReturn(builderMock);
        Mockito.when(builderMock.get()).thenReturn(orderedPostResponseMock);
        Mockito.when(clientMock.target("/order/fakeorder/nodeId/%2Fid")).thenReturn(unorderedMock);
        Mockito.when(unorderedMock.request()).thenReturn(builderUnorderedMock);
        Mockito.when(builderUnorderedMock.get()).thenReturn(unorderedPostResponseMock);
        service.createOrderedAndUnorderedRelationsForNode("/id");
        Mockito.verify(targetMock).request();
        Mockito.verify(builderMock).post(null);
        Mockito.verify(unorderedMock).request();
        Mockito.verify(builderUnorderedMock).get();
    }

    /**
     * Test that the REST calls are invoked for the ordered and unordered relations for multiple nodes
     */
    @Test
    public void createOrderedAndUnorderedRelationsForNodesTest(){
        WebTarget targetMock = Mockito.mock(WebTarget.class);
        Invocation.Builder builderMock = Mockito.mock(Invocation.Builder.class);
        Response orderedPostResponseMock = Mockito.mock(Response.class);
        WebTarget unorderedMock = Mockito.mock(WebTarget.class);
        Invocation.Builder builderUnorderedMock = Mockito.mock(Invocation.Builder.class);
        Response unorderedPostResponseMock = Mockito.mock(Response.class);
        Mockito.when(clientMock.target("/relations/generate/order/%2Fid")).thenReturn(targetMock);
        Mockito.when(targetMock.request()).thenReturn(builderMock);
        Mockito.when(builderMock.get()).thenReturn(orderedPostResponseMock);
        Mockito.when(clientMock.target("/order/fakeorder/nodeId/%2Fid")).thenReturn(unorderedMock);
        Mockito.when(unorderedMock.request()).thenReturn(builderUnorderedMock);
        Mockito.when(builderUnorderedMock.get()).thenReturn(unorderedPostResponseMock);
        List<String> ids = new ArrayList<>();
        ids.add("/id");
        service.createOrderedAndUnorderedRelationsForNodes(ids);
        Mockito.verify(targetMock).request();
        Mockito.verify(builderMock).post(null);
        Mockito.verify(unorderedMock).request();
        Mockito.verify(builderUnorderedMock).get();
    }


    /**
     * Test delete one node
     */
    @Test
    public void deleteNodeTest(){
        WebTarget targetMock = Mockito.mock(WebTarget.class);
        Invocation.Builder builderMock = Mockito.mock(Invocation.Builder.class);
        Response postResponseMock = Mockito.mock(Response.class);
        Mockito.when(clientMock.target("/relations/delete/%2Fid")).thenReturn(targetMock);
        Mockito.when(targetMock.request()).thenReturn(builderMock);
        Mockito.when(builderMock.get()).thenReturn(postResponseMock);
        service.deleteNode("/id");
        Mockito.verify(targetMock).request();
        Mockito.verify(builderMock).post(null);
    }

    /**
     * Test delete list of nodes
     */
    @Test
    public void deleteNodesTest(){
        WebTarget targetMock = Mockito.mock(WebTarget.class);
        Invocation.Builder builderMock = Mockito.mock(Invocation.Builder.class);
        Response postResponseMock = Mockito.mock(Response.class);
        Mockito.when(clientMock.target("/relations/delete/%2Fid")).thenReturn(targetMock);
        Mockito.when(targetMock.request()).thenReturn(builderMock);
        Mockito.when(builderMock.get()).thenReturn(postResponseMock);
        List<String> ids = new ArrayList<>();
        ids.add("/id");
        service.deleteNodes(ids);
        Mockito.verify(targetMock).request();
        Mockito.verify(builderMock).post(null);
    }

    /**
     * Test the deletion of a collection
     */
    @Test
    public void deleteByCollectionTest() {
        WebTarget targetMock = Mockito.mock(WebTarget.class);
        Invocation.Builder builderMock = Mockito.mock(Invocation.Builder.class);
        Response getResponseMock = Mockito.mock(Response.class);
        Mockito.when(clientMock.target("/collection/collection")).thenReturn(targetMock);
        Mockito.when(targetMock.request()).thenReturn(builderMock);
        Mockito.when(builderMock.get()).thenReturn(getResponseMock);
        service.deleteByCollection("collection");
        Mockito.verify(targetMock).request();
        Mockito.verify(builderMock).get();
    }

}
