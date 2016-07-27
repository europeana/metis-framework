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
package eu.europeana.metis.dereference.service.utils;

import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Provides a Java Redis (Jedis) implementation for writing items to cache
 * 
 * @author Bram Lohman
 */
@Component
public class RedisProvider {

	JedisPool pool;

	public RedisProvider(String host, int port, String password) {
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		System.out.println("host:"+host+port+password);
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
		poolConfig.setMaxIdle(5);
		// Minimum number of idle connections to Redis
		// These can be seen as always open and ready to serve
		poolConfig.setMinIdle(1);
		// Tests whether connections are dead during idle periods
		poolConfig.setTestWhileIdle(true);
		// Maximum number of connections to test in each idle check
		poolConfig.setNumTestsPerEvictionRun(10);
		// Idle connection checking period
		poolConfig.setTimeBetweenEvictionRunsMillis(60000);

		// Create the jedisPool
		pool = new JedisPool(poolConfig, host, port, 300, password);
	}

	/**
	 * Request a jedis resource from the resource pool 
	 * 
	 * @return Jedis
	 */
	public Jedis getJedis() {
		Jedis jedis = pool.getResource();
		return jedis;
	}
	
	/**
	 * Return the jedis resource to the resource pool
	 * @param jedis The jedis resource
	 */
	public void returnJedis(Jedis jedis) {
        pool.returnResource(jedis);
    }

}
