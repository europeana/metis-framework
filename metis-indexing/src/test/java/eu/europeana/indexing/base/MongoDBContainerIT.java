package eu.europeana.indexing.base;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.MongoDBContainer;

/**
 * The type Mongo db container it.
 */
public class MongoDBContainerIT extends TestContainer {

  private static final Logger LOGGER = LoggerFactory.getLogger(MongoDBContainerIT.class);
  private static MongoDBContainer mongoDBContainer;
  /**
   * The constant MONGO_VERSION.
   */
  public static final String MONGO_VERSION = "mongo:5.0.12";

  /**
   * Instantiates a new Mongo db container it.
   */
  public MongoDBContainerIT() {
    mongoDBContainer = new MongoDBContainer(MONGO_VERSION);
    mongoDBContainer.start();

    logConfiguration();
  }

  @Override
  public void logConfiguration() {
    LOGGER.info("MongoDB container created:");
    LOGGER.info("Url: {}", mongoDBContainer.getReplicaSetUrl());
    LOGGER.info("Host: {}", mongoDBContainer.getHost());
    LOGGER.info("Port: {}", mongoDBContainer.getFirstMappedPort());
  }

  @Override
  public void dynamicProperties(DynamicPropertyRegistry registry) {
    registry.add("mongo.application-name", () -> "mongo-testcontainer-test");
    registry.add("mongo.db", () -> "test");
    registry.add("mongo.redirect.db", () -> "test_redirect");
    registry.add("mongo.hosts", mongoDBContainer::getHost);
    registry.add("mongo.port", mongoDBContainer::getFirstMappedPort);
  }

  @Override
  public void runScripts(List<String> scripts) {
    //nothing to do at this moment
  }
}
