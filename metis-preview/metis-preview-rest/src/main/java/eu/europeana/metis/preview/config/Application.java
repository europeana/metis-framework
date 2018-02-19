/*
 * Copyright 2007-2013 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */
package eu.europeana.metis.preview.config;

import java.util.List;
import javax.annotation.PreDestroy;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import com.mongodb.MongoClientOptions;
import eu.europeana.corelib.edm.exceptions.MongoDBException;
import eu.europeana.corelib.edm.utils.construct.FullBeanHandler;
import eu.europeana.corelib.edm.utils.construct.SolrDocumentHandler;
import eu.europeana.corelib.mongo.server.EdmMongoServer;
import eu.europeana.corelib.mongo.server.impl.EdmMongoServerImpl;
import eu.europeana.corelib.storage.impl.MongoProviderImpl;
import eu.europeana.corelib.web.socks.SocksProxy;
import eu.europeana.metis.identifier.RestClient;
import eu.europeana.metis.preview.persistence.RecordDao;
import eu.europeana.metis.preview.service.ZipService;
import eu.europeana.metis.preview.service.executor.ValidationUtils;
import eu.europeana.validation.client.ValidationClient;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Configuration file for Spring MVC
 */
@Configuration
@ComponentScan(basePackages = {"eu.europeana.metis.preview.rest", "eu.europeana.metis.preview.service", "eu.europeana.metis.preview" })
@PropertySource("classpath:preview.properties")
@EnableWebMvc
@EnableSwagger2
@EnableScheduling
public class Application extends WebMvcConfigurerAdapter implements InitializingBean{

  private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);
  
  private static final int MAX_UPLOAD_SIZE = 50_000_000;

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
  @Value("${solr.search.url}")
  private String solrSearchUrl;

  @Value("${validation.schema.before_transformation}")
  private String schemaBeforeTransformation;
  @Value("${validation.schema.after_transformation}")
  private String schemaAfterTransformation;

  @Value("${transformation.default}")
  private String defaultTransformationFile;

  private MongoProviderImpl mongoProvider;
  private HttpSolrServer solrServer;

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
    mongoProvider = new MongoProviderImpl(mongoHosts, mongoPorts.toString(), mongoDb, mongoUsername,
        mongoPassword, options);
  }

  @Override
  public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    converters.add(new MappingJackson2HttpMessageConverter());
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
  RestClient restClient() {
    return new RestClient();
  }

  @Bean
  ValidationClient validationClient() {
    return new ValidationClient();
  }

  @Bean
  ZipService zipService(){ return new ZipService();}

  @Bean
  @DependsOn("edmMongoServer")
  FullBeanHandler fullBeanHandler() throws MongoDBException {
    return new FullBeanHandler(edmMongoServer());
  }

  @Bean(name = "edmMongoServer")
  EdmMongoServer edmMongoServer() throws MongoDBException {
    return new EdmMongoServerImpl(mongoProvider.getMongo(), mongoDb);
  }

  @Bean
  @DependsOn(value = "solrServer")
  SolrDocumentHandler solrDocumentHandler() {
    return new SolrDocumentHandler(solrServer());
  }

  @Bean(name = "solrServer")
  SolrServer solrServer() {
    solrServer = new HttpSolrServer(solrSearchUrl);
    return solrServer;
  }

  @Bean
  @DependsOn(value = "solrServer")
  RecordDao recordDao() throws MongoDBException {
    return new RecordDao(fullBeanHandler(), solrDocumentHandler(), solrServer(), edmMongoServer());
  }

  @Bean
  public CommonsMultipartResolver multipartResolver() {
    CommonsMultipartResolver commonsMultipartResolver = new CommonsMultipartResolver();
    commonsMultipartResolver.setDefaultEncoding("utf-8");
    commonsMultipartResolver.setMaxUploadSize(MAX_UPLOAD_SIZE);
    return commonsMultipartResolver;
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

  @Bean
  public ValidationUtils getValidationUtils() throws MongoDBException {
    return new ValidationUtils(restClient(), validationClient(), recordDao(),
        schemaBeforeTransformation, schemaAfterTransformation, defaultTransformationFile);
  }

  @PreDestroy
  public void close() {
    LOGGER.info("Closing connections..");
    if (mongoProvider != null) {
      mongoProvider.close();
    }
    if (solrServer != null) {
      solrServer.shutdown();
    }
  }

  private ApiInfo apiInfo() {
    return new ApiInfo(
        "Preview REST API",
        "Preview REST API for Europeana",
        "v1",
        "API TOS",
        new Contact("development", "europeana.eu", "development@europeana.eu"),
        "EUPL Licence v1.1",
        ""
    );
  }


}
