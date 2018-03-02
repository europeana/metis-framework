package eu.europeana.identifier.service;

import eu.europeana.identifier.service.utils.Decoupler;
import eu.europeana.identifier.service.utils.IdentifierNormalizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by ymamakis on 7/14/16.
 */
@Configuration
public class Application {
    @Bean
    IdentifierService getIdentifierService(){
        return new IdentifierService();
    }

    @Bean
    ItemizationService getItemizationService(){
        return new ItemizationService();
    }

    @Bean
    IdentifierNormalizer getIdentifierNormalizer(){
        return new IdentifierNormalizer();
    }

    @Bean
    Decoupler getDecoupler(){
        return new Decoupler();
    }
}
