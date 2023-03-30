package eu.europeana.indexing.base;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.utility.DockerImageName;

/**
 * The type Solr container it.
 */
public class SolrContainerIT extends TestContainer {

  private static final Logger LOGGER = LoggerFactory.getLogger(SolrContainerIT.class);

  private static SolrMetisContainer solrContainer;
  /**
   * The constant SOLR_VERSION.
   */
  public static final String SOLR_VERSION = "solr:7.7.3-slim";
  /**
   * The constant SOLR_COLLECTION_NAME.
   */
  public static final String SOLR_COLLECTION_NAME = "solr_publish_test";


  /**
   * Instantiates a new Solr container it.
   */
  public SolrContainerIT() {
    solrContainer = new SolrMetisContainer(DockerImageName.parse(SOLR_VERSION))
        .withCollection(SOLR_COLLECTION_NAME)
        .withConfiguration("test",
            getClass().getClassLoader().getResource("solr/solrconfig.xml"))
        //At the moment this container doesn't support solr.AliasingSearchHandler is a custom plugin
        .withSchema(getClass().getClassLoader().getResource("solr/schema.xml"))
        .withConfigSchemaFiles(List.of(getClass().getClassLoader().getResource("solr/enumsConfig.xml"),
            getClass().getClassLoader().getResource("solr/query_aliases.xml"),
            getClass().getClassLoader().getResource("solr/elevate.xml")));

    solrContainer.start();

    logConfiguration();
  }

  @Override
  public void logConfiguration() {
    LOGGER.info("Solr container created:");
    LOGGER.info("Host: {}", solrContainer.getHost());
    LOGGER.info("Port: {}", solrContainer.getSolrPort());
  }

  @Override
  public void dynamicProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.solr.port", solrContainer::getSolrPort);
    registry.add("spring.data.solr.host", solrContainer::getHost);

    String solrUrlHost =
        "http://" + solrContainer.getHost() + ":" + solrContainer.getSolrPort() + "/solr/" + SOLR_COLLECTION_NAME;

    registry.add("metis.test.publish.solr.hosts", () -> solrUrlHost);
  }

  @Override
  public void runScripts(List<String> scripts) {
    //nothing to do at this moment
  }
}
