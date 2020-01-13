package eu.europeana.metis.solr;

import java.net.InetSocketAddress;
import java.net.URI;
import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.LBHttpSolrClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class can set up and provide a Solr client given the Solr properties.
 *
 * @param <E> The type of exception thrown when the properties are not valid.
 */
public class SolrClientProvider<E extends Exception> {

  private static final Logger LOGGER = LoggerFactory.getLogger(SolrClientProvider.class);

  private final SolrProperties<E> settings;

  /**
   * Constructor.
   *
   * @param properties The properties of the Mongo connection.
   */
  public SolrClientProvider(SolrProperties<E> properties) {
    this.settings = properties;
  }

  /**
   * Creates a Solr client from the properties. This method can be called multiple times and will
   * return a different client each time.
   *
   * @return A Solr client.
   * @throws E In case there is a problem with the supplied properties.
   */
  public CompoundSolrClient createSolrClient() throws E {
    final LBHttpSolrClient httpSolrClient = setUpHttpSolrConnection();
    final CloudSolrClient cloudSolrClient;
    if (settings.hasZookeeperConnection()) {
      cloudSolrClient = setUpCloudSolrConnection(httpSolrClient);
    } else {
      cloudSolrClient = null;
    }
    return new CompoundSolrClient(httpSolrClient, cloudSolrClient);
  }

  private LBHttpSolrClient setUpHttpSolrConnection() throws E {
    final String[] solrHosts =
            settings.getSolrHosts().stream().map(URI::toString).toArray(String[]::new);
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Connecting to Solr hosts: [{}]", String.join(", ", solrHosts));
    }
    return new LBHttpSolrClient.Builder().withBaseSolrUrls(solrHosts).build();
  }

  private CloudSolrClient setUpCloudSolrConnection(LBHttpSolrClient httpSolrClient) throws E {

    // Get information from settings
    final Set<String> hosts = settings.getZookeeperHosts().stream()
            .map(SolrClientProvider::toCloudSolrClientAddressString).collect(Collectors.toSet());
    final String chRoot = settings.getZookeeperChroot();
    final String defaultCollection = settings.getZookeeperDefaultCollection();
    final Integer connectionTimeoutInSecs = settings.getZookeeperTimeoutInSecs();

    // Configure connection builder
    final CloudSolrClient.Builder builder = new CloudSolrClient.Builder();
    builder.withZkHost(hosts);
    if (chRoot != null) {
      builder.withZkChroot(chRoot);
    }
    builder.withLBHttpSolrClient(httpSolrClient);

    // Set up Zookeeper connection
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info(
              "Connecting to Zookeeper hosts: [{}] with chRoot [{}] and default connection [{}]. Connection time-out: {}.",
              String.join(", ", hosts), chRoot, defaultCollection,
              connectionTimeoutInSecs == null ? "default" : (connectionTimeoutInSecs + " seconds"));
    }
    final CloudSolrClient cloudSolrClient = builder.build();
    cloudSolrClient.setDefaultCollection(defaultCollection);
    if (connectionTimeoutInSecs != null) {
      final int timeoutInMillis = (int) Duration.ofSeconds(connectionTimeoutInSecs).toMillis();
      cloudSolrClient.setZkConnectTimeout(timeoutInMillis);
      cloudSolrClient.setZkClientTimeout(timeoutInMillis);
    }
    cloudSolrClient.connect();

    // Done
    return cloudSolrClient;
  }

  /**
   * This utility method converts an address (host plus port) to a string that is accepted by {@link
   * CloudSolrClient}.
   *
   * @param address The address to convert.
   * @return The compliant string.
   */
  static String toCloudSolrClientAddressString(InetSocketAddress address) {
    return address.getHostString() + ":" + address.getPort();
  }
}
