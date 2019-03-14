import eu.europeana.metis.utils.NetworkUtil;
import eu.europeana.validation.service.ClasspathResourceResolver;
import eu.europeana.validation.service.PredefinedSchemas;
import eu.europeana.validation.service.SchemaProvider;
import eu.europeana.validation.service.ValidationExecutionService;
import eu.europeana.validation.service.ValidationServiceConfig;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;


@EnableWebMvc
@Configuration
public class TestApplication {

  static int portForWireMock = 9999;

  static {
    try {
      portForWireMock = NetworkUtil.getAvailableLocalPort();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

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
    return new ValidationExecutionService(new Config(), getLSResourceResolver(), getSchemaProvider());
  }

  @Bean(name = "lsResourcResolver")
  public ClasspathResourceResolver getLSResourceResolver() {
    return new ClasspathResourceResolver();
  }

  @Bean
  public SchemaProvider getSchemaProvider() {
    PredefinedSchemas predefinedSchemas = new PredefinedSchemas();

    predefinedSchemas
        .add("EDM-INTERNAL", "http://localhost:" + portForWireMock + "/test_schema.zip", "EDM-INTERNAL.xsd",
            "schematron/schematron-internal.xsl");
    predefinedSchemas.add("EDM-EXTERNAL", "http://localhost:" + portForWireMock + "/test_schema.zip", "EDM.xsd",
        "schematron/schematron.xsl");

    return new SchemaProvider(predefinedSchemas);
  }
}

