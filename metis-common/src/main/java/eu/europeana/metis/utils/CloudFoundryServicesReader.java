package eu.europeana.metis.utils;

import com.mongodb.MongoClientURI;
import eu.europeana.metis.cache.redis.RedisProvider;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-04-10
 */
public interface CloudFoundryServicesReader {
  MongoClientURI getMongoClientUriFromService();
  RedisProvider getRedisProviderFromService();
}
