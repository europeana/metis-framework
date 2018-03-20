package eu.europeana.metis.cache.redis;

import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

/**
 * Redis Connection provider
 * Created by gmamakis on 24-1-17.
 */
@Component
public class JedisProviderUtils {

  private final Jedis jedis;

  public JedisProviderUtils(Jedis jedis) {
    this.jedis = jedis;
  }

  /**
   * Get all the values from a given key
   *
   * @param key The key to get the values from
   * @return The list of values associated in this key
   */
  public List<String> getAll(String key) {
    if (jedis.exists(key)) {
      return jedis.hvals(key);
    }
    return Collections.emptyList();
  }

  /**
   * Add a specific value to a keyset
   *
   * @param key The key to access
   * @param field The field to add
   * @param value The value for the field
   */
  public void set(String key, String field, String value) {
    jedis.hset(key, field, value);
  }

  /**
   * Remove a specific field from a key
   *
   * @param key The key to access
   * @param field The field to remove
   */
  public void remove(String key, String field) {
    jedis.hdel(key, field);
  }
}
