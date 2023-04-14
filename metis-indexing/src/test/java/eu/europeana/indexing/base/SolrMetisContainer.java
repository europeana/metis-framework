package eu.europeana.indexing.base;

import com.github.dockerjava.api.command.InspectContainerResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.SolrClientUtils;
import org.testcontainers.containers.SolrClientUtilsException;
import org.testcontainers.containers.SolrContainerConfiguration;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.shaded.okhttp3.HttpUrl;
import org.testcontainers.shaded.okhttp3.MediaType;
import org.testcontainers.shaded.okhttp3.OkHttpClient;
import org.testcontainers.shaded.okhttp3.Request;
import org.testcontainers.shaded.okhttp3.RequestBody;
import org.testcontainers.shaded.okhttp3.Response;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;
import org.testcontainers.utility.DockerImageName;


/**
 * The type Solr metis container.
 */
public class SolrMetisContainer extends GenericContainer<SolrMetisContainer> {

  /**
   * The constant DEFAULT_TAG.
   */
  public static final String DEFAULT_TAG = "7.7.3";
  /**
   * The constant ZOOKEEPER_PORT.
   */
  public static final Integer ZOOKEEPER_PORT;
  /**
   * The constant SOLR_PORT.
   */
  public static final Integer SOLR_PORT;
  private static final DockerImageName DEFAULT_IMAGE_NAME = DockerImageName.parse("solr");
  private static final OkHttpClient httpClient = new OkHttpClient();

  static {
    ZOOKEEPER_PORT = 9983;
    SOLR_PORT = 8983;
  }

  private final SolrContainerConfiguration configuration;
  private ArrayList<URL> configSchemaFiles;

  /**
   * Instantiates a new Solr metis container.
   *
   * @param dockerImageName the docker image name
   */
  public SolrMetisContainer(DockerImageName dockerImageName) {
    super(dockerImageName);
    dockerImageName.assertCompatibleWith(DEFAULT_IMAGE_NAME);
    this.waitStrategy = (new LogMessageWaitStrategy()).withRegEx(".*o\\.e\\.j\\.s\\.Server Started.*").withStartupTimeout(
        Duration.of(60L, ChronoUnit.SECONDS));
    this.configuration = new SolrContainerConfiguration();
  }

  /**
   * Upload configuration.
   *
   * @param hostname the hostname
   * @param port the port
   * @param configurationName the configuration name
   * @param solrConfig the solr config
   * @param solrSchema the solr schema
   * @param configSchemaFiles the config schema files
   * @throws URISyntaxException the uri syntax exception
   * @throws IOException the io exception
   */
  public static void uploadConfiguration(String hostname, int port, String configurationName, URL solrConfig, URL solrSchema,
      List<URL> configSchemaFiles) throws URISyntaxException, IOException {
    Map<String, String> parameters = new HashMap();
    parameters.put("action", "UPLOAD");
    parameters.put("name", configurationName);
    HttpUrl url = generateSolrURL(hostname, port, Arrays.asList("admin", "configs"), parameters);
    byte[] configurationZipFile = generateConfigZipFile(solrConfig, solrSchema, configSchemaFiles);
    executePost(url, configurationZipFile);
  }

  private static void executePost(HttpUrl url, byte[] data) throws IOException {
    RequestBody requestBody = data == null ? RequestBody.create(MediaType.parse("text/plain"), "")
        : RequestBody.create(MediaType.parse("application/octet-stream"), data);
    Request request = (new Request.Builder()).url(url).post(requestBody).build();
    Response response = httpClient.newCall(request).execute();
    if (!response.isSuccessful()) {
      String responseBody = "";
      if (response.body() != null) {
        responseBody = response.body().string();
        response.close();
      }

      throw new SolrClientUtilsException(response.code(), "Unable to upload binary\n" + responseBody);
    } else {
      if (response.body() != null) {
        response.close();
      }

    }
  }

  private static HttpUrl generateSolrURL(String hostname, int port, List<String> pathSegments, Map<String, String> parameters) {
    HttpUrl.Builder builder = new HttpUrl.Builder();
    builder.scheme("http");
    builder.host(hostname);
    builder.port(port);
    builder.addPathSegment("solr");
    if (pathSegments != null) {
      pathSegments.forEach(builder::addPathSegment);
    }

    parameters.forEach(builder::addQueryParameter);
    return builder.build();
  }

  private static byte[] generateConfigZipFile(URL solrConfiguration, URL solrSchema, List<URL> configFiles) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ZipOutputStream zipOutputStream = new ZipOutputStream(bos);
    zipOutputStream.putNextEntry(new ZipEntry("solrconfig.xml"));
    IOUtils.copy(solrConfiguration.openStream(), zipOutputStream);
    zipOutputStream.closeEntry();
    if (solrSchema != null) {
      zipOutputStream.putNextEntry(new ZipEntry("schema.xml"));
      IOUtils.copy(solrSchema.openStream(), zipOutputStream);
      zipOutputStream.closeEntry();
    }
    if (configFiles != null) {
      configFiles.stream().forEach(url -> {
        try {
          zipOutputStream.putNextEntry(new ZipEntry(Paths.get(url.getPath()).getFileName().toString()));
          IOUtils.copy(url.openStream(), zipOutputStream);
          zipOutputStream.closeEntry();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      });
    }

    zipOutputStream.close();
    return bos.toByteArray();
  }

  /**
   * With zookeeper solr metis container.
   *
   * @param zookeeper the zookeeper
   * @return the solr metis container
   */
  public SolrMetisContainer withZookeeper(boolean zookeeper) {
    this.configuration.setZookeeper(zookeeper);
    return this.self();
  }

  /**
   * With collection solr metis container.
   *
   * @param collection the collection
   * @return the solr metis container
   */
  public SolrMetisContainer withCollection(String collection) {
    if (StringUtils.isEmpty(collection)) {
      throw new IllegalArgumentException("Collection name must not be empty");
    } else {
      this.configuration.setCollectionName(collection);
      return this.self();
    }
  }

  /**
   * With configuration solr metis container.
   *
   * @param name the name
   * @param solrConfig the solr config
   * @return the solr metis container
   */
  public SolrMetisContainer withConfiguration(String name, URL solrConfig) {
    if (!StringUtils.isEmpty(name) && solrConfig != null) {
      this.configuration.setConfigurationName(name);
      this.configuration.setSolrConfiguration(solrConfig);
      return this.self();
    } else {
      throw new IllegalArgumentException();
    }
  }

  /**
   * With schema solr metis container.
   *
   * @param schema the schema
   * @return the solr metis container
   */
  public SolrMetisContainer withSchema(URL schema) {
    this.configuration.setSolrSchema(schema);
    return this.self();
  }

  /**
   * With config schema files solr metis container.
   *
   * @param configFiles the config files
   * @return the solr metis container
   */
  public SolrMetisContainer withConfigSchemaFiles(List<URL> configFiles) {
    this.configSchemaFiles = new ArrayList<>(configFiles);
    return this.self();
  }

  /**
   * Gets solr port.
   *
   * @return the solr port
   */
  public int getSolrPort() {
    return this.getMappedPort(SOLR_PORT);
  }

  /**
   * Gets zookeeper port.
   *
   * @return the zookeeper port
   */
  public int getZookeeperPort() {
    return this.getMappedPort(ZOOKEEPER_PORT);
  }

  @Override
  public Set<Integer> getLivenessCheckPortNumbers() {
    return new HashSet(this.getSolrPort());
  }

  @Override
  protected void containerIsStarted(InspectContainerResponse containerInfo) {
    try {
      if (!this.configuration.isZookeeper()) {
        Container.ExecResult result = this.execInContainer(
            "solr", "create_core", "-c", this.configuration.getCollectionName());
        if (result.getExitCode() != 0) {
          throw new IllegalStateException(
              "Unable to create solr core:\nStdout: " + result.getStdout() + "\nStderr:" + result.getStderr());
        }
      } else {
        if (StringUtils.isNotEmpty(this.configuration.getConfigurationName())) {
          uploadConfiguration(this.getHost(), this.getSolrPort(), this.configuration.getConfigurationName(),
              this.configuration.getSolrConfiguration(), this.configuration.getSolrSchema(), this.configSchemaFiles);
        }

        SolrClientUtils.createCollection(this.getHost(), this.getSolrPort(), this.configuration.getCollectionName(),
            this.configuration.getConfigurationName());
      }
    } catch (Throwable containerThrowable) {
      throw new RuntimeException(containerThrowable);
    }
  }

  @Override
  protected void configure() {
    try {
      if (this.configuration.getSolrSchema() != null && this.configuration.getSolrConfiguration() == null) {
        throw new IllegalStateException("Solr needs to have a configuration is you want to use a schema");
      } else {
        String command = "solr -f";
        this.addExposedPort(SOLR_PORT);
        if (this.configuration.isZookeeper()) {
          this.addExposedPort(ZOOKEEPER_PORT);
          command = "-DzkRun -h localhost";
        }

        this.setCommand(command);
      }
    } catch (Throwable configureThrowable) {
      throw configureThrowable;
    }
  }

  @Override
  protected void waitUntilContainerStarted() {
    this.getWaitStrategy().waitUntilReady(this);
  }
}
