package eu.europeana.metis.config;

import javax.annotation.PreDestroy;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mongodb.MongoClient;

import eu.europeana.metis.mongo.MongoProvider;
import eu.europeana.metis.ui.mongo.dao.DBUserDao;
import eu.europeana.metis.ui.mongo.dao.RoleRequestDao;
import eu.europeana.metis.ui.mongo.domain.DBUser;
import eu.europeana.metis.ui.mongo.domain.RoleRequest;
import eu.europeana.metis.ui.mongo.service.UserService;

/**
 * The configuration aims to give access to the user account data such as Skype account, Country, Organization, etc.
 * @author alena
 *
 */
@Configuration
public class MetisDBConfig {
	
    int port = 10005;
    
    public MetisDBConfig(){
        MongoProvider.start(port);
    }

    @Bean
    public DBUserDao dbUserDao(){
        Morphia morphia = new Morphia();
        morphia.map(DBUser.class);
        Datastore ds = morphia.createDatastore(new MongoClient("localhost", port), "test");
        return new DBUserDao(DBUser.class, ds);
    }

    @Bean
    public RoleRequestDao roleRequestDao(){
        Morphia morphia = new Morphia();
        morphia.map(RoleRequest.class);
        Datastore ds = morphia.createDatastore(new MongoClient("localhost", port), "test");
        return new RoleRequestDao(RoleRequest.class, ds);
    }
    
    @Bean
    public UserService service(){
        return new UserService();
    }

    @PreDestroy
    public void stop(){
        MongoProvider.stop();
    }
}
