package eu.europeana.metis.dereference.service.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.metis.dereference.ProcessedEntity;
import redis.clients.jedis.Jedis;

import java.io.IOException;


/**
 * DAO for Cache (Redis)
 * Created by ymamakis on 2/11/16.
 */

public class CacheDao implements AbstractDao<ProcessedEntity> {
    private Jedis jedis;
    private ObjectMapper om = new ObjectMapper();



    public CacheDao(Jedis jedis){
        this.jedis = jedis;
    }

    @Override
    public ProcessedEntity getByUri(String uri) {
        try {
            String entity = jedis.get(uri);
            if(entity!=null) {
                return om.readValue(entity, ProcessedEntity.class);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void save(ProcessedEntity entity) {
        try {
            jedis.set(entity.getURI(), om.writeValueAsString(entity));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(String uri) {
        jedis.del(uri);
    }

    @Override
    public void update(String uri, ProcessedEntity entity) {
        save(entity);
    }

    /**
     * Empty the cache of processed entities
     */
    public void emptyCache(){
        jedis.flushAll();
    }
}
