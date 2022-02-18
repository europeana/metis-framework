package eu.europeana.metis.dereference.rest.config;

import com.mongodb.client.MongoClient;
import eu.europeana.corelib.web.socks.SocksProxy;
import eu.europeana.metis.dereference.service.dao.ProcessedEntityDao;
import eu.europeana.metis.dereference.service.dao.VocabularyDao;
import eu.europeana.metis.dereference.vocimport.VocabularyCollectionImporterFactory;
import eu.europeana.metis.mongo.connection.MongoClientProvider;
import eu.europeana.metis.mongo.connection.MongoProperties;

import java.util.Arrays;
import java.util.Collections;
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
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Spring configuration class Created by ymamakis on 12-2-16.
 */
@Configuration
@ComponentScan(basePackages = {"eu.europeana.metis.dereference.rest",
    "eu.europeana.metis.dereference.rest.exceptions"})
@PropertySource("classpath:dereferencing.properties")
@EnableWebMvc
@EnableSwagger2
public class Application implements WebMvcConfigurer, InitializingBean {

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
  @Value("${valid.directories.values}")
  private String[] validDirectories;

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
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    return new PropertySourcesPlaceholderConfigurer();
  }

  @Bean
  public VocabularyCollectionImporterFactory getVocabularyCollectionImporterFactory(){
    return new VocabularyCollectionImporterFactory(Arrays.asList(validDirectories));
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

  private ApiInfo apiInfo() {
    return new ApiInfo(
        "Dereference REST API",
        "Dereference REST API for Europeana",
        "v1",
        "API TOS",
        new Contact("development", "europeana.eu", "development@europeana.eu"),
        "EUPL Licence v1.2",
        "https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12", Collections.emptyList());
  }
}
