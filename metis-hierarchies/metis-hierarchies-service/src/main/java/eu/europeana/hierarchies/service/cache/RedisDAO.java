package eu.europeana.hierarchies.service.cache;

import org.codehaus.jackson.map.ObjectMapper;
import redis.clients.jedis.Jedis;

import javax.inject.Inject;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Redis Implementation of Cache
 * Created by ymamakis on 1/25/16.
 */
public class RedisDAO implements CacheDAO{

    public RedisDAO(){

    }

    public RedisDAO(Jedis jedis){
        this.jedis= jedis;
    }

    @Inject
    private Jedis jedis;
    private ObjectMapper objMapper = new ObjectMapper();


    @Override
    public void addParentToSet(String collection, String parent) throws IOException {
        String entry = jedis.get(collection);
        CacheEntry cache = new CacheEntry();
        Set<String> parents = new HashSet<String>();
        if(entry!=null){
            cache = objMapper.readValue(entry,CacheEntry.class);
            parents = cache.getParents();
        }
        parents.add(parent);
        cache.setParents(parents);
        cache.setCollection(collection);
        jedis.set(collection, objMapper.writeValueAsString(cache));

    }

    @Override
    public void addParentsToSet(String collection, Set<String> parents)throws IOException {
        String entry = jedis.get(collection);
        CacheEntry cache = new CacheEntry();
        Set<String> parentsToAdd = new HashSet<String>();
        if(entry!=null){
            cache = objMapper.readValue(entry,CacheEntry.class);
            parents = cache.getParents();
        }
        parents.addAll(parents);
        cache.setParents(parentsToAdd);
        cache.setCollection(collection);
        jedis.set(collection, objMapper.writeValueAsString(cache));
    }

    @Override
    public CacheEntry getByCollection(String collection) throws IOException {
        String entry = jedis.get(collection);
        if(entry!=null){
            return objMapper.readValue(entry,CacheEntry.class);
        }
        return null;
    }
}
