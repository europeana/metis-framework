package eu.europeana.metis.ui.test.config;

import com.mongodb.MongoClient;
import eu.europeana.metis.mongo.MongoProvider;
import eu.europeana.metis.ui.ldap.dao.UserDao;
import eu.europeana.metis.ui.ldap.dao.impl.UserDaoImpl;
import eu.europeana.metis.ui.mongo.dao.DBUserDao;
import eu.europeana.metis.ui.mongo.dao.RoleRequestDao;
import eu.europeana.metis.ui.mongo.domain.DBUser;
import eu.europeana.metis.ui.mongo.domain.RoleRequest;
import eu.europeana.metis.ui.mongo.service.UserService;
import org.mockito.Mockito;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;

import javax.annotation.PreDestroy;

/**
 * Created by ymamakis on 11/25/16.
 */
@Configuration
public class TestApplication {

    int port = 10005;
    public TestApplication(){
        MongoProvider.start(port);
    }

    @Bean
    public DBUserDao dbUserDao(){
        Morphia morphia = new Morphia();
        morphia.map(DBUser.class);
        Datastore ds = morphia.createDatastore(new MongoClient("localhost",port),"test");
        return new DBUserDao(DBUser.class,ds);
    }

    @Bean
    public RoleRequestDao roleRequestDao(){
        Morphia morphia = new Morphia();
        morphia.map(RoleRequest.class);
        Datastore ds = morphia.createDatastore(new MongoClient("localhost",port),"test");
        return new RoleRequestDao(RoleRequest.class,ds);
    }

    @Bean
    public UserDao userDao(){
        return Mockito.mock(UserDaoImpl.class);
    }

    @Bean
    public UserService service(){
        return new UserService();
    }

    @Bean
    public LdapTemplate template(){
        return Mockito.mock(LdapTemplate.class);
    }
    @PreDestroy
    public void stop(){
        MongoProvider.stop();
    }
}
