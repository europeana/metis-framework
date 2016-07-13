package eu.europeana.redirects.service.config;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import eu.europeana.corelib.lookup.impl.CollectionMongoServerImpl;
import eu.europeana.corelib.lookup.impl.EuropeanaIdMongoServerImpl;
import eu.europeana.corelib.tools.lookuptable.CollectionMongoServer;
import eu.europeana.corelib.tools.lookuptable.EuropeanaIdMongoServer;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;

import java.io.IOException;
import java.util.Properties;

/**
 * Configuration class for Service redirects class
 *
 * Created by ymamakis on 1/13/16.
 */

public class ServiceConfig {

    private Properties props;
    private EuropeanaIdMongoServer mongoServer;
    private CloudSolrServer productionSolrServer;




    private CollectionMongoServer collectionMongoServer;


    public ServiceConfig(){
        try {
            if(System.getenv().get("VCAP_SERVICES")==null) {
                props = new Properties();
                props.load(ServiceConfig.class.getClassLoader().getResourceAsStream("redirects.properties"));
                Mongo mongo = new MongoClient(props.getProperty("mongo.host"),
                        Integer.parseInt(props.getProperty("mongo.port")));
                mongoServer = new EuropeanaIdMongoServerImpl(mongo, props.getProperty("mongo.db"),
                        props.getProperty("mongo.username"), props.getProperty("mongo.password"));
                if (StringUtils.isNotEmpty(props.getProperty("mongo.username"))) {
                    collectionMongoServer = new CollectionMongoServerImpl(mongo, props.getProperty("mongo.collections.db"),
                            props.getProperty("mongo.username"), props.getProperty("mongo.password"));
                } else {
                    collectionMongoServer = new CollectionMongoServerImpl(mongo, props.getProperty("mongo.collections.db"));
                }

                LBHttpSolrServer lbTargetProduction = new LBHttpSolrServer(props.getProperty("solr.production"));
                productionSolrServer = new CloudSolrServer(props.getProperty("zookeeper.production"), lbTargetProduction);
                productionSolrServer.setDefaultCollection(props.getProperty("solr.production.core"));
                productionSolrServer.connect();
            } else {
                JsonParser parser = new JsonParser();
                JsonObject object = parser.parse(System.getenv().get("VCAP_SERVICES")).getAsJsonObject();
                JsonObject element = object.getAsJsonArray("mongodb-2.0").get(0).getAsJsonObject();

                JsonObject credentials = element.getAsJsonObject("credentials");

                String mongoDb = credentials.get("db").getAsString();
                String mongoHost = credentials.get("host").getAsString();
                int mongoPort = credentials.get("port").getAsInt();
                String mongoUsername = credentials.get("username").getAsString();
                String mongoPassword = credentials.get("password").getAsString();
                Mongo mongo = new MongoClient(mongoHost,mongoPort);
                mongoServer = new EuropeanaIdMongoServerImpl(mongo, mongoDb,
                        mongoUsername, mongoPassword);
                if (StringUtils.isNotEmpty(mongoUsername)) {
                    collectionMongoServer = new CollectionMongoServerImpl(mongo,mongoDb,
                            mongoUsername,mongoPassword);
                } else {
                    collectionMongoServer = new CollectionMongoServerImpl(mongo, mongoDb);
                }

                LBHttpSolrServer lbTargetProduction = new LBHttpSolrServer(System.getenv().get("solrProduction"));
                productionSolrServer = new CloudSolrServer(System.getenv("zookeeperProduction"), lbTargetProduction);
                productionSolrServer.setDefaultCollection(System.getenv("solrCore"));
                productionSolrServer.connect();


            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public EuropeanaIdMongoServer getMongoServer() {
        return mongoServer;
    }

    public void setMongoServer(EuropeanaIdMongoServer mongoServer) {
        this.mongoServer = mongoServer;
    }



    public CloudSolrServer getProductionSolrServer() {
        return productionSolrServer;
    }

    public void setProductionSolrServer(CloudSolrServer productionSolrServer) {
        this.productionSolrServer = productionSolrServer;
    }


    public CollectionMongoServer getCollectionMongoServer() {
        return collectionMongoServer;
    }

    public void setCollectionMongoServer(CollectionMongoServer collectionMongoServer) {
        this.collectionMongoServer = collectionMongoServer;
    }
}
