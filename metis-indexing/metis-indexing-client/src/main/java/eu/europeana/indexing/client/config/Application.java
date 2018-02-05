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
package eu.europeana.indexing.client.config;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.ServerAddress;

import eu.europeana.corelib.storage.impl.MongoProviderImpl;
import eu.europeana.indexing.service.dao.FullBeanDao;
import eu.europeana.metis.utils.PivotalCloudFoundryServicesReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PreDestroy;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@ComponentScan(basePackages = {"eu.europeana.indexing.service, eu.europeana.indexing.test, eu.europeana.indexing.service.config"})
@PropertySource("classpath:indexing.properties")
public class Application implements InitializingBean {
  // Mongo
  @Value("${mongo.host}")
  private String mongoHost;
  @Value("${mongo.port}")
  private String mongoPort;
  @Value("${mongo.username}")
  private String mongoUsername;
  @Value("${mongo.password}")
  private String mongoPassword;
  @Value("${fullbean.db}")
  private String fullBeanDB;
    
  // Zookeeper
  @Value("${zookeeper.host}")
  private String zookeeperHost;
  @Value("${zookeeper.port}")
  private String zookeeperPort;
  
  // Solr
  @Value("${solr.host}")
  private String solrHost;
  @Value("${solr.port}")
  private String solrPort;
  @Value("${solrcloud.host}")
  private String solrCloudHost;
  @Value("${solrcloud.port}")
  private String solrCloudPort;
  @Value("${solr.collection}")
  private String solrCollection;
    
  private MongoProviderImpl mongoProviderFullBean;
  private CloudSolrServer cloudSolrServer;
  private LBHttpSolrServer solrServer; 
  private FullBeanDao fullBeanDao;

  /**
   * Used for overwriting properties if cloud foundry environment is used
   */
  @Override
  public void afterPropertiesSet() throws Exception {
    
    String vcapServicesJson = System.getenv().get("VCAP_SERVICES");
    if (StringUtils.isNotEmpty(vcapServicesJson) && !StringUtils.equals(vcapServicesJson, "{}")) {
      PivotalCloudFoundryServicesReader vcapServices = new PivotalCloudFoundryServicesReader(vcapServicesJson);

      MongoClientURI mongoClientURI = vcapServices.getMongoClientUriFromService();
      
      if (mongoClientURI != null) {
        String mongoHostAndPort = mongoClientURI.getHosts().get(0);
        mongoHost = mongoHostAndPort.substring(0, mongoHostAndPort.lastIndexOf(':'));
        //mongoPort = Integer.parseInt(mongoHostAndPort.substring(mongoHostAndPort.lastIndexOf(':') + 1));
        mongoUsername = mongoClientURI.getUsername();
        mongoPassword = String.valueOf(mongoClientURI.getPassword());
        fullBeanDB = mongoClientURI.getDatabase();
      }     
    }
    
    String[] mongoHostsArray = mongoHost.split(",");
    StringBuilder mongoPorts = new StringBuilder();
    for (int i = 0; i < mongoHostsArray.length; i++) {
      mongoPorts.append(mongoPort + ",");
    }
    mongoPorts.replace(mongoPorts.lastIndexOf(","), mongoPorts.lastIndexOf(","), "");
    MongoClientOptions.Builder options = MongoClientOptions.builder();
    mongoProviderFullBean = new MongoProviderImpl(
    		mongoHost, 
    		mongoPorts.toString(), 
    		fullBeanDB, 
    		mongoUsername, 
    		mongoPassword, 
    		options);
        
    String[] hostList = StringUtils.split(mongoHost, ",");
    String[] portList = StringUtils.split(mongoPort, ",");
    List<ServerAddress> serverAddresses = new ArrayList<>();
    int i = 0;
    for (String host : hostList) {
        if (host.length() > 0) {
            try {
                ServerAddress address = new ServerAddress(host, Integer.parseInt(portList[i]));
                serverAddresses.add(address);
            } catch (NumberFormatException e) {
                //LOG.error("Error parsing port numbers", e);
            }
        }
        i++;
    }
	
	fullBeanDao = new FullBeanDao(new MongoClient(serverAddresses), fullBeanDB);
	
	LBHttpSolrServer solrServer = null;
	
    try {
    	solrServer = new LBHttpSolrServer(solrHost);
    	cloudSolrServer = new CloudSolrServer(zookeeperHost, solrServer);
    	cloudSolrServer.setDefaultCollection(solrCollection);
    	cloudSolrServer.connect();

    } catch (MalformedURLException e) {
    	e.printStackTrace();
    }
  }

  MongoClient getFullBeanMongoClient() {
    return mongoProviderFullBean.getMongo();
  }

  @Bean
  FullBeanDao getFullBeanDao() {
    return fullBeanDao;
  }
  
  @Bean
  CloudSolrServer getCloudSolrServer() {
    return cloudSolrServer;
  }
  
  @Bean
  LBHttpSolrServer getSolrServer() {
    return solrServer;
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
