package eu.europeana.indexing.base;

/**
 * The type Test container factory it.
 */
public class TestContainerFactoryIT {

  /**
   * Gets container.
   *
   * @param type the type
   * @return the container
   */
  public static TestContainer getContainer(TestContainerType type) {
    return switch (type) {
      case MONGO -> new MongoDBContainerIT();
      case SOLR -> new SolrContainerIT();
    };
  }
}
