package eu.europeana.metis.framework.cache;

import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import java.util.List;

/**
 * Redis Connection provider
 * Created by gmamakis on 24-1-17.
 */
@Component
public class JedisProvider {
    private Jedis jedis;

    /**
     * Default constructor for Redis
     * @param hostname Hostname
     * @param password Password
     * @param port Port
     */
    public JedisProvider(String hostname,String password, int port){
        jedis = new Jedis(hostname,port);
        jedis.auth(password);
        jedis.connect();

    }

    /**
     * Get all the values from a given key
     * @param key The key to get the values from
     * @return The list of values associated in this key
     */
    public List<String> getAll(String key){
        return jedis.hvals(key);
    }

    /**
     * Add a specific value to a keyset
     * @param key The key to access
     * @param field The field to add
     * @param value The value for the field
     */
    public void set(String key, String field, String value){
        jedis.hset(key,field,value);
    }

    /**
     * Remove a specific field from a key
     * @param key The key to access
     * @param field  The field to remove
     */
    public void remove(String key, String field){
        jedis.hdel(key,field);
    }
}
