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
import eu.europeana.cloud.mcs.driver.DataSetServiceClient;
import eu.europeana.corelib.storage.impl.MongoProviderImpl;
import eu.europeana.metis.cache.redis.JedisProviderUtils;
import eu.europeana.metis.cache.redis.RedisProvider;
import eu.europeana.metis.core.api.MetisKey;
import eu.europeana.metis.core.dao.AuthorizationDao;
import eu.europeana.metis.core.dao.DatasetDao;
import eu.europeana.metis.core.dao.ExecutionDao;
import eu.europeana.metis.core.dao.FailedRecordsDao;
import eu.europeana.metis.core.dao.OrganizationDao;
import eu.europeana.metis.core.dao.UserWorkflowDao;
import eu.europeana.metis.core.dao.UserWorkflowExecutionDao;
import eu.europeana.metis.core.dao.ZohoClient;
import eu.europeana.metis.core.dao.ecloud.EcloudDatasetDao;
import eu.europeana.metis.core.mail.config.MailConfig;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.core.rest.RequestLimits;
import eu.europeana.metis.core.search.config.SearchApplication;
import eu.europeana.metis.core.search.service.MetisSearchService;
import eu.europeana.metis.core.service.CrmUserService;
import eu.europeana.metis.core.service.DatasetService;
import eu.europeana.metis.core.service.MetisAuthorizationService;
import eu.europeana.metis.core.service.OrchestratorService;
import eu.europeana.metis.core.service.OrganizationService;
import eu.europeana.metis.core.service.UserWorkflowExecutorManager;
import eu.europeana.metis.core.workflow.Execution;
import eu.europeana.metis.core.workflow.FailedRecords;
import eu.europeana.metis.json.CustomObjectMapper;
import eu.europeana.metis.utils.PivotalCloudFoundryServicesReader;
import java.util.List;
import javax.annotation.PreDestroy;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Morphia;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
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

/**
 * Spring configuration class
 * Created by ymamakis on 12-2-16.
 */
@Configuration
@ComponentScan(basePackages = {"eu.europeana.metis.core.rest"})
@PropertySource({"classpath:metis.properties", "classpath:ecloud.properties"})
@EnableWebMvc
@EnableSwagger2
@Import({MailConfig.class, SearchApplication.class})
public class Application extends WebMvcConfigurerAdapter implements InitializingBean {

  //Redis
  @Value("${redis.host}")
  private String redisHost;
  @Value("${redis.port}")
  private int redisPort;
  @Value("${redis.password}")
  private String redisPassword;

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

  //Ecloud
  @Value("${ecloud.baseMcsUrl}")
  private String ecloudBaseMcsUrl;
  @Value("${ecloud.username}")
  private String ecloudUsername;
  @Value("${ecloud.password}")
  private String ecloudPassword;

  private MongoProviderImpl mongoProvider;
  private RedisProvider redisProvider;

  @Autowired
  private ZohoRestConfig zohoRestConfig;

  /**
   * Used for overwriting properties if cloud foundry environment is used
   */
  @Override
  public void afterPropertiesSet() throws Exception {
    String vcapServicesJson = System.getenv().get("VCAP_SERVICES");
    if (StringUtils.isNotEmpty(vcapServicesJson) && !StringUtils.equals(vcapServicesJson, "{}")) {
      PivotalCloudFoundryServicesReader vcapServices = new PivotalCloudFoundryServicesReader(
          vcapServicesJson);

      MongoClientURI mongoClientURI = vcapServices.getMongoClientUriFromService();
      if (mongoClientURI != null) {
        String mongoHostAndPort = mongoClientURI.getHosts().get(0);
        mongoHosts = mongoHostAndPort.substring(0, mongoHostAndPort.lastIndexOf(":"));
        mongoPort = Integer
            .parseInt(mongoHostAndPort.substring(mongoHostAndPort.lastIndexOf(":") + 1));
        mongoUsername = mongoClientURI.getUsername();
        mongoPassword = String.valueOf(mongoClientURI.getPassword());
        mongoDb = mongoClientURI.getDatabase();
      }

      RedisProvider redisProviderFromService = vcapServices.getRedisProviderFromService();
      if (redisProviderFromService != null) {
        redisProvider = vcapServices.getRedisProviderFromService();
      }
    }

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

    if (redisProvider == null) {
      redisProvider = new RedisProvider(redisHost, redisPort, redisPassword);
    }
  }


  @Bean
  @Scope("singleton")
  MorphiaDatastoreProvider getMorphiaDatastoreProvider() {
    return new MorphiaDatastoreProvider(mongoProvider.getMongo(), mongoDb);
  }

  @Bean
  ZohoClient getZohoRestClient() {
    return zohoRestConfig.getZohoClient();
  }

  @Bean(name = "jedisProviderUtils")
  JedisProviderUtils getJedisProviderUtils() {
    return new JedisProviderUtils(redisProvider.getJedis());
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

  @Bean
  public ExecutionDao getExecutionDao(MorphiaDatastoreProvider morphiaDatastoreProvider) {
    Morphia morphia = new Morphia();
    morphia.map(Execution.class);
    return new ExecutionDao(morphiaDatastoreProvider.getDatastore().getMongo(), morphia,
        morphiaDatastoreProvider.getDatastore().getDB().getName());
  }

  @Bean
  public FailedRecordsDao getFailedRecordsDao(MorphiaDatastoreProvider morphiaDatastoreProvider) {
    Morphia morphia = new Morphia();
    morphia.map(FailedRecords.class);
    return new FailedRecordsDao(morphiaDatastoreProvider.getDatastore().getMongo(), morphia,
        morphiaDatastoreProvider.getDatastore().getDB().getName());
  }

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
  public UserWorkflowExecutionDao getUserWorkflowExecutionDao(
      MorphiaDatastoreProvider morphiaDatastoreProvider) {
    return new UserWorkflowExecutionDao(morphiaDatastoreProvider);
  }

  @Bean
  public UserWorkflowDao getUserWorkflowDao(MorphiaDatastoreProvider morphiaDatastoreProvider) {
    return new UserWorkflowDao(morphiaDatastoreProvider);
  }

  @Bean
  DataSetServiceClient dataSetServiceClient() {
    return new DataSetServiceClient(ecloudBaseMcsUrl, ecloudUsername, ecloudPassword);
  }

  @Bean
  EcloudDatasetDao ecloudDatasetDao(DataSetServiceClient dataSetServiceClient) {
    return new EcloudDatasetDao(dataSetServiceClient);
  }

  @Bean
  public OrganizationDao getOrganizationDao(MorphiaDatastoreProvider morphiaDatastoreProvider) {
    OrganizationDao organizationDao = new OrganizationDao(morphiaDatastoreProvider);
    organizationDao.setOrganizationsPerRequest(RequestLimits.ORGANIZATIONS_PER_REQUEST.getLimit());
    return organizationDao;
  }

  @Bean
  public DatasetService getDatasetService(DatasetDao datasetDao, EcloudDatasetDao ecloudDatasetDao,
      OrganizationDao organizationDao) {
    return new DatasetService(datasetDao, ecloudDatasetDao, organizationDao);
  }

  @Bean
  public OrganizationService getOrganizationService(OrganizationDao organizationDao,
      DatasetDao datasetDao, ZohoClient zohoClient, MetisSearchService metisSearchService) {
    return new OrganizationService(organizationDao, datasetDao, zohoClient, metisSearchService);
  }

  @Bean
  public MetisAuthorizationService getMetisAuthorizationService() {
    return new MetisAuthorizationService();
  }

  @Bean
  public CrmUserService getCrmUserService(ZohoClient zohoClient) {
    return new CrmUserService(zohoClient);
  }

//  @Bean
//  public VoidMetisPlugin getVoidMetisWorkflow() {
//    return new VoidMetisPlugin(10000);
//  }
//
//  @Bean
//  public VoidHTTPHarvestPlugin getVoidHTTPHarvestPlugin() {
//    return new VoidHTTPHarvestPlugin(10000);
//  }
//
//  @Bean
//  public VoidOaipmhHarvestPlugin getVoidOaipmhHarvestPlugin() {
//    return new VoidOaipmhHarvestPlugin(10000);
//  }

//  @Bean
//  @DependsOn("jedisProviderUtils")
//  public QAPlugin getStatisticsWorkflow() {
//    return new QAPlugin();
//  }

//  @Bean
//  @Qualifier("metisPluginRegistry")
//  public PluginRegistry<AbstractMetisPlugin, PluginType> getPluginRegistry(
//      VoidHTTPHarvestPlugin voidHTTPHarvestPlugin, VoidOaipmhHarvestPlugin voidOaipmhHarvestPlugin,
//      VoidMetisPlugin voidMetisPlugin) {
//
//    ArrayList<AbstractMetisPlugin> abstractMetisPlugins = new ArrayList<>();
//    abstractMetisPlugins.add(voidMetisPlugin);
//    abstractMetisPlugins.add(voidHTTPHarvestPlugin);
//    abstractMetisPlugins.add(voidOaipmhHarvestPlugin);
//
//    return SimplePluginRegistry.create(abstractMetisPlugins);
//  }

//  @Bean
//  public OrchestratorService getOrchestrator(ExecutionDao executionDao,
//      DatasetService datasetService,
//      FailedRecordsDao failedRecordsDao,
//      @Qualifier("metisPluginRegistry") PluginRegistry<AbstractMetisPlugin, PluginType> pluginRegistry) {
//    return new OrchestratorService(executionDao, datasetService, failedRecordsDao,
//        pluginRegistry);
//  }

  @Bean
  public UserWorkflowExecutorManager getUserWorkflowExecutorManager(UserWorkflowExecutionDao userWorkflowExecutionDao) {
    return new UserWorkflowExecutorManager(userWorkflowExecutionDao);
  }

  @Bean
  public OrchestratorService getOrchestratorService(UserWorkflowDao userWorkflowDao,
      UserWorkflowExecutionDao userWorkflowExecutionDao, ExecutionDao executionDao,
      DatasetDao datasetDao,
      FailedRecordsDao failedRecordsDao,
      UserWorkflowExecutorManager userWorkflowExecutorManager) {
    return new OrchestratorService(userWorkflowDao, userWorkflowExecutionDao, executionDao,
        datasetDao, failedRecordsDao, userWorkflowExecutorManager);
  }

  @Override
  public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    converters.add(new MappingJackson2HttpMessageConverter());
    converters.add(new MappingJackson2XmlHttpMessageConverter());
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

  @PreDestroy
  public void close() {
    if (mongoProvider != null) {
      mongoProvider.close();
    }
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
