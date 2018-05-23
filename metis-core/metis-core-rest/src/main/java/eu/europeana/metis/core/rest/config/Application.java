package eu.europeana.metis.core.rest.config;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientOptions.Builder;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import eu.europeana.cloud.mcs.driver.DataSetServiceClient;
import eu.europeana.corelib.web.socks.SocksProxy;
import eu.europeana.metis.authentication.rest.client.AuthenticationClient;
import eu.europeana.metis.core.dao.DatasetDao;
import eu.europeana.metis.core.dao.DatasetXsltDao;
import eu.europeana.metis.core.dao.ScheduledWorkflowDao;
import eu.europeana.metis.core.dao.WorkflowDao;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.core.rest.RequestLimits;
import eu.europeana.metis.core.service.Authorizer;
import eu.europeana.metis.core.service.DatasetService;
import eu.europeana.metis.json.CustomObjectMapper;
import eu.europeana.metis.utils.CustomTruststoreAppender;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PreDestroy;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.core.net.ssl.TrustStoreConfigurationException;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
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
@EnableWebMvc
public class Application extends WebMvcConfigurerAdapter {

  private final ConfigurationPropertiesHolder propertiesHolder;
  private MongoClient mongoClient;

  /**
   * Autowired constructor for Spring Configuration class.
   *
   * @param propertiesHolder the object that holds all boot configuration values
   * @throws TrustStoreConfigurationException if the configuration of the truststore failed
   */
  @Autowired
  public Application(ConfigurationPropertiesHolder propertiesHolder)
      throws TrustStoreConfigurationException {
    this.propertiesHolder = propertiesHolder;
    preConfigurationInitialization();
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**").allowedMethods("GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS")
        .allowedOrigins(propertiesHolder.getAllowedCorsHosts());
  }

  private void preConfigurationInitialization() throws TrustStoreConfigurationException {
    if (StringUtils.isNotEmpty(propertiesHolder.getTruststorePath()) && StringUtils
        .isNotEmpty(propertiesHolder.getTruststorePassword())) {
      CustomTruststoreAppender
          .appendCustomTrustoreToDefault(propertiesHolder.getTruststorePath(),
              propertiesHolder.getTruststorePassword());
    }
    if (propertiesHolder.isSocksProxyEnabled()) {
      new SocksProxy(propertiesHolder.getSocksProxyHost(), propertiesHolder.getSocksProxyPort(),
          propertiesHolder
              .getSocksProxyUsername(),
          propertiesHolder.getSocksProxyPassword()).init();
    }

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

    MongoClientOptions.Builder optionsBuilder = new Builder();
    optionsBuilder.sslEnabled(propertiesHolder.isMongoEnableSSL());
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
  }

  @Bean
  AuthenticationClient getAuthenticationClient() {
    return new AuthenticationClient(propertiesHolder.getAuthenticationBaseUrl());
  }

  @Bean
  MorphiaDatastoreProvider getMorphiaDatastoreProvider() {
    return new MorphiaDatastoreProvider(mongoClient, propertiesHolder.getMongoDb());
  }

  @Bean
  Authorizer geAuthorizer(DatasetDao datasetDao) {
    return new Authorizer(datasetDao);
  }

  /**
   * Get the DAO for datasets.
   *
   * @param morphiaDatastoreProvider {@link MorphiaDatastoreProvider}
   * @param ecloudDataSetServiceClient the ecloud dataset client
   * @return {@link DatasetDao} used to access the database for datasets
   */
  @Bean
  public DatasetDao getDatasetDao(MorphiaDatastoreProvider morphiaDatastoreProvider, DataSetServiceClient ecloudDataSetServiceClient) {
    DatasetDao datasetDao = new DatasetDao(morphiaDatastoreProvider, ecloudDataSetServiceClient);
    datasetDao.setDatasetsPerRequest(RequestLimits.DATASETS_PER_REQUEST.getLimit());
    datasetDao.setEcloudProvider(propertiesHolder.getEcloudProvider());
    return datasetDao;
  }

  /**
   * Get the DAO for xslts.
   *
   * @param morphiaDatastoreProvider {@link MorphiaDatastoreProvider}
   * @return {@link DatasetXsltDao} used to access the database for datasets
   */
  @Bean
  public DatasetXsltDao getXsltDao(MorphiaDatastoreProvider morphiaDatastoreProvider) {
    return new DatasetXsltDao(morphiaDatastoreProvider);
  }

  /**
   * Get the Service for datasets.
   * <p>It encapsulates several DAOs and combines their functionality into methods</p>
   *
   * @param datasetDao the Dao instance to access the Dataset database
   * @param datasetXsltDao the Dao instance to access the DatasetXslt database
   * @param workflowDao the Dao instance to access the Workflow database
   * @param workflowExecutionDao the Dao instance to access the WorkflowExecution database
   * @param scheduledWorkflowDao the Dao instance to access the ScheduledWorkflow database
   * @param redissonClient {@link RedissonClient}
   * @param authorizer the authorizer for this service
   * @return the dataset service instance instantiated
   */
  @Bean
  public DatasetService getDatasetService(DatasetDao datasetDao, DatasetXsltDao datasetXsltDao,
      WorkflowDao workflowDao, WorkflowExecutionDao workflowExecutionDao,
      ScheduledWorkflowDao scheduledWorkflowDao, RedissonClient redissonClient,
      Authorizer authorizer) {
    DatasetService datasetService = new DatasetService(datasetDao, datasetXsltDao, workflowDao,
        workflowExecutionDao, scheduledWorkflowDao, redissonClient, authorizer);
    datasetService.setMetisCoreUrl(propertiesHolder.getMetisCoreBaseUrl());
    return datasetService;
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
    converters.add(new StringHttpMessageConverter(StandardCharsets.UTF_8));
    super.configureMessageConverters(converters);
  }
}
