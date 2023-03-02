package eu.europeana.metis.repository.rest.config;

import com.mongodb.client.MongoClient;
import eu.europeana.corelib.web.socks.SocksProxy;
import eu.europeana.metis.mongo.connection.MongoClientProvider;
import eu.europeana.metis.mongo.connection.MongoProperties;
import eu.europeana.metis.mongo.connection.MongoProperties.ReadPreferenceValue;
import eu.europeana.metis.repository.dao.RecordDao;
import eu.europeana.metis.utils.CustomTruststoreAppender;
import eu.europeana.metis.utils.CustomTruststoreAppender.TrustStoreConfigurationException;
import javax.annotation.PreDestroy;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * The web application making available the repository functionality. This provides all the
 * configuration and is the starting point for all injections and beans. It also performs the
 * required setup.
 */
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = {"eu.europeana.metis.repository.rest.controller",
        "eu.europeana.metis.repository.rest.view"})
public class ApplicationConfiguration implements WebMvcConfigurer {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationConfiguration.class);

  private final ConfigurationPropertiesHolder propertiesHolder;

  private final MongoClient mongoClient;

  /**
   * Constructor.
   *
   * @param propertiesHolder The properties.
   * @throws TrustStoreConfigurationException If something goes wrong initializing the application
   */
  @Autowired
  public ApplicationConfiguration(ConfigurationPropertiesHolder propertiesHolder) throws TrustStoreConfigurationException {
    this.mongoClient = ApplicationConfiguration.initializeApplication(propertiesHolder);
    this.propertiesHolder = propertiesHolder;
  }

  /**
   * This method performs the initializing tasks for the application.
   *
   * @param propertiesHolder The properties.
   * @return The Mongo client that can be used to access the mongo database.
   * @throws TrustStoreConfigurationException In case a problem occurred with the truststore.
   */
  static MongoClient initializeApplication(ConfigurationPropertiesHolder propertiesHolder)
      throws TrustStoreConfigurationException {

    // Set the SOCKS proxy
    if (propertiesHolder.isSocksProxyEnabled()) {
      new SocksProxy(propertiesHolder.getSocksProxyHost(), propertiesHolder.getSocksProxyPort(),
              propertiesHolder.getSocksProxyUsername(), propertiesHolder.getSocksProxyPassword()).init();
    }

    // Set the truststore.
    LOGGER.info("Append default truststore with custom truststore");
    if (StringUtils.isNotEmpty(propertiesHolder.getTruststorePath())
            && StringUtils.isNotEmpty(propertiesHolder.getTruststorePassword())) {
      CustomTruststoreAppender.appendCustomTrustoreToDefault(propertiesHolder.getTruststorePath(),
              propertiesHolder.getTruststorePassword());
    }

    // Create the mongo connection
    LOGGER.info("Creating Mongo connection");
    final MongoProperties<IllegalArgumentException> mongoProperties = new MongoProperties<>(
            IllegalArgumentException::new);
    mongoProperties
            .setAllProperties(propertiesHolder.getMongoHosts(), new int[]{propertiesHolder.getMongoPort()},
                    propertiesHolder.getMongoAuthenticationDb(), propertiesHolder.getMongoUsername(),
                    propertiesHolder.getMongoPassword(), propertiesHolder.isMongoEnableSsl(),
                    ReadPreferenceValue.PRIMARY_PREFERRED, propertiesHolder.getMongoApplicationName());

    return new MongoClientProvider<>(mongoProperties).createMongoClient();
  }

  @Bean(name = DispatcherServlet.MULTIPART_RESOLVER_BEAN_NAME)
  public StandardServletMultipartResolver getMultipartResolver() {
    return new StandardServletMultipartResolver();
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/swagger-ui/**")
            .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/")
            .resourceChain(false);
  }

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addRedirectViewController("/", "/swagger-ui/index.html");
  }

  @Bean
  public RecordDao getRecordDao() {
    return new RecordDao(mongoClient, propertiesHolder.getMongoRecordDb());
  }

  /**
   * Closes any connections previous acquired.
   */
  @PreDestroy
  public void close() {
    if (mongoClient != null) {
      mongoClient.close();
    }
  }
}
