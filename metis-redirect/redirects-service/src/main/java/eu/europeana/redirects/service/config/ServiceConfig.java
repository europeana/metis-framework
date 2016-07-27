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
package eu.europeana.redirects.service.config;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
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
                JsonObject element = object.getAsJsonArray("mlab").get(0).getAsJsonObject();

                JsonObject credentials = element.getAsJsonObject("credentials");
                JsonPrimitive mongouri = credentials.getAsJsonPrimitive("uri");

                String mongoUsername = StringUtils.substringBetween(mongouri.getAsString(),"mongodb://",":");
                String mongoPassword = StringUtils.substringBetween(mongouri.getAsString(),mongoUsername+":","@");
                String mongoHost = StringUtils.substringBetween(mongouri.getAsString(),mongoPassword+"@",":");
                int mongoPort = Integer.parseInt(StringUtils.substringBetween(mongouri.getAsString(),mongoHost+":","/"));
                String mongoDb = StringUtils.substringAfterLast(mongouri.getAsString(),"/");

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
