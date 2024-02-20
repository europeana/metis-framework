package eu.europeana.metis.dereference.rest.config;

import com.mongodb.client.MongoClient;
//import eu.europeana.corelib.web.socks.SocksProxy;
import eu.europeana.metis.dereference.rest.config.properties.MetisDereferenceConfigurationProperties;
import eu.europeana.metis.dereference.service.DereferenceService;
import eu.europeana.metis.dereference.service.DereferencingManagementService;
import eu.europeana.metis.dereference.service.MongoDereferenceService;
import eu.europeana.metis.dereference.service.MongoDereferencingManagementService;
import eu.europeana.metis.dereference.service.dao.ProcessedEntityDao;
import eu.europeana.metis.dereference.service.dao.VocabularyDao;
import eu.europeana.metis.dereference.vocimport.VocabularyCollectionImporterFactory;
import eu.europeana.metis.mongo.connection.MongoClientProvider;
import eu.europeana.metis.mongo.connection.MongoProperties;
import eu.europeana.metis.mongo.connection.MongoProperties.ReadPreferenceValue;
import eu.europeana.metis.utils.CustomTruststoreAppender;
import eu.europeana.metis.utils.apm.ElasticAPMConfiguration;
import java.lang.invoke.MethodHandles;
import java.util.Set;
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
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Entry class with configuration fields and beans initialization for the application.
 */
@Configuration
@EnableConfigurationProperties({
    ElasticAPMConfiguration.class, TruststoreConfigurationProperties.class,
    SocksProxyConfigurationProperties.class, MongoConfigurationProperties.class,
    MetisDereferenceConfigurationProperties.class})
@EnableScheduling
@ComponentScan(basePackages = {
    "eu.europeana.metis.dereference.rest.controller",
    "eu.europeana.metis.dereference.rest.exceptions"})
@EnableWebMvc
public class ApplicationConfiguration implements WebMvcConfigurer {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final MongoClient mongoClient;
  private ProcessedEntityDao processedEntityDao;

  /**
   * Constructor.
   *
   * @param truststoreConfigurationProperties the truststore configuration properties
   * @param socksProxyConfigurationProperties the socks proxy configuration properties
   * @param mongoConfigurationProperties the mongo configuration properties
   * @throws CustomTruststoreAppender.TrustStoreConfigurationException if the configuration of the truststore failed
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
   * Truststore initializer
   *
   * @param truststoreConfigurationProperties the truststore configuration properties
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

  @Bean
  ProcessedEntityDao getProcessedEntityDao(MongoConfigurationProperties mongoConfigurationProperties) {
    processedEntityDao = new ProcessedEntityDao(mongoClient, mongoConfigurationProperties.getDatabase());
    return processedEntityDao;
  }

  @Bean
  VocabularyDao getVocabularyDao(MongoConfigurationProperties mongoConfigurationProperties) {
    return new VocabularyDao(mongoClient, mongoConfigurationProperties.getDatabase());
  }

  @Bean
  Set<String> getAllowedUrlDomains(MetisDereferenceConfigurationProperties metisDereferenceConfigurationProperties) {
    return Set.of(metisDereferenceConfigurationProperties.getAllowedUrlDomains());
  }

  /**
   * Used to allow x-forwarded-prefix header for applications behind a reverse proxy.
   * <p>This is required for springdoc/swagger internal redirects if the application is for example deployed with a context
   * path</p>
   *
   * @return the forwarded header filter
   */
  @Bean
  ForwardedHeaderFilter forwardedHeaderFilter() {
    return new ForwardedHeaderFilter();
  }

  /**
   * Empty Cache with XML entries null or empty. This will remove entries with null or empty XML in the cache (Redis). If the same
   * redis instance/cluster is used for multiple services then the cache for other services is cleared as well. This task is
   * scheduled by a cron expression.
   */
  // TODO: 24/08/2023 Is there a better way to load the configuration here?
  @Scheduled(cron = "${metis-dereference.getPurgeEmptyXmlFrequency}")
  public void dereferenceCacheNullOrEmpty() {
    processedEntityDao.purgeByNullOrEmptyXml();
  }

  /**
   * Empty Cache. This will remove ALL entries in the cache (Redis). If the same redis instance/cluster is used for multiple
   * services then the cache for other services is cleared as well. This task is scheduled by a cron expression.
   */
  @Scheduled(cron = "${metis-dereference.getPurgeAllFrequency}")
  public void dereferenceCachePurgeAll() {
    processedEntityDao.purgeAll();
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
