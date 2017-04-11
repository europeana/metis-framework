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

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import eu.europeana.corelib.edm.exceptions.MongoDBException;
import eu.europeana.corelib.edm.utils.construct.FullBeanHandler;
import eu.europeana.corelib.edm.utils.construct.SolrDocumentHandler;
import eu.europeana.corelib.mongo.server.EdmMongoServer;
import eu.europeana.corelib.mongo.server.impl.EdmMongoServerImpl;
import eu.europeana.metis.identifier.RestClient;
import eu.europeana.metis.preview.persistence.RecordDao;
import eu.europeana.metis.preview.service.PreviewService;
import eu.europeana.validation.client.ValidationClient;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.jibx.runtime.JiBXException;
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
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Configuration file for Spring MVC
 */
@ComponentScan(basePackages = {"eu.europeana.metis.preview.rest",
    "eu.europeana.metis.preview.exceptions.handler"})
@PropertySource("classpath:preview.properties")
@EnableWebMvc
@EnableSwagger2
@Configuration
@EnableScheduling
public class Application extends WebMvcConfigurerAdapter implements InitializingBean {

  @Value("${mongo.host}")
  private String mongoHost;
  @Value("${mongo.port}")
  private String mongoPort;
  @Value("${mongo.username}")
  private String mongoUsername;
  @Value("${mongo.password}")
  private String mongoPassword;
  @Value("${mongo.db}")
  private String mongoDb;
  @Value("${preview.portal.url}")
  private String previewPortalUrl;
  @Value("${solr.search.url}")
  private String solrSearchUrl;

  /**
   * Used for overwriting properties if cloud foundry environment is used
   */
  @Override
  public void afterPropertiesSet() throws Exception {
    String vcapServicesJson = System.getenv().get("VCAP_SERVICES");
    if (StringUtils.isNotEmpty(vcapServicesJson) && !StringUtils.equals(vcapServicesJson, "{}")) {
    }
  }

  @Bean
  PreviewService previewService() {
    try {
      return new PreviewService(previewPortalUrl);
    } catch (JiBXException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Bean
  RecordDao recordDao() {
    return new RecordDao();
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
  @DependsOn("edmMongoServer")
  FullBeanHandler fullBeanHandler() {
    return new FullBeanHandler(edmMongoServer());
  }

  @Bean(name = "edmMongoServer")
  EdmMongoServer edmMongoServer() {
    try {
      ServerAddress address = new ServerAddress(mongoHost, Integer.parseInt(mongoPort));
      MongoCredential mongoCredential;
      MongoClient mongoClient;
      if (StringUtils.isNotEmpty(mongoUsername) && StringUtils.isNotEmpty(mongoPassword)) {
        mongoCredential = MongoCredential
            .createCredential(mongoUsername, mongoDb, mongoPassword.toCharArray());
        mongoClient = new MongoClient(address, Arrays.asList(mongoCredential));
      } else {
        mongoClient = new MongoClient(address);
      }

      return new EdmMongoServerImpl(mongoClient, mongoDb);
    } catch (MongoDBException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Bean
  @DependsOn(value = "solrServer")
  SolrDocumentHandler solrDocumentHandler() {
    return new SolrDocumentHandler(solrServer());
  }

  @Bean(name = "solrServer")
  SolrServer solrServer() {
    HttpSolrServer server = new HttpSolrServer(solrSearchUrl);
    return server;
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
  public CommonsMultipartResolver multipartResolver() {
    CommonsMultipartResolver commonsMultipartResolver = new CommonsMultipartResolver();
    commonsMultipartResolver.setDefaultEncoding("utf-8");
    commonsMultipartResolver.setMaxUploadSize(50000000);
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

  private ApiInfo apiInfo() {
    ApiInfo apiInfo = new ApiInfo(
        "Preview REST API",
        "Preview REST API for Europeana",
        "v1",
        "API TOS",
        "development@europeana.eu",
        "EUPL Licence v1.1",
        ""
    );
    return apiInfo;
  }
}
