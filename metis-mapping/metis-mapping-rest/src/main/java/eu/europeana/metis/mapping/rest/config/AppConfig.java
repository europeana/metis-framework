package eu.europeana.metis.mapping.rest.config;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import eu.europeana.corelib.storage.impl.MongoProviderImpl;
import eu.europeana.corelib.web.socks.SocksProxy;
import eu.europeana.metis.json.CustomObjectMapper;
import eu.europeana.metis.mapping.persistence.AttributeDao;
import eu.europeana.metis.mapping.persistence.DatasetStatisticsDao;
import eu.europeana.metis.mapping.persistence.ElementDao;
import eu.europeana.metis.mapping.persistence.FlagDao;
import eu.europeana.metis.mapping.persistence.MappingSchemaDao;
import eu.europeana.metis.mapping.persistence.MappingsDao;
import eu.europeana.metis.mapping.persistence.MongoMappingDao;
import eu.europeana.metis.mapping.persistence.StatisticsDao;
import eu.europeana.metis.service.MongoMappingService;
import eu.europeana.metis.service.StatisticsService;
import eu.europeana.metis.service.ValidationService;
import eu.europeana.metis.service.XSDService;
import eu.europeana.metis.service.XSLTGenerationService;
import java.util.List;
import javax.annotation.PreDestroy;
import org.mongodb.morphia.Morphia;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * The Spring application configuration
 * Created by ymamakis on 6/13/16.
 */
@Configuration
@EnableWebMvc
@EnableSwagger2
@ComponentScan(basePackages = {"eu.europeana.metis.mapping.rest.controllers"})
@PropertySource("classpath:mapping.properties")
public class AppConfig extends WebMvcConfigurerAdapter implements InitializingBean {

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

  @Value("${mongo.hosts}")
  private String mongoHosts;
  @Value("${mongo.port}")
  private int mongoPort;
  @Value("${mongo.username}")
  private String mongoUsername;
  @Value("${mongo.password}")
  private String mongoPassword;
  @Value("${mongo.db}")
  private String mongoDb;

  private MongoProviderImpl mongoProvider;

  /**
   * Used for overwriting properties if cloud foundry environment is used
   */
  @Override
  public void afterPropertiesSet() throws Exception {
    if (socksProxyEnabled) {
      new SocksProxy(socksProxyHost, socksProxyPort, socksProxyUsername, socksProxyPassword).init();
    }

    String[] mongoHostsArray = mongoHosts.split(",");
    StringBuilder mongoPorts = new StringBuilder();
    for (int i = 0; i < mongoHostsArray.length; i++) {
      mongoPorts.append(mongoPort + ",");
    }
    mongoPorts.replace(mongoPorts.lastIndexOf(","), mongoPorts.lastIndexOf(","), "");
    MongoClientOptions.Builder options = MongoClientOptions.builder();
    options.socketKeepAlive(true);
    mongoProvider = new MongoProviderImpl(mongoHosts, mongoPorts.toString(), mongoDb, mongoUsername,
        mongoPassword, options);
  }

  @Bean
  XSDService getXsdService() {
    return new XSDService();
  }

  @Bean
  MongoMappingService getMongoMappingService() {
    return new MongoMappingService();
  }

  @Bean
  MongoMappingDao getMongoMappingDao() {
    Morphia morphia = new Morphia();
    MongoClient client = mongoProvider.getMongo();
    morphia.mapPackage("eu.europeana.metis.mapping.common", true)
        .mapPackage("java.math.BigInteger", true);
    return new MongoMappingDao(morphia, client, mongoDb);
  }

  @Bean
  MappingsDao getMappingsDao() {
    Morphia morphia = new Morphia();
    MongoClient client = mongoProvider.getMongo();
    morphia.mapPackage("eu.europeana.metis.mapping.common", true)
        .mapPackage("java.math.BigInteger", true);
    return new MappingsDao(morphia, client, mongoDb);
  }

  @Bean
  ElementDao getElementDao() {
    Morphia morphia = new Morphia();
    MongoClient client = mongoProvider.getMongo();
    morphia.mapPackage("eu.europeana.metis.mapping.common", true)
        .mapPackage("java.math.BigInteger", true);
    return new ElementDao(morphia, client, mongoDb);
  }

  @Bean
  AttributeDao getAttributeDao() {
    Morphia morphia = new Morphia();
    MongoClient client = mongoProvider.getMongo();
    morphia.mapPackage("eu.europeana.metis.mapping.common", true)
        .mapPackage("java.math.BigInteger", true);
    return new AttributeDao(morphia, client, mongoDb);
  }

  @Bean
  MappingSchemaDao getMappingSchemaDao() {
    Morphia morphia = new Morphia();
    MongoClient client = mongoProvider.getMongo();
    morphia.mapPackage("eu.europeana.metis.mapping.common", true)
        .mapPackage("java.math.BigInteger", true);
    return new MappingSchemaDao(morphia, client, mongoDb);
  }

  @Bean
  public CommonsMultipartResolver multipartResolver() {
    CommonsMultipartResolver commonsMultipartResolver = new CommonsMultipartResolver();
    commonsMultipartResolver.setDefaultEncoding("utf-8");
    commonsMultipartResolver.setMaxUploadSize(50000000);
    return commonsMultipartResolver;
  }

  @Bean
  FlagDao getFlagDao() {
    Morphia morphia = new Morphia();
    MongoClient client = mongoProvider.getMongo();
    morphia.mapPackage("eu.europeana.metis.mapping.validation", true)
        .mapPackage("eu.europeana.metis.mapping.common", true)
        .mapPackage("java.math.BigInteger", true);

    return new FlagDao(morphia, client, mongoDb);
  }

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    return new PropertySourcesPlaceholderConfigurer();
  }

  @Bean
  DatasetStatisticsDao getDatasetStatisticsDao() {
    Morphia morphia = new Morphia();
    MongoClient client = mongoProvider.getMongo();
    morphia.mapPackage("eu.europeana.metis.mapping.statistics", true)
        .mapPackage("eu.europeana.metis.mapping.model", true)
        .mapPackage("java.math.BigInteger", true);
    return new DatasetStatisticsDao(morphia, client, mongoDb);
  }

  @Bean
  StatisticsDao getStatisticsDao() {
    Morphia morphia = new Morphia();
    MongoClient client = mongoProvider.getMongo();
    morphia.mapPackage("eu.europeana.metis.mapping.statistics", true)
        .mapPackage("eu.europeana.metis.mapping.model", true)
        .mapPackage("java.math.BigInteger", true);
    return new StatisticsDao(morphia, client, mongoDb);
  }

  @Bean
  StatisticsService getStatisticsService() {
    return new StatisticsService();
  }

  @Bean
  ValidationService getValidationService() {
    return new ValidationService();
  }

  @Bean
  XSLTGenerationService getXsltGenerationService() {
    return new XSLTGenerationService();
  }

  @Override
  public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    MappingJackson2HttpMessageConverter conv = new MappingJackson2HttpMessageConverter();
    conv.setObjectMapper(new CustomObjectMapper());
    converters.add(conv);

    super.configureMessageConverters(converters);
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("swagger-ui.html")
        .addResourceLocations("classpath:/META-INF/resources/");
    registry.addResourceHandler("/webjars/**")
        .addResourceLocations("classpath:/META-INF/resources/webjars/");
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

  @PreDestroy
  public void close()
  {
    if (mongoProvider != null)
      mongoProvider.close();
  }

  private ApiInfo apiInfo() {
    ApiInfo apiInfo = new ApiInfo(
        "Mapping REST API",
        "Mapping REST API for Europeana",
        "v1",
        "API TOS",
        "development@europeana.eu",
        "EUPL Licence v1.1",
        ""
    );
    return apiInfo;
  }

}