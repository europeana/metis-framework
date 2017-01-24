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
package eu.europeana.metis.framework.rest.config;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import eu.europeana.metis.framework.cache.JedisProvider;
import eu.europeana.metis.framework.dao.*;
import eu.europeana.metis.framework.mongo.MongoProvider;
import eu.europeana.metis.framework.rest.RestConfig;
import eu.europeana.metis.framework.service.DatasetService;
import eu.europeana.metis.framework.service.Orchestrator;
import eu.europeana.metis.framework.service.OrganizationService;
import eu.europeana.metis.framework.service.UserService;
import eu.europeana.metis.framework.workflow.AbstractMetisWorkflow;
import eu.europeana.metis.framework.workflow.Execution;
import eu.europeana.metis.framework.workflow.FailedRecords;
import eu.europeana.metis.framework.workflow.VoidMetisWorkflow;
import eu.europeana.metis.mail.config.MailConfig;
import eu.europeana.metis.workflow.qa.QAWorkflow;
import org.apache.commons.lang.StringUtils;
import org.mongodb.morphia.Morphia;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.plugin.core.config.EnablePluginRegistries;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.net.UnknownHostException;
import java.util.List;

/**
 * Spring configuration class
 * Created by ymamakis on 12-2-16.
 */
@Configuration
@ComponentScan (basePackages = {"eu.europeana.metis.framework.rest"})
@PropertySource("classpath:metis.properties")
@EnableWebMvc
@EnableSwagger2
@EnablePluginRegistries(AbstractMetisWorkflow.class)
@EnableScheduling
@Import({MailConfig.class})
public class Application extends WebMvcConfigurerAdapter {

    @Value("${mongo.host}")
    private String mongoHost;
    @Value("${mongo.port}")
    private String mongoPort;
    @Value("${mongo.db}")
    private String db;
    @Value("${mongo.username}")
    private String username;
    @Value("${mongo.pass}")
    private String password;
    private MongoProvider provider;
    @Value ("${redis.host}")
    private String redisHost;
    @Value("${redis.port}")
    private int redisPort;
    @Value("${redis.password}")
    private String redisPassword;
    @Autowired
    @Lazy
    private RestConfig restConfig;

    @Override
    public  void configureMessageConverters(List<HttpMessageConverter<?>> converters){

        converters.add(new MappingJackson2HttpMessageConverter());

        super.configureMessageConverters(converters);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
    @Bean(name = "mongoProvider")
    MongoProvider getMongoProvider(){
        try {
            if(System.getenv().get("VCAP_SERVICES")==null) {
                provider = new MongoProvider(mongoHost, Integer.parseInt(mongoPort), db, username, password);
                return provider;
            } else {
                JsonParser parser = new JsonParser();
                JsonObject object = parser.parse(System.getenv().get("VCAP_SERVICES")).getAsJsonObject();
                JsonObject element = object.getAsJsonArray("mlab").get(0).getAsJsonObject();

                JsonObject credentials = element.getAsJsonObject("credentials");
                JsonPrimitive uri = credentials.getAsJsonPrimitive("uri");
                String db = StringUtils.substringAfterLast(uri.getAsString(),"/");
                provider = new MongoProvider(uri.getAsString(),db);
                return provider;
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Bean
    @Order(100)
    ZohoClient getZohoRestClient(){
        return restConfig.getZohoClient();
    }
    @Bean
    public static PropertySourcesPlaceholderConfigurer properties() {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        propertySourcesPlaceholderConfigurer.setLocation(new ClassPathResource("metis.properties"));
        return propertySourcesPlaceholderConfigurer;
    }

    @Bean
    JedisProvider getJedisProvider(){
        if(System.getenv().get("VCAP_SERVICES")!=null){
            JsonParser parser = new JsonParser();
            JsonObject object = parser.parse(System.getenv().get("VCAP_SERVICES")).getAsJsonObject();
            JsonObject element = object.getAsJsonArray("rediscloud").get(0).getAsJsonObject();
            JsonObject credentials = element.getAsJsonObject("credentials");
            redisHost = credentials.getAsJsonPrimitive("hostname").getAsString();
            redisPassword = credentials.getAsJsonPrimitive("password").getAsString();
            redisPort = credentials.getAsJsonPrimitive("port").getAsInt();

        }
        return new JedisProvider(redisHost,redisPassword,redisPort);
    }

    @Bean
    @DependsOn(value = "mongoProvider")
    public ExecutionDao getExecutionDao(){
        Morphia morphia = new Morphia();
        morphia.map(Execution.class);
        return new ExecutionDao(provider.getDatastore().getMongo(),morphia,provider.getDatastore().getDB().getName());
    }
    @Bean
    @DependsOn(value = "mongoProvider")
    public FailedRecordsDao getFailedRecordsDao(){
        Morphia morphia = new Morphia();
        morphia.map(FailedRecords.class);
        return new FailedRecordsDao(provider.getDatastore().getMongo(),morphia,provider.getDatastore().getDB().getName());
    }
    @Bean
    public DatasetDao getDatasetDao(){
        return new DatasetDao();
    }

    @Bean
    public OrganizationDao getOrganizationDao(){
        return new OrganizationDao();
    }

    @Bean
    public DatasetService getDatasetService(){
        return new DatasetService();
    }

    @Bean
    public OrganizationService getOrganizationService(){
        return new OrganizationService();
    }

    @Bean
    public UserService getUserService(){
        return new UserService();
    }

    @Bean
    public Orchestrator getOrchestrator(){
        return new Orchestrator();
    }

    @Bean
    public VoidMetisWorkflow getVoidMetisWorkflow(){
        return new VoidMetisWorkflow();
    }

    @Bean
    public QAWorkflow getStatisticsWorkflow(){
        return new QAWorkflow();
    }
    @Bean
    public Docket api(){
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.regex("/.*"))
                .build()
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        ApiInfo apiInfo = new ApiInfo(
                "Metis framework REST API",
                "Metis framework REST API for Europeana",
                "v1",
                "API TOS",
                "development@europeana.eu",
                "EUPL Licence v1.1",
                ""
        );
        return apiInfo;
    }
}
