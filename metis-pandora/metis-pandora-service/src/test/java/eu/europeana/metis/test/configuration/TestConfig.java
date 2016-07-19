package eu.europeana.metis.test.configuration;

import com.mongodb.MongoClient;
import eu.europeana.metis.mapping.persistence.DatasetStatisticsDao;
import eu.europeana.metis.mapping.persistence.FlagDao;
import eu.europeana.metis.mapping.persistence.MongoMappingDao;
import eu.europeana.metis.service.*;
import org.mongodb.morphia.Morphia;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;

/**
 * Created by ymamakis on 6/27/16.
 */
@Configuration
public class TestConfig {

    public TestConfig(){
        MongoProvider.start(10000);
    }

    @Bean
    MongoMappingService getMongoMappingService(){
        return new MongoMappingService();
    }

    @Bean
    StatisticsService getStatisticsService(){
        return new StatisticsService();
    }

    @Bean
    ValidationService getValidationService(){
        return new ValidationService();
    }

    @Bean
    XSDService getXsdService(){
        return new XSDService();
    }

    @Bean
    XSLTGenerationService getXsltGenerationService(){
        return new XSLTGenerationService();
    }

    @Bean
    MongoMappingDao getMongoMappingDao(){
        Morphia morphia = new Morphia();
        MongoClient client = new MongoClient("localhost", 10000);
        morphia.mapPackage("eu.europeana.metis.mapping.common", true)
                .mapPackage("java.math.BigInteger",true);
        return new MongoMappingDao(morphia, client, "mapping-test");
    }

    @Bean
    FlagDao getFlagDao() {
        Morphia morphia = new Morphia();
        MongoClient client = new MongoClient("localhost", 10000);
        morphia.mapPackage("eu.europeana.metis.mapping.validation", true)
                .mapPackage("eu.europeana.metis.mapping.common", true)
                .mapPackage("java.math.BigInteger",true);

        return new FlagDao(morphia, client, "flag-test");
    }

    @Bean
    DatasetStatisticsDao getDatasetStatisticsDao() {
        Morphia morphia = new Morphia();
        MongoClient client = new MongoClient("localhost",10000);
        morphia.mapPackage("eu.europeana.metis.mapping.statistics", true)
                .mapPackage("eu.europeana.metis.mapping.model", true)
                .mapPackage("java.math.BigInteger",true);
        return new DatasetStatisticsDao(morphia, client, "statistics-test");
    }

    @PreDestroy
    public void close(){
        MongoProvider.clear();
        MongoProvider.stop();
    }
}
