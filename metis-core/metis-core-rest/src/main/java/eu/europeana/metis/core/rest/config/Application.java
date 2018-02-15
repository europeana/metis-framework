package eu.europeana.metis.core.rest.config;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientOptions.Builder;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import eu.europeana.corelib.web.socks.SocksProxy;
import eu.europeana.metis.authentication.rest.client.AuthenticationClient;
import eu.europeana.metis.core.dao.DatasetDao;
import eu.europeana.metis.core.dao.ScheduledWorkflowDao;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.core.rest.RequestLimits;
import eu.europeana.metis.core.service.DatasetService;
import eu.europeana.metis.json.CustomObjectMapper;
import eu.europeana.metis.utils.CustomTruststoreAppender;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PreDestroy;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.core.net.ssl.TrustStoreConfigurationException;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.BeanNameViewResolver;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

/**
 * Entry class with configuration fields and beans initialization for the application.
 */
@Configuration
@ComponentScan(basePackages = {"eu.europeana.metis.core.rest"})
@PropertySource({"classpath:metis.properties"})
@EnableWebMvc
public class Application extends WebMvcConfigurerAdapter implements InitializingBean {

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
  @Value("${mongo.db}")
  private String mongoDb;
  @Value("${mongo.enableSSL}")
  private boolean mongoEnableSSL;

  //Redis
  @Value("${redis.host}")
  private String redisHost;
  @Value("${redis.port}")
  private int redisPort;
  @Value("${redis.password}")
  private String redisPassword;
  @Value("${redisson.lock.watchdog.timeout.in.secs}")
  private int redissonLockWatchdogTimeoutInSecs;

  //Authentication
  @Value("${authentication.baseUrl}")
  private String authenticationBaseUrl;

  @Value("${allowed.cors.hosts}")
  private String[] allowedCorsHosts;

  private MongoClient mongoClient;

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**").allowedMethods("GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS")
        .allowedOrigins(allowedCorsHosts);
  }

  /**
   * Used for overwriting properties if cloud foundry environment is used
   */
  @Override
  public void afterPropertiesSet() throws TrustStoreConfigurationException {
    if (StringUtils.isNotEmpty(truststorePath) && StringUtils.isNotEmpty(truststorePassword)) {
      CustomTruststoreAppender.appendCustomTrustoreToDefault(truststorePath, truststorePassword);
    }
    if (socksProxyEnabled) {
      new SocksProxy(socksProxyHost, socksProxyPort, socksProxyUsername, socksProxyPassword).init();
    }

    if (mongoHosts.length != mongoPorts.length && mongoPorts.length != 1) {
      throw new IllegalArgumentException("Mongo hosts and ports are not properly configured.");
    }

    List<ServerAddress> serverAddresses = new ArrayList<>();
    for (int i = 0; i < mongoHosts.length; i++) {
      ServerAddress address;
      if (mongoHosts.length == mongoPorts.length) {
        address = new ServerAddress(mongoHosts[i], mongoPorts[i]);
      } else { // Same port for all
        address = new ServerAddress(mongoHosts[i], mongoPorts[0]);
      }
      serverAddresses.add(address);
    }

    MongoClientOptions.Builder optionsBuilder = new Builder();
    optionsBuilder.sslEnabled(mongoEnableSSL);
    if (StringUtils.isEmpty(mongoDb) || StringUtils.isEmpty(mongoUsername) || StringUtils
        .isEmpty(mongoPassword)) {
      mongoClient = new MongoClient(serverAddresses, optionsBuilder.build());
    } else {
      MongoCredential mongoCredential = MongoCredential
          .createCredential(mongoUsername, mongoAuthenticationDb, mongoPassword.toCharArray());
      mongoClient = new MongoClient(serverAddresses, mongoCredential, optionsBuilder.build());
    }
  }

  @Bean
  AuthenticationClient getAuthenticationClient() {
    return new AuthenticationClient(authenticationBaseUrl);
  }

  @Bean
  MorphiaDatastoreProvider getMorphiaDatastoreProvider() {
    return new MorphiaDatastoreProvider(mongoClient, mongoDb);
  }

  /**
   * Get the DAO for datasets.
   *
   * @param morphiaDatastoreProvider {@link MorphiaDatastoreProvider}
   * @return {@link DatasetDao} used to access the database for datasets
   */
  @Bean
  public DatasetDao getDatasetDao(MorphiaDatastoreProvider morphiaDatastoreProvider) {
    DatasetDao datasetDao = new DatasetDao(morphiaDatastoreProvider);
    datasetDao.setDatasetsPerRequest(RequestLimits.DATASETS_PER_REQUEST.getLimit());
    return datasetDao;
  }

  /**
   * Get the Service for datasets.
   * <p>It encapsulates several DAOs and combines their functionality into methods</p>
   *
   * @param datasetDao {@link DatasetDao}
   * @param workflowExecutionDao {@link WorkflowExecutionDao}
   * @param scheduledWorkflowDao {@link ScheduledWorkflowDao}
   * @param redissonClient {@link RedissonClient}
   * @return {@link DatasetService}
   */
  @Bean
  public DatasetService getDatasetService(DatasetDao datasetDao,
      WorkflowExecutionDao workflowExecutionDao,
      ScheduledWorkflowDao scheduledWorkflowDao, RedissonClient redissonClient) {
    return new DatasetService(datasetDao, workflowExecutionDao,
        scheduledWorkflowDao, redissonClient);
  }

  /**
   * Closes connections to databases when the application closes.
   */
  @PreDestroy
  public void close() {
    if (mongoClient != null) {
      mongoClient.close();
    }
  }

  /**
   * Required for json serialization for REST.
   *
   * @return {@link View}
   */
  @Bean
  public View json() {
    MappingJackson2JsonView view = new MappingJackson2JsonView();
    view.setPrettyPrint(true);
    view.setObjectMapper(new CustomObjectMapper());
    return view;
  }

  /**
   * Required for json serialization for REST.
   *
   * @return {@link ViewResolver}
   */
  @Bean
  public ViewResolver viewResolver() {
    return new BeanNameViewResolver();
  }

  @Override
  public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    converters.add(new MappingJackson2HttpMessageConverter());
    converters.add(new MappingJackson2XmlHttpMessageConverter());
    super.configureMessageConverters(converters);
  }
}
