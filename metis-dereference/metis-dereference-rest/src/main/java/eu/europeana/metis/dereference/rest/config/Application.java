package eu.europeana.metis.dereference.rest.config;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import eu.europeana.corelib.storage.impl.MongoProviderImpl;
import eu.europeana.corelib.web.socks.SocksProxy;
import eu.europeana.enrichment.rest.client.EnrichmentClient;
import eu.europeana.metis.cache.redis.RedisProvider;
import eu.europeana.metis.dereference.service.dao.CacheDao;
import eu.europeana.metis.dereference.service.dao.EntityDao;
import eu.europeana.metis.dereference.service.dao.VocabularyDao;
import javax.annotation.PreDestroy;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Spring configuration class
 * Created by ymamakis on 12-2-16.
 */
@Configuration
@ComponentScan(basePackages = {"eu.europeana.metis.dereference.rest",
    "eu.europeana.metis.dereference.rest.exceptions"})
@PropertySource("classpath:dereferencing.properties")
@EnableWebMvc
@EnableSwagger2
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

  //Redis
  @Value("${redis.host}")
  private String redisHost;
  @Value("${redis.port}")
  private int redisPort;
  @Value("${redis.password}")
  private String redisPassword;

  //Mongo
  @Value("${mongo.hosts}")
  private String[] mongoHosts;
  @Value("${mongo.port}")
  private int mongoPort;
  @Value("${mongo.username}")
  private String mongoUsername;
  @Value("${mongo.password}")
  private String mongoPassword;
  @Value("${entity.db}")
  private String entityDb;
  @Value("${vocabulary.db}")
  private String vocabularyDb;

  @Value("${enrichment.url}")
  private String enrichmentUrl;

  private MongoProviderImpl mongoProviderEntity;
  private MongoProviderImpl mongoProviderVocabulary;
  private RedisProvider redisProvider;

  /**
   * Used for overwriting properties if cloud foundry environment is used
   */
  @Override
  public void afterPropertiesSet() {
    if (socksProxyEnabled) {
      new SocksProxy(socksProxyHost, socksProxyPort, socksProxyUsername, socksProxyPassword).init();
    }

    String[] mongoPorts = new String[mongoHosts.length];
    for (int i = 0; i < mongoHosts.length; i++) {
      mongoPorts[i]= Integer.toString(mongoPort);
    }
    MongoClientOptions.Builder options = MongoClientOptions.builder();
    mongoProviderEntity = new MongoProviderImpl(mongoHosts, mongoPorts, entityDb,
        mongoUsername,
        mongoPassword, options);
    mongoProviderVocabulary = new MongoProviderImpl(mongoHosts, mongoPorts, vocabularyDb,
        mongoUsername,
        mongoPassword, options);

    if (redisProvider == null) {
      redisProvider = new RedisProvider(redisHost, redisPort, redisPassword);
    }
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("swagger-ui.html")
        .addResourceLocations("classpath:/META-INF/resources/");
    registry.addResourceHandler("/webjars/**")
        .addResourceLocations("classpath:/META-INF/resources/webjars/");
  }

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addRedirectViewController("/", "swagger-ui.html");
  }

  @Bean
  EnrichmentClient getEnrichmentClient() {
    return new EnrichmentClient(enrichmentUrl);
  }

  MongoClient getEntityMongoClient() {
    return mongoProviderEntity.getMongo();
  }

  MongoClient getVocabularyMongoClient() {
    return mongoProviderVocabulary.getMongo();
  }

  @Bean
  CacheDao getCacheDao() {
    return new CacheDao(redisProvider);
  }

  @Bean
  EntityDao getEntityDao() {
    return new EntityDao(getEntityMongoClient(), entityDb);
  }

  @Bean
  VocabularyDao getVocabularyDao() {
    return new VocabularyDao(getVocabularyMongoClient(), vocabularyDb);
  }

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    return new PropertySourcesPlaceholderConfigurer();
  }

  @Bean
  public Docket api() {
    return new Docket(DocumentationType.SWAGGER_2)
        .useDefaultResponseMessages(false)
        .select()
        .apis(RequestHandlerSelectors.any())
        .paths(PathSelectors.regex("/.*"))
        .build()
        .apiInfo(apiInfo());
  }

  /**
   * Closes any connections previous acquired.
   */
  @PreDestroy
  public void close() {
    if (mongoProviderVocabulary != null) {
      mongoProviderVocabulary.close();
    }
    if (mongoProviderEntity != null) {
      mongoProviderEntity.close();
    }
  }

  private ApiInfo apiInfo() {
    return new ApiInfo(
        "Dereference REST API",
        "Dereference REST API for Europeana",
        "v1",
        "API TOS",
        new Contact("development", "europeana.eu", "development@europeana.eu"),
        "EUPL Licence v1.1",
        ""
    );
  }
}
