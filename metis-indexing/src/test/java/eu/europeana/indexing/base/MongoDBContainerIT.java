package eu.europeana.indexing.base;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.MongoDBContainer;

public class MongoDBContainerIT extends TestContainer {
  private static final Logger LOGGER = LoggerFactory.getLogger(MongoDBContainerIT.class);
  private static MongoDBContainer mongoDBContainer;
  public static final String MONGO_VERSION = "mongo:5.0.12";

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
    registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    registry.add("spring.data.mongodb.port", mongoDBContainer::getFirstMappedPort);
    registry.add("spring.data.mongodb.host", mongoDBContainer::getHost);
    registry.add("spring.data.mongodb.db", () -> "test");

    registry.add("mongo.application-name", () -> "mongo-testcontainer-test");
    registry.add("mongo.db", () -> "test");
    registry.add("mongo.hosts", mongoDBContainer::getHost);
    registry.add("mongo.port", mongoDBContainer::getFirstMappedPort);
  }

  @Override
  public void runScripts(List<String> scripts) {
    //nothing to do at this moment
  }
}
