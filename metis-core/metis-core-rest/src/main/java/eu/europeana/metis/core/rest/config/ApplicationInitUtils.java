package eu.europeana.metis.core.rest.config;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientOptions.Builder;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import eu.europeana.corelib.web.socks.SocksProxy;
import eu.europeana.metis.utils.CustomTruststoreAppender;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.core.net.ssl.TrustStoreConfigurationException;

/**
 * This class is responsible for performing initializing tasks for the application.
 */
final class ApplicationInitUtils {

  private ApplicationInitUtils() {
  }

  /**
   * This method performs the initializing tasks for the application.
   * @param propertiesHolder The properties.
   * @return The Mongo client that can be used to access the mongo database.
   * @throws TrustStoreConfigurationException In case a problem occurred with the truststore.
   */
  static MongoClient initializeApplication(ConfigurationPropertiesHolder propertiesHolder)
      throws TrustStoreConfigurationException {

    // Load the trust store file.
    if (StringUtils.isNotEmpty(propertiesHolder.getTruststorePath()) && StringUtils
        .isNotEmpty(propertiesHolder.getTruststorePassword())) {
      CustomTruststoreAppender
          .appendCustomTrustoreToDefault(propertiesHolder.getTruststorePath(),
              propertiesHolder.getTruststorePassword());
    }

    // Initialize the socks proxy.
    if (propertiesHolder.isSocksProxyEnabled()) {
      new SocksProxy(propertiesHolder.getSocksProxyHost(), propertiesHolder.getSocksProxyPort(),
          propertiesHolder
              .getSocksProxyUsername(),
          propertiesHolder.getSocksProxyPassword()).init();
    }

    // Load the mongo server addresses from the config file.
    if (propertiesHolder.getMongoHosts().length != propertiesHolder.getMongoPorts().length
        && propertiesHolder.getMongoPorts().length != 1) {
      throw new IllegalArgumentException("Mongo hosts and ports are not properly configured.");
    }
    List<ServerAddress> serverAddresses = new ArrayList<>(propertiesHolder.getMongoHosts().length);
    for (int i = 0; i < propertiesHolder.getMongoHosts().length; i++) {
      ServerAddress address;
      if (propertiesHolder.getMongoHosts().length == propertiesHolder.getMongoPorts().length) {
        address = new ServerAddress(propertiesHolder.getMongoHosts()[i],
            propertiesHolder.getMongoPorts()[i]);
      } else { // Same port for all
        address = new ServerAddress(propertiesHolder.getMongoHosts()[i],
            propertiesHolder.getMongoPorts()[0]);
      }
      serverAddresses.add(address);
    }

    // Set up the mongo client.
    MongoClientOptions.Builder optionsBuilder = new Builder();
    optionsBuilder.sslEnabled(propertiesHolder.isMongoEnableSSL());
    final MongoClient mongoClient;
    if (StringUtils.isEmpty(propertiesHolder.getMongoDb()) || StringUtils
        .isEmpty(propertiesHolder.getMongoUsername())
        || StringUtils
        .isEmpty(propertiesHolder.getMongoPassword())) {
      mongoClient = new MongoClient(serverAddresses, optionsBuilder.build());
    } else {
      MongoCredential mongoCredential = MongoCredential
          .createCredential(propertiesHolder.getMongoUsername(),
              propertiesHolder.getMongoAuthenticationDb(),
              propertiesHolder.getMongoPassword().toCharArray());
      mongoClient = new MongoClient(serverAddresses, mongoCredential, optionsBuilder.build());
    }

    // Done
    return mongoClient;
  }
}
