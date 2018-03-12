package eu.europeana.indexing.client.config;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientOptions.Builder;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import eu.europeana.indexing.service.PublishingService;
import eu.europeana.indexing.service.dao.FullBeanDao;
import eu.europeana.indexing.service.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.exception.ConfigFileException;
import eu.europeana.metis.exception.IndexingException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexingConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger(IndexingConfig.class);
	private static IndexingConfig instance = null;
	private PublishingService publishingService;
	private FullBeanDao fullBeanDao;
	private static final String INDEXING_CONFIG_LOAD_ERROR_MSG = "Could not load indexing configuration.";
	
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

		// Mongo details from read configuration file
		String mongoHost = props.getProperty("mongo.host");
		String mongoPort = props.getProperty("mongo.port");
		String mongoUsername = props.getProperty("mongo.usernaname");
		String mongoPassword = props.getProperty("mongo.password");		
		String mongoDB = props.getProperty("mongo.db");
		String mongoAuthenticationDB = props.getProperty("mongo.authentication.db");
		String mongoEnableSSL = props.getProperty("mongo.authentication.db");	
		
		// Zookeeper details from read configuration file
		String zookeeperHost = props.getProperty("zookeeper.host");
		String zookeeperPort = props.getProperty("zookeeper.port");
		
		// Solr details from read configuration file
		String solrHost = props.getProperty("solr.host");
		String solrPort = props.getProperty("solr.port");
		String solrCloudHost = props.getProperty("solrcloud.host");
		String solrCloudPort = props.getProperty("solrcloud.port");
		String solrCollection = props.getProperty("solr.collection");
		  
		
		String[] mongoHostList = StringUtils.split(mongoHost, ",");
	    String[] mongoPortList = StringUtils.split(mongoPort, ",");
	    
	    String[] zookeeperHostList = StringUtils.split(zookeeperHost, ",");
	    String[] zookeeperPortList = StringUtils.split(zookeeperPort, ",");
	    
	    String[] solrCloudHostList = StringUtils.split(solrCloudHost, ",");
	    String[] solrCloudPortList = StringUtils.split(solrCloudPort, ",");
	    
	    LOGGER.info("Loading indexing configuration.");

	    if ((StringUtils.isEmpty(mongoUsername) || StringUtils.isEmpty(mongoPassword)) && !StringUtils.isEmpty(mongoAuthenticationDB))
	    {
	    	LOGGER.error(INDEXING_CONFIG_LOAD_ERROR_MSG);	    	
	    	throw new IndexingException(INDEXING_CONFIG_LOAD_ERROR_MSG, new ConfigFileException("Mongo credentials are improperly defined in the configuration file."));
	    }
	    
	    if (mongoHostList.length != mongoPortList.length && mongoPortList.length != 1)
	    {
	    	LOGGER.error(INDEXING_CONFIG_LOAD_ERROR_MSG);	    	
	    	throw new IndexingException(INDEXING_CONFIG_LOAD_ERROR_MSG, new ConfigFileException("Mongo hosts and ports are improperly defined in the configuration file."));
	    }
	    
	    if (zookeeperHostList.length != zookeeperPortList.length && zookeeperPortList.length != 1)
	    {
	    	LOGGER.error(INDEXING_CONFIG_LOAD_ERROR_MSG);   	
	    	throw new IndexingException(INDEXING_CONFIG_LOAD_ERROR_MSG, new ConfigFileException("Zookeeper hosts and ports are improperly defined in the configuration file."));
	    }
	    	   
	    List<ServerAddress> mongoServerAddresses = new ArrayList<>();
	    int i = 0;
	    boolean samePortForAll = false;
	    if (mongoPortList.length == 1)
	    	samePortForAll = true;
	    
	    for (String host : mongoHostList) {
	        if (host.length() > 0) {
	            try {
	                ServerAddress address = new ServerAddress(host, Integer.parseInt(mongoPortList[i]));
	                mongoServerAddresses.add(address);
	            } catch (NumberFormatException e) {
	            	LOGGER.error(INDEXING_CONFIG_LOAD_ERROR_MSG);
	            	throw new IndexingException(INDEXING_CONFIG_LOAD_ERROR_MSG, e.getCause());
	            }
	        }
	        if (!samePortForAll)
	        	i++;
	    }
	    
	    i = 0;
	    samePortForAll = false;
	    if (zookeeperPortList.length == 1)
	    	samePortForAll = true;
	    
	    String zkHost = "";
	    
	    for (String host : zookeeperHostList) {
	        if (host.length() > 0) {
	            try {
	                String address = host.concat(":".concat(zookeeperPortList[i]));
	                zkHost = zkHost.concat(address.concat(","));
	            } catch (NumberFormatException e) {
	            	LOGGER.error(INDEXING_CONFIG_LOAD_ERROR_MSG);
	            	throw new IndexingException(INDEXING_CONFIG_LOAD_ERROR_MSG, e.getCause());
	            }
	        }
	        if (!samePortForAll)
	        	i++;
	    }
	    
	    i = 0;
	    samePortForAll = false;
	    if (solrCloudPortList.length == 1)
	    	samePortForAll = true;
	    
	    String cloudSolrServerUrls = "";
	    
	    for (String host : solrCloudHostList) {
	        if (host.length() > 0) {
	            try {
	                String address = host.concat(":".concat(solrCloudPortList[i]));
	                cloudSolrServerUrls = cloudSolrServerUrls.concat(address.concat(","));
	            } catch (NumberFormatException e) {
	            	LOGGER.error(INDEXING_CONFIG_LOAD_ERROR_MSG);
	            	throw new IndexingException(INDEXING_CONFIG_LOAD_ERROR_MSG, e.getCause());
	            }
	        }
	        if (!samePortForAll)
	        	i++;
	    }
	    
	    cloudSolrServerUrls = cloudSolrServerUrls.substring(0, cloudSolrServerUrls.length()-1);
	    
	    LOGGER.info("cloudSolrServerUrls = {}", cloudSolrServerUrls);
	    	    
	    LBHttpSolrServer solrServer = null;
	    CloudSolrServer cloudSolrServer = null;
	    
	    String solrServerUrl = "http://".concat(solrHost.concat(":".concat(solrPort.concat("/solr"))));
	    
	    LOGGER.info("solrServerUrl = {}", solrServerUrl);
	    
	    // ***
	    HttpSolrServer t = null;
	    t = new HttpSolrServer(solrServerUrl);
    	
	   
	    
	    try {
	    	solrServer = new LBHttpSolrServer(cloudSolrServerUrls);
	    	cloudSolrServer = new CloudSolrServer(zkHost, solrServer);
	    	
	    	cloudSolrServer.setDefaultCollection(solrCollection);
	    	cloudSolrServer.connect();
	    } catch (MalformedURLException e) {
	    	LOGGER.error(INDEXING_CONFIG_LOAD_ERROR_MSG);
	    	throw new IndexingException(INDEXING_CONFIG_LOAD_ERROR_MSG, e.getCause());
	    }
	    
	    LOGGER.info("Successfully loaded indexing configuration.");
	    
	    MorphiaDatastoreProvider morphiaDatastoreProvider;
	    MongoClientOptions.Builder optionsBuilder = new Builder();
	    
	    if (mongoEnableSSL.equalsIgnoreCase("true"))
	    	optionsBuilder.sslEnabled(true);
	    else
	    	optionsBuilder.sslEnabled(false);
	    	   
	    if (StringUtils.isEmpty(mongoDB) || StringUtils.isEmpty(mongoUsername) || StringUtils.isEmpty(mongoPassword))
	    	morphiaDatastoreProvider = new MorphiaDatastoreProvider(new MongoClient(mongoServerAddresses, optionsBuilder.build()), mongoDB);	    	
	    else {
	    	MongoCredential mongoCredential = MongoCredential.createCredential(mongoUsername, mongoAuthenticationDB, mongoPassword.toCharArray());

	    	ArrayList<MongoCredential> mongoCredentials = new ArrayList<>();
	    	mongoCredentials.add(mongoCredential);

	    	morphiaDatastoreProvider = new MorphiaDatastoreProvider(new MongoClient(mongoServerAddresses, mongoCredentials, optionsBuilder.build()), mongoDB);
	    }
	    
	    fullBeanDao = new FullBeanDao(morphiaDatastoreProvider);		
		publishingService = new PublishingService(fullBeanDao, cloudSolrServer);
	}
}
