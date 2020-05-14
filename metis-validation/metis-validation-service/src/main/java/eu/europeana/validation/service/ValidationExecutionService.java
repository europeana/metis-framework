package eu.europeana.validation.service;

import eu.europeana.validation.model.ValidationResult;
import eu.europeana.validation.model.ValidationResultList;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Schema service service Created by gmamakis on 18-12-15.
 */
@Service
public class ValidationExecutionService {

  private final ClasspathResourceResolver lsResourceResolver;
  private final ExecutorService es;
  private final SchemaProvider schemaProvider;
  private static final int DEFAULT_THREADS_COUNT = 10;

  /**
   * Creates {@link ValidationExecutionService} instance based on given configuration
   *
   * @param validationServiceConfig the configuration to use
   * @param lsResourceResolver resolver for split xsds
   * @param schemaProvider the schema provider
   */
  @Autowired
  public ValidationExecutionService(ValidationServiceConfig validationServiceConfig,
      ClasspathResourceResolver lsResourceResolver, SchemaProvider schemaProvider) {
    this.lsResourceResolver = lsResourceResolver;
    this.es = Executors.newFixedThreadPool(validationServiceConfig.getThreadCount());
    this.schemaProvider = schemaProvider;
  }


  /**
   * Constructs the service instance that has default configuration but uses external property file
   * with default URLs for edm-internal and edm-external schemas. The properties must contain:
   * predefinedSchemas=EDM-INTERNAL,EDM-EXTERNAL predefinedSchemas.EDM-INTERNAL.url=
   * predefinedSchemas.EDM-INTERNAL.rootLocation= predefinedSchemas.EDM-EXTERNAL.url=
   * predefinedSchemas.EDM-EXTERNAL.rootLocation=
   *
   * @param predefinedSchemasLocations properties with defined URLs and locations for edm-internal
   * and edm-external schemas
   */
  public ValidationExecutionService(Properties predefinedSchemasLocations) {
    this(() -> DEFAULT_THREADS_COUNT, new ClasspathResourceResolver(),
        getSchemaProvider(predefinedSchemasLocations));
  }


  private static SchemaProvider getSchemaProvider(Properties predefinedSchemasLocations) {
    PredefinedSchemas predefinedSchemas = PredefinedSchemasGenerator
        .generate(predefinedSchemasLocations);
    return new SchemaProvider(predefinedSchemas);
  }

  /**
   * Perform single service given a schema.
   *
   * @param schema The schema to perform service against.
   * @param rootFileLocation location of the schema root file
   * @param schematronFileLocation location of the schematron file
   * @param document The document to validate against
   * @return A service result
   */
  public ValidationResult singleValidation(final String schema, final String rootFileLocation,
          String schematronFileLocation, final String document) {
    try (final ByteArrayInputStream inputStream = new ByteArrayInputStream(
            document.getBytes(StandardCharsets.UTF_8))) {
      return singleValidation(schema, rootFileLocation, schematronFileLocation, inputStream);
    } catch (IOException e) {
      // Shouldn't happen
      throw new IllegalStateException(e);
    }
  }

  /**
   * Perform single service given a schema.
   *
   * @param schema The schema to perform service against.
   * @param rootFileLocation location of the schema root file
   * @param schematronFileLocation location of the schematron file
   * @param document The document to validate against
   * @return A service result
   */
  public ValidationResult singleValidation(final String schema, final String rootFileLocation,
          String schematronFileLocation, final InputStream document) {
    return new Validator(schema, rootFileLocation, schematronFileLocation, document,
            schemaProvider, lsResourceResolver).call();
  }

  /**
   * Batch service given a schema
   *
   * @param schema The schema to validate against
   * @param documents The documents to validate
   * @param rootFileLocation place where entry xsd file is located
   * @param schematronFileLocation place where schematron file is located
   * @return A list of service results
   */
  public ValidationResultList batchValidation(final String schema, final String rootFileLocation,
      final String schematronFileLocation,
      List<? extends InputStream> documents) throws InterruptedException, ExecutionException {

    ExecutorCompletionService<ValidationResult> cs = new ExecutorCompletionService<>(es);
    for (final InputStream document : documents) {
      cs.submit(
          new Validator(schema, rootFileLocation, schematronFileLocation, document, schemaProvider,
              lsResourceResolver));
    }

    List<ValidationResult> results = new ArrayList<>();
    for (int i = 0; i < documents.size(); i++) {
      Future<ValidationResult> future = cs.take();
      ValidationResult res = future.get();
      if (!res.isSuccess()) {
        results.add(res);
      }
    }

    ValidationResultList resultList = new ValidationResultList();
    resultList.setResultList(results);
    if (resultList.getResultList().isEmpty()) {
      resultList.setSuccess(true);
    }
    return resultList;
  }

  @PreDestroy
  void cleanup() throws IOException {
    if (es != null) {
      es.shutdown();
    }
    if (schemaProvider != null) {
      schemaProvider.cleanUp();
    }
  }

}
