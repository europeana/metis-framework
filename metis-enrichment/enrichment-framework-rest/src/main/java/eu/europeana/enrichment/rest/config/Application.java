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
package eu.europeana.enrichment.rest.config;

import eu.europeana.enrichment.rest.client.EnrichmentProxyClient;
import eu.europeana.enrichment.service.Enricher;
import eu.europeana.enrichment.service.RedisInternalEnricher;
import eu.europeana.enrichment.service.RedisProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Spring configuration class
 * Created by ymamakis on 12-2-16.
 */
@Configuration
@ComponentScan (basePackages = {"eu.europeana.enrichment.rest","eu.europeana.enrichment.rest.exception"})
@PropertySource("classpath:enrichment.properties")
@EnableWebMvc
@EnableSwagger2
public class Application extends WebMvcConfigurerAdapter {


    @Value("${enrichment.mongo}")
    private static String enrichmentMongo;
    @Value("${redis.host}")
    private static String redisHost;
    @Value("${redis.port}")
    private static int redisPort;
    @Value("${redis.password}")
    private static String redisPassword;
    @Value("${memcache.host}")
    private static String memcacheHost;
    @Value("${memcache.port}")
    private static int memcachePort;

    @Value(("${enrichment.proxy}"))
    private static String enrichmentProxy;
    @Bean
    @DependsOn("redisInternalEnricher")
    Enricher enricher(){
        Enricher enricher = new Enricher("");
        try {
            enricher.init("Europeana",enrichmentMongo,"27017");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return enricher;
    }

    @Bean
    @Order(1)
    public static PropertySourcesPlaceholderConfigurer properties() {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        if (System.getenv().get("VCAP_SERVICES") == null) {
            propertySourcesPlaceholderConfigurer.setLocation(new ClassPathResource("enrichment.properties"));
        } else {
            enrichmentMongo= System.getenv().get("enrichment_mongoDb");
            redisHost= System.getenv().get("redis_host");
            redisPort=Integer.parseInt(System.getenv().get("redis_port"));
            memcacheHost = System.getenv().get("memcache_host");
            memcachePort=Integer.parseInt(System.getenv().get("memcache_port"));
            enrichmentProxy = System.getenv().get("enrichment_proxy");
        }
        return propertySourcesPlaceholderConfigurer;
    }


    RedisProvider getRedisProvider(){
        return new RedisProvider(redisHost, redisPort, redisPassword);
    }
   // MemcachedProvider getMemcachedProvider(){return new MemcachedProvider(memcacheHost,memcachePort);}
    @Bean(name = "redisInternalEnricher")
    RedisInternalEnricher getRedisInternalEnricher(){
        return new RedisInternalEnricher(enrichmentMongo,getRedisProvider());
    }
    @Bean
    EnrichmentProxyClient getEnrichmentProxyClient(){
        return new EnrichmentProxyClient(enrichmentProxy);
    }
    //@Bean(name = "memcachedInternalEnricher")
    //MemcachedInternalEnricher getMemcachedInternalEnricher(){
    //    return new MemcachedInternalEnricher(enrichmentMongo,getMemcachedProvider());
    //}
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
                "Enrichment REST API",
                "Enrichment REST API for Europeana",
                "v1",
                "API TOS",
                "development@europeana.eu",
                "EUPL Licence v1.1",
                ""
        );
        return apiInfo;
    }
}
