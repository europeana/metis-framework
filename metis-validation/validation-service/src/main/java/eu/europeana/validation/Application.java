package eu.europeana.validation;

import eu.europeana.validation.validation.ValidationExecutionService;
import eu.europeana.validation.validation.ValidationManagementService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by ymamakis on 7/14/16.
 */
@Configuration
public class Application {
    @Bean
    ValidationManagementService getValidationManagementService(){
        return new ValidationManagementService();
    }

    @Bean
    ValidationExecutionService getValidationExecutionService(){
        return new ValidationExecutionService();
    }

}
