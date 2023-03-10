package eu.europeana.indexing.base;

public class TestContainerFactoryIT {

  public static TestContainer getContainer(TestContainerType type) {
    switch(type) {
      case MONGO:
        return new MongoDBContainerIT();
      case SOLR:
        return new SolrContainerIT();
      default:
        throw new IllegalArgumentException("Pass a valid container type");
    }
  }
}
