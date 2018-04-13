package eu.europeana.indexing;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import eu.europeana.indexing.exception.IndexerConfigurationException;

/**
 * This class contains all settings needed for indexing. These settings are not thread-safe.
 * 
 * @author jochen
 */
public final class IndexingSettings {

  // Mongo settings
  private final List<ServerAddress> mongoHosts = new ArrayList<>();
  private String mongoDatabaseName = null;
  private MongoCredential mongoCredentials = null;
  private boolean mongoEnableSsl = false;

  // Zookeeper settings
  private List<InetSocketAddress> zookeeperHosts = new ArrayList<>();
  private String zookeeperChroot = null;
  private String zookeeperDefaultCollection = null;

  // Solr settings
  private final List<URI> solrHosts = new ArrayList<>();

  /**
   * Add a Mongo host. This method must be called at least once.
   * 
   * @param host Mongo host.
   * @throws IndexerConfigurationException In case the provided value is null.
   */
  public void addMongoHost(InetSocketAddress host) throws IndexerConfigurationException {
    mongoHosts.add(new ServerAddress(nonNull(host, "host")));
  }

  /**
   * Set the Mongo database name. This method must be called.
   * 
   * @param mongoDatabaseName Mongo database name.
   * @throws IndexerConfigurationException In case the provided value is null.
   */
  public void setMongoDatabaseName(String mongoDatabaseName) throws IndexerConfigurationException {
    this.mongoDatabaseName = nonNull(mongoDatabaseName, "mongoDatabaseName");
  }

  /**
   * Set Mongo credentials. This method is optional: by default, there are no credentials set.
   * 
   * @param username Username.
   * @param password Password.
   * @param authenticationDatabase The authentication database where the user is known.
   * @throws IndexerConfigurationException In case any of the provided values are null.
   */
  public void setMongoCredentials(String username, String password, String authenticationDatabase)
      throws IndexerConfigurationException {
    this.mongoCredentials = MongoCredential.createCredential(nonNull(username, "username"),
        nonNull(authenticationDatabase, "authenticationDatabase"),
        nonNull(password, "password").toCharArray());
  }

  /**
   * Enable SSL for the Mongo connection. This method is optional: by default this is disabled.
   */
  public void setMongoEnableSsl() {
    this.mongoEnableSsl = true;
  }

  /**
   * Add a Zookeeper host. This method is optional. By default the list is empty, signifying that a
   * direct connection is to be made with Solr (i.e. not via zookeeper). Any value set through
   * {@link #setZookeeperChroot(String)} will be ignored in this case.
   * 
   * @param host Zookeeper host.
   * @throws IndexerConfigurationException In case the provided value is null.
   */
  public void addZookeeperHost(InetSocketAddress host) throws IndexerConfigurationException {
    zookeeperHosts.add(nonNull(host, "host"));
  }

  /**
   * Set the Zookeeper chroot (which would apply to all the zookeeper hosts). See the documentation
   * of {@link org.apache.zookeeper.ZooKeeper} constructors, for instance
   * {@link org.apache.zookeeper.ZooKeeper#ZooKeeper(String, int, org.apache.zookeeper.Watcher)}.
   * The chroot must start with a '/' character. This method is optional: by default, there is no
   * chroot.
   * 
   * @param chroot The chroot.
   * @throws IndexerConfigurationException
   */
  public void setZookeeperChroot(String chroot) throws IndexerConfigurationException {
    if (chroot == null || chroot.trim().isEmpty()) {
      this.zookeeperChroot = null;
    } else if (chroot.startsWith("/")) {
      this.zookeeperChroot = chroot;
    } else {
      throw new IndexerConfigurationException("A chroot, if provided, must start with '/'.");
    }
  }

  /**
   * Set the Zookeeper default collection name. This method must be called if zookeeper is to be
   * used (i.e. if {@link #addZookeeperHost(InetSocketAddress)} is called).
   * 
   * @param zookeeperDefaultCollection Zookeeper default collection. Cannot be null.
   * @throws IndexerConfigurationException
   */
  public void setZookeeperDefaultCollection(String zookeeperDefaultCollection)
      throws IndexerConfigurationException {
    this.zookeeperDefaultCollection =
        nonNull(zookeeperDefaultCollection, "zookeeperDefaultCollection");
  }

  /**
   * Add a Solr host. This method must be called at least once.
   * 
   * @param host Solr host.
   * @throws IndexerConfigurationException In case the provided value is null.
   */
  public void addSolrHost(URI host) throws IndexerConfigurationException {
    solrHosts.add(nonNull(host, "host"));
  }

  /**
   * This method returns the list of Mongo hosts.
   * 
   * @return The Mongo hosts.
   * @throws IndexerConfigurationException In case no such hosts were set.
   */
  public List<ServerAddress> getMongoHosts() throws IndexerConfigurationException {
    if (mongoHosts.isEmpty()) {
      throw new IndexerConfigurationException("Please provide at least one Mongo host.");
    }
    return Collections.unmodifiableList(mongoHosts);
  }

  /**
   * This method returns the Mongo database name.
   * 
   * @return The Mongo database name.
   * @throws IndexerConfigurationException In case no Mongo database name was set.
   */
  public String getMongoDatabaseName() throws IndexerConfigurationException {
    if (mongoDatabaseName == null) {
      throw new IndexerConfigurationException("Please provide a Mongo database name.");
    }
    return mongoDatabaseName;
  }

  /**
   * This method returns the Mongo credentials.
   * 
   * @return The credentials, or null if no such credentials were set.
   */
  public MongoCredential getMongoCredentials() {
    return mongoCredentials;
  }

  /**
   * This method returns whether SSL is to be enabled for the Mongo connection.
   * 
   * @return Whether SSL is to be enabled for the Mongo connection.
   */
  public boolean mongoEnableSsl() {
    return mongoEnableSsl;
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
   *         Zookeeper connection is to be established).
   */
  public String getZookeeperChroot() {
    return establishZookeeperConnection() ? zookeeperChroot : null;
  }

  /**
   * This method returns the Zookeeper default collection name.
   * 
   * @return The Zookeeper default collection name, or null if no Zookeeper connection is to be
   *         established.
   * @throws IndexerConfigurationException In case a Zookeeper connection is to be established, but
   *         no default collection name was set.
   */
  String getZookeeperDefaultCollection() throws IndexerConfigurationException {
    if (!establishZookeeperConnection()) {
      return null;
    }
    if (zookeeperDefaultCollection == null) {
      throw new IndexerConfigurationException(
          "Please provide a Zookeeper default collection name.");
    }
    return zookeeperDefaultCollection;
  }

  /**
   * This method returns the Solr hosts.
   * 
   * @return The solr hosts.
   * @throws IndexerConfigurationException In case no such hosts were set.
   */
  List<URI> getSolrHosts() throws IndexerConfigurationException {
    if (solrHosts.isEmpty()) {
      throw new IndexerConfigurationException("Please provide at least one Solr host.");
    }
    return Collections.unmodifiableList(solrHosts);
  }

  /**
   * This method returns whether or not a Zookeeper connection is to be established.
   * 
   * @return Whether a Zookeeper connection is to be established.
   */
  boolean establishZookeeperConnection() {
    return !zookeeperHosts.isEmpty();
  }

  private static <T> T nonNull(T value, String fieldName) throws IndexerConfigurationException {
    if (value == null) {
      throw new IndexerConfigurationException(
          String.format("Value '%s' cannot be null.", fieldName));
    }
    return value;
  }
}
