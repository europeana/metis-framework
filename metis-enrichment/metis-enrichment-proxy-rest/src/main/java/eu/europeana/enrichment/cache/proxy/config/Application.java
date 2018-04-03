package eu.europeana.enrichment.cache.proxy.config;

import eu.europeana.enrichment.service.RedisInternalEnricher;
import eu.europeana.enrichment.utils.EnrichmentEntityDao;
import eu.europeana.metis.cache.redis.RedisProvider;
import org.apache.commons.lang.StringUtils;
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
@ComponentScan(basePackages = {"eu.europeana.enrichment.cache.proxy"})
@PropertySource("classpath:enrichment.proxy.properties")
@EnableWebMvc
@EnableSwagger2
public class Application extends WebMvcConfigurerAdapter implements InitializingBean {

  @Value("${enrichment.mongo}")
  private String enrichmentMongo;
  @Value("${enrichment.mongoPort:27017}")
  private int enrichmentPort;
  @Value("${redis.host}")
  private String redisHost;
  @Value("${redis.port}")
  private int redisPort;
  @Value("${redis.password:\"\"}")
  private String redisPassword;

  private RedisProvider redisProvider;

  /**
   * Used for overwriting properties if cloud foundry environment is used
   */
  @Override
  public void afterPropertiesSet() throws Exception {
    String vcapServicesJson = System.getenv().get("VCAP_SERVICES");
    if (StringUtils.isNotEmpty(vcapServicesJson) && !StringUtils.equals(vcapServicesJson, "{}")) {
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
  RedisProvider getRedisProvider() {
    redisProvider = new RedisProvider(redisHost, redisPort, redisPassword);
    return redisProvider;
  }

  @Bean
  EnrichmentEntityDao getEntityDao() {
    return new EnrichmentEntityDao(enrichmentMongo, enrichmentPort);
  }

  @Bean(name = "redisInternalEnricher")
  RedisInternalEnricher getRedisInternalEnricher() {
    return new RedisInternalEnricher(getEntityDao(), getRedisProvider(), true);
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
    Contact contact = new Contact("Europeana", "http://www.europeana.eu", "development@europeana.eu");
    ApiInfo apiInfo = new ApiInfo(
        "Enrichment Cache REST API",
        "Enrichment cache management for Europeana",
        "v1",
        "API TOS",
        contact,
        "EUPL Licence v1.1",
        ""
    );
    return apiInfo;
  }
}
