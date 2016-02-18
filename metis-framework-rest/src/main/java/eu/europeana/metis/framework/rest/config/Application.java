package eu.europeana.metis.framework.rest.config;

import eu.europeana.metis.framework.dao.DatasetDao;
import eu.europeana.metis.framework.dao.OrganizationDao;
import eu.europeana.metis.framework.mongo.MongoProvider;
import eu.europeana.metis.framework.service.DatasetService;
import eu.europeana.metis.framework.service.OrganizationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.net.UnknownHostException;
import java.util.List;

/**
 * Spring configuration class
 * Created by ymamakis on 12-2-16.
 */
@Configuration
@ComponentScan (basePackages = "eu.europeana.metis.framework.rest")
@PropertySource("classpath:metis.properties")
@EnableWebMvc
public class Application extends WebMvcConfigurerAdapter {

    @Value("${mongo.host}")
    private String mongoHost;
    @Value("${mongo.port}")
    private String mongoPort;
    @Value("${mongo.db}")
    private String db;
    @Value("${mongo.username}")
    private String username;
    @Value("${mongo.pass}")
    private String password;

    @Override
    public  void configureMessageConverters(List<HttpMessageConverter<?>> converters){
        converters.add(new MappingJackson2HttpMessageConverter());

        super.configureMessageConverters(converters);
    }
    @Bean
    MongoProvider getMongoProvider(){
        try {
            return  new MongoProvider(mongoHost,Integer.parseInt(mongoPort),db,username,password);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer properties() {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        propertySourcesPlaceholderConfigurer.setLocation(new ClassPathResource("metis.properties"));
        return propertySourcesPlaceholderConfigurer;
    }
    @Bean
    public DatasetDao getDatasetDao(){
        return new DatasetDao();
    }

    @Bean
    public OrganizationDao getOrganizationDao(){
        return new OrganizationDao();
    }

    @Bean
    public DatasetService getDatasetService(){
        return new DatasetService();
    }

    @Bean
    public OrganizationService getOrganizationService(){
        return new OrganizationService();
    }


}
