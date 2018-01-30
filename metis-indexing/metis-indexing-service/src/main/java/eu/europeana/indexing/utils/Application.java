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
package eu.europeana.indexing.utils;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import eu.europeana.corelib.storage.impl.MongoProviderImpl;
import eu.europeana.corelib.web.socks.SocksProxy;
import javax.annotation.PreDestroy;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@ComponentScan(basePackages = {"eu.europeana.indexing.utils"})
@PropertySource("classpath:dereferencing.properties")
public class Application implements InitializingBean {
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
  @Value("${entity.db}")
  private String entityDb;
  @Value("${vocabulary.db}")
  private String vocabularyDb;

  @Value("${enrichment.url}")
  private String enrichmentUrl;
  
  private MongoProviderImpl mongoProviderFullBean;

  /**
   * Used for overwriting properties if cloud foundry environment is used
   */
  @Override
  public void afterPropertiesSet() throws Exception {
    if (socksProxyEnabled) {
      new SocksProxy(socksProxyHost, socksProxyPort, socksProxyUsername, socksProxyPassword).init();
    }

    /*
    String vcapServicesJson = System.getenv().get("VCAP_SERVICES");
    if (StringUtils.isNotEmpty(vcapServicesJson) && !StringUtils.equals(vcapServicesJson, "{}")) {
      PivotalCloudFoundryServicesReader vcapServices = new PivotalCloudFoundryServicesReader(vcapServicesJson);

      MongoClientURI mongoClientURI = vcapServices.getMongoClientUriFromService();
      if (mongoClientURI != null) {
        String mongoHostAndPort = mongoClientURI.getHosts().get(0);
        mongoHosts = mongoHostAndPort.substring(0, mongoHostAndPort.lastIndexOf(':'));
        mongoPort = Integer.parseInt(mongoHostAndPort.substring(mongoHostAndPort.lastIndexOf(':') + 1));
        mongoUsername = mongoClientURI.getUsername();
        mongoPassword = String.valueOf(mongoClientURI.getPassword());
        vocabularyDb = mongoClientURI.getDatabase();
        entityDb = mongoClientURI.getDatabase();
      }     
    }
    */

    String[] mongoHostsArray = mongoHosts.split(",");
    StringBuilder mongoPorts = new StringBuilder();
    for (int i = 0; i < mongoHostsArray.length; i++) {
      mongoPorts.append(mongoPort + ",");
    }
    mongoPorts.replace(mongoPorts.lastIndexOf(","), mongoPorts.lastIndexOf(","), "");
    MongoClientOptions.Builder options = MongoClientOptions.builder();
    mongoProviderFullBean = new MongoProviderImpl(mongoHosts, mongoPorts.toString(), entityDb,
        mongoUsername,
        mongoPassword, options);
  }

  MongoClient getFullBeanMongoClient() {
    return mongoProviderFullBean.getMongo();
  }

  @Bean
  FullBeanDao getFullBeanDao() {
    return new FullBeanDao(getFullBeanMongoClient(), entityDb);
  }

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    return new PropertySourcesPlaceholderConfigurer();
  }

  @PreDestroy
  public void close()
  {
    if (mongoProviderFullBean != null)
      mongoProviderFullBean.close();
  }
}
