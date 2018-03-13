import com.github.tomakehurst.wiremock.junit.WireMockRule;
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
import java.nio.charset.StandardCharsets;
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

        wireMockRule.stubFor(get(urlEqualTo("/external_test_schema.zip"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(2000)
                        .withBodyFile("test_schema.zip")));

        wireMockRule.stubFor(get(urlEqualTo("/edm_internal_schema.zip"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(2000)
                        .withBodyFile("test_schema.zip")));

        wireMockRule.stubFor(get(urlEqualTo("/edm_external_schema.zip"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(2000)
                        .withBodyFile("test_schema.zip")));

    }

    @Test
    public void testSingleValidationSuccessForPredefinedSchema() throws Exception {
        String fileToValidate = IOUtils.toString(new FileInputStream("src/test/resources/Item_35834473_test.xml"), StandardCharsets.UTF_8);
        ValidationResult result = validationExecutionService.singleValidation(EDM_INTERNAL, null, null, fileToValidate);
        Assert.assertTrue(result.isSuccess());
        Assert.assertNull(result.getRecordId());
        Assert.assertNull(result.getMessage());
    }

    @Test
    public void testSingleValidationSuccessForCustomSchema() throws Exception {
        String fileToValidate = IOUtils.toString(new FileInputStream("src/test/resources/Item_35834473_test.xml"), StandardCharsets.UTF_8);
        ValidationResult result = validationExecutionService.singleValidation("http://localhost:9999/external_test_schema.zip", "EDM-INTERNAL.xsd", "schematron/schematron-internal.xsl", fileToValidate);
        Assert.assertTrue(result.isSuccess());
        Assert.assertNull(result.getRecordId());
        Assert.assertNull(result.getMessage());
    }

    @Test
    public void validationShouldFailForCustomSchemaAndNotProvidedRootLocation() throws IOException {
        String fileToValidate = IOUtils.toString(new FileInputStream("src/test/resources/Item_35834473_test.xml"), StandardCharsets.UTF_8);
        ValidationResult result = validationExecutionService.singleValidation("http://localhost:9999/test_schema.zip", null, null, fileToValidate);
        Assert.assertFalse(result.isSuccess());
        Assert.assertEquals("Missing record identifier for EDM record", result.getRecordId());
        Assert.assertEquals("Missing root file location for custom schema", result.getMessage());
        Assert.assertNull(result.getNodeId());
    }

    @Test
    public void testSingleValidationFailure() throws Exception {

        String fileToValidate = IOUtils.toString(new FileInputStream("src/test/resources/Item_35834473_wrong.xml"), StandardCharsets.UTF_8);
        ValidationResult result = validationExecutionService.singleValidation(EDM_INTERNAL, "EDM-INTERNAL.xsd", "schematron/schematron-internal.xsl", fileToValidate);
        Assert.assertFalse(result.isSuccess());
        Assert.assertEquals("Missing record identifier for EDM record", result.getRecordId());
        Assert.assertNull(result.getNodeId());
        Assert.assertEquals("cvc-complex-type.2.4.a: Invalid content was found starting with element 'edm:WebResource'. One of '{\"http://www.europeana.eu/schemas/edm/\":ProvidedCHO}' is expected.", result.getMessage());
    }

    @Test
    public void shouldFailOnSchematronValidation() throws Exception {

        String fileToValidate = IOUtils.toString(new FileInputStream("src/test/resources/Item_schematron_invalid.xml"), StandardCharsets.UTF_8);
        ValidationResult result = validationExecutionService.singleValidation(EDM_EXTERNAL, "EDM-EXTERNAL.xsd", "schematron/schematron.xsl", fileToValidate);
        Assert.assertFalse(result.isSuccess());
        Assert.assertEquals("URN:RS:NAE:5485bed1-1b22-42c9-8ad7-3c5978ebfa9acho", result.getRecordId());
        Assert.assertEquals("http://digitalna.nb.rs/wb/NBS/Knjige/sabrana_dela_vuka_karadzica/II_146423_24", result.getNodeId());
        Assert.assertEquals("Schematron error: An ore:Aggregation must have at least one instance of edm:dataProvider", result.getMessage());
    }

    @Test
    public void shouldFailOnSchematronValidationWithNoNodeId() throws Exception {

        String fileToValidate = IOUtils.toString(new FileInputStream("src/test/resources/Item_schematron_invalid2.xml"), StandardCharsets.UTF_8);
        ValidationResult result = validationExecutionService.singleValidation(EDM_EXTERNAL, "EDM-EXTERNAL.xsd", "schematron/schematron.xsl", fileToValidate);
        Assert.assertFalse(result.isSuccess());
        Assert.assertEquals("#UEDIN:214", result.getRecordId());
        Assert.assertEquals("Missing node identifier", result.getNodeId());
        Assert.assertEquals("Schematron error: Empty rdf:resource attribute is not allowed for edm:hasView element.", result.getMessage());
    }

    @Test
    public void testSingleValidationFailureWrongSchema() throws Exception {

        String fileToValidate = IOUtils.toString(new FileInputStream("src/test/resources/Item_35834473.xml"), StandardCharsets.UTF_8);
        ValidationResult result = validationExecutionService.singleValidation(EDM_EXTERNAL, "EDM-INTERNAL.xsd", "schematron/schematron.xsl", fileToValidate);
        Assert.assertFalse(result.isSuccess());
        Assert.assertNotNull(result.getRecordId());
        Assert.assertNotNull(result.getMessage());
    }

    @Test
    public void testBatchValidationSuccess() throws IOException, ExecutionException, InterruptedException, ZipException {
        String fileName = "src/test/resources/test";
        ZipFile file = new ZipFile("src/test/resources/test_batch.zip");
        file.extractAll(fileName);

        File[] files = new File(fileName).listFiles();
        List<String> xmls = new ArrayList<>();
        for (File input : files) {
            xmls.add(IOUtils.toString(new FileInputStream(input), StandardCharsets.UTF_8));
        }
        ValidationResultList result = validationExecutionService.batchValidation(EDM_INTERNAL, null, null, xmls);
        Assert.assertTrue(result.isSuccess());
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
            xmls.add(IOUtils.toString(fileInputStream, StandardCharsets.UTF_8));
            fileInputStream.close();
        }
        ValidationResultList result = validationExecutionService.batchValidation(EDM_INTERNAL, "EDM-INTERNAL.xsd", null, xmls);
        Assert.assertFalse(result.isSuccess());
        Assert.assertEquals(1, result.getResultList().size());


        FileUtils.forceDelete(new File(fileName));
    }

    @Test
    public void testBatchValidationFailureWrongSchema() throws IOException, ExecutionException, InterruptedException, ZipException {

        String fileName = "src/test/resources/test";
        ZipFile file = new ZipFile("src/test/resources/test_batch.zip");
        file.extractAll(fileName);

        File[] files = new File(fileName).listFiles();
        List<String> xmls = new ArrayList<>();
        for (File input : files) {
            xmls.add(IOUtils.toString(new FileInputStream(input),StandardCharsets.UTF_8));
        }
        ValidationResultList result = validationExecutionService.batchValidation(EDM_EXTERNAL, "EDM.xsd", "schematron/schematron.xsl", xmls);
        Assert.assertFalse(result.isSuccess());
        Assert.assertEquals(1506, result.getResultList().size());


        FileUtils.forceDelete(new File(fileName));
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
    public void ValidationExecutionServiceTestWithProvidedProperties() throws SchemaProviderException {
        Properties property = loadDefaultProperties("src/test/resources/custom-validation.properties");
        ValidationExecutionService validationExecutionService = new ValidationExecutionService(property);
        ExecutorService es = Whitebox.getInternalState(validationExecutionService, "es");
        Assert.assertNotNull(es);
        SchemaProvider schemaProvider = Whitebox.getInternalState(validationExecutionService, "schemaProvider");
        Properties properties = loadDefaultProperties("src/test/resources/custom-validation.properties");
        Assert.assertNotNull(schemaProvider);
        PredefinedSchemas locations = Whitebox.getInternalState(schemaProvider, "predefinedSchemasLocations");
        Assert.assertNotNull(locations);
        Assert.assertEquals(properties.getProperty("predefinedSchemas.EDM-INTERNAL.url"), locations.get("EDM-INTERNAL").getLocation());
        Assert.assertEquals("EDM-INTERNAL", locations.get("EDM-INTERNAL").getKey());
        Assert.assertEquals("EDM-INTERNAL.xsd", locations.get("EDM-INTERNAL").getRootFileLocation());

        Assert.assertEquals(properties.getProperty("predefinedSchemas.EDM-EXTERNAL.url"), locations.get("EDM-EXTERNAL").getLocation());
        Assert.assertEquals("EDM-EXTERNAL", locations.get("EDM-EXTERNAL").getKey());
        Assert.assertEquals("EDM.xsd", locations.get("EDM-EXTERNAL").getRootFileLocation());
    }

    @Test
    public void ValidationExecutionServiceTestWithCustomConfiguration() throws SchemaProviderException {
        PredefinedSchemas predefinedSchemas = new PredefinedSchemas();
        predefinedSchemas.add("name", "location", "root", "schematronFile");
        predefinedSchemas.add("name1", "location1", "root1", "schematronFile1");
        ValidationExecutionService validationExecutionService = new ValidationExecutionService(new ValidationServiceConfig() {
            @Override
            public int getThreadCount() {
                return 12;
            }
        }, new ClasspathResourceResolver(), new SchemaProvider(predefinedSchemas));
        ExecutorService es = Whitebox.getInternalState(validationExecutionService, "es");
        Assert.assertNotNull(es);
        SchemaProvider schemaProvider = Whitebox.getInternalState(validationExecutionService, "schemaProvider");
        Assert.assertNotNull(schemaProvider);
        PredefinedSchemas locations = Whitebox.getInternalState(schemaProvider, "predefinedSchemasLocations");
        Assert.assertNotNull(locations);
        Assert.assertEquals(locations.get("name").getKey(), predefinedSchemas.get("name").getKey());
        Assert.assertEquals(locations.get("name").getLocation(), predefinedSchemas.get("name").getLocation());
        Assert.assertEquals(locations.get("name").getRootFileLocation(), predefinedSchemas.get("name").getRootFileLocation());
        //
        Assert.assertEquals(locations.get("name1").getKey(), predefinedSchemas.get("name1").getKey());
        Assert.assertEquals(locations.get("name1").getLocation(), predefinedSchemas.get("name1").getLocation());
        Assert.assertEquals(locations.get("name1").getRootFileLocation(), predefinedSchemas.get("name1").getRootFileLocation());
    }

}
