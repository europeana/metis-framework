package eu.europeana.metis.solr;

import eu.europeana.metis.network.InetAddressUtil;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;

/**
 * This class holds all the properties that are required to set up a Solr connection.
 *
 * @param <E> The type of exception thrown when the properties are not valid.
 */
public class SolrProperties<E extends Exception> {

  // Default settings
  private static final int DEFAULT_ZOOKEEPER_TIMEOUT_IN_SECONDS = 30;

  // Exception creator
  private final Function<String, E> exceptionCreator;

  // Zookeeper settings
  private final List<InetSocketAddress> zookeeperHosts = new ArrayList<>();
  private String zookeeperChroot;
  private String zookeeperDefaultCollection;
  private Integer zookeeperTimeoutInSecs = DEFAULT_ZOOKEEPER_TIMEOUT_IN_SECONDS;

  // Solr settings
  private final List<URI> solrHosts = new ArrayList<>();

  /**
   * Constructor.
   *
   * @param exceptionCreator Function for creating exception from a given message.
   */
  public SolrProperties(Function<String, E> exceptionCreator) {
    this.exceptionCreator = exceptionCreator;
  }

  /**
   * Setter for all properties. This is a convenience method for {@link
   * #addZookeeperHost(InetSocketAddress)}. It clears the list of hosts (removing all added hosts)
   * and adds the given ones.
   *
   * @param hosts The hosts. This cannot be null.
   * @param ports The ports. This cannot be null. Must contain either the same number of elements as
   * the hosts array, or exactly 1 element (which will then apply to all hosts).
   * @throws E In case either of the arrays is null, or their lengths don't match.
   */
  public void setZookeeperHosts(String[] hosts, int[] ports) throws E {
    final List<InetSocketAddress> addresses = new InetAddressUtil<>(this.exceptionCreator)
            .getAddressesFromHostsAndPorts(nonNull(hosts, "hosts"), nonNull(ports, "ports"));
    zookeeperHosts.clear();
    for (InetSocketAddress address : nonNull(addresses, "addresses")) {
      addZookeeperHost(address);
    }
  }

  /**
   * Add a Zookeeper host. This method is optional. By default the list is empty, signifying that a
   * direct connection is to be made with Solr (i.e. not via zookeeper). Any value set through
   * {@link #setZookeeperChroot(String)} will be ignored in this case.
   *
   * @param host Zookeeper host.
   * @throws E In case the provided value is null.
   */
  public void addZookeeperHost(InetSocketAddress host) throws E {
    zookeeperHosts.add(nonNull(host, "host"));
  }

  /**
   * Set the Zookeeper chroot (which would apply to all the zookeeper hosts). See the documentation
   * of {@link org.apache.zookeeper.ZooKeeper} constructors, for instance {@link
   * org.apache.zookeeper.ZooKeeper#ZooKeeper(String, int, org.apache.zookeeper.Watcher)}. The
   * chroot must start with a '/' character. This method is optional: by default, there is no
   * chroot. This method has effect only if zookeeper is to be used (i.e. if {@link
   * #addZookeeperHost(InetSocketAddress)} is called).
   *
   * @param chroot The chroot.
   * @throws E If the chroot does not start with a '/'.
   */
  public void setZookeeperChroot(String chroot) throws E {
    if (StringUtils.isBlank(chroot)) {
      this.zookeeperChroot = null;
    } else if (chroot.charAt(0) == '/') {
      this.zookeeperChroot = chroot;
    } else {
      throw exceptionCreator.apply("A chroot, if provided, must start with '/'.");
    }
  }

  /**
   * Set the Zookeeper default collection name. This method must be called if zookeeper is to be
   * used (i.e. if {@link #addZookeeperHost(InetSocketAddress)} is called).
   *
   * @param zookeeperDefaultCollection Zookeeper default collection. Cannot be null.
   * @throws E In case the provided value is null.
   */
  public void setZookeeperDefaultCollection(String zookeeperDefaultCollection) throws E {
    this.zookeeperDefaultCollection =
            nonNull(zookeeperDefaultCollection, "zookeeperDefaultCollection");
  }

  /**
   * Set the Zookeeper connection time-out . This method is optional: by default, there is no
   * connection time-out. This method has effect only if zookeeper is to be used (i.e. if {@link
   * #addZookeeperHost(InetSocketAddress)} is called).
   *
   * @param zookeeperTimeoutInSecs The time-out (in seconds) to be applied to Zookeeper connections.
   * If this number is zero or negative, the default value will be applied.
   */
  public void setZookeeperTimeoutInSecs(int zookeeperTimeoutInSecs) {
    this.zookeeperTimeoutInSecs = zookeeperTimeoutInSecs <= 0 ? null : zookeeperTimeoutInSecs;
  }

  /**
   * Add a Solr host. This method must be called at least once.
   *
   * @param host Solr host.
   * @throws E In case the provided value is null.
   */
  public void addSolrHost(URI host) throws E {
    solrHosts.add(nonNull(host, "host"));
  }

  private <T> T nonNull(T value, String fieldName) throws E {
    if (value == null) {
      throw exceptionCreator.apply(String.format("Value '%s' cannot be null.", fieldName));
    }
    return value;
  }

  /**
   * This method returns the Zookeeper hosts.
   *
   * @return The Zookeeper hosts. Or empty, if no Zookeeper connection is to be used.
   */
  public List<InetSocketAddress> getZookeeperHosts() {
    return Collections.unmodifiableList(zookeeperHosts);
  }

  /**
   * This method returns the Zookeeper chroot.
   *
   * @return The Zookeeper chroot, or null if no Zookeeper chroot is to be applied (or if no
   * Zookeeper connection is to be established).
   */
  public String getZookeeperChroot() {
    return hasZookeeperConnection() ? zookeeperChroot : null;
  }

  /**
   * This method returns the Zookeeper default collection name.
   *
   * @return The Zookeeper default collection name, or null if no Zookeeper connection is to be
   * established.
   * @throws E In case a Zookeeper connection is to be established, but no default collection name
   * was set.
   */
  public String getZookeeperDefaultCollection() throws E {
    if (!hasZookeeperConnection()) {
      return null;
    }
    if (zookeeperDefaultCollection == null) {
      throw exceptionCreator.apply("Please provide a Zookeeper default collection name.");
    }
    return zookeeperDefaultCollection;
  }

  /**
   * This method returns the Zookeeper connection time-out in seconds.
   *
   * @return The Zookeeper connection time-out in seconds, or null if the default Zookeeper
   * connection time-out is to be applied (or if no Zookeeper connection is to be established).
   */
  public Integer getZookeeperTimeoutInSecs() {
    return hasZookeeperConnection() ? zookeeperTimeoutInSecs : null;
  }

  /**
   * This method returns the Solr hosts.
   *
   * @return The solr hosts.
   * @throws E In case no such hosts were set.
   */
  public List<URI> getSolrHosts() throws E {
    if (solrHosts.isEmpty()) {
      throw exceptionCreator.apply("Please provide at least one Solr host.");
    }
    return Collections.unmodifiableList(solrHosts);
  }

  /**
   * This method returns whether or not a Zookeeper connection is to be established.
   *
   * @return Whether a Zookeeper connection is to be established.
   */
  public boolean hasZookeeperConnection() {
    return !zookeeperHosts.isEmpty();
  }
}
