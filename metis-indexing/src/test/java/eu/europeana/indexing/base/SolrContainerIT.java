package eu.europeana.indexing.base;

import eu.europeana.metis.utils.TempFileUtils;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.UrlResource;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.images.builder.ImageFromDockerfile;

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

  private static final String GITHUB_RAW_BASE_URL = "https://raw.githubusercontent.com/europeana/search/refs/heads/master/solr_confs/metadata/conf/";
  private static final String QUERY_ALIASES_FILE = "query_aliases.xml";
  private static final String GITHUB_RAW_QUERY_ALIASES_CONFIG_URL = GITHUB_RAW_BASE_URL + QUERY_ALIASES_FILE;
  private static final String GITHUB_RAW_ENUMS_CONFIG_URL = GITHUB_RAW_BASE_URL + "enumsConfig.xml";
  private static final String GITHUB_RAW_ELEVATE_CONFIG_URL = GITHUB_RAW_BASE_URL + "elevate.xml";
  private static final String GITHUB_RAW_SOLR_SCHEMA_URL = GITHUB_RAW_BASE_URL + "schema.xml";
  private static final String GITHUB_RAW_SOLR_CONFIG_URL = GITHUB_RAW_BASE_URL + "solrconfig.xml";
  private static final String ALIASING_LIB_JAR = "europeana-collection-aliasing-solr6.6.5-0.0.1-SNAPSHOT.jar";
  private static final String ALIASING_LIB_CONTAINER_PATH = "/opt/solr/contrib/lib/" + ALIASING_LIB_JAR;
  private static final String ALIASING_LIB_RESOURCE_PATH = "solr/"+ ALIASING_LIB_JAR;
  private static final String QUERY_ALIASES_PATH = "/opt/solr/server/solr/solr_publish_test_shard1_replica_n1/conf/" + QUERY_ALIASES_FILE;
  private static final String FILE_PERMISSIONS = "644";
  private static final String OWNERSHIP_COMMAND = "chown -R solr:solr /opt/solr/server/solr && chown -R solr:solr /opt/solr/contrib/lib";
  /**
   * Instantiates a new Solr container it.
   */
  public SolrContainerIT() {
    try {
      File temp = TempFileUtils.createSecureTempFile("query_aliases", ".xml").toFile();
      FileUtils.copyURLToFile(new UrlResource(URI.create(GITHUB_RAW_QUERY_ALIASES_CONFIG_URL)).getURL(), temp);
      ImageFromDockerfile image = new ImageFromDockerfile()
          .withDockerfileFromBuilder(builder ->
              builder.from(SOLR_VERSION)
                     .user("root")
                     .copy(ALIASING_LIB_JAR, ALIASING_LIB_CONTAINER_PATH)
                     .copy(QUERY_ALIASES_FILE, QUERY_ALIASES_PATH)
                     .run(OWNERSHIP_COMMAND)
                     .user("solr")
                     .build())
          .withFileFromFile(QUERY_ALIASES_FILE, temp, Integer.parseInt(FILE_PERMISSIONS, 8))
          .withFileFromClasspath(ALIASING_LIB_JAR, ALIASING_LIB_RESOURCE_PATH);

      solrContainer = new SolrMetisContainer(image)
          .withCollection(SOLR_COLLECTION_NAME)
          .withConfiguration("test", new UrlResource(URI.create(GITHUB_RAW_SOLR_CONFIG_URL)).getURL())
          .withSchema(new UrlResource(URI.create(GITHUB_RAW_SOLR_SCHEMA_URL)).getURL())
          .withConfigSchemaFiles(List.of(new UrlResource(URI.create(GITHUB_RAW_ENUMS_CONFIG_URL)).getURL(),
              new UrlResource(URI.create(GITHUB_RAW_QUERY_ALIASES_CONFIG_URL)).getURL(),
              new UrlResource(URI.create(GITHUB_RAW_ELEVATE_CONFIG_URL)).getURL()));

    } catch (MalformedURLException e) {
      throw new RuntimeException("Failed to create SolrMetisContainer due to malformed URL", e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

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
