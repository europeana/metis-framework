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

import eu.europeana.validation.service.ClasspathResourceResolver;
import eu.europeana.validation.service.ValidationExecutionService;
import eu.europeana.validation.service.ValidationServiceConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.w3c.dom.ls.LSResourceResolver;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * Created by ymamakis on 7/14/16.
 */
@Configuration
public class TestApplication {

  private class Config implements ValidationServiceConfig {
    @Override
    public int getThreadCount() {
      return 10;
    }
  }

  public TestApplication() {
  }

  @Bean
  @DependsOn(value = "lsResourceResolver")
  ValidationExecutionService getValidationExecutionService() {
    return new ValidationExecutionService(new Config(), getLSResourceResolver());
  }

  @Bean(name = "lsResourceResolver")
  public LSResourceResolver getLSResourceResolver() {
      return new ClasspathResourceResolver();
  }

  @PostConstruct
  public void startup() throws IOException {
  }
}
