package eu.europeana.indexing;

import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import eu.europeana.indexing.exception.SetupRelatedIndexingException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * This class contains all settings needed for indexing. These settings are not thread-safe.
 *
 * @author jochen
 */
public final class IndexingSettings {

  // Default settings
  private static final int DEFAULT_ZOOKEEPER_TIMEOUT_IN_SECONDS = 30;

  // Mongo settings
  private final List<ServerAddress> mongoHosts = new ArrayList<>();
  private String mongoDatabaseName;
  private String recordRedirectDatabaseName;
  private MongoCredential mongoCredentials;
  private boolean mongoEnableSsl;

  // Zookeeper settings
  private final List<InetSocketAddress> zookeeperHosts = new ArrayList<>();
  private String zookeeperChroot;
  private String zookeeperDefaultCollection;
  private Integer zookeeperTimeoutInSecs = DEFAULT_ZOOKEEPER_TIMEOUT_IN_SECONDS;

  // Solr settings
  private final List<URI> solrHosts = new ArrayList<>();

  /**
   * Add a Mongo host. This method must be called at least once.
   *
   * @param host Mongo host.
   * @throws SetupRelatedIndexingException In case the provided value is null.
   */
  public void addMongoHost(InetSocketAddress host) throws SetupRelatedIndexingException {
    mongoHosts.add(new ServerAddress(nonNull(host, "host")));
  }

  /**
   * Set the Mongo database name. This method must be called.
   *
   * @param mongoDatabaseName Mongo database name.
   * @throws SetupRelatedIndexingException In case the provided value is null.
   */
  public void setMongoDatabaseName(String mongoDatabaseName) throws SetupRelatedIndexingException {
    this.mongoDatabaseName = nonNull(mongoDatabaseName, "mongoDatabaseName");
  }

  public void setRecordRedirectDatabaseName(String recordRedirectDatabaseName)
      throws SetupRelatedIndexingException {
    this.recordRedirectDatabaseName = nonNull(recordRedirectDatabaseName, "recordRedirectDatabaseName");
  }

  /**
   * Set Mongo credentials. This method is optional: by default, there are no credentials set.
   *
   * @param username Username.
   * @param password Password.
   * @param authenticationDatabase The authentication database where the user is known.
   * @throws SetupRelatedIndexingException In case any of the provided values are null.
   */
  public void setMongoCredentials(String username, String password, String authenticationDatabase)
      throws SetupRelatedIndexingException {
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
   * @throws SetupRelatedIndexingException In case the provided value is null.
   */
  public void addZookeeperHost(InetSocketAddress host) throws SetupRelatedIndexingException {
    zookeeperHosts.add(nonNull(host, "host"));
  }

  /**
   * Set the Zookeeper chroot (which would apply to all the zookeeper hosts). See the documentation
   * of {@link org.apache.zookeeper.ZooKeeper} constructors, for instance
   * {@link org.apache.zookeeper.ZooKeeper#ZooKeeper(String, int, org.apache.zookeeper.Watcher)}.
   * The chroot must start with a '/' character. This method is optional: by default, there is no
   * chroot. This method has effect only if zookeeper is to be used (i.e. if
   * {@link #addZookeeperHost(InetSocketAddress)} is called).
   *
   * @param chroot The chroot.
   * @throws SetupRelatedIndexingException If the chroot does not start with a '/'.
   */
  public void setZookeeperChroot(String chroot) throws SetupRelatedIndexingException {
    if (StringUtils.isBlank(chroot)) {
      this.zookeeperChroot = null;
    } else if (chroot.charAt(0) == '/') {
      this.zookeeperChroot = chroot;
    } else {
      throw new SetupRelatedIndexingException("A chroot, if provided, must start with '/'.");
    }
  }

  /**
   * Set the Zookeeper default collection name. This method must be called if zookeeper is to be
   * used (i.e. if {@link #addZookeeperHost(InetSocketAddress)} is called).
   *
   * @param zookeeperDefaultCollection Zookeeper default collection. Cannot be null.
   * @throws SetupRelatedIndexingException In case the provided value is null.
   */
  public void setZookeeperDefaultCollection(String zookeeperDefaultCollection)
      throws SetupRelatedIndexingException {
    this.zookeeperDefaultCollection =
        nonNull(zookeeperDefaultCollection, "zookeeperDefaultCollection");
  }

  /**
   * Set the Zookeeper connection time-out . This method is optional: by default, there is no
   * connection time-out. This method has effect only if zookeeper is to be used (i.e. if
   * {@link #addZookeeperHost(InetSocketAddress)} is called).
   *
   * @param zookeeperTimeoutInSecs The time-out (in seconds) to be applied to Zookeeper connections.
   *        If this number is zero or negative, the default value will be applied.
   */
  public void setZookeeperTimeoutInSecs(int zookeeperTimeoutInSecs) {
    this.zookeeperTimeoutInSecs = zookeeperTimeoutInSecs <= 0 ? null : zookeeperTimeoutInSecs;
  }

  /**
   * Add a Solr host. This method must be called at least once.
   *
   * @param host Solr host.
   * @throws SetupRelatedIndexingException In case the provided value is null.
   */
  public void addSolrHost(URI host) throws SetupRelatedIndexingException {
    solrHosts.add(nonNull(host, "host"));
  }

  /**
   * This method returns the list of Mongo hosts.
   *
   * @return The Mongo hosts.
   * @throws SetupRelatedIndexingException In case no such hosts were set.
   */
  public List<ServerAddress> getMongoHosts() throws SetupRelatedIndexingException {
    if (mongoHosts.isEmpty()) {
      throw new SetupRelatedIndexingException("Please provide at least one Mongo host.");
    }
    return Collections.unmodifiableList(mongoHosts);
  }

  /**
   * This method returns the Mongo database name.
   *
   * @return The Mongo database name.
   * @throws SetupRelatedIndexingException In case no Mongo database name was set.
   */
  public String getMongoDatabaseName() throws SetupRelatedIndexingException {
    if (mongoDatabaseName == null) {
      throw new SetupRelatedIndexingException("Please provide a Mongo database name.");
    }
    return mongoDatabaseName;
  }

  /**
   * This method returns the Mongo record redirect database name.
   *
   * @return The Mongo record redirect database name.
   * @throws SetupRelatedIndexingException In case no Mongo record redirect database name was set.
   */
  public String getRecordRedirectDatabaseName() throws SetupRelatedIndexingException {
    if (recordRedirectDatabaseName == null) {
      throw new SetupRelatedIndexingException("Please provide a Mongo record redirect database name.");
    }
    return recordRedirectDatabaseName;
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
    return hasZookeeperConnection() ? zookeeperChroot : null;
  }

  /**
   * This method returns the Zookeeper default collection name.
   *
   * @return The Zookeeper default collection name, or null if no Zookeeper connection is to be
   *         established.
   * @throws SetupRelatedIndexingException In case a Zookeeper connection is to be established, but
   *         no default collection name was set.
   */
  public String getZookeeperDefaultCollection() throws SetupRelatedIndexingException {
    if (!hasZookeeperConnection()) {
      return null;
    }
    if (zookeeperDefaultCollection == null) {
      throw new SetupRelatedIndexingException(
          "Please provide a Zookeeper default collection name.");
    }
    return zookeeperDefaultCollection;
  }

  /**
   * This method returns the Zookeeper connection time-out in seconds.
   *
   * @return The Zookeeper connection time-out in seconds, or null if the default Zookeeper
   *         connection time-out is to be applied (or if no Zookeeper connection is to be
   *         established).
   */
  public Integer getZookeeperTimeoutInSecs() {
    return hasZookeeperConnection() ? zookeeperTimeoutInSecs : null;
  }

  /**
   * This method returns the Solr hosts.
   *
   * @return The solr hosts.
   * @throws SetupRelatedIndexingException In case no such hosts were set.
   */
  public List<URI> getSolrHosts() throws SetupRelatedIndexingException {
    if (solrHosts.isEmpty()) {
      throw new SetupRelatedIndexingException("Please provide at least one Solr host.");
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

  private static <T> T nonNull(T value, String fieldName) throws SetupRelatedIndexingException {
    if (value == null) {
      throw new SetupRelatedIndexingException(
          String.format("Value '%s' cannot be null.", fieldName));
    }
    return value;
  }
}
