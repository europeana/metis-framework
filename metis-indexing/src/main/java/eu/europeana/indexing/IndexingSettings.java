package eu.europeana.indexing;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

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
  private URI zookeeperHost = null;

  // Solr settings
  private final List<URI> solrHosts = new ArrayList<>();
  private String solrCollectionName = null;

  /**
   * Add a mongo host.
   * 
   * @param host Mongo host.
   * @throws IndexerConfigurationException In case the provided value is null.
   */
  public void addMongoHost(InetSocketAddress host) throws IndexerConfigurationException {
    mongoHosts.add(new ServerAddress(nonNull(host, "host")));
  }

  /**
   * Set the Mongo database name.
   * 
   * @param mongoDatabaseName Mongo database name.
   * @throws IndexerConfigurationException In case the provided value is null.
   */
  public void setMongoDatabaseName(String mongoDatabaseName) throws IndexerConfigurationException {
    this.mongoDatabaseName = nonNull(mongoDatabaseName, "mongoDatabaseName");
  }

  /**
   * Set Mongo credentials. By default, there are no credentials set.
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
   * Enable SSL for the Mongo connection. By default this is disabled.
   */
  public void setMongoEnableSsl() {
    this.mongoEnableSsl = true;
  }

  /**
   * Add a Zookeeper host.
   * 
   * @param host Zookeeper host.
   * @throws IndexerConfigurationException In case the provided value is null.
   */
  public void addZookeeperHost(URI host) throws IndexerConfigurationException {
    zookeeperHost = nonNull(host, "host");
  }

  /**
   * Add a Solr host.
   * 
   * @param host Solr host.
   * @throws IndexerConfigurationException In case the provided value is null.
   */
  public void addSolrHost(URI host) throws IndexerConfigurationException {
    solrHosts.add(nonNull(host, "host"));
  }

  /**
   * Set the Solr collection name.
   * 
   * @param solrCollectionName Solr collection name.
   * @throws IndexerConfigurationException
   */
  public void setSolrCollectionName(String solrCollectionName)
      throws IndexerConfigurationException {
    this.solrCollectionName = nonNull(solrCollectionName, "solrCollectionName");
  }

  List<ServerAddress> getMongoHosts() throws IndexerConfigurationException {
    if (mongoHosts.isEmpty()) {
      throw new IndexerConfigurationException("Please provide at least one Mongo host.");
    }
    return Collections.unmodifiableList(mongoHosts);
  }

  String getMongoDatabaseName() throws IndexerConfigurationException {
    if (mongoDatabaseName == null) {
      throw new IndexerConfigurationException("Please provide a Mongo database name.");
    }
    return mongoDatabaseName;
  }

  MongoCredential getMongoCredentials() {
    return mongoCredentials;
  }

  boolean mongoEnableSsl() {
    return mongoEnableSsl;
  }

  URI getZookeeperHost() throws IndexerConfigurationException {
    if (zookeeperHost == null) {
      throw new IndexerConfigurationException("Please provide a Zookeeper host.");
    }
    return zookeeperHost;
  }

  List<URI> getSolrHosts() throws IndexerConfigurationException {
    if (solrHosts.isEmpty()) {
      throw new IndexerConfigurationException("Please provide at least one Solr host.");
    }
    return Collections.unmodifiableList(solrHosts);
  }

  String getSolrCollectionName() throws IndexerConfigurationException {
    if (solrCollectionName == null) {
      throw new IndexerConfigurationException("Please provide a Solr collection name.");
    }
    return solrCollectionName;
  }

  private static <T> T nonNull(T value, String fieldName) throws IndexerConfigurationException {
    if (value == null) {
      throw new IndexerConfigurationException("Value '" + fieldName + "' cannot be null.");
    }
    return value;
  }
}
