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

import eu.europeana.metis.mongo.EmbeddedLocalhostMongo;
import eu.europeana.validation.service.ValidationExecutionService;
import eu.europeana.validation.service.ValidationManagementService;
import java.io.IOException;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by ymamakis on 7/14/16.
 */
@Configuration
public class TestApplication {

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
    ValidationManagementService validationManagementService = new ValidationManagementService(
        eu.europeana.validation.service.Configuration
            .getInstance(mongoHost, mongoPort, "validation", "/tmp/schema"));

    return validationManagementService;
  }

  @Bean
  ValidationExecutionService getValidationExecutionService() {
    return new ValidationExecutionService();
  }

  @PostConstruct
  public void startup() throws IOException {
  }

  @PreDestroy
  public void shutdown() {
    embeddedLocalhostMongo.stop();
  }
}
