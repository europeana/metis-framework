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
import eu.europeana.metis.mongo.connection.MongoProperties;
import java.util.Set;
import javax.annotation.PreDestroy;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
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
public class ApplicationConfiguration implements WebMvcConfigurer, InitializingBean {

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

  //Mongo
  @Value("${mongo.hosts}")
  private String[] mongoHosts;
  @Value("${mongo.port}")
  private int mongoPort;
  @Value("${mongo.username}")
  private String mongoUsername;
  @Value("${mongo.password}")
  private String mongoPassword;
  @Value("${mongo.application.name}")
  private String mongoApplicationName;
  @Value("${entity.db}")
  private String entityDb;
  @Value("${vocabulary.db}")
  private String vocabularyDb;

  //Valid directories list
  @Value("${allowed.url.domains}")
  private String[] allowedUrlDomains;
  private MongoClient mongoClientEntity;
  private MongoClient mongoClientVocabulary;

  /**
   * Used for overwriting properties if cloud foundry environment is used
   */
  @Override
  public void afterPropertiesSet() {
    if (socksProxyEnabled) {
      new SocksProxy(socksProxyHost, socksProxyPort, socksProxyUsername, socksProxyPassword).init();
    }

    final MongoProperties<IllegalArgumentException> mongoProperties = new MongoProperties<>(
        IllegalArgumentException::new);
    mongoProperties.setMongoHosts(mongoHosts, new int[]{mongoPort});
    mongoProperties.setApplicationName(mongoApplicationName);
    mongoClientEntity = new MongoClientProvider<>(mongoProperties).createMongoClient();
    mongoClientVocabulary = new MongoClientProvider<>(mongoProperties).createMongoClient();
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
    return new ProcessedEntityDao(getEntityMongoClient(), entityDb);
  }

  @Bean
  VocabularyDao getVocabularyDao() {
    return new VocabularyDao(getVocabularyMongoClient(), vocabularyDb);
  }

  @Bean
  Set<String> getAllowedUrlDomains() {
    return Set.of(allowedUrlDomains);
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
