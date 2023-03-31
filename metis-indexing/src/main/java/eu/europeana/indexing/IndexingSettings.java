package eu.europeana.indexing;

import static eu.europeana.indexing.utils.IndexingSettingsUtils.nonNullFieldName;
import static eu.europeana.indexing.utils.IndexingSettingsUtils.nonNullMessage;

import com.mongodb.ServerAddress;
import eu.europeana.indexing.exception.SetupRelatedIndexingException;
import eu.europeana.metis.mongo.connection.MongoProperties;
import eu.europeana.metis.mongo.connection.MongoProperties.ReadPreferenceValue;
import eu.europeana.metis.solr.connection.SolrProperties;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * This class contains all settings needed for indexing. These settings are not thread-safe.
 *
 * @author jochen
 */
public final class IndexingSettings {

  // Mongo settings
  private String mongoDatabaseName;
  private String recordRedirectDatabaseName;
  private final MongoProperties<SetupRelatedIndexingException> mongoProperties = new MongoProperties<>(
      SetupRelatedIndexingException::new);

  // Zookeeper settings
  private final SolrProperties<SetupRelatedIndexingException> solrProperties = new SolrProperties<>(
      SetupRelatedIndexingException::new);

  /**
   * Add a Mongo host. This method must be called at least once.
   *
   * @param host Mongo host.
   * @throws SetupRelatedIndexingException In case the provided value is null.
   */
  public void addMongoHost(InetSocketAddress host) throws SetupRelatedIndexingException {
    this.mongoProperties.addMongoHost(host);
  }

  /**
   * Set the Mongo database name. This method must be called.
   *
   * @param mongoDatabaseName Mongo database name.
   * @throws SetupRelatedIndexingException In case the provided value is null.
   */
  public void setMongoDatabaseName(String mongoDatabaseName) throws SetupRelatedIndexingException {
    this.mongoDatabaseName = nonNullFieldName(mongoDatabaseName, "mongoDatabaseName");
  }

  public void setRecordRedirectDatabaseName(String recordRedirectDatabaseName)
      throws SetupRelatedIndexingException {
    this.recordRedirectDatabaseName = nonNullFieldName(recordRedirectDatabaseName,
        "recordRedirectDatabaseName");
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
    this.mongoProperties.setMongoCredentials(username, password, authenticationDatabase);
  }

  /**
   * Enable SSL for the Mongo connection. This method is optional: by default this is disabled.
   */
  public void setMongoEnableSsl() {
    this.mongoProperties.setMongoEnableSsl();
  }

  /**
   * Set the read preference value. Can be null, where then the default applies
   *
   * @param readPreferenceString the read preference value as String
   */
  public void setMongoReadPreference(String readPreferenceString) {
    ReadPreferenceValue readPreferenceValue = null;
    if (StringUtils.isNotBlank(readPreferenceString)) {
      readPreferenceValue = ReadPreferenceValue.valueOf(readPreferenceString);
    }
    this.mongoProperties.setReadPreferenceValue(readPreferenceValue);
  }

  /**
   * Set the application name. Can be null, in which case a default generic application name is
   * to be used.
   *
   * @param applicationName The application name, or null for the default.
   */
  public void setMongoApplicationName(String applicationName) {
    this.mongoProperties.setApplicationName(applicationName);
  }

  /**
   * Set the maximum number of connections pool size in mongo. Can be null, in which case a default
   * number of connections will be used.
   *
   * @param mongoMaxConnectionPoolSize The value as an integer for the number of max connections in mongo
   */
  public void setMongoMaxConnectionPoolSize(Integer mongoMaxConnectionPoolSize){
    this.mongoProperties.setMaxConnectionPoolSize(mongoMaxConnectionPoolSize);
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
    this.solrProperties.addZookeeperHost(host);
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
   * @throws SetupRelatedIndexingException If the chroot does not start with a '/'.
   */
  public void setZookeeperChroot(String chroot) throws SetupRelatedIndexingException {
    this.solrProperties.setZookeeperChroot(chroot);
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
    this.solrProperties.setZookeeperDefaultCollection(zookeeperDefaultCollection);
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
    this.solrProperties.setZookeeperTimeoutInSecs(zookeeperTimeoutInSecs);
  }

  /**
   * Add a Solr host. This method must be called at least once.
   *
   * @param host Solr host.
   * @throws SetupRelatedIndexingException In case the provided value is null.
   */
  public void addSolrHost(URI host) throws SetupRelatedIndexingException {
    this.solrProperties.addSolrHost(host);
  }

  /**
   * This method returns the list of Mongo hosts.
   *
   * @return The Mongo hosts.
   * @throws SetupRelatedIndexingException In case no such hosts were set.
   * @deprecated Use the equivalent method in {@link #getSolrProperties()} instead.
   */
  @Deprecated
  public List<ServerAddress> getMongoHosts() throws SetupRelatedIndexingException {
    return this.mongoProperties.getMongoHosts();
  }

  /**
   * This method returns the Mongo database name.
   *
   * @return The Mongo database name.
   * @throws SetupRelatedIndexingException In case no Mongo database name was set.
   */
  public String getMongoDatabaseName() throws SetupRelatedIndexingException {
    return nonNullMessage(mongoDatabaseName, "Please provide a Mongo database name.");
  }

  /**
   * This method returns the Mongo record redirect database name.
   *
   * @return The Mongo record redirect database name.
   */
  public String getRecordRedirectDatabaseName() {
    return recordRedirectDatabaseName;
  }

  public MongoProperties<SetupRelatedIndexingException> getMongoProperties() {
    return this.mongoProperties;
  }

  /**
   * This method returns the Zookeeper hosts.
   *
   * @return The Zookeeper hosts. Or empty, if no Zookeeper connection is to be used.
   * @deprecated Use the equivalent method in {@link #getSolrProperties()} instead.
   */
  @Deprecated
  public List<InetSocketAddress> getZookeeperHosts() {
    return this.solrProperties.getZookeeperHosts();
  }

  public SolrProperties<SetupRelatedIndexingException> getSolrProperties() {
    return this.solrProperties;
  }
}
