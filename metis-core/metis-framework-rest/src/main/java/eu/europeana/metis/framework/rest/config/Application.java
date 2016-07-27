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

import eu.europeana.metis.framework.dao.DatasetDao;
import eu.europeana.metis.framework.dao.OrganizationDao;
import eu.europeana.metis.framework.dao.ZohoClient;
import eu.europeana.metis.framework.mongo.MongoProvider;
import eu.europeana.metis.framework.service.DatasetService;
import eu.europeana.metis.framework.service.OrganizationService;
import eu.europeana.metis.framework.rest.RestConfig;
import eu.europeana.metis.framework.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
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
    @Bean
    MongoProvider getMongoProvider(){
        try {
            return  new MongoProvider(mongoHost,Integer.parseInt(mongoPort),db,username,password);
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
