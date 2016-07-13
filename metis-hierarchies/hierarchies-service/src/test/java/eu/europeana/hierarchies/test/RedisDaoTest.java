package eu.europeana.hierarchies.test;

import eu.europeana.hierarchies.service.cache.CacheEntry;
import eu.europeana.hierarchies.service.cache.RedisDAO;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by ymamakis on 1/29/16.
 */
public class RedisDaoTest {

    private Jedis jedis = Mockito.mock(Jedis.class);
    private RedisDAO redisDAO;
    @Before
    public void prepare(){
        redisDAO = new RedisDAO(jedis);
    }

    @Test
    public void testGetByCollection() throws IOException {
        CacheEntry cacheEntry = new CacheEntry();
        Set<String> parents = new HashSet<>();
        parents.add("parent1");
        parents.add("parent2");
        parents.add("parent3");
        cacheEntry.setParents(parents);
        Mockito.when(jedis.get("collection")).thenReturn(new ObjectMapper().writeValueAsString(cacheEntry));
        CacheEntry entry = redisDAO.getByCollection("collection");
        Assert.assertNotNull(entry.getParents());
        Assert.assertTrue(entry.getParents().size()==3);
        Assert.assertTrue(entry.getParents().contains("parent1"));
    }

    @Test
    public void getByCollectionNull() throws IOException {
        Mockito.when(jedis.get("collection")).thenReturn(null);
        CacheEntry entry = redisDAO.getByCollection("collection");
        Assert.assertNull(entry);
    }

    @Test
    public void testAddParentToSet() throws IOException {
        CacheEntry entry = new CacheEntry();
        Set<String> parents = new HashSet<>();
        parents.add("parent1");
        entry.setParents(parents);
        Mockito.when(jedis.get("collection")).thenReturn(new ObjectMapper().writeValueAsString(entry));

        Mockito.when(jedis.set("collection",new ObjectMapper().writeValueAsString(entry))).thenAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                return null;
            }
        });
        redisDAO.addParentToSet("collection","parent2");
    }

    @Test
    public void testAddParentToSetNull() throws IOException {
        CacheEntry entry = new CacheEntry();
        Set<String> parents = new HashSet<>();
        parents.add("parent1");
        entry.setParents(parents);
        Mockito.when(jedis.get("collection")).thenReturn(null);

        Mockito.when(jedis.set("collection",new ObjectMapper().writeValueAsString(entry))).thenAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                return null;
            }
        });
        redisDAO.addParentToSet("collection","parent1");
    }

    @Test
    public void testAddParentsToSet() throws IOException {
        CacheEntry entry = new CacheEntry();
        Set<String> parents = new HashSet<>();
        parents.add("parent1");
        entry.setParents(parents);
        Mockito.when(jedis.get("collection")).thenReturn(new ObjectMapper().writeValueAsString(entry));

        Mockito.when(jedis.set("collection",new ObjectMapper().writeValueAsString(entry))).thenAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                return null;
            }
        });
        Set<String> toAdd = new HashSet<>();
        toAdd.add("parent2");
        redisDAO.addParentsToSet("collection",toAdd);
    }

    @Test
    public void testAddParentsToSetNull() throws IOException {
        CacheEntry entry = new CacheEntry();
        Set<String> parents = new HashSet<>();
        parents.add("parent1");
        entry.setParents(parents);
        Mockito.when(jedis.get("collection")).thenReturn(null);

        Mockito.when(jedis.set("collection",new ObjectMapper().writeValueAsString(entry))).thenAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                return null;
            }
        });
        Set<String> toAdd = new HashSet<>();
        toAdd.add("parent2");
        redisDAO.addParentsToSet("collection",toAdd);
    }

    @Test
    public void testAddParentsToSetWithEmptySet() throws IOException {
        CacheEntry entry = new CacheEntry();
        Set<String> parents = new HashSet<>();
        parents.add("parent1");
        entry.setParents(parents);
        Mockito.when(jedis.get("collection")).thenReturn(null);

        Mockito.when(jedis.set("collection",new ObjectMapper().writeValueAsString(entry))).thenAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                return null;
            }
        });
        Set<String> toAdd = new HashSet<>();

        redisDAO.addParentsToSet("collection",toAdd);
    }
}
