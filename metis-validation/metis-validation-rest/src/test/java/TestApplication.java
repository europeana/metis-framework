import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import eu.europeana.metis.mongo.EmbeddedLocalhostMongo;
import eu.europeana.validation.model.Schema;
import eu.europeana.validation.service.*;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;


@EnableWebMvc
@Configuration
public class TestApplication {

    private class Config implements ValidationServiceConfig {
        @Override
        public int getThreadCount() {
            return 10;
        }
    }

    private final String mongoHost;
    private final int mongoPort;
    private EmbeddedLocalhostMongo embeddedLocalhostMongo;

    public TestApplication() throws IOException {
        embeddedLocalhostMongo = new EmbeddedLocalhostMongo();
        embeddedLocalhostMongo.start();
        mongoHost = embeddedLocalhostMongo.getMongoHost();
        mongoPort = embeddedLocalhostMongo.getMongoPort();
    }

    @Bean
    ValidationManagementService getValidationManagementService() {
        ServerAddress address = new ServerAddress(mongoHost, mongoPort);
        MongoClient client = new MongoClient(address);
        Morphia morphia = new Morphia();
        morphia.map(Schema.class);
        Datastore datastore = morphia.createDatastore(client, "validation");
        datastore.ensureIndexes();
        AbstractSchemaDao abstractSchemaDao = new SchemaDao(datastore, "/tmp/schema");
        ValidationManagementService validationManagementService = new ValidationManagementService(
                abstractSchemaDao);

        return validationManagementService;
    }

    @Bean
    @DependsOn(value = "abstractLSResourcResolver")
    ValidationExecutionService getValidationExecutionService() {
        return new ValidationExecutionService(new Config(), getValidationManagementService(), getAbstractLSResourceResolver());
    }

    @Bean(name = "abstractLSResourcResolver")
    public AbstractLSResourceResolver getAbstractLSResourceResolver() {
        return new ClasspathResourceResolver();
    }

    @PostConstruct
    public void startup() throws IOException {
    }

    @PreDestroy
    public void shutdown() {
        embeddedLocalhostMongo.stop();
    }
}

