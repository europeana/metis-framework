import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tomakehurst.wiremock.WireMockServer;
import eu.europeana.metis.network.NetworkUtil;
import eu.europeana.validation.model.Schema;
import eu.europeana.validation.service.PredefinedSchemas;
import eu.europeana.validation.service.SchemaProvider;
import eu.europeana.validation.service.SchemaProviderException;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Created by pwozniak on 12/21/17
 */
class TestSchemaProvider {

  private static int portForWireMock = 9999;

  static {
    try {
      portForWireMock = new NetworkUtil().getAvailableLocalPort();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static WireMockServer wireMockServer;
  private static final PredefinedSchemas PREDEFINED_SCHEMAS_LOCATIONS = new PredefinedSchemas();
  private static final SchemaProvider schemaProvider;

  static {
    PREDEFINED_SCHEMAS_LOCATIONS
        .add("EDM-INTERNAL", "http://localhost:" + portForWireMock + "/internal_test_schema.zip",
            "EDM-INTERNAL.xsd",
            "schematron/schematron-internal.xsl");
    PREDEFINED_SCHEMAS_LOCATIONS
        .add("EDM-EXTERNAL", "http://localhost:" + portForWireMock + "/external_test_schema.zip",
            "EDM.xsd",
            "schematron/schematron.xsl");
    schemaProvider = new SchemaProvider(PREDEFINED_SCHEMAS_LOCATIONS);
  }

  @BeforeAll
  static void setUp() {
    wireMockServer = new WireMockServer(wireMockConfig().port(portForWireMock));
    wireMockServer.start();
  }

  @AfterAll
  static void destroy() throws IOException {
    wireMockServer.stop();
    schemaProvider.cleanUp();
  }

  @Test
  void shouldCreateCorrectSchemaForEdmInternal() throws SchemaProviderException {

    wireMockServer.stubFor(get(urlEqualTo("/internal_test_schema.zip"))
        .willReturn(aResponse()
            .withStatus(200)
            .withBodyFile("test_schema.zip")));
    Schema s = schemaProvider.getSchema("EDM-INTERNAL");
    //then
    assertEquals("localhost_internal_test_schema", s.getName());
    assertEquals(
        entryFileLocation( "localhost_internal_test_schema", "EDM-INTERNAL.xsd"),
        s.getPath());
    assertNotNull(s.getSchematronPath());
    assertZipFileExistence(s);
  }

  @Test
  void shouldCreateCorrectSchemaForEdmExternal() throws SchemaProviderException {
    wireMockServer.stubFor(get(urlEqualTo("/external_test_schema.zip"))
        .willReturn(aResponse()
            .withStatus(200)
            .withBodyFile("test_schema.zip")));
    Schema schema = schemaProvider.getSchema("EDM-EXTERNAL");
    assertEquals("localhost_external_test_schema", schema.getName());
    assertEquals(entryFileLocation("localhost_external_test_schema", "EDM.xsd"),
        schema.getPath());
    assertNotNull(schema.getSchematronPath());
    assertZipFileExistence(schema);
  }

  @Test
  void shouldCreateCorrectSchemaForCustomSchema() throws SchemaProviderException {
    wireMockServer.stubFor(get(urlEqualTo("/custom_schema.zip"))
        .willReturn(aResponse()
            .withStatus(200)
            .withBodyFile("test_schema.zip")));
    Schema schema = schemaProvider
        .getSchema("http://localhost:" + portForWireMock + "/custom_schema.zip", "DC.xsd", null);
    //then
    assertEquals("localhost_custom_schema", schema.getName());
    assertEquals(entryFileLocation("localhost_custom_schema", "DC.xsd"),
        schema.getPath());
    assertNull(schema.getSchematronPath());
    assertZipFileExistence(schema);
  }

  @Test
  void shouldThrowExceptionForNonExistingRootFile() {
    wireMockServer.stubFor(get(urlEqualTo("/custom_schema.zip"))
        .willReturn(aResponse()
            .withStatus(200)
            .withBodyFile("test_schema.zip")));
    assertThrows(SchemaProviderException.class, () -> schemaProvider
        .getSchema("http://localhost:" + portForWireMock + "/custom_schema.zip", "nonExisting.xsd",
            null));
  }

  @Test
  void exceptionShouldBeThrownForMalformedUrl() {
    assertThrows(SchemaProviderException.class,
        () -> schemaProvider.getSchema("malformedUrl", "EDM.xsd", null));
  }

  @Test
  void zipFileShouldBeCreatedInCorrectLocationForEdmInternal()
      throws SchemaProviderException {
    wireMockServer.resetAll();
    wireMockServer.stubFor(get(urlEqualTo("/internal_test_schema.zip"))
        .willReturn(aResponse()
            .withStatus(200)
            .withBodyFile("test_schema.zip")));
    schemaProvider.getSchema("EDM-INTERNAL");
    File zipFile = new File(schemaProvider.getSchemasDirectory() + "localhost_internal_test_schema",
        "zip.zip");
    assertTrue(zipFile.exists());
  }

  @Test
  void zipFileShouldBeCreatedInCorrectLocationForEdmExternal()
      throws SchemaProviderException {
    wireMockServer.resetAll();
    wireMockServer.stubFor(get(urlEqualTo("/external_test_schema.zip"))
        .willReturn(aResponse()
            .withStatus(200)
            .withBodyFile("test_schema.zip")));
    schemaProvider.getSchema("EDM-EXTERNAL");
    File zipFile = new File(schemaProvider.getSchemasDirectory() + "localhost_external_test_schema",
        "zip.zip");
    assertTrue(zipFile.exists());
  }

  @Test
  void zipFileShouldBeCreatedInCorrectLocationForCustomZip() throws SchemaProviderException {
    wireMockServer.resetAll();
    wireMockServer.stubFor(get(urlEqualTo("/userDefinedSchema.zip"))
        .willReturn(aResponse()
            .withStatus(200)
            .withBodyFile("test_schema.zip")));
    schemaProvider
        .getSchema("http://localhost:" + portForWireMock + "/userDefinedSchema.zip", "EDM.xsd",
            "schematron/schematron.xsl");
    File zipFile = new File(schemaProvider.getSchemasDirectory() + "localhost_userDefinedSchema",
        "zip.zip");
    assertTrue(zipFile.exists());
  }

  @Test
  void zipFileShouldBeDownloadedWhenNotAvailable()
      throws IOException, SchemaProviderException {
    wireMockServer.stubFor(get(urlEqualTo("/internal_test_schema.zip"))
        .willReturn(aResponse()
            .withStatus(200)
            .withBodyFile("test_schema.zip")));
    clearSchemasDir();
    Schema s = schemaProvider.getSchema("EDM-INTERNAL");
    assertEquals("localhost_internal_test_schema", s.getName());
    assertEquals(entryFileLocation("localhost_internal_test_schema", "EDM-INTERNAL.xsd"), s.getPath());
    assertNotNull(s.getSchematronPath());
    assertZipFileExistence(s);
  }

  private void clearSchemasDir() throws IOException {
    String TMP_DIR = System.getProperty("java.io.tmpdir");
    File schemasDirectory = new File(TMP_DIR, "schemas");
    FileUtils.deleteDirectory(schemasDirectory);
    schemasDirectory.mkdirs();
  }

  private void assertZipFileExistence(Schema s) {
    File tempDirectory = new File(schemaProvider.getSchemasDirectory());
    File zipFile = new File(tempDirectory, s.getName().toLowerCase() + "/zip.zip");
    assertTrue(zipFile.exists());
  }

  private String entryFileLocation(String schemaName, String fileLocation) {
    return directoryLocation(schemaName) + fileLocation;
  }

  private String directoryLocation(String schemaName) {
    return schemaProvider.getSchemasDirectory() + schemaName + File.separator;
  }
}