package eu.europeana.metis.framework.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import eu.europeana.metis.framework.dataset.Dataset;
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
        if(StringUtils.isEmpty(username)){
            MongoClient mongo = new MongoClient(host, port);
            datastore = morphia.createDatastore(mongo,dbName);
        } else {
            MongoCredential credential =  MongoCredential.createMongoCRCredential(username,dbName,password.toCharArray());
            ServerAddress address = new ServerAddress(host,port);
            List<MongoCredential> credentials  = new ArrayList<>();
            credentials.add(credential);
            MongoClient mongo = new MongoClient(address,credentials);
            datastore = morphia.createDatastore(mongo,dbName);
        }
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
