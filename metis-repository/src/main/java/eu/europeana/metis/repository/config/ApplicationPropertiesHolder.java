package eu.europeana.metis.repository.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * Holder for the property values from Spring injection/property loading.
 */
@Component
@PropertySource({"classpath:application.properties"})
public class ApplicationPropertiesHolder {

  //Mongo
  @Value("${mongo.hosts}")
  private String[] mongoHosts;
  @Value("${mongo.port}")
  private int mongoPort;
  @Value("${mongo.authentication.db}")
  private String mongoAuthenticationDb;
  @Value("${mongo.username}")
  private String mongoUsername;
  @Value("${mongo.password}")
  private String mongoPassword;
  @Value("${mongo.enable.ssl}")
  private boolean mongoEnableSsl;
  @Value("${mongo.application.name}")
  private String mongoApplicationName;
  @Value("${mongo.record.db}")
  private String mongoRecordDb;

  // truststore
  @Value("${truststore.path}")
  private String truststorePath;
  @Value("${truststore.password}")
  private String truststorePassword;

  public String[] getMongoHosts() {
    return mongoHosts.clone();
  }

  public int getMongoPort() {
    return mongoPort;
  }

  public String getMongoAuthenticationDb() {
    return mongoAuthenticationDb;
  }

  public String getMongoUsername() {
    return mongoUsername;
  }

  public String getMongoPassword() {
    return mongoPassword;
  }

  public boolean isMongoEnableSsl() {
    return mongoEnableSsl;
  }

  public String getMongoApplicationName() {
    return mongoApplicationName;
  }

  public String getMongoRecordDb() {
    return mongoRecordDb;
  }

  public String getTruststorePath() {
    return truststorePath;
  }

  public String getTruststorePassword() {
    return truststorePassword;
  }
}
