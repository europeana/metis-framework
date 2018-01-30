package eu.europeana.europeanauim.publish.service;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.google.code.morphia.Morphia;
import eu.europeana.corelib.edm.exceptions.MongoDBException;
import eu.europeana.corelib.mongo.server.EdmMongoServer;
import eu.europeana.corelib.mongo.server.impl.EdmMongoServerImpl;
import eu.europeana.europeanauim.publish.OsgiEdmMongoServer;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;

import com.mongodb.Mongo;
import com.mongodb.ServerAddress;

import eu.europeana.corelib.tools.lookuptable.EuropeanaIdMongoServer;
import eu.europeana.europeanauim.publish.utils.OsgiEuropeanaIdMongoServer;
import eu.europeana.europeanauim.publish.utils.PropertyReader;
import eu.europeana.europeanauim.publish.utils.UimConfigurationProperty;
import eu.europeana.uim.common.BlockingInitializer;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;

public class PublishServiceImpl implements PublishService {
    private CloudSolrServer solrServer;
    private CloudSolrServer solrIngestionServer;
    private String zookeeperUrl;
    private String zookeeperUrlProduction;
    private static OsgiEuropeanaIdMongoServer idserver;
    private static OsgiEuropeanaIdMongoServer idserverProduction;
    private static String[] mongoHost = PropertyReader.getProperty(
            UimConfigurationProperty.MONGO_HOSTURL).split(",");
    private static String[] mongoHostProduction = PropertyReader.getProperty(
            UimConfigurationProperty.MONGO_HOSTURL_PRODUCTION).split(",");
    private static String mongoDbIngestion = PropertyReader.getProperty(
            UimConfigurationProperty.MONGO_INGESTION_DB);
    private static String mongoDbProduction = PropertyReader.getProperty(
            UimConfigurationProperty.MONGO_PRODUCTION_DB);
    private static String mongoDBEuropeanaIDIngestion = PropertyReader.getProperty(UimConfigurationProperty.MONGODB_EUROPEANA_ID);
    private static String mongoDBEuropeanaIDProduction = PropertyReader.getProperty(UimConfigurationProperty.MONGODB_EUROPEANA_ID_PRODUCTION);
    private static String usernameIngestion = PropertyReader.getProperty(UimConfigurationProperty.MONGO_INGESTION_USERNAME);
    private static String passwordIngestion = PropertyReader.getProperty(UimConfigurationProperty.MONGO_INGESTION_PASSWORD);
    private static String usernameProduction = PropertyReader.getProperty(UimConfigurationProperty.MONGO_PRODUCTION_USERNAME);
    private static String passwordProduction = PropertyReader.getProperty(UimConfigurationProperty.MONGO_PRODUCTION_PASSWORD);

    @Override
    public OsgiEdmMongoServer getMongoIngestion() {
        return mongoIngestion;
    }


    @Override
    public OsgiEdmMongoServer getMongoProduction() {
        return mongoProduction;
    }


    private static OsgiEdmMongoServer mongoIngestion;
    private static OsgiEdmMongoServer mongoProduction;

    public PublishServiceImpl() {
        final String solrUrl =
                PropertyReader.getProperty(UimConfigurationProperty.SOLR_CLOUD_PRODUCTION_HOSTURL);
        final String solrCore =
                PropertyReader.getProperty(UimConfigurationProperty.SOLR_CLOUD_PRODUCTION_CORE);

        zookeeperUrlProduction = PropertyReader.getProperty(UimConfigurationProperty.ZOOKEEPER_PRODUCTION_HOSTURL);
        zookeeperUrl = PropertyReader.getProperty(UimConfigurationProperty.ZOOKEEPER_INGESTION_HOSTURL);

        try {

            LBHttpSolrServer lbTarget = new LBHttpSolrServer(solrUrl.split(","));
            solrServer = new CloudSolrServer(zookeeperUrlProduction, lbTarget);
            solrServer.setDefaultCollection(solrCore);
            solrServer.connect();
            // solrServer = new CloudSolrServer(new URL(solrUrl) + solrCore);
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        final String solrIngestionUrl =
                PropertyReader.getProperty(UimConfigurationProperty.SOLR_HOSTURL);
        final String solrIngestionCore =
                PropertyReader.getProperty(UimConfigurationProperty.SOLR_CORE);


        LBHttpSolrServer lbTarget = null;
        try {
            lbTarget = new LBHttpSolrServer(solrIngestionUrl.split(","));
            solrIngestionServer = new CloudSolrServer(zookeeperUrl, lbTarget);
            solrIngestionServer.setDefaultCollection(solrIngestionCore);
            solrIngestionServer.connect();
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        }

        //solrIngestionServer = new CloudSolrServer(new URL(solrIngestionUrl) + solrIngestionCore);


        List<ServerAddress> addresses = new ArrayList<>();
        for (String mongoStr : mongoHost) {
            ServerAddress address;
            try {
                address = new ServerAddress(mongoStr, 27017);
                addresses.add(address);
            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        final Mongo tgtMongo = new Mongo(addresses);
        BlockingInitializer init2 = new BlockingInitializer() {
            @Override
            protected void initializeInternal() {
                if (StringUtils.isNotEmpty(usernameIngestion)) {
                    idserver = new OsgiEuropeanaIdMongoServer((tgtMongo), mongoDBEuropeanaIDIngestion, usernameIngestion, passwordIngestion);
                } else {
                    idserver = new OsgiEuropeanaIdMongoServer((tgtMongo), mongoDBEuropeanaIDIngestion, null, null);
                }
                idserver.createDatastore();
                idserver.retrieveEuropeanaIdFromOld("test");
                idserver.retrieveEuropeanaIdFromNew("test");
            }
        };
        init2.initialize(OsgiEuropeanaIdMongoServer.class.getClassLoader());
        List<ServerAddress> addressesProduction = new ArrayList<>();
        for (String mongoStr : mongoHostProduction) {
            ServerAddress address;
            try {
                address = new ServerAddress(mongoStr, 27017);
                addressesProduction.add(address);
            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        final Mongo tgtProductionMongo = new Mongo(addressesProduction);

        BlockingInitializer init1 = new BlockingInitializer() {
            @Override
            protected void initializeInternal() {
                if (StringUtils.isNotEmpty(usernameProduction)) {
                    idserverProduction = new OsgiEuropeanaIdMongoServer((tgtProductionMongo), mongoDBEuropeanaIDProduction, usernameProduction, passwordProduction);
                } else {
                    idserverProduction = new OsgiEuropeanaIdMongoServer((tgtProductionMongo), mongoDBEuropeanaIDProduction, null, null);
                }
                idserverProduction.createDatastore();
                idserverProduction.retrieveEuropeanaIdFromOld("test");
                idserverProduction.retrieveEuropeanaIdFromNew("test");
            }
        };
        init1.initialize(OsgiEuropeanaIdMongoServer.class.getClassLoader());

        BlockingInitializer initializer = new BlockingInitializer() {
            @Override
            protected void initializeInternal() {
                try {
                    if (StringUtils.isNotEmpty(usernameIngestion)) {
                        mongoIngestion = new OsgiEdmMongoServer((tgtMongo), mongoDbIngestion, usernameIngestion, passwordIngestion);
                    } else {
                        mongoIngestion = new OsgiEdmMongoServer((tgtMongo), mongoDbIngestion,null,null);
                    }
                    Morphia morphia = new Morphia();
                    mongoIngestion.createDatastore(morphia);
                    mongoIngestion.getFullBean("test");
                } catch (MongoDBException e) {
                    e.printStackTrace();
                }

            }
        };
        initializer.initialize(OsgiEdmMongoServer.class.getClassLoader());


        BlockingInitializer initializer1 = new BlockingInitializer() {
            @Override
            protected void initializeInternal() {
                try {
                    if(StringUtils.isNotBlank(usernameProduction)) {
                        mongoProduction = new OsgiEdmMongoServer((tgtProductionMongo), mongoDbProduction, usernameProduction, passwordProduction);
                    } else {
                        mongoProduction = new OsgiEdmMongoServer((tgtProductionMongo), mongoDbProduction, null,null);
                    }
                    Morphia morphia = new Morphia();
                    mongoProduction.createDatastore(morphia);
                    mongoProduction.getFullBean("test");
                } catch (MongoDBException e) {
                    e.printStackTrace();
                }
            }
        };
        initializer1.initialize(OsgiEdmMongoServer.class.getClassLoader());

    }


    @Override
    public SolrServer getSolrServer() {

        return solrServer;
    }

    @Override
    public SolrServer getSolrIngestionServer() {

        return solrIngestionServer;
    }

    @Override
    public EuropeanaIdMongoServer getEuropeanaIdMongoServer() {

        return idserver;
    }

    @Override
    public EuropeanaIdMongoServer getEuropeanaIdMongoServerProduction() {

        return idserverProduction;
    }


}
