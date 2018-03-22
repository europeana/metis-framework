package eu.europeana.metis.dereference.service.dao;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.metis.dereference.ProcessedEntity;
import redis.clients.jedis.Jedis;


/**
 * DAO for Cache of processed entities (Redis)
 */
public class CacheDao implements AbstractDao<ProcessedEntity> {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(CacheDao.class);
  private Jedis jedis;
  private ObjectMapper om = new ObjectMapper();

  /**
   * Constructor.
   * 
   * @param jedis Client to the Redis database.
   */
  public CacheDao(Jedis jedis) {
    this.jedis = jedis;
  }

  @Override
  public ProcessedEntity get(String resourceId) {
    try {
      String entity = jedis.get(resourceId);
      if (entity != null) {
        return om.readValue(entity, ProcessedEntity.class);
      }
    } catch (IOException e) {
      LOGGER.warn("Unable to read entity " + resourceId, e);
    }
    return null;
  }

  @Override
  public void save(ProcessedEntity entity) {
    try {
      jedis.set(entity.getResourceId(), om.writeValueAsString(entity));
    } catch (JsonProcessingException e) {
      LOGGER.warn("Unable to save entity", e);
    }
  }

  @Override
  public void delete(String resourceId) {
    jedis.del(resourceId);
  }

  @Override
  public void update(String resourceId, ProcessedEntity entity) {
    save(entity);
  }

  /**
   * Empty the cache of processed entities
   */
  public void emptyCache() {
    jedis.flushAll();
  }
}
