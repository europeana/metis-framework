import eu.europeana.validation.service.ClasspathResourceResolver;
import eu.europeana.validation.service.ValidationExecutionService;
import eu.europeana.validation.service.ValidationServiceConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.w3c.dom.ls.LSResourceResolver;


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
    public LSResourceResolver getLSResourceResolver() {
        return new ClasspathResourceResolver();
    }
}

