package eu.europeana.metis.core.rest.config;

import com.mongodb.client.MongoClient;
import eu.europeana.cloud.mcs.driver.DataSetServiceClient;
import eu.europeana.metis.authentication.rest.client.AuthenticationClient;
import eu.europeana.metis.core.dao.DatasetDao;
import eu.europeana.metis.core.dao.DatasetXsltDao;
import eu.europeana.metis.core.dao.DepublishRecordIdDao;
import eu.europeana.metis.core.dao.ScheduledWorkflowDao;
import eu.europeana.metis.core.dao.WorkflowDao;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProviderImpl;
import eu.europeana.metis.core.rest.RequestLimits;
import eu.europeana.metis.core.rest.config.properties.MetisCoreConfigurationProperties;
import eu.europeana.metis.core.service.Authorizer;
import eu.europeana.metis.core.service.DatasetService;
import eu.europeana.metis.core.service.DepublishRecordIdService;
import eu.europeana.metis.core.service.OrchestratorService;
import eu.europeana.metis.mongo.connection.MongoClientProvider;
import eu.europeana.metis.mongo.connection.MongoProperties;
import eu.europeana.metis.mongo.connection.MongoProperties.ReadPreferenceValue;
import eu.europeana.metis.mongo.utils.CustomObjectMapper;
import eu.europeana.metis.utils.CustomTruststoreAppender;
import eu.europeana.metis.utils.CustomTruststoreAppender.TrustStoreConfigurationException;
import eu.europeana.metis.utils.apm.ElasticAPMConfiguration;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.annotation.PreDestroy;
import metis.common.config.properties.TruststoreConfigurationProperties;
import metis.common.config.properties.ecloud.EcloudConfigurationProperties;
import metis.common.config.properties.mongo.MongoConfigurationProperties;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.BeanNameViewResolver;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

/**
 * Entry class with configuration fields and beans initialization for the application.
 */
@Configuration
@EnableConfigurationProperties({
    ElasticAPMConfiguration.class, TruststoreConfigurationProperties.class, MongoConfigurationProperties.class,
    MetisCoreConfigurationProperties.class, EcloudConfigurationProperties.class})
@ComponentScan(basePackages = {"eu.europeana.metis.core.rest.controller"})
public class ApplicationConfiguration implements WebMvcConfigurer, ApplicationContextAware {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final MongoClient mongoClient;

  @Value(value = "classpath:default_transformation.xslt")
  private Resource defaultTransformation;
  private ApplicationContext applicationContext;

  /**
   * Autowired constructor for Spring Configuration class.
   *
   * @throws TrustStoreConfigurationException if the configuration of the truststore failed
   */
  @Autowired
  public ApplicationConfiguration(TruststoreConfigurationProperties truststoreConfigurationProperties,
      MongoConfigurationProperties mongoConfigurationProperties)
      throws TrustStoreConfigurationException {
    ApplicationConfiguration.initializeTruststore(truststoreConfigurationProperties);
    this.mongoClient = ApplicationConfiguration.getMongoClient(mongoConfigurationProperties);
  }

  /**
   * Set the application context.
   *
   * @param applicationContext the application context
   * @throws BeansException if a beans exception occurs
   */
  @Override
  public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
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

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    MetisCoreConfigurationProperties metisCoreConfigurationProperties =
        applicationContext.getBean(MetisCoreConfigurationProperties.class);
    registry.addMapping("/**").allowedMethods("GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedOrigins(metisCoreConfigurationProperties.getAllowedCorsHosts());
  }

  @Bean(name = DispatcherServlet.MULTIPART_RESOLVER_BEAN_NAME)
  public StandardServletMultipartResolver getMultipartResolver() {
    return new StandardServletMultipartResolver();
  }

  @Bean
  AuthenticationClient getAuthenticationClient(MetisCoreConfigurationProperties metisCoreConfigurationProperties) {
    return new AuthenticationClient(metisCoreConfigurationProperties.getAuthenticationBaseUrl());
  }

  @Bean
  MorphiaDatastoreProvider getMorphiaDatastoreProvider(MongoConfigurationProperties mongoConfigurationProperties)
      throws IOException {
    return new MorphiaDatastoreProviderImpl(mongoClient, mongoConfigurationProperties.getDatabase(),
        defaultTransformation::getInputStream);
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
  public DatasetDao getDatasetDao(
      MorphiaDatastoreProvider morphiaDatastoreProvider, DataSetServiceClient ecloudDataSetServiceClient,
      EcloudConfigurationProperties ecloudConfigurationProperties) {
    DatasetDao datasetDao = new DatasetDao(morphiaDatastoreProvider, ecloudDataSetServiceClient);
    datasetDao.setDatasetsPerRequest(RequestLimits.DATASETS_PER_REQUEST.getLimit());
    datasetDao.setEcloudProvider(ecloudConfigurationProperties.getProvider());
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
   * Get the DAO for depublished records.
   *
   * @param morphiaDatastoreProvider {@link MorphiaDatastoreProvider}
   * @return DAO used to access the database for depublished records.
   */
  @Bean
  public DepublishRecordIdDao getDepublishedRecordDao(
      MorphiaDatastoreProvider morphiaDatastoreProvider,
      MetisCoreConfigurationProperties metisCoreConfigurationProperties) {
    return new DepublishRecordIdDao(morphiaDatastoreProvider,
        metisCoreConfigurationProperties.getMaxDepublishRecordIdsPerDataset());
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
  public DatasetService getDatasetService(
      DatasetDao datasetDao, DatasetXsltDao datasetXsltDao,
      WorkflowDao workflowDao, WorkflowExecutionDao workflowExecutionDao,
      ScheduledWorkflowDao scheduledWorkflowDao, RedissonClient redissonClient,
      Authorizer authorizer, MetisCoreConfigurationProperties metisCoreConfigurationProperties) {
    DatasetService datasetService = new DatasetService(datasetDao, datasetXsltDao, workflowDao,
        workflowExecutionDao, scheduledWorkflowDao, redissonClient, authorizer);
    datasetService.setMetisCoreUrl(metisCoreConfigurationProperties.getBaseUrl());
    return datasetService;
  }

  @Bean
  public DepublishRecordIdService getDepublishedRecordService(
      DepublishRecordIdDao depublishRecordIdDao, OrchestratorService orchestratorService,
      Authorizer authorizer) {
    return new DepublishRecordIdService(authorizer, orchestratorService, depublishRecordIdDao);
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
  }
}
