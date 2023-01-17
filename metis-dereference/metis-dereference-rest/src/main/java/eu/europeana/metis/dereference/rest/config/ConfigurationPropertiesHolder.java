package eu.europeana.metis.dereference.rest.config;

import eu.europeana.metis.mongo.connection.MongoProperties;
import eu.europeana.metis.mongo.connection.MongoProperties.ReadPreferenceValue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * Class that is used to read all configuration properties for the application.
 * <p>
 * It uses {@link PropertySource} to identify the properties on application startup
 * </p>
 */
@Component
public class ConfigurationPropertiesHolder {

  //Socks proxy
  @Value("${socks.proxy.enabled}")
  private boolean socksProxyEnabled;
  @Value("${socks.proxy.host}")
  private String socksProxyHost;
  @Value("${socks.proxy.port}")
  private String socksProxyPort;
  @Value("${socks.proxy.username}")
  private String socksProxyUsername;
  @Value("${socks.proxy.password}")
  private String socksProxyPassword;

  //Custom trustore
  @Value("${truststore.path}")
  private String truststorePath;
  @Value("${truststore.password}")
  private String truststorePassword;

  //Mongo
  @Value("${mongo.hosts}")
  private String[] mongoHosts;
  @Value("${mongo.port}")
  private int[] mongoPorts;
  @Value("${mongo.username}")
  private String mongoUsername;
  @Value("${mongo.password}")
  private String mongoPassword;
  @Value("${mongo.authentication.db}")
  private String mongoAuthenticationDb;
  @Value("${mongo.enableSSL}")
  private boolean mongoEnableSSL;
  @Value("${mongo.application.name}")
  private String mongoApplicationName;
  @Value("${entity.db}")
  private String entityDb;
  @Value("${vocabulary.db}")
  private String vocabularyDb;


  //Valid directories list
  @Value("${allowed.url.domains}")
  private String[] allowedUrlDomains;

  public boolean isSocksProxyEnabled() {
    return socksProxyEnabled;
  }

  public String getSocksProxyHost() {
    return socksProxyHost;
  }

  public String getSocksProxyPort() {
    return socksProxyPort;
  }

  public String getSocksProxyUsername() {
    return socksProxyUsername;
  }

  public String getSocksProxyPassword() {
    return socksProxyPassword;
  }

  public String getTruststorePath() {
    return truststorePath;
  }

  public String getTruststorePassword() {
    return truststorePassword;
  }

  public MongoProperties<IllegalArgumentException> getMongoProperties() {
    final MongoProperties<IllegalArgumentException> mongoProperties = new MongoProperties<>(
        IllegalArgumentException::new);
    mongoProperties.setAllProperties(mongoHosts, mongoPorts, mongoAuthenticationDb, mongoUsername,
        mongoPassword, mongoEnableSSL, ReadPreferenceValue.PRIMARY_PREFERRED, mongoApplicationName);
    return mongoProperties;
  }

  public String getEntityDb() {
    return entityDb;
  }

  public String getVocabularyDb() {
    return vocabularyDb;
  }

  public String[] getAllowedUrlDomains() {
    return allowedUrlDomains == null ? null : allowedUrlDomains.clone();
  }
}
