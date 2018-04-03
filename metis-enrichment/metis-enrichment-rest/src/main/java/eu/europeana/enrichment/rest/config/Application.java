package eu.europeana.enrichment.rest.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import eu.europeana.corelib.web.socks.SocksProxy;
import eu.europeana.enrichment.service.Converter;
import eu.europeana.enrichment.service.Enricher;
import eu.europeana.enrichment.service.EntityRemover;
import eu.europeana.enrichment.service.RedisInternalEnricher;
import eu.europeana.enrichment.utils.EnrichmentEntityDao;
import eu.europeana.metis.cache.redis.RedisProvider;
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
@ComponentScan(basePackages = {"eu.europeana.enrichment.rest",
    "eu.europeana.enrichment.rest.exception"})
@PropertySource("classpath:enrichment.properties")
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

  @Value("${enrichment.mongoDb}")
  private String enrichmentMongo;
  @Value("${enrichment.mongoPort:27017}")
  private int enrichmentMongoPort;

  @Value("${enrichment.proxy.url}")
  private String enrichmentProxyUrl;

  private RedisProvider redisProvider;

  /**
   * Used for overwriting properties if cloud foundry environment is used
   */
  @Override
  public void afterPropertiesSet() {
    if (socksProxyEnabled) {
      new SocksProxy(socksProxyHost, socksProxyPort, socksProxyUsername, socksProxyPassword).init();
    }

    if(redisProvider == null)
      redisProvider = new RedisProvider(redisHost, redisPort, redisPassword);
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
  @DependsOn("redisInternalEnricher")
  Enricher enricher() {
    return new Enricher(getRedisInternalEnricher());
  }

  @Bean
  EnrichmentEntityDao getEntityDao() {
    return new EnrichmentEntityDao(enrichmentMongo, enrichmentMongoPort);
  }
  
  @Bean
  EntityRemover entityRemover() {
    return new EntityRemover(getRedisInternalEnricher(), getEntityDao());
  }
  
  @Bean
  Converter converter() {
    return new Converter();
  }

  @Bean
  RedisProvider getRedisProvider() {
      return redisProvider;
  }

  @Bean(name = "redisInternalEnricher")
  RedisInternalEnricher getRedisInternalEnricher() {
    return new RedisInternalEnricher(getEntityDao(), getRedisProvider(), false);
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
    Contact contact = new Contact("Europeana", "http:\\www.europeana.eu", "development@europeana.eu");

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
