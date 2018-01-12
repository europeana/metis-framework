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
package eu.europeana.validation.service;

import eu.europeana.validation.model.ValidationResult;
import eu.europeana.validation.model.ValidationResultList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.*;

/**
 * Schema service service
 * Created by gmamakis on 18-12-15.
 */
@Service
public class ValidationExecutionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationExecutionService.class);

    private final ClasspathResourceResolver lsResourceResolver;
    private final ValidationServiceConfig config;
    private final ExecutorService es;

    @Autowired
    private SchemaProvider schemaProvider;

    @Autowired
    public ValidationExecutionService(ValidationServiceConfig config, ClasspathResourceResolver lsResourceResolver) {
        this.config = config;
        this.lsResourceResolver = lsResourceResolver;
        this.es = Executors.newFixedThreadPool(config.getThreadCount());
    }

    public ValidationExecutionService(ValidationServiceConfig config, ClasspathResourceResolver lsResourceResolver, SchemaProvider schemaProvider) {
        this(config, lsResourceResolver);
        this.schemaProvider = schemaProvider;
    }

    /**
     * Default constructor that creates a fully configured instance of this service with default configuration and schema provider
     */
    public ValidationExecutionService() {
        this(null);
    }

    /**
     * Constructs the service instance that has default configuration but uses external property file with default URLs for
     * edm-internal and edm-external schemas. The property file must contain key value pairs for "edm-internal" and "edm-external".
     *
     * @param propertyFilename path to a property file with defined URLs for edm-internal and edm-external schemas
     */
    public ValidationExecutionService(String propertyFilename) {
        this(new ValidationServiceConfig() {
            @Override
            public int getThreadCount() {
                return 10;
            }
        }, new ClasspathResourceResolver());
        this.schemaProvider = getSchemaProvider(propertyFilename);
    }

    /**
     * Read properties from external file if specified. Otherwise internal property file is used.
     *
     * @param propertyFilename path to a property file with defined URLs for edm-internal and edm-external schemas
     * @return
     */
    private Properties readProperties(String propertyFilename) {
        InputStream is = null;
        Properties props = new Properties();
        try {
            if (propertyFilename == null) {
                is = this.getClass().getClassLoader().getResourceAsStream("validation.properties");
            } else {
                is = new FileInputStream(propertyFilename);
            }
            props.load(is);
        } catch (IOException e) {
            LOGGER.warn("Validation properties file could not be loaded.");
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    LOGGER.warn("Could not close property file input stream", e);
                }
            }
        }

        return props;
    }

    private SchemaProvider getSchemaProvider(String propertyFilename) {
        Properties predefinedSchemasLocations = readProperties(propertyFilename);
        PredefinedSchemas predefinedSchemas = PredefinedSchemasGenerator.generate(predefinedSchemasLocations);
        return new SchemaProvider(predefinedSchemas);
    }

    /**
     * Perform single service given a schema.
     *
     * @param schema           The schema to perform service against.
     * @param rootFileLocation location of the schema root file
     * @param document         The document to validate against
     * @return A service result
     */
    public ValidationResult singleValidation(final String schema,final String rootFileLocation, final String document) {
        return new Validator(schema, rootFileLocation, document, schemaProvider, lsResourceResolver).call();
    }

    /**
     * Batch service given a schema
     *
     * @param schema    The schema to validate against
     * @param documents The documents to validate
     * @return A list of service results
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public ValidationResultList batchValidation(final String schema, final String rootFileLocation, List<String> documents) throws InterruptedException, ExecutionException {

        ExecutorCompletionService cs = new ExecutorCompletionService(es);
        for (final String document : documents) {
            cs.submit(new Validator(schema, rootFileLocation, document,schemaProvider, lsResourceResolver));
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
        if (resultList.getResultList().size() == 0) {
            resultList.setSuccess(true);
        }
        return resultList;
    }

    @PreDestroy
    void cleanup() {
        if (es != null) {
            es.shutdown();
        }
    }

}
