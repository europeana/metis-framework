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
package eu.europeana.metis.core.rest.config;


import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import eu.europeana.corelib.storage.impl.MongoProviderImpl;
import eu.europeana.corelib.web.socks.SocksProxy;
import eu.europeana.metis.core.api.MetisKey;
import eu.europeana.metis.core.dao.AuthorizationDao;
import eu.europeana.metis.core.dao.DatasetDao;
import eu.europeana.metis.core.dao.OrganizationDao;
import eu.europeana.metis.core.dao.ScheduledWorkflowDao;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.core.rest.RequestLimits;
import eu.europeana.metis.core.search.config.SearchApplication;
import eu.europeana.metis.core.search.service.MetisSearchService;
import eu.europeana.metis.core.service.DatasetService;
import eu.europeana.metis.core.service.MetisAuthorizationService;
import eu.europeana.metis.core.service.OrganizationService;
import eu.europeana.metis.json.CustomObjectMapper;
import eu.europeana.metis.utils.PivotalCloudFoundryServicesReader;
import java.util.List;
import javax.annotation.PreDestroy;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Morphia;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.BeanNameViewResolver;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@ComponentScan(basePackages = {"eu.europeana.metis.core.rest"})
@PropertySource({"classpath:metis.properties"})
@EnableWebMvc
@EnableSwagger2
@Import({SearchApplication.class, OrchestratorConfig.class, ECloudConfig.class})
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

  //Mongo
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

    checkAndSetCloudFoundryProperties();

    String[] mongoHostsArray = mongoHosts.split(",");
    StringBuilder mongoPorts = new StringBuilder();
    for (String aMongoHostsArray : mongoHostsArray) {
      mongoPorts.append(mongoPort).append(",");
    }
    mongoPorts.replace(mongoPorts.lastIndexOf(","), mongoPorts.lastIndexOf(","), "");
    MongoClientOptions.Builder options = MongoClientOptions.builder();
    options.socketKeepAlive(true);
    mongoProvider = new MongoProviderImpl(mongoHosts, mongoPorts.toString(), mongoDb, mongoUsername,
        mongoPassword, options);
  }

  private void checkAndSetCloudFoundryProperties() {
    String vcapServicesJson = System.getenv().get("VCAP_SERVICES");
    if (StringUtils.isNotEmpty(vcapServicesJson) && !StringUtils.equals(vcapServicesJson, "{}")) {
      PivotalCloudFoundryServicesReader vcapServices = new PivotalCloudFoundryServicesReader(
          vcapServicesJson);

      MongoClientURI mongoClientURI = vcapServices.getMongoClientUriFromService();
      if (mongoClientURI != null) {
        String mongoHostAndPort = mongoClientURI.getHosts().get(0);
        mongoHosts = mongoHostAndPort.substring(0, mongoHostAndPort.lastIndexOf(':'));
        mongoPort = Integer
            .parseInt(mongoHostAndPort.substring(mongoHostAndPort.lastIndexOf(':') + 1));
        mongoUsername = mongoClientURI.getUsername();
        mongoPassword = String.valueOf(mongoClientURI.getPassword());
        mongoDb = mongoClientURI.getDatabase();
      }
    }
  }

  @Bean
  MorphiaDatastoreProvider getMorphiaDatastoreProvider() {
    return new MorphiaDatastoreProvider(mongoProvider.getMongo(), mongoDb);
  }

  // TODO: 22-11-17 Needs to be removed and handled in the metis-authentication service
  @Bean
  public AuthorizationDao getAuthorizationDao() {
    Morphia morphia = new Morphia();
    morphia.map(MetisKey.class);
    return new AuthorizationDao();
  }

  @Bean
  public DatasetDao getDatasetDao(MorphiaDatastoreProvider morphiaDatastoreProvider) {
    DatasetDao datasetDao = new DatasetDao(morphiaDatastoreProvider);
    datasetDao.setDatasetsPerRequest(RequestLimits.DATASETS_PER_REQUEST.getLimit());
    return datasetDao;
  }

  @Bean
  public DatasetService getDatasetService(DatasetDao datasetDao,
      OrganizationDao organizationDao,
      WorkflowExecutionDao workflowExecutionDao,
      ScheduledWorkflowDao scheduledWorkflowDao) {
    return new DatasetService(datasetDao, organizationDao, workflowExecutionDao,
        scheduledWorkflowDao);
  }

  @Bean
  public OrganizationDao getOrganizationDao(MorphiaDatastoreProvider morphiaDatastoreProvider) {
    OrganizationDao organizationDao = new OrganizationDao(morphiaDatastoreProvider);
    organizationDao.setOrganizationsPerRequest(RequestLimits.ORGANIZATIONS_PER_REQUEST.getLimit());
    return organizationDao;
  }

  @Bean
  public OrganizationService getOrganizationService(OrganizationDao organizationDao,
      DatasetDao datasetDao, MetisSearchService metisSearchService) {
    return new OrganizationService(organizationDao, datasetDao, metisSearchService);
  }

  @Bean
  public MetisAuthorizationService getMetisAuthorizationService() {
    return new MetisAuthorizationService();
  }

  @PreDestroy
  public void close() {
    if (mongoProvider != null) {
      mongoProvider.close();
    }
  }

  @Bean
  public View json() {
    MappingJackson2JsonView view = new MappingJackson2JsonView();
    view.setPrettyPrint(true);
    view.setObjectMapper(new CustomObjectMapper());
    return view;
  }

  @Bean
  public ViewResolver viewResolver() {
    return new BeanNameViewResolver();
  }

  @Override
  public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    converters.add(new MappingJackson2HttpMessageConverter());
    converters.add(new MappingJackson2XmlHttpMessageConverter());
    super.configureMessageConverters(converters);
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
        .directModelSubstitute(ObjectId.class, String.class)
        .useDefaultResponseMessages(false)
        .apiInfo(apiInfo());
  }

  private ApiInfo apiInfo() {
    return new ApiInfo(
        "Metis framework REST API",
        "Metis framework REST API for Europeana",
        "v1",
        "API TOS",
        new Contact("development", "europeana.eu", "development@europeana.eu"),
        "EUPL Licence v1.1",
        ""
    );
  }
}
