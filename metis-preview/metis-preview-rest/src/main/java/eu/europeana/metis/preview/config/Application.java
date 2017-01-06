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
import eu.europeana.corelib.edm.exceptions.MongoDBException;
import eu.europeana.corelib.edm.utils.construct.FullBeanHandler;
import eu.europeana.corelib.edm.utils.construct.SolrDocumentHandler;
import eu.europeana.corelib.mongo.server.EdmMongoServer;
import eu.europeana.corelib.mongo.server.impl.EdmMongoServerImpl;
import eu.europeana.metis.identifier.RestClient;
import eu.europeana.metis.preview.persistence.RecordDao;
import eu.europeana.metis.preview.service.PreviewService;
import eu.europeana.validation.client.ValidationClient;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.jibx.runtime.JiBXException;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
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

import java.net.UnknownHostException;
import java.util.List;

/**
 * Configuration file for Spring MVC
 */
@ComponentScan(basePackages = {"eu.europeana.metis.preview.rest","eu.europeana.metis.preview.exceptions.handler"})
@PropertySource("classpath:preview.properties")
@EnableWebMvc
@EnableSwagger2
@Configuration
@EnableScheduling
public class Application extends WebMvcConfigurerAdapter {



    private static String mongoHost;

    private static String mongoPort;

    private static String mongoUsername;

    private static String mongoPassword;

    private static String maindb;

    private static String previewPortalUrl;


    private static String solrUrl;

    @Bean
    PreviewService previewService(){
        try {
            return new PreviewService(previewPortalUrl);
        } catch (JiBXException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Bean
    RecordDao recordDao(){
        return new RecordDao();
    }
    @Bean
     RestClient restClient(){
        return new RestClient();
    }

    @Bean
     ValidationClient validationClient(){
        return new ValidationClient();
    }

    @Bean
    @DependsOn("edmMongoServer")
     FullBeanHandler fullBeanHandler(){
        return new FullBeanHandler(edmMongoServer());
    }

    @Bean(name = "edmMongoServer")
     EdmMongoServer edmMongoServer(){
        MongoClient client = null;
        try {

            client = new MongoClient(mongoHost,Integer.parseInt(mongoPort));
            return new EdmMongoServerImpl(client,maindb,mongoUsername,mongoPassword);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (MongoDBException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Bean
    @DependsOn(value = "solrServer")
     SolrDocumentHandler solrDocumentHandler(){
        return new SolrDocumentHandler(solrServer());
    }

    @Bean(name = "solrServer")
     SolrServer solrServer(){
        HttpSolrServer server = new HttpSolrServer(solrUrl);
        return server;
    }
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
    public CommonsMultipartResolver multipartResolver(){
        CommonsMultipartResolver commonsMultipartResolver = new CommonsMultipartResolver();
        commonsMultipartResolver.setDefaultEncoding("utf-8");
        commonsMultipartResolver.setMaxUploadSize(50000000);
        return commonsMultipartResolver;
    }
    @Bean
    @Order(1)
    public static PropertySourcesPlaceholderConfigurer properties() {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();

        if (System.getenv().get("VCAP_SERVICES") == null) {
            propertySourcesPlaceholderConfigurer.setLocation(new ClassPathResource("preview.properties"));
        } else {
            mongoHost= System.getenv().get("mongohost");

            mongoPort=System.getenv().get("mongoPort");

            mongoUsername=System.getenv().get("mongoUsername");

            mongoPassword=System.getenv().get("mongoPassword");

            maindb=System.getenv("maindb");

            previewPortalUrl=System.getenv().get("previewPortalUrl");


            solrUrl=System.getenv().get("solrUrl");

        }
        return propertySourcesPlaceholderConfigurer;
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
