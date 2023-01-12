package eu.europeana.metis.dereference.rest.config;

import com.mongodb.client.MongoClient;
import eu.europeana.corelib.web.socks.SocksProxy;
import eu.europeana.metis.dereference.service.DereferenceService;
import eu.europeana.metis.dereference.service.DereferencingManagementService;
import eu.europeana.metis.dereference.service.MongoDereferenceService;
import eu.europeana.metis.dereference.service.MongoDereferencingManagementService;
import eu.europeana.metis.dereference.service.dao.ProcessedEntityDao;
import eu.europeana.metis.dereference.service.dao.VocabularyDao;
import eu.europeana.metis.dereference.vocimport.VocabularyCollectionImporterFactory;
import eu.europeana.metis.mongo.connection.MongoClientProvider;
import eu.europeana.metis.utils.CustomTruststoreAppender;
import eu.europeana.metis.utils.CustomTruststoreAppender.TrustStoreConfigurationException;
import java.util.Set;
import javax.annotation.PreDestroy;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Entry class with configuration fields and beans initialization for the application.
 */
@Configuration
@EnableScheduling
@ComponentScan(basePackages = {"eu.europeana.metis.dereference.rest.controller",
    "eu.europeana.metis.dereference.rest.exceptions"})
@EnableWebMvc
public class ApplicationConfiguration implements WebMvcConfigurer {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationConfiguration.class);

  private final ConfigurationPropertiesHolder propertiesHolder;
  private final MongoClient mongoClientEntity;
  private final MongoClient mongoClientVocabulary;

  /**
   * Autowired constructor for Spring Configuration class.
   *
   * @param propertiesHolder the object that holds all boot configuration values
   * @throws TrustStoreConfigurationException if the configuration of the truststore failed
   */
  public ApplicationConfiguration(ConfigurationPropertiesHolder propertiesHolder) throws TrustStoreConfigurationException {
    mongoClientEntity = ApplicationConfiguration.initializeApplication(propertiesHolder);
    mongoClientVocabulary = ApplicationConfiguration.initializeApplication(propertiesHolder);
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

  /**
   * Get a dereference service instance bean.
   *
   * @param processedEntityDao the processed entity dao
   * @param vocabularyDao the vocabulary dao
   * @return a dereference service bean
   */
  @Bean
  public DereferenceService getDereferenceService(ProcessedEntityDao processedEntityDao, VocabularyDao vocabularyDao) {
    return new MongoDereferenceService(processedEntityDao, vocabularyDao);
  }

  /**
   * Get a dereferencing management service instance bean.
   *
   * @param processedEntityDao the processed entity dao
   * @param vocabularyDao the vocabulary dao
   * @param vocabularyCollectionImporterFactory the vocabulary collection importer factory
   * @return a dereferencing management service instance bean
   */
  @Bean
  public DereferencingManagementService getDereferencingManagementService(ProcessedEntityDao processedEntityDao,
      VocabularyDao vocabularyDao, VocabularyCollectionImporterFactory vocabularyCollectionImporterFactory) {
    return new MongoDereferencingManagementService(vocabularyDao, processedEntityDao, vocabularyCollectionImporterFactory);
  }

  @Bean
  public VocabularyCollectionImporterFactory getVocabularyCollectionImporterFactory() {
    return new VocabularyCollectionImporterFactory();
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

  MongoClient getEntityMongoClient() {
    return mongoClientEntity;
  }

  MongoClient getVocabularyMongoClient() {
    return mongoClientVocabulary;
  }

  @Bean
  ProcessedEntityDao getProcessedEntityDao() {
    return new ProcessedEntityDao(getEntityMongoClient(), propertiesHolder.getEntityDb());
  }

  @Bean
  VocabularyDao getVocabularyDao() {
    return new VocabularyDao(getVocabularyMongoClient(), propertiesHolder.getVocabularyDb());
  }

  @Bean
  Set<String> getAllowedUrlDomains() {
    return Set.of(propertiesHolder.getAllowedUrlDomains());
  }

  /**
   * Empty Cache with XML entries null or empty. This will remove entries with null or empty XML in the cache (Redis). If the same
   * redis instance/cluster is used for multiple services then the cache for other services is cleared as well. This task is
   * scheduled by a cron expression.
   */

  @Scheduled(cron = "${dereference.purge.emptyxml.frequency}")
  public void dereferenceCacheNullOrEmpty() {
    getProcessedEntityDao().purgeByNullOrEmptyXml();
  }

  /**
   * Empty Cache. This will remove ALL entries in the cache (Redis). If the same redis instance/cluster is used for multiple
   * services then the cache for other services is cleared as well. This task is scheduled by a cron expression.
   */
  @Scheduled(cron = "${dereference.purge.all.frequency}")
  public void dereferenceCachePurgeAll() {
    getProcessedEntityDao().purgeAll();
  }

  /**
   * Closes any connections previous acquired.
   */
  @PreDestroy
  public void close() {
    if (mongoClientVocabulary != null) {
      mongoClientVocabulary.close();
    }
    if (mongoClientEntity != null) {
      mongoClientEntity.close();
    }
  }
}
