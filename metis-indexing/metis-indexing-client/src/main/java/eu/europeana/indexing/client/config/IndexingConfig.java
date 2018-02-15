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
import com.mongodb.ServerAddress;
import eu.europeana.indexing.service.PublishingService;
import eu.europeana.indexing.service.dao.FullBeanDao;
import eu.europeana.indexing.service.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.exception.IndexingException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexingConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger(IndexingConfig.class);
	private static IndexingConfig instance = null;
	private PublishingService publishingService;
	private FullBeanDao fullBeanDao;
	private static final String IndexingConfigLoadErrorMsg = "Could not load indexing configuration.";
	
	protected IndexingConfig() throws IndexingException {
		init();		
	}
	
	public static IndexingConfig getInstance() throws IndexingException {
		if (instance == null)
			instance = new IndexingConfig();		
		return instance;
	}
	
	public PublishingService getPublishingService() {
		return publishingService;
	}
	
	public FullBeanDao getFullBeanDao() {
		return fullBeanDao;
	}
	
	private void init() throws IndexingException {
		String resourceName = "indexing.properties";
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Properties props = new Properties();

		LOGGER.info("Reading indexing.properties file.");
		try(InputStream resourceStream = loader.getResourceAsStream(resourceName)) {
		    props.load(resourceStream);
		} catch (IOException e) {
			LOGGER.error("Failed to read indexing.properties file.");
			throw new IndexingException("Failed to read indexing.properties file.", e.getCause());
		}
		LOGGER.info("Successfully read indexing.properties file.");

		// Mongo
		String mongoHost = props.getProperty("mongo.host");
		String mongoPort = props.getProperty("mongo.port");
		String mongoUsername = props.getProperty("mongo.usernaname");
		String mongoPassword = props.getProperty("mongo.password");		
		String fullBeanDB = props.getProperty("fullbean.db");
		
		// Zookeeper
		String zookeeperHost = props.getProperty("zookeeper.host");
		String zookeeperPort = props.getProperty("zookeeper.port");
		
		// Solr
		String solrHost = props.getProperty("solr.host");
		String solrPort = props.getProperty("solr.port");
		String solrCloudHost = props.getProperty("solrcloud.host");
		String solrCloudPort = props.getProperty("solrcloud.port");
		String solrCollection = props.getProperty("solr.collection");
		  
		String[] hostList = StringUtils.split(mongoHost, ",");
	    String[] portList = StringUtils.split(mongoPort, ",");
	    
	    LOGGER.info("Loading indexing configuration.");
	    List<ServerAddress> serverAddresses = new ArrayList<>();
	    int i = 0;
	    for (String host : hostList) {
	        if (host.length() > 0) {
	            try {
	                ServerAddress address = new ServerAddress(host, Integer.parseInt(portList[i]));
	                serverAddresses.add(address);
	            } catch (NumberFormatException e) {
	            	LOGGER.error(IndexingConfigLoadErrorMsg);
	            	throw new IndexingException(IndexingConfigLoadErrorMsg, e.getCause());
	            }
	        }
	        i++;
	    }
	    	    
	    LBHttpSolrServer solrServer = null;
	    CloudSolrServer cloudSolrServer = null;
	    		
	    try {
	    	solrServer = new LBHttpSolrServer(solrHost);
	    	cloudSolrServer = new CloudSolrServer(zookeeperHost, solrServer);
	    	cloudSolrServer.setDefaultCollection(solrCollection);
	    	cloudSolrServer.connect();
	    } catch (MalformedURLException e) {
	    	LOGGER.error(IndexingConfigLoadErrorMsg);
	    	throw new IndexingException(IndexingConfigLoadErrorMsg, e.getCause());
	    }
	    
	    LOGGER.info("Successfully loaded indexing configuration.");
	    
	    MorphiaDatastoreProvider morphiaDatastoreProvider = new MorphiaDatastoreProvider(new MongoClient(serverAddresses), fullBeanDB);
	    fullBeanDao = new FullBeanDao(morphiaDatastoreProvider);		
		publishingService = new PublishingService(fullBeanDao, solrServer, cloudSolrServer);
	}
}
