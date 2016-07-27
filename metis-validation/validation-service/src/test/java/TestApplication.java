/*
 * Copyright 2007-2013 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */
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
