package eu.europeana.metis.config;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import eu.europeana.metis.framework.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.service.ExampleMappingService;
import eu.europeana.metis.service.MappingService;
import eu.europeana.metis.ui.ldap.dao.UserDao;
import eu.europeana.metis.ui.ldap.dao.impl.UserDaoImpl;
import eu.europeana.metis.ui.mongo.dao.DBUserDao;
import eu.europeana.metis.ui.mongo.dao.RoleRequestDao;
import eu.europeana.metis.ui.mongo.domain.DBUser;
import eu.europeana.metis.ui.mongo.domain.RoleRequest;
import eu.europeana.metis.ui.mongo.service.UserService;
import eu.europeana.metis.utils.PivotalCloudFoundryServicesReader;
import java.util.Arrays;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * The configuration is for DB access (MongoDB), where the user account data such as Skype account,
 * Country, Organization, etc. is stored.
 *
 * @author alena
 */
@Configuration
@PropertySource("classpath:mongo.properties")
public class MetisConfig implements InitializingBean {
  //Mongo
  @Value("${mongo.host}")
  private String mongoHost;
  @Value("${mongo.port}")
  private int mongoPort;
  @Value("${mongo.username}")
  private String mongoUsername;
  @Value("${mongo.password}")
  private String mongoPassword;
  @Value("${mongo.db}")
  private String mongoDb;

  private MongoClientURI mongoClientURI;
  private MorphiaDatastoreProvider provider;

  /**
   * Used for overwriting properties if cloud foundry environment is used
   */
  @Override
  public void afterPropertiesSet() throws Exception {
    String vcapServicesJson = System.getenv().get("VCAP_SERVICES");
    if (StringUtils.isNotEmpty(vcapServicesJson) && !StringUtils.equals(vcapServicesJson, "{}")) {
      PivotalCloudFoundryServicesReader vcapServices = new PivotalCloudFoundryServicesReader(
          vcapServicesJson);
      mongoClientURI = vcapServices.getMongoClientUriFromService();
    }
  }

  @Bean
  public UserDao userDao() {
    return new UserDaoImpl();
  }

  @Bean
  @DependsOn(value = "morphiaDatastoreProvider")
  public DBUserDao dbUserDao() {
    return new DBUserDao(DBUser.class, provider.getDatastore());
  }

  @Bean
  @DependsOn(value = "morphiaDatastoreProvider")
  public RoleRequestDao roleRequestDao() {
    return new RoleRequestDao(RoleRequest.class, provider.getDatastore());
  }

  @Bean(name = "morphiaDatastoreProvider")
  MorphiaDatastoreProvider getMongoProvider() {
    if (mongoClientURI != null) {
      provider = new MorphiaDatastoreProvider(new MongoClient(mongoClientURI),
          mongoClientURI.getDatabase());
      return provider;
    } else {
      ServerAddress address = new ServerAddress(mongoHost, mongoPort);
      MongoCredential mongoCredential;
      MongoClient mongoClient;
      if (StringUtils.isNotEmpty(mongoUsername) && StringUtils.isNotEmpty(mongoPassword)) {
        mongoCredential = MongoCredential
            .createCredential(mongoUsername, mongoDb, mongoPassword.toCharArray());
        mongoClient = new MongoClient(address, Arrays.asList(mongoCredential));
      } else {
        mongoClient = new MongoClient(address);
      }
      provider = new MorphiaDatastoreProvider(mongoClient, mongoDb);
      return provider;
    }
  }

  @Bean
  public UserService userService() {
    return new UserService();
  }

  @Bean
  public MappingService mappingService() {
    return new ExampleMappingService();
  }

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    return new PropertySourcesPlaceholderConfigurer();
  }

}
