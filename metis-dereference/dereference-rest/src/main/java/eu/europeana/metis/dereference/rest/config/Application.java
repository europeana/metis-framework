package eu.europeana.metis.dereference.rest.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import eu.europeana.enrichment.rest.client.EnrichmentDriver;
import eu.europeana.metis.dereference.service.MongoDereferenceService;
import eu.europeana.metis.dereference.service.MongoDereferencingManagementService;
import eu.europeana.metis.dereference.service.dao.CacheDao;
import eu.europeana.metis.dereference.service.dao.EntityDao;
import eu.europeana.metis.dereference.service.dao.VocabularyDao;
import eu.europeana.metis.dereference.service.utils.RdfRetriever;
import eu.europeana.metis.dereference.service.utils.RedisProvider;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Spring configuration class
 * Created by ymamakis on 12-2-16.
 */
@Configuration
@ComponentScan (basePackages = {"eu.europeana.metis.dereference.rest","eu.europeana.metis.dereference.rest.exceptions"})
@PropertySource("classpath:dereferencing.properties")
@EnableWebMvc
@EnableSwagger2
public class Application extends WebMvcConfigurerAdapter {

    Logger logger = Logger.getLogger(Application.class);
    @Value("${mongo.host}")
    private String mongoHost;

    @Value("${mongo.port}")
    private int mongoPort;

    @Value("${redis.host}")
    private String redisHost;
    @Value("${redis.port}")
    private int redisPort;
    @Value("${redis.password}")
    private String redisPassword;
    @Value("${entity.db}")
    private String db;
    @Value("${mongo.username}")
    private String username;
    @Value("${mongo.password}")
    private String password;
    @Value("${vocabulary.db}")
    private String vocDb;

    @Value("${enrichment.path}")
    private String enrichmentPath;

    @Bean
    EnrichmentDriver getEnrichmentDriver(){
        return new EnrichmentDriver(enrichmentPath);
    }

    @Bean
    MongoClient getMongo(){
        try {
            if(StringUtils.isEmpty(username)) {
                return new MongoClient(mongoHost, mongoPort);
            } else {
                MongoCredential credential =  MongoCredential.createMongoCRCredential(username,db,password.toCharArray());
                ServerAddress address = new ServerAddress(mongoHost,mongoPort);
                List<MongoCredential> credentials  = new ArrayList<>();
                credentials.add(credential);
               return  new MongoClient(address,credentials);
            }
        } catch (UnknownHostException e) {
           logger.error("Failed to connect to Mongo: " + e.getMessage());
        }
        return null;
    }
    @Override
    public  void configureMessageConverters(List<HttpMessageConverter<?>> converters){
        converters.add(new MappingJackson2HttpMessageConverter());

        super.configureMessageConverters(converters);
    }
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }


    @Bean
    @Order(1)
    public static PropertySourcesPlaceholderConfigurer properties() {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        if(System.getenv().get("VCAP_SERVICES")==null) {
            propertySourcesPlaceholderConfigurer.setLocation(new ClassPathResource("dereferencing.properties"));
        } else {
            Properties properties = new Properties();
            JsonParser parser = new JsonParser();
            JsonObject object = parser.parse(System.getenv().get("VCAP_SERVICES")).getAsJsonObject();
            JsonObject element = object.getAsJsonArray("mongodb-2.0").get(0).getAsJsonObject();

            JsonObject credentials = element.getAsJsonObject("credentials");
            properties.setProperty("vocabulary.db",credentials.get("db").getAsString());
            properties.setProperty("entity.db",credentials.get("db").getAsString());
            properties.setProperty("mongo.host",credentials.get("host").getAsString());
            properties.setProperty("mongo.port",credentials.get("port").getAsString());
            properties.setProperty("mongo.username",credentials.get("username").getAsString());
            properties.setProperty("mongo.password",credentials.get("password").getAsString());
            properties.setProperty("enrichment.path",System.getenv().get("enrichmentpath"));
            JsonObject redisElement = object.getAsJsonArray("redis-2.2").get(0).getAsJsonObject();
            JsonObject redisCredentials = redisElement.getAsJsonObject("credentials");
            properties.setProperty("redis.host",redisCredentials.get("host").getAsString());
            properties.setProperty("redis.port",redisCredentials.get("port").getAsString());
            properties.setProperty("redis.password",redisCredentials.get("password").getAsString());

            propertySourcesPlaceholderConfigurer.setProperties(properties);
        }

        return propertySourcesPlaceholderConfigurer;
    }
    @Bean
    RdfRetriever getRdfRetriever(){
        return new RdfRetriever();
    }
    @Bean
    CacheDao getCacheDao(){
        Logger.getLogger(this.getClass()).error(redisHost+redisPassword+redisPort);

        return new CacheDao(getRedisProvider().getJedis());
    }

    @Bean
    RedisProvider getRedisProvider(){
        return new RedisProvider(redisHost, redisPort, redisPassword);
    }

    @Bean
    EntityDao getEntityDao(){
        return new EntityDao(getMongo(),db,username,password);
    }

    @Bean
    VocabularyDao getVocabularyDao(){
        return new VocabularyDao(getMongo(),vocDb,username,password);
    }

    @Bean
    MongoDereferenceService getMongoDereferenceService(){
        return new MongoDereferenceService();
    }
    @Bean
    MongoDereferencingManagementService getMongoDereferencingManagementService(){
        return new MongoDereferencingManagementService();
    }

    @Bean
    public Docket api(){
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.regex("/.*"))
                .build()
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        ApiInfo apiInfo = new ApiInfo(
                "Dereference REST API",
                "Dereference REST API for Europeana",
                "v1",
                "API TOS",
                "development@europeana.eu",
                "EUPL Licence v1.1",
                ""
        );
        return apiInfo;
    }
}
