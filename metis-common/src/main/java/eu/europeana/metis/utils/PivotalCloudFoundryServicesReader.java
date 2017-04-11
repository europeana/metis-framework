package eu.europeana.metis.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.mongodb.MongoClientURI;
import eu.europeana.metis.cache.redis.RedisProvider;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-04-10
 */
public class PivotalCloudFoundryServicesReader implements CloudFoundryServicesReader {
  private final String vcapJson;

  public PivotalCloudFoundryServicesReader(String vcapJson) {
    this.vcapJson = vcapJson;
  }

  @Override
  public MongoClientURI getMongoClientUriFromService() {
    JsonParser parser = new JsonParser();
    JsonObject object = parser.parse(vcapJson).getAsJsonObject();
    JsonObject element = object.getAsJsonArray("mlab").get(0).getAsJsonObject();

    JsonObject credentials = element.getAsJsonObject("credentials");
    JsonPrimitive uri = credentials.getAsJsonPrimitive("uri");
    String mongoUri= uri.getAsString();

    return new MongoClientURI(mongoUri);
  }

  @Override
  public RedisProvider getRedisProviderFromService() {
    JsonParser parser = new JsonParser();
    JsonObject object = parser.parse(vcapJson).getAsJsonObject();
    JsonObject redisElement = object.getAsJsonArray("rediscloud").get(0).getAsJsonObject();
    JsonObject redisCredentials = redisElement.getAsJsonObject("credentials");
    String redisHost = redisCredentials.get("hostname").getAsString();
    int redisPort = Integer.parseInt(redisCredentials.get("port").getAsString());
    String redisPassword = redisCredentials.get("password").getAsString();

    return new RedisProvider(redisHost, redisPort, redisPassword);
  }
}
