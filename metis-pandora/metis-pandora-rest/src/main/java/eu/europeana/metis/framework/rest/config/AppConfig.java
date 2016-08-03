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
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import eu.europeana.metis.json.CustomObjectMapper;
import eu.europeana.metis.mapping.persistence.DatasetStatisticsDao;
import eu.europeana.metis.mapping.persistence.FlagDao;
import eu.europeana.metis.mapping.persistence.MongoMappingDao;
import eu.europeana.metis.service.*;
import org.apache.commons.lang.StringUtils;
import org.mongodb.morphia.Morphia;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
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

import java.util.List;

/**
 * The Spring application configuration
 * Created by ymamakis on 6/13/16.
 */
@Configuration
@EnableWebMvc
@EnableSwagger2
@ComponentScan(basePackages = {"eu.europeana.metis.framework.rest.controllers"})
@PropertySource("classpath:mapping.properties")
public class AppConfig extends WebMvcConfigurerAdapter {

    @Value("mongo.uri")
    static String uri;
    @Value("mongo.db")
    static String db;

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
        MongoClient client = new MongoClient(new MongoClientURI(uri));
        morphia.mapPackage("eu.europeana.metis.mapping.common", true)
                .mapPackage("java.math.BigInteger",true);
        return new MongoMappingDao(morphia, client, db);
    }
    @Bean
    public CommonsMultipartResolver multipartResolver(){
        CommonsMultipartResolver commonsMultipartResolver = new CommonsMultipartResolver();
        commonsMultipartResolver.setDefaultEncoding("utf-8");
        commonsMultipartResolver.setMaxUploadSize(50000000);
        return commonsMultipartResolver;
    }
    @Bean
    FlagDao getFlagDao() {
        Morphia morphia = new Morphia();
        MongoClient client = new MongoClient(new MongoClientURI(uri));
        morphia.mapPackage("eu.europeana.metis.mapping.validation", true)
                .mapPackage("eu.europeana.metis.mapping.common", true)
                .mapPackage("java.math.BigInteger",true);

        return new FlagDao(morphia, client, db);
    }

    @Bean
    @Order(1)
    public static PropertySourcesPlaceholderConfigurer properties() {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        if (System.getenv().get("VCAP_SERVICES") == null) {
            propertySourcesPlaceholderConfigurer.setLocation(new ClassPathResource("mapping.properties"));
        } else {
            JsonParser parser = new JsonParser();
            JsonObject object = parser.parse(System.getenv().get("VCAP_SERVICES")).getAsJsonObject();
            JsonObject element = object.getAsJsonArray("mlab").get(0).getAsJsonObject();

            JsonObject credentials = element.getAsJsonObject("credentials");
            JsonPrimitive uriPrimitive = credentials.getAsJsonPrimitive("uri");

            String dbPrimitive = StringUtils.substringAfterLast(uriPrimitive.getAsString(), "/");

            uri = uriPrimitive.getAsString();

            db = dbPrimitive;

        }

        return propertySourcesPlaceholderConfigurer;
    }

    @Bean
    DatasetStatisticsDao getDatasetStatisticsDao() {
        Morphia morphia = new Morphia();
        MongoClient client = new MongoClient(new MongoClientURI(uri));
        morphia.mapPackage("eu.europeana.metis.mapping.statistics", true)
                .mapPackage("eu.europeana.metis.mapping.model", true)
                .mapPackage("java.math.BigInteger",true);
        return new DatasetStatisticsDao(morphia, client, db);
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
        registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
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