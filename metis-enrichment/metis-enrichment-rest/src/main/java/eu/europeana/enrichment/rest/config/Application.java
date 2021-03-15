package eu.europeana.enrichment.rest.config;

import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.mongodb.client.MongoClient;
import eu.europeana.corelib.web.socks.SocksProxy;
import eu.europeana.enrichment.service.EnrichmentService;
import eu.europeana.enrichment.service.PersistentEntityResolver;
import eu.europeana.enrichment.service.dao.EnrichmentDao;
import eu.europeana.metis.mongo.connection.MongoClientProvider;
import eu.europeana.metis.mongo.connection.MongoProperties;
import javax.annotation.PreDestroy;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
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

@Configuration
@ComponentScan(basePackages = {"eu.europeana.enrichment.rest",
    "eu.europeana.enrichment.rest.exception"})
@PropertySource("classpath:enrichment.properties")
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

  @Value("${enrichment.mongo.host}")
  private String enrichmentMongoHost;
  @Value("${enrichment.mongo.port:27017}")
  private int enrichmentMongoPort;
  @Value("${enrichment.mongo.database}")
  private String enrichmentMongoDatabase;
  @Value("${enrichment.mongo.application.name}")
  private String enrichmentMongoApplicationName;

  private MongoClient mongoClient;

  /**
   * Used for overwriting properties if cloud foundry environment is used
   */
  @Override
  public void afterPropertiesSet() {
    if (socksProxyEnabled) {
      new SocksProxy(socksProxyHost, socksProxyPort, socksProxyUsername, socksProxyPassword).init();
    }
  }

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addRedirectViewController("/", "swagger-ui.html");
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("swagger-ui.html")
        .addResourceLocations("classpath:/META-INF/resources/");
    registry.addResourceHandler("/webjars/**")
        .addResourceLocations("classpath:/META-INF/resources/webjars/");
  }

  @Bean
  EnrichmentService getEnrichmentService(EnrichmentDao enrichmentDao) {
    return new EnrichmentService(new PersistentEntityResolver(enrichmentDao));
  }

  @Bean
  MongoClient getMongoClient() {
    final MongoProperties<IllegalArgumentException> mongoProperties = new MongoProperties<>(
        IllegalArgumentException::new);
    mongoProperties
        .setMongoHosts(new String[]{enrichmentMongoHost}, new int[]{enrichmentMongoPort});
    mongoProperties.setApplicationName(enrichmentMongoApplicationName);
    mongoClient = new MongoClientProvider<>(mongoProperties).createMongoClient();
    return mongoClient;
  }

  @Bean
  EnrichmentDao getEnrichmentDao(MongoClient mongoClient) {
    return new EnrichmentDao(mongoClient, enrichmentMongoDatabase);
  }

  @Bean
  public Jackson2ObjectMapperBuilder objectMapperBuilder() {
    Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
    JaxbAnnotationModule module = new JaxbAnnotationModule();
    builder.modules(module);
    return builder;
  }

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    return new PropertySourcesPlaceholderConfigurer();
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
    Contact contact = new Contact("Europeana", "http:\\www.europeana.eu",
        "development@europeana.eu");

    return new ApiInfo(
        "Enrichment REST API",
        "Enrichment REST API for Europeana",
        "v1",
        "API TOS",
        contact,
        "EUPL Licence v1.1",
        ""
    );
  }
}
