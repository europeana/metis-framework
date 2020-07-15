package eu.europeana.enrichment.cache.proxy.config;

import com.mongodb.MongoClient;
import eu.europeana.enrichment.service.RedisInternalEnricher;
import eu.europeana.enrichment.utils.EntityDao;
import eu.europeana.enrichment.utils.RedisProvider;
import eu.europeana.metis.mongo.MongoClientProvider;
import eu.europeana.metis.mongo.MongoProperties;
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
 * Spring configuration class
 * Created by ymamakis on 12-2-16.
 */
@Configuration
@ComponentScan(basePackages = {"eu.europeana.enrichment.cache.proxy"})
@PropertySource("classpath:enrichment.proxy.properties")
@EnableWebMvc
@EnableSwagger2
public class Application implements WebMvcConfigurer {

  @Value("${enrichment.mongo.host}")
  private String enrichmentMongoHost;
  @Value("${enrichment.mongo.port:27017}")
  private int enrichmentMongoPort;
  @Value("${enrichment.mongo.database:27017}")
  private String enrichmentMongoDatabase;
  @Value("${redis.host}")
  private String redisHost;
  @Value("${redis.port}")
  private int redisPort;
  @Value("${redis.password:\"\"}")
  private String redisPassword;

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
  RedisProvider getRedisProvider() {
    return new RedisProvider(redisHost, redisPort, redisPassword);
  }

  @Bean
  EntityDao getEntityDao() {
    final MongoProperties<IllegalArgumentException> mongoProperties = new MongoProperties<>(
        IllegalArgumentException::new);
    mongoProperties.setMongoHosts(new String[]{enrichmentMongoHost}, new int[]{enrichmentMongoPort});
    final MongoClient mongoClient = new MongoClientProvider<>(mongoProperties).createMongoClient();
    return new EntityDao(mongoClient, enrichmentMongoDatabase);
  }

  @Bean(name = "redisInternalEnricher")
  RedisInternalEnricher getRedisInternalEnricher() {
    final RedisInternalEnricher enricher = new RedisInternalEnricher(getEntityDao(),
            getRedisProvider());
    new Thread(enricher::recreateCache).start();
    return enricher;
  }

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    return new PropertySourcesPlaceholderConfigurer();
  }

  @Bean
  public Docket api() {
    return new Docket(DocumentationType.SWAGGER_2)
        .select()
        .apis(RequestHandlerSelectors.any())
        .paths(PathSelectors.regex("/.*"))
        .build()
        .apiInfo(apiInfo());
  }

  private ApiInfo apiInfo() {
    return new ApiInfo(
        "Enrichment Cache REST API",
        "Enrichment cache management for Europeana",
        "v1",
        "API TOS",
        new Contact("Europeana", "europeana.eu", "development@europeana.eu"),
        "EUPL Licence v1.1",
        ""
    );
  }
}
