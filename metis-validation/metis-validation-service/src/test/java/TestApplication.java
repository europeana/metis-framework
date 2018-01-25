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
import eu.europeana.validation.service.SchemaProvider;
import eu.europeana.validation.service.ValidationExecutionService;
import eu.europeana.validation.service.ValidationServiceConfig;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import eu.europeana.validation.service.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.ls.LSResourceResolver;

import javax.annotation.PostConstruct;
import java.io.FileNotFoundException;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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

    @Resource(name = "validationProperties")
    Properties predefinedSchemasLocations;

    @Bean(name = "validationProperties")
    PropertiesFactoryBean mapper() {
        PropertiesFactoryBean bean = new PropertiesFactoryBean();
        bean.setLocation(new ClassPathResource(
                "validation.properties"));
        return bean;
    }

    @Bean(name = "schemaProvider")
    @DependsOn(value = "validationProperties")
    public SchemaProvider getSchemaProvider() {
        PredefinedSchemas predefinedSchemas = PredefinedSchemasGenerator.generate(predefinedSchemasLocations);
        return new SchemaProvider(predefinedSchemas);
    }

    @Bean
    @DependsOn(value = {"lsResourceResolver", "schemaProvider"})
    ValidationExecutionService getValidationExecutionService() {
        return new ValidationExecutionService(new Config(), getLSResourceResolver());
    }

    @Bean(name = "lsResourceResolver")
    public ClasspathResourceResolver getLSResourceResolver() {
        return new ClasspathResourceResolver();
    }

    @Bean
    public SchemaProvider schemaManager() {
        PredefinedSchemas predefinedSchemas = new PredefinedSchemas();

        predefinedSchemas.add("EDM-INTERNAL", "http://localhost:9999/edm_internal_schema.zip", "EDM-INTERNAL.xsd", "schematron/schematron-internal.xsl");
        predefinedSchemas.add("EDM-EXTERNAL", "http://localhost:9999/edm_external_schema.zip", "EDM.xsd", "schematron/schematron.xsl");

        return new SchemaProvider(predefinedSchemas);
    }

    @PostConstruct
    public void startup() throws IOException {
    }
}
