import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tomakehurst.wiremock.WireMockServer;
import eu.europeana.validation.model.ValidationResult;
import eu.europeana.validation.model.ValidationResultList;
import eu.europeana.validation.service.ClasspathResourceResolver;
import eu.europeana.validation.service.PredefinedSchemas;
import eu.europeana.validation.service.SchemaProvider;
import eu.europeana.validation.service.ValidationExecutionService;
import eu.europeana.validation.service.ValidationServiceConfig;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Created by gmamakis on 18-12-15.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestApplication.class, loader = AnnotationConfigContextLoader.class)
@WebAppConfiguration
class TestValidationExecution {

  private static int portForWireMock = TestApplication.portForWireMock;

  private static final String EDM_EXTERNAL = "EDM-EXTERNAL";

  private static final String EDM_INTERNAL = "EDM-INTERNAL";

  private static WireMockServer wireMockServer;

  @Autowired
  ValidationExecutionService validationExecutionService;

  @BeforeAll
  static void setUp() {
    System.out.println("PSS" + portForWireMock);
    wireMockServer = new WireMockServer(wireMockConfig().port(portForWireMock));
    wireMockServer.start();
  }

  @AfterAll
  static void destroy() {
    wireMockServer.stop();
  }

  @BeforeEach
  void prepare() {
    wireMockServer.resetAll();
    wireMockServer.stubFor(get(urlEqualTo("/test_schema.zip"))
        .willReturn(aResponse()
            .withStatus(200)
            .withFixedDelay(2000)
            .withBodyFile("test_schema.zip")));

    wireMockServer.stubFor(get(urlEqualTo("/external_test_schema.zip"))
        .willReturn(aResponse()
            .withStatus(200)
            .withFixedDelay(2000)
            .withBodyFile("test_schema.zip")));

    wireMockServer.stubFor(get(urlEqualTo("/edm_internal_schema.zip"))
        .willReturn(aResponse()
            .withStatus(200)
            .withFixedDelay(2000)
            .withBodyFile("test_schema.zip")));

    wireMockServer.stubFor(get(urlEqualTo("/edm_external_schema.zip"))
        .willReturn(aResponse()
            .withStatus(200)
            .withFixedDelay(2000)
            .withBodyFile("test_schema.zip")));

  }

  @Test
  void testSingleValidationSuccessForPredefinedSchema() throws Exception {
    String fileToValidate = IOUtils
        .toString(new FileInputStream("src/test/resources/Item_35834473_test.xml"),
            StandardCharsets.UTF_8);
    ValidationResult result = validationExecutionService
        .singleValidation(EDM_INTERNAL, null, null, fileToValidate);
    assertTrue(result.isSuccess());
    assertNull(result.getRecordId());
    assertNull(result.getMessage());
  }

  @Test
  void testSingleValidationSuccessForCustomSchema() throws Exception {
    String fileToValidate = IOUtils
        .toString(new FileInputStream("src/test/resources/Item_35834473_test.xml"),
            StandardCharsets.UTF_8);
    ValidationResult result = validationExecutionService
        .singleValidation("http://localhost:" + portForWireMock + "/external_test_schema.zip",
            "EDM-INTERNAL.xsd",
            "schematron/schematron-internal.xsl", fileToValidate);
    assertTrue(result.isSuccess());
    assertNull(result.getRecordId());
    assertNull(result.getMessage());
  }

  @Test
  void validationShouldFailForCustomSchemaAndNotProvidedRootLocation() throws IOException {
    String fileToValidate = IOUtils
        .toString(new FileInputStream("src/test/resources/Item_35834473_test.xml"),
            StandardCharsets.UTF_8);
    ValidationResult result = validationExecutionService
        .singleValidation("http://localhost:" + portForWireMock + "/test_schema.zip", null, null,
            fileToValidate);
    assertFalse(result.isSuccess());
    assertEquals("Missing record identifier for EDM record", result.getRecordId());
    assertEquals("Missing root file location for custom schema", result.getMessage());
    assertNull(result.getNodeId());
  }

  @Test
  void testSingleValidationFailure() throws Exception {

    String fileToValidate = IOUtils
        .toString(new FileInputStream("src/test/resources/Item_35834473_wrong.xml"),
            StandardCharsets.UTF_8);
    ValidationResult result = validationExecutionService
        .singleValidation(EDM_INTERNAL, "EDM-INTERNAL.xsd", "schematron/schematron-internal.xsl",
            fileToValidate);
    assertFalse(result.isSuccess());
    assertEquals("Missing record identifier for EDM record", result.getRecordId());
    assertNull(result.getNodeId());
    assertEquals(
        "cvc-complex-type.2.4.a: Invalid content was found starting with element 'edm:WebResource'. One of '{\"http://www.europeana.eu/schemas/edm/\":ProvidedCHO}' is expected.",
        result.getMessage());
  }

  @Test
  void shouldFailOnSchematronValidation() throws Exception {

    String fileToValidate = IOUtils
        .toString(new FileInputStream("src/test/resources/Item_schematron_invalid.xml"),
            StandardCharsets.UTF_8);
    ValidationResult result = validationExecutionService
        .singleValidation(EDM_EXTERNAL, "EDM-EXTERNAL.xsd", "schematron/schematron.xsl",
            fileToValidate);
    assertFalse(result.isSuccess());
    assertEquals("URN:RS:NAE:5485bed1-1b22-42c9-8ad7-3c5978ebfa9acho", result.getRecordId());
    assertEquals("http://digitalna.nb.rs/wb/NBS/Knjige/sabrana_dela_vuka_karadzica/II_146423_24",
        result.getNodeId());
    assertEquals(
        "Schematron error: An ore:Aggregation must have at least one instance of edm:dataProvider",
        result.getMessage());
  }

  @Test
  void shouldFailOnSchematronValidationWithNoNodeId() throws Exception {

    String fileToValidate = IOUtils
        .toString(new FileInputStream("src/test/resources/Item_schematron_invalid2.xml"),
            StandardCharsets.UTF_8);
    ValidationResult result = validationExecutionService
        .singleValidation(EDM_EXTERNAL, "EDM-EXTERNAL.xsd", "schematron/schematron.xsl",
            fileToValidate);
    assertFalse(result.isSuccess());
    assertEquals("#UEDIN:214", result.getRecordId());
    assertEquals("Missing node identifier", result.getNodeId());
    assertEquals(
        "Schematron error: Empty rdf:resource attribute is not allowed for edm:hasView element.",
        result.getMessage());
  }

  @Test
  void testSingleValidationFailureWrongSchema() throws Exception {

    String fileToValidate = IOUtils
        .toString(new FileInputStream("src/test/resources/Item_35834473.xml"),
            StandardCharsets.UTF_8);
    ValidationResult result = validationExecutionService
        .singleValidation(EDM_EXTERNAL, "EDM-INTERNAL.xsd", "schematron/schematron.xsl",
            fileToValidate);
    assertFalse(result.isSuccess());
    assertNotNull(result.getRecordId());
    assertNotNull(result.getMessage());
  }

  @Test
  void testBatchValidationSuccess()
      throws IOException, ExecutionException, InterruptedException, ZipException {
    String fileName = "src/test/resources/test";
    ZipFile file = new ZipFile("src/test/resources/test_batch.zip");
    file.extractAll(fileName);

    File[] files = new File(fileName).listFiles();
    List<String> xmls = new ArrayList<>();
    for (File input : files) {
      xmls.add(IOUtils.toString(new FileInputStream(input), StandardCharsets.UTF_8));
    }
    ValidationResultList result = validationExecutionService
        .batchValidation(EDM_INTERNAL, null, null, xmls);
    assertTrue(result.isSuccess());
    assertEquals(0, result.getResultList().size());

    FileUtils.forceDelete(new File(fileName));
  }

  @Test
  void testBatchValidationFailure()
      throws IOException, ExecutionException, InterruptedException, ZipException {

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
    ValidationResultList result = validationExecutionService
        .batchValidation(EDM_INTERNAL, "EDM-INTERNAL.xsd", null, xmls);
    assertFalse(result.isSuccess());
    assertEquals(1, result.getResultList().size());

    FileUtils.forceDelete(new File(fileName));
  }

  @Test
  void testBatchValidationFailureWrongSchema()
      throws IOException, ExecutionException, InterruptedException, ZipException {

    String fileName = "src/test/resources/test";
    ZipFile file = new ZipFile("src/test/resources/test_batch.zip");
    file.extractAll(fileName);

    File[] files = new File(fileName).listFiles();
    List<String> xmls = new ArrayList<>();
    for (File input : files) {
      xmls.add(IOUtils.toString(new FileInputStream(input), StandardCharsets.UTF_8));
    }
    ValidationResultList result = validationExecutionService
        .batchValidation(EDM_EXTERNAL, "EDM.xsd", "schematron/schematron.xsl", xmls);
    assertFalse(result.isSuccess());
    assertEquals(1506, result.getResultList().size());

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
  void ValidationExecutionServiceTestWithProvidedProperties() {
    Properties property = loadDefaultProperties("src/test/resources/custom-validation.properties");
    ValidationExecutionService validationExecutionService = new ValidationExecutionService(
        property);
    ExecutorService es = Whitebox.getInternalState(validationExecutionService, "es");
    assertNotNull(es);
    SchemaProvider schemaProvider = Whitebox
        .getInternalState(validationExecutionService, "schemaProvider");
    Properties properties = loadDefaultProperties(
        "src/test/resources/custom-validation.properties");
    assertNotNull(schemaProvider);
    PredefinedSchemas locations = Whitebox
        .getInternalState(schemaProvider, "predefinedSchemasLocations");
    assertNotNull(locations);
    assertEquals(properties.getProperty("predefinedSchemas.EDM-INTERNAL.url"),
        locations.get("EDM-INTERNAL").getLocation());
    assertEquals("EDM-INTERNAL", locations.get("EDM-INTERNAL").getKey());
    assertEquals("EDM-INTERNAL.xsd", locations.get("EDM-INTERNAL").getRootFileLocation());

    assertEquals(properties.getProperty("predefinedSchemas.EDM-EXTERNAL.url"),
        locations.get("EDM-EXTERNAL").getLocation());
    assertEquals("EDM-EXTERNAL", locations.get("EDM-EXTERNAL").getKey());
    assertEquals("EDM.xsd", locations.get("EDM-EXTERNAL").getRootFileLocation());
  }

  @Test
  void ValidationExecutionServiceTestWithCustomConfiguration() {
    PredefinedSchemas predefinedSchemas = new PredefinedSchemas();
    predefinedSchemas.add("name", "location", "root", "schematronFile");
    predefinedSchemas.add("name1", "location1", "root1", "schematronFile1");
    ValidationExecutionService validationExecutionService = new ValidationExecutionService(
        new ValidationServiceConfig() {
          @Override
          public int getThreadCount() {
            return 12;
          }
        }, new ClasspathResourceResolver(), new SchemaProvider(predefinedSchemas));
    ExecutorService es = Whitebox.getInternalState(validationExecutionService, "es");
    assertNotNull(es);
    SchemaProvider schemaProvider = Whitebox
        .getInternalState(validationExecutionService, "schemaProvider");
    assertNotNull(schemaProvider);
    PredefinedSchemas locations = Whitebox
        .getInternalState(schemaProvider, "predefinedSchemasLocations");
    assertNotNull(locations);
    assertEquals(locations.get("name").getKey(), predefinedSchemas.get("name").getKey());
    assertEquals(locations.get("name").getLocation(), predefinedSchemas.get("name").getLocation());
    assertEquals(locations.get("name").getRootFileLocation(),
        predefinedSchemas.get("name").getRootFileLocation());
    //
    assertEquals(locations.get("name1").getKey(), predefinedSchemas.get("name1").getKey());
    assertEquals(locations.get("name1").getLocation(),
        predefinedSchemas.get("name1").getLocation());
    assertEquals(locations.get("name1").getRootFileLocation(),
        predefinedSchemas.get("name1").getRootFileLocation());
  }

}
