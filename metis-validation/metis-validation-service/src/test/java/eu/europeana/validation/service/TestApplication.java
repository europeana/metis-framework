package eu.europeana.validation.service;

import eu.europeana.metis.network.NetworkUtil;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
public class TestApplication {

  static int portForWireMock = 9999;

  static {
    try {
      portForWireMock = new NetworkUtil().getAvailableLocalPort();
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

  @Bean
  @DependsOn(value = {"lsResourceResolver", "getSchemaProvider"})
  ValidationExecutionService getValidationExecutionService() {
    return new ValidationExecutionService(new Config(), getLSResourceResolver(),
        getSchemaProvider());
  }

  @Bean(name = "lsResourceResolver")
  public ClasspathResourceResolver getLSResourceResolver() {
    return new ClasspathResourceResolver();
  }

  @Bean
  public SchemaProvider getSchemaProvider() {
    PredefinedSchemas predefinedSchemas = new PredefinedSchemas();

    predefinedSchemas
        .add("EDM-INTERNAL", "http://localhost:" + portForWireMock + "/edm_internal_schema.zip",
            "EDM-INTERNAL.xsd", "schematron/schematron-internal.xsl");
    predefinedSchemas
        .add("EDM-EXTERNAL", "http://localhost:" + portForWireMock + "/edm_external_schema.zip",
            "EDM.xsd", "schematron/schematron.xsl");

    return new SchemaProvider(predefinedSchemas);
  }
}
