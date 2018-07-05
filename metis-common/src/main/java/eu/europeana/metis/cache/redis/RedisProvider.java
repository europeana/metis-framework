package eu.europeana.metis.cache.redis;

import javax.annotation.PreDestroy;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 * Provides a Java Redis (Jedis) implementation for writing items to cache
 *
 * @author Bram Lohman
 */
@Component
public class RedisProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(RedisProvider.class);

  private static final int JEDIS_TIMEOUT = 600_000;

  private static final int TIME_BETWEEN_EVICTION_RUNS_MILLISECONDS = 60_000;

  private static final int TESTS_PER_EVICTION_RUN = 10;

  private static final int MAX_NUMBER_OF_IDLE_CONNECTIONS = 5;

  private JedisPool pool;

  private final String host;
  private final int port;
  private final String password;

  /**
   * Constructor with credentials
   *
   * @param host the host of the redis database
   * @param port the port of the redis database
   * @param password the password used to connect
   */
  public RedisProvider(String host, int port, String password) {
    this.host = host;
    this.port = port;
    this.password = password;
    this.pool = getPool(host, port, password);
  }

  private JedisPool getPool(String host, int port, String password) {
    synchronized (this) {
      if (pool == null || pool.isClosed()) {
        LOGGER.info("Get new pool from Redis Host: {}, port: {}{}", host, port,
            (StringUtils.isNotEmpty(password) ? " using a password." : "."));
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        // 'Borrowed' from http://www.ncolomer.net/2011/07/time-to-redis/
        // Tests whether connection is dead when connection
        // retrieval method is called
        poolConfig.setTestOnBorrow(true);
        /* Some extra configuration */
        // Tests whether connection is dead when returning a
        // connection to the pool
        poolConfig.setTestOnReturn(true);
        // Number of connections to Redis that just sit there
        // and do nothing
        poolConfig.setMaxIdle(MAX_NUMBER_OF_IDLE_CONNECTIONS);
        // Minimum number of idle connections to Redis
        // These can be seen as always open and ready to serve
        poolConfig.setMinIdle(1);
        // Tests whether connections are dead during idle periods
        poolConfig.setTestWhileIdle(true);
        // Maximum number of connections to test in each idle check
        poolConfig.setNumTestsPerEvictionRun(TESTS_PER_EVICTION_RUN);
        // Idle connection checking period
        poolConfig.setTimeBetweenEvictionRunsMillis(TIME_BETWEEN_EVICTION_RUNS_MILLISECONDS);

        // Create the jedisPool
        if (StringUtils.isNotEmpty(password)) {
          pool = new JedisPool(poolConfig, host, port, JEDIS_TIMEOUT, password);
        } else {
          pool = new JedisPool(poolConfig, host, port, JEDIS_TIMEOUT);
        }
      }

      //Check if connection works
      try {
        pool.getResource();
      } catch (JedisConnectionException e) {
        LOGGER.error("Cannot get resource from pool..", e);
      }

      return pool;
    }
  }

  /**
   * Request a jedis resource from the resource pool
   *
   * @return Jedis
   */
  public Jedis getJedis() {
    synchronized (this) {
      LOGGER.info("Requesting a new jedis connection");
      try {
        return pool.getResource();
      } catch (RuntimeException e) {
        LOGGER.error("Cannot get resource from pool..", e);
        close();
        pool = getPool(host, port, password);
        return pool.getResource();
      }
    }
  }

  /**
   * Close method for releasing resources.
   */
  @PreDestroy
  public void close() {
    if (pool != null && !pool.isClosed()) {
      pool.close();
    }
  }
}
