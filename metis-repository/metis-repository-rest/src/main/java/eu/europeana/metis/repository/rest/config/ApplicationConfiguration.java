package eu.europeana.metis.repository.rest.config;

import com.mongodb.client.MongoClient;
//import eu.europeana.corelib.web.socks.SocksProxy;
import eu.europeana.metis.mongo.connection.MongoClientProvider;
import eu.europeana.metis.mongo.connection.MongoProperties;
import eu.europeana.metis.mongo.connection.MongoProperties.ReadPreferenceValue;
import eu.europeana.metis.repository.rest.dao.RecordDao;
import eu.europeana.metis.utils.CustomTruststoreAppender;
import eu.europeana.metis.utils.CustomTruststoreAppender.TrustStoreConfigurationException;
import eu.europeana.metis.utils.apm.ElasticAPMConfiguration;
import javax.annotation.PreDestroy;
import metis.common.config.properties.SocksProxyConfigurationProperties;
import metis.common.config.properties.TruststoreConfigurationProperties;
import metis.common.config.properties.mongo.MongoConfigurationProperties;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
 * The web application making available the repository functionality. This provides all the configuration and is the starting
 * point for all injections and beans. It also performs the required setup.
 */
@Configuration
@EnableWebMvc
@EnableConfigurationProperties({
    ElasticAPMConfiguration.class, TruststoreConfigurationProperties.class,
    SocksProxyConfigurationProperties.class, MongoConfigurationProperties.class})
@ComponentScan(basePackages = {
    "eu.europeana.metis.repository.rest.controller",
    "eu.europeana.metis.repository.rest.view"})
public class ApplicationConfiguration implements WebMvcConfigurer {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationConfiguration.class);
  private final MongoClient mongoClient;

  /**
   * Constructor.
   *
   * @param truststoreConfigurationProperties the truststore configuration properties
   * @param socksProxyConfigurationProperties the socks proxy configuration properties
   * @throws TrustStoreConfigurationException If something goes wrong initializing the application
   */
  @Autowired
  public ApplicationConfiguration(TruststoreConfigurationProperties truststoreConfigurationProperties,
      SocksProxyConfigurationProperties socksProxyConfigurationProperties,
      MongoConfigurationProperties mongoConfigurationProperties)
      throws CustomTruststoreAppender.TrustStoreConfigurationException {
    ApplicationConfiguration.initializeTruststore(truststoreConfigurationProperties);
    ApplicationConfiguration.initializeSocksProxy(socksProxyConfigurationProperties);
    this.mongoClient = ApplicationConfiguration.getMongoClient(mongoConfigurationProperties);
  }

  /**
   * This method performs the initializing tasks for the application.
   *
   * @param truststoreConfigurationProperties The properties.
   * @throws CustomTruststoreAppender.TrustStoreConfigurationException In case a problem occurred with the truststore.
   */
  static void initializeTruststore(TruststoreConfigurationProperties truststoreConfigurationProperties)
      throws CustomTruststoreAppender.TrustStoreConfigurationException {
    if (StringUtils.isNotEmpty(truststoreConfigurationProperties.getPath()) && StringUtils
        .isNotEmpty(truststoreConfigurationProperties.getPassword())) {
      CustomTruststoreAppender
          .appendCustomTruststoreToDefault(truststoreConfigurationProperties.getPath(),
              truststoreConfigurationProperties.getPassword());
      LOGGER.info("Custom truststore appended to default truststore");
    }
  }

  /**
   * Socks proxy initializer.
   *
   * @param socksProxyConfigurationProperties the socks proxy configuration properties
   */
  static void initializeSocksProxy(SocksProxyConfigurationProperties socksProxyConfigurationProperties) {
    //TODO: Need to remove SocksProxy
//    if (socksProxyConfigurationProperties.isEnabled()) {
//      new SocksProxy(socksProxyConfigurationProperties.getHost(), socksProxyConfigurationProperties.getPort(),
//          socksProxyConfigurationProperties.getUsername(),
//          socksProxyConfigurationProperties.getPassword()).init();
//      LOGGER.info("Socks proxy enabled");
//    }
  }

  public static MongoClient getMongoClient(MongoConfigurationProperties mongoConfigurationProperties) {
    final MongoProperties<IllegalArgumentException> mongoProperties = new MongoProperties<>(
        IllegalArgumentException::new);
    mongoProperties.setAllProperties(
        mongoConfigurationProperties.getHosts(),
        mongoConfigurationProperties.getPorts(),
        mongoConfigurationProperties.getAuthenticationDatabase(),
        mongoConfigurationProperties.getUsername(),
        mongoConfigurationProperties.getPassword(),
        mongoConfigurationProperties.isEnableSsl(),
        ReadPreferenceValue.PRIMARY_PREFERRED,
        mongoConfigurationProperties.getApplicationName());

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
  public RecordDao getRecordDao(MongoConfigurationProperties mongoConfigurationProperties) {
    return new RecordDao(mongoClient, mongoConfigurationProperties.getDatabase());
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
