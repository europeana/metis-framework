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

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import eu.europeana.validation.model.Record;
import eu.europeana.validation.model.ValidationResult;
import eu.europeana.validation.model.ValidationResultList;
import eu.europeana.validation.service.*;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.*;
import org.junit.runner.RunWith;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

/**
 * Created by gmamakis on 18-12-15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestApplication.class, loader = AnnotationConfigContextLoader.class)
@WebAppConfiguration
public class TestValidationExecution {

    private static final String EDM_EXTERNAL = "EDM-EXTERNAL";

    private static final String EDM_INTERNAL = "EDM-INTERNAL";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(9999));

    @Autowired
    ValidationExecutionService validationExecutionService;

    @Before
    public void prepare() {
        wireMockRule.resetAll();
        wireMockRule.stubFor(get(urlEqualTo("/test_schema.zip"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(2000)
                        .withBodyFile("test_schema.zip")));
    }

    @Test
    public void testSingleValidationSuccess() throws Exception {
        String fileToValidate = IOUtils.toString(new FileInputStream("src/test/resources/Item_35834473_test.xml"));
        ValidationResult result = validationExecutionService.singleValidation(EDM_INTERNAL,"EDM-INTERNAL.xsd", fileToValidate);
        Assert.assertEquals(true, result.isSuccess());
        Assert.assertNull(result.getRecordId());
        Assert.assertNull(result.getMessage());
    }

    @Test
    public void testSingleValidationFailure() throws Exception {

        String fileToValidate = IOUtils.toString(new FileInputStream("src/test/resources/Item_35834473_wrong.xml"));
        ValidationResult result = validationExecutionService.singleValidation(EDM_INTERNAL,"EDM-INTERNAL.xsd", fileToValidate);
        Assert.assertEquals(false, result.isSuccess());
        Assert.assertNotNull(result.getRecordId());
        Assert.assertNotNull(result.getMessage());
    }

    @Test
    public void testSingleValidationFailureWrongSchema() throws Exception {

        String fileToValidate = IOUtils.toString(new FileInputStream("src/test/resources/Item_35834473.xml"));
        ValidationResult result = validationExecutionService.singleValidation(EDM_EXTERNAL, "EDM-INTERNAL.xsd", fileToValidate);
        Assert.assertEquals(false, result.isSuccess());
        Assert.assertNotNull(result.getRecordId());
        Assert.assertNotNull(result.getMessage());
    }

    @Test
    public void testBatchValidationSuccess() throws IOException, ExecutionException, InterruptedException, ZipException {
        String fileName = "src/test/resources/test";
        ZipFile file = new ZipFile("src/test/resources/test.zip");
        file.extractAll(fileName);

        File[] files = new File(fileName).listFiles();
        List<String> xmls = new ArrayList<>();
        for (File input : files) {
            xmls.add(IOUtils.toString(new FileInputStream(input)));
        }
        ValidationResultList result = validationExecutionService.batchValidation(EDM_INTERNAL, "EDM-INTERNAL.xsd", xmls);
        Assert.assertEquals(true, result.isSuccess());
        Assert.assertEquals(0, result.getResultList().size());

        FileUtils.forceDelete(new File(fileName));
    }

    @Test
    public void testBatchValidationFailure() throws IOException, ExecutionException, InterruptedException, ZipException {

        String fileName = "src/test/resources/test_wrong";
        ZipFile file = new ZipFile("src/test/resources/test_wrong.zip");
        file.extractAll(fileName);

        File[] files = new File(fileName).listFiles();
        List<String> xmls = new ArrayList<>();
        for (File input : files) {
            FileInputStream fileInputStream = new FileInputStream(input);
            xmls.add(IOUtils.toString(fileInputStream));
            fileInputStream.close();
        }
        ValidationResultList result = validationExecutionService.batchValidation(EDM_INTERNAL, "EDM-INTERNAL.xsd", xmls);
        Assert.assertEquals(false, result.isSuccess());
        Assert.assertEquals(1, result.getResultList().size());


        FileUtils.forceDelete(new File(fileName));
    }

    @Test
    public void testBatchValidationFailureWrongSchema() throws IOException, ExecutionException, InterruptedException, ZipException {

        String fileName = "src/test/resources/test";
        ZipFile file = new ZipFile("src/test/resources/test.zip");
        file.extractAll(fileName);

        File[] files = new File(fileName).listFiles();
        List<String> xmls = new ArrayList<>();
        for (File input : files) {
            xmls.add(IOUtils.toString(new FileInputStream(input)));
        }
        ValidationResultList result = validationExecutionService.batchValidation(EDM_EXTERNAL, "EDM.xsd", xmls);
        Assert.assertEquals(false, result.isSuccess());
        Assert.assertEquals(1506, result.getResultList().size());


        FileUtils.forceDelete(new File(fileName));
    }

    @Test
    public void ValidationExecutionServiceTestWithDefaultPropertyFile() throws SchemaProviderException {
        ValidationExecutionService validationExecutionService = new ValidationExecutionService();
        ExecutorService es = Whitebox.getInternalState(validationExecutionService, "es");
        Assert.assertNotNull(es);
        ValidationServiceConfig config = Whitebox.getInternalState(validationExecutionService, "config");
        Assert.assertNotNull(config);
        Assert.assertEquals(config.getThreadCount(), 10);
        SchemaProvider schemaProvider = Whitebox.getInternalState(validationExecutionService, "schemaProvider");
        Properties properties = loadDefaultProperties("src/test/resources/validation.properties");
        Assert.assertNotNull(schemaProvider);
        Map<String, String> locations = Whitebox.getInternalState(schemaProvider, "predefinedSchemasLocations");
        Assert.assertNotNull(locations);
        Assert.assertEquals(locations.get("edm-internal"), properties.getProperty("edm-internal"));
        Assert.assertEquals(locations.get("edm-external"), properties.getProperty("edm-external"));
    }

    private Properties loadDefaultProperties(String propertyFile) {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(propertyFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    @Test
    public void ValidationExecutionServiceTestWithProvidedPropertyFile() throws SchemaProviderException {
        ValidationExecutionService validationExecutionService = new ValidationExecutionService("src/test/resources/custom-validation.properties");
        ExecutorService es = Whitebox.getInternalState(validationExecutionService, "es");
        Assert.assertNotNull(es);
        ValidationServiceConfig config = Whitebox.getInternalState(validationExecutionService, "config");
        Assert.assertNotNull(config);
        Assert.assertEquals(config.getThreadCount(), 10);
        SchemaProvider schemaProvider = Whitebox.getInternalState(validationExecutionService, "schemaProvider");
        Properties properties = loadDefaultProperties("src/test/resources/custom-validation.properties");
        Assert.assertNotNull(schemaProvider);
        Map<String, String> locations = Whitebox.getInternalState(schemaProvider, "predefinedSchemasLocations");
        Assert.assertNotNull(locations);
        Assert.assertEquals(locations.get("edm-internal"), properties.getProperty("edm-internal"));
        Assert.assertEquals(locations.get("edm-external"), properties.getProperty("edm-external"));
    }

    @Test
    public void ValidationExecutionServiceTestWithCustomConfiguration() throws SchemaProviderException {
        Map<String, String> predefinedSchemasLocations = new HashMap<>();
        predefinedSchemasLocations.put("edm-internal","url to edm-internal");
        predefinedSchemasLocations.put("edm-external","url to edm-external");
        ValidationExecutionService validationExecutionService = new ValidationExecutionService(new ValidationServiceConfig() {
            @Override
            public int getThreadCount() {
                return 12;
            }
        }, new ClasspathResourceResolver(), new SchemaProvider(predefinedSchemasLocations));
        ExecutorService es = Whitebox.getInternalState(validationExecutionService, "es");
        Assert.assertNotNull(es);
        ValidationServiceConfig config = Whitebox.getInternalState(validationExecutionService, "config");
        Assert.assertNotNull(config);
        Assert.assertEquals(config.getThreadCount(), 12);
        SchemaProvider schemaProvider = Whitebox.getInternalState(validationExecutionService, "schemaProvider");
        Assert.assertNotNull(schemaProvider);
        Map<String, String> locations = Whitebox.getInternalState(schemaProvider, "predefinedSchemasLocations");
        Assert.assertNotNull(locations);
        Assert.assertEquals(locations.get("edm-internal"), predefinedSchemasLocations.get("edm-internal"));
        Assert.assertEquals(locations.get("edm-external"), predefinedSchemasLocations.get("edm-external"));
    }

}
