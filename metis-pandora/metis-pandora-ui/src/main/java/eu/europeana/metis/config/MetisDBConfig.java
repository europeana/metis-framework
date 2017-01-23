package eu.europeana.metis.config;

import java.net.UnknownHostException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import eu.europeana.metis.framework.mongo.MongoProvider;
import eu.europeana.metis.ui.ldap.dao.UserDao;
import eu.europeana.metis.ui.ldap.dao.impl.UserDaoImpl;
import eu.europeana.metis.ui.mongo.dao.DBUserDao;
import eu.europeana.metis.ui.mongo.dao.RoleRequestDao;
import eu.europeana.metis.ui.mongo.domain.DBUser;
import eu.europeana.metis.ui.mongo.domain.RoleRequest;
import eu.europeana.metis.ui.mongo.service.UserService;

/**
 * The configuration is for DB access (MongoDB), where the user account data such as Skype account, 
 * Country, Organization, etc. is stored.
 * @author alena
 *
 */
@Configuration
@PropertySource("classpath:/mongoDB.properties")
public class MetisDBConfig {
	

    @Value("${mongo.host}")
    private String mongoHost;
    
    @Value("${mongo.port}")
    private String mongoPort;
    
    @Value("${mongo.db}")
    private String db;
    
    @Value("${mongo.username}")
    private String username;
    
    @Value("${mongo.password}")
    private String password;
    
    private MongoProvider provider;
    
    @Bean
    public UserDao userDao() {
    	return new UserDaoImpl();
    }

    @Bean
    @DependsOn(value = "mongoProvider")
    public DBUserDao dbUserDao(){
        return new DBUserDao(DBUser.class, provider.getDatastore());
    }

    @Bean
    @DependsOn(value = "mongoProvider")
    public RoleRequestDao roleRequestDao(){
        return new RoleRequestDao(RoleRequest.class, provider.getDatastore());
    }
    
    @Bean(name = "mongoProvider")
    MongoProvider mongoProvider(){
        try {
        	//FIXME this piece of code below is commented in order to have the app running in Pivotal server!
//            if(System.getenv().get("VCAP_SERVICES")==null) {
                provider = new MongoProvider(mongoHost, Integer.parseInt(mongoPort), db, username, password);
                return provider;
//            } else {
//                JsonParser parser = new JsonParser();
//                JsonObject object = parser.parse(System.getenv().get("VCAP_SERVICES")).getAsJsonObject();
//                JsonObject element = object.getAsJsonArray("mlab").get(0).getAsJsonObject();
//
//                JsonObject credentials = element.getAsJsonObject("credentials");
//                JsonPrimitive uri = credentials.getAsJsonPrimitive("uri");
//                String db = StringUtils.substringAfterLast(uri.getAsString(),"/");
//                provider = new MongoProvider(uri.getAsString(),db);
//                return provider;
//            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    @Bean
    public UserService userService() {
    	return new UserService();
    }
}
