import eu.europeana.validation.service.ValidationExecutionService;
import eu.europeana.validation.service.ValidationManagementService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Created by ymamakis on 7/14/16.
 */
@Configuration
public class TestApplication{
    @Bean
    ValidationManagementService getValidationManagementService(){
        return new ValidationManagementService();
    }

    @Bean
    ValidationExecutionService getValidationExecutionService(){
        return new ValidationExecutionService();
    }

    @PostConstruct
    public void startup(){
        MongoProvider.start();
    }

    @PreDestroy
    public void shutdown(){
        MongoProvider.stop();
    }
}
