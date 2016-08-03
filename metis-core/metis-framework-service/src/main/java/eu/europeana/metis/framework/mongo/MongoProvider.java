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
package eu.europeana.metis.framework.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import eu.europeana.metis.framework.common.*;
import eu.europeana.metis.framework.dataset.Dataset;
import eu.europeana.metis.framework.dataset.FtpDatasetMetadata;
import eu.europeana.metis.framework.dataset.HttpDatasetMetadata;
import eu.europeana.metis.framework.dataset.OAIDatasetMetadata;
import eu.europeana.metis.framework.organization.Organization;
import org.apache.commons.lang.StringUtils;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.springframework.stereotype.Component;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class providing connections to Mongo
 * Created by ymamakis on 2/17/16.
 */
@Component
public class MongoProvider {

    private Datastore datastore;

    public MongoProvider(String host, int port, String dbName,String username, String password) throws UnknownHostException {


        Morphia morphia = new Morphia();
        morphia.map(Dataset.class);
        morphia.map(Organization.class);
        morphia.map(FtpDatasetMetadata.class);
        morphia.map(FtpMetadata.class);
        morphia.map(FolderMetadata.class);
        morphia.map(HttpMetadata.class);
        morphia.map(HarvestingMetadata.class);
        morphia.map(OAIMetadata.class);
        morphia.map(HttpDatasetMetadata.class);
        morphia.map(OAIDatasetMetadata.class);
        morphia.map(PrefLabel.class);
        morphia.map(AltLabel.class);
        MongoClient mongo;
        if(StringUtils.isEmpty(username)){
            mongo = new MongoClient(host, port);
            datastore = morphia.createDatastore(mongo,dbName);
        } else {
            MongoCredential credential =  MongoCredential.createMongoCRCredential(username,dbName,password.toCharArray());
            ServerAddress address = new ServerAddress(host,port);
            List<MongoCredential> credentials  = new ArrayList<>();
            credentials.add(credential);
            mongo = new MongoClient(address,credentials);
            datastore = morphia.createDatastore(mongo,dbName);
        }
        datastore.ensureIndexes();

    }

    public MongoProvider(String uri,String db){
        Morphia morphia = new Morphia();
        morphia.map(Dataset.class);
        morphia.map(Organization.class);
        morphia.map(FtpDatasetMetadata.class);
        morphia.map(FtpMetadata.class);
        morphia.map(FolderMetadata.class);
        morphia.map(HttpMetadata.class);
        morphia.map(HarvestingMetadata.class);
        morphia.map(OAIMetadata.class);
        morphia.map(HttpDatasetMetadata.class);
        morphia.map(OAIDatasetMetadata.class);
        morphia.map(PrefLabel.class);
        morphia.map(AltLabel.class);
        MongoClient mongo=new MongoClient(new MongoClientURI(uri));
        datastore = morphia.createDatastore(mongo,db);
        datastore.ensureIndexes();
    }
    /**
     * Retrieve the datastore connection to Mongo
     * @return The datastore connection to Mongo
     */
    public Datastore getDatastore(){
        return datastore;
    }
}
