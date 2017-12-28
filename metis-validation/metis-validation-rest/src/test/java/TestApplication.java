import eu.europeana.validation.service.ClasspathResourceResolver;
import eu.europeana.validation.service.SchemaProvider;
import eu.europeana.validation.service.ValidationExecutionService;
import eu.europeana.validation.service.ValidationServiceConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.HashMap;
import java.util.Map;


@EnableWebMvc
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
    @DependsOn(value = "lsResourcResolver")
    ValidationExecutionService getValidationExecutionService() {
        return new ValidationExecutionService(new Config(), getLSResourceResolver());
    }

    @Bean(name = "lsResourcResolver")
    public ClasspathResourceResolver getLSResourceResolver() {
        return new ClasspathResourceResolver();
    }

    @Bean
    public SchemaProvider schemaManager(){
        Map<String,String> predefinedSchemasLocations = new HashMap();

        predefinedSchemasLocations.put("edm-internal", "http://localhost/schema.zip");
        predefinedSchemasLocations.put("edm-external", "http://localhost/schema.zip");

        return new SchemaProvider(predefinedSchemasLocations);
    }
}

