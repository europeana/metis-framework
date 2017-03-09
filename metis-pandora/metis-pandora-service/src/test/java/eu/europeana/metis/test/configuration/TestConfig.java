package eu.europeana.metis.test.configuration;

import com.mongodb.MongoClient;
import eu.europeana.metis.mapping.persistence.AttributeDao;
import eu.europeana.metis.mapping.persistence.DatasetStatisticsDao;
import eu.europeana.metis.mapping.persistence.ElementDao;
import eu.europeana.metis.mapping.persistence.FlagDao;
import eu.europeana.metis.mapping.persistence.MappingSchemaDao;
import eu.europeana.metis.mapping.persistence.MappingsDao;
import eu.europeana.metis.mapping.persistence.MongoMappingDao;
import eu.europeana.metis.mapping.persistence.StatisticsDao;
import eu.europeana.metis.mongo.MongoProvider;
import eu.europeana.metis.service.MongoMappingService;
import eu.europeana.metis.service.StatisticsService;
import eu.europeana.metis.service.ValidationService;
import eu.europeana.metis.service.XSDService;
import eu.europeana.metis.service.XSLTGenerationService;
import eu.europeana.metis.utils.NetworkUtil;
import java.io.IOException;
import javax.annotation.PreDestroy;
import org.mongodb.morphia.Morphia;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by ymamakis on 6/27/16.
 */
@Configuration
public class TestConfig {
    private final int port;
    private MongoProvider mongoProvider;
    public TestConfig() throws IOException {
        port = NetworkUtil.getAvailableLocalPort();
        mongoProvider = new MongoProvider();
        mongoProvider.start(port);
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
        MongoClient client = new MongoClient("localhost", port);
        morphia.mapPackage("eu.europeana.metis.mapping.common", true)
                .mapPackage("java.math.BigInteger",true);
        return new MongoMappingDao(morphia, client, "mapping-test");
    }
    @Bean
    MappingsDao getMappingsDao(){
        Morphia morphia = new Morphia();
        MongoClient client = new MongoClient("localhost", port);
        morphia.mapPackage("eu.europeana.metis.mapping.common", true)
                .mapPackage("java.math.BigInteger",true);
        return new MappingsDao(morphia, client, "mapping-test");
    }

    @Bean
    MappingSchemaDao getMappingSchemaDao() {
        Morphia morphia = new Morphia();
        MongoClient client = new MongoClient("localhost", port);
        morphia.mapPackage("eu.europeana.metis.mapping.common", true)
                .mapPackage("java.math.BigInteger",true);
        return new MappingSchemaDao(morphia, client, "mapping-test");
    }

    @Bean
    ElementDao getElementDao() {
        Morphia morphia = new Morphia();
        MongoClient client = new MongoClient("localhost", port);
        morphia.mapPackage("eu.europeana.metis.mapping.common", true)
                .mapPackage("java.math.BigInteger",true);
        return new ElementDao(morphia, client, "mapping-test");
    }

    @Bean
    AttributeDao getAttributeDao() {
        Morphia morphia = new Morphia();
        MongoClient client = new MongoClient("localhost", port);
        morphia.mapPackage("eu.europeana.metis.mapping.common", true)
                .mapPackage("java.math.BigInteger",true);
        return new AttributeDao(morphia, client, "mapping-test");
    }
    @Bean
    StatisticsDao getStatisticsDao(){
        Morphia morphia = new Morphia();
        MongoClient client = new MongoClient("localhost", port);
        morphia.mapPackage("eu.europeana.metis.mapping.statistics", true)
                .mapPackage("java.math.BigInteger",true);
        return new StatisticsDao(morphia,client,"statistics-test");
    }
    @Bean
    FlagDao getFlagDao() {
        Morphia morphia = new Morphia();
        MongoClient client = new MongoClient("localhost", port);
        morphia.mapPackage("eu.europeana.metis.mapping.validation", true)
                .mapPackage("eu.europeana.metis.mapping.common", true)
                .mapPackage("java.math.BigInteger",true);

        return new FlagDao(morphia, client, "flag-test");
    }

    @Bean
    DatasetStatisticsDao getDatasetStatisticsDao() {
        Morphia morphia = new Morphia();
        MongoClient client = new MongoClient("localhost",port);
        morphia.mapPackage("eu.europeana.metis.mapping.statistics", true)
                .mapPackage("eu.europeana.metis.mapping.model", true)
                .mapPackage("java.math.BigInteger",true);
        return new DatasetStatisticsDao(morphia, client, "statistics-test");
    }

    @PreDestroy
    public void close(){
        mongoProvider.stop();
    }
}
