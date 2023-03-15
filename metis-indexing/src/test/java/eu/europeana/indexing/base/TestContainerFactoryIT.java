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
    switch (type) {
      case MONGO:
        return new MongoDBContainerIT();
      case SOLR:
        return new SolrContainerIT();
      default:
        throw new IllegalArgumentException("Pass a valid container type");
    }
  }
}
