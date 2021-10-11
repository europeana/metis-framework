package eu.europeana.metis.core.rest.config;

import com.mongodb.client.MongoClient;
import eu.europeana.corelib.web.socks.SocksProxy;
import eu.europeana.metis.mongo.connection.MongoClientProvider;
import eu.europeana.metis.utils.CustomTruststoreAppender;
import eu.europeana.metis.utils.CustomTruststoreAppender.TrustStoreConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for performing initializing tasks for the application.
 */
final class ApplicationInitUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationInitUtils.class);

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
      LOGGER.info("Custom truststore appended to default truststore");
    }

    // Initialize the socks proxy.
    if (propertiesHolder.isSocksProxyEnabled()) {
      new SocksProxy(propertiesHolder.getSocksProxyHost(), propertiesHolder.getSocksProxyPort(),
          propertiesHolder
              .getSocksProxyUsername(),
          propertiesHolder.getSocksProxyPassword()).init();
      LOGGER.info("Socks proxy enabled");
    }

    // Initialize the Mongo connection
    return new MongoClientProvider<>(propertiesHolder.getMongoProperties()).createMongoClient();
  }
}
