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
package eu.europeana.enrichment.cache.proxy.config;

import eu.europeana.enrichment.service.RedisInternalEnricher;
import eu.europeana.metis.RedisProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Spring configuration class
 * Created by ymamakis on 12-2-16.
 */
@Configuration
@ComponentScan (basePackages = {"eu.europeana.enrichment.cache.proxy"})
@PropertySource("classpath:enrichment.properties")
@EnableWebMvc
public class Application extends WebMvcConfigurerAdapter {


    @Value("${enrichment.mongo}")
    private  String enrichmentMongo;
    @Value("${redis.host}")
    private  String redisHost;
    @Value("${redis.port}")
    private  int redisPort;
    //@Value("${redis.password}")
    private  String redisPassword;
    //@Value("${memcache.host}")
    private  String memcacheHost;
    //@Value("${memcache.port}")
    private  int memcachePort;



    @Bean
    @Order(1)
    public static PropertySourcesPlaceholderConfigurer properties() {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        return propertySourcesPlaceholderConfigurer;
    }


    RedisProvider getRedisProvider(){
        return new RedisProvider(redisHost, redisPort, redisPassword);
    }
   // MemcachedProvider getMemcachedProvider(){return new MemcachedProvider(memcacheHost,memcachePort);}
    @Bean(name = "redisInternalEnricher")
    RedisInternalEnricher getRedisInternalEnricher(){
        return new RedisInternalEnricher(enrichmentMongo,getRedisProvider(), true);
    }
    //@Bean(name = "memcachedInternalEnricher")
    //MemcachedInternalEnricher getMemcachedInternalEnricher(){
    //    return new MemcachedInternalEnricher(enrichmentMongo,getMemcachedProvider());
    //}

}
