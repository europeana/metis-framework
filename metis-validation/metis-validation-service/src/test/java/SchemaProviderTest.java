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
import eu.europeana.validation.model.Schema;
import eu.europeana.validation.service.PredefinedSchemas;
import eu.europeana.validation.service.SchemaProvider;
import eu.europeana.validation.service.SchemaProviderException;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Created by pwozniak on 12/21/17
 */
class SchemaProviderTest {

  static WireMockServer wireMockServer;
  private static final PredefinedSchemas PREDEFINED_SCHEMAS_LOCATIONS = new PredefinedSchemas();

  static {
    PREDEFINED_SCHEMAS_LOCATIONS
        .add("EDM-INTERNAL", "http://localhost:9999/internal_test_schema.zip", "EDM-INTERNAL.xsd",
            "schematron/schematron-internal.xsl");
    PREDEFINED_SCHEMAS_LOCATIONS
        .add("EDM-EXTERNAL", "http://localhost:9999/external_test_schema.zip", "EDM.xsd",
            "schematron/schematron.xsl");
  }

  @BeforeAll
  static void setUp() {
    wireMockServer = new WireMockServer(wireMockConfig().port(9999));
    wireMockServer.start();
  }

  @AfterAll
  static void destroy() {
    wireMockServer.stop();
  }

  @Test
  void shouldCreateCorrectSchemaForEdmInternal() throws SchemaProviderException {

    wireMockServer.stubFor(get(urlEqualTo("/internal_test_schema.zip"))
        .willReturn(aResponse()
            .withStatus(200)
            .withBodyFile("test_schema.zip")));
    //given
    SchemaProvider provider = new SchemaProvider(PREDEFINED_SCHEMAS_LOCATIONS);
    //when
    Schema s = provider.getSchema("EDM-INTERNAL");
    //then
    assertEquals("localhost_internal_test_schema", s.getName());
    assertEquals(entryFileLocation(provider, "localhost_internal_test_schema", "EDM-INTERNAL.xsd"),
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
    //given
    SchemaProvider provider = new SchemaProvider(PREDEFINED_SCHEMAS_LOCATIONS);
    //when
    Schema schema = provider.getSchema("EDM-EXTERNAL");
    //then
    assertEquals("localhost_external_test_schema", schema.getName());
    assertEquals(entryFileLocation(provider, "localhost_external_test_schema", "EDM.xsd"),
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
    //given
    SchemaProvider provider = new SchemaProvider(PREDEFINED_SCHEMAS_LOCATIONS);
    //when
    Schema schema = provider.getSchema("http://localhost:9999/custom_schema.zip", "DC.xsd", null);
    //then
    assertEquals("localhost_custom_schema", schema.getName());
    assertEquals(entryFileLocation(provider, "localhost_custom_schema", "DC.xsd"),
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
    //given
    SchemaProvider provider = new SchemaProvider(PREDEFINED_SCHEMAS_LOCATIONS);
    //when
    AtomicReference<Schema> schema = new AtomicReference<>();
    assertThrows(SchemaProviderException.class, () -> schema.set(
        provider.getSchema("http://localhost:9999/custom_schema.zip", "nonExisting.xsd", null)));
    //then
    assertEquals("localhost_custom_schema", schema.get().getName());
    assertEquals(entryFileLocation(provider, "localhost_custom_schema", "nonExisting.xsd"),
        schema.get().getPath());
    assertNull(schema.get().getSchematronPath());
    assertZipFileExistence(schema.get());
  }

  @Test
  void exceptionShouldBeThrownForMalformedUrl() {
    //given
    SchemaProvider provider = new SchemaProvider(PREDEFINED_SCHEMAS_LOCATIONS);
    //when
    AtomicReference<Schema> schema = new AtomicReference<>();
    assertThrows(SchemaProviderException.class,
        () -> schema.set(provider.getSchema("malformedUrl", "EDM.xsd", null)));
    //then
    assertEquals("EDM-EXTERNAL", schema.get().getName());
    assertEquals(entryFileLocation(provider, "edm-external", "EDM.xsd"), schema.get().getPath());
    assertNull(schema.get().getSchematronPath());
    assertZipFileExistence(schema.get());
  }

  @Test
  void zipFileShouldBeCreatedInCorrectLocationForEdmInternal()
      throws SchemaProviderException {
    wireMockServer.resetAll();
    wireMockServer.stubFor(get(urlEqualTo("/internal_test_schema.zip"))
        .willReturn(aResponse()
            .withStatus(200)
            .withBodyFile("test_schema.zip")));
    //given
    SchemaProvider provider = new SchemaProvider(PREDEFINED_SCHEMAS_LOCATIONS);
    //when
    provider.getSchema("EDM-INTERNAL");
    //then
    File directory = new File(SchemaProvider.TMP_DIR,
        "schemas" + File.separator + "localhost_internal_test_schema");
    File zipFile = new File(directory, "zip.zip");
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
    //given
    SchemaProvider provider = new SchemaProvider(PREDEFINED_SCHEMAS_LOCATIONS);
    //when
    provider.getSchema("EDM-EXTERNAL");
    //then
    File directory = new File(SchemaProvider.TMP_DIR,
        "schemas" + File.separator + "localhost_external_test_schema");
    File zipFile = new File(directory, "zip.zip");
    assertTrue(zipFile.exists());
  }

  @Test
  void zipFileShouldBeCreatedInCorrectLocationForCustomZip() throws SchemaProviderException {
    wireMockServer.resetAll();
    wireMockServer.stubFor(get(urlEqualTo("/userDefinedSchema.zip"))
        .willReturn(aResponse()
            .withStatus(200)
            .withBodyFile("test_schema.zip")));
    //given
    SchemaProvider provider = new SchemaProvider(PREDEFINED_SCHEMAS_LOCATIONS);
    //when
    provider.getSchema("http://localhost:9999/userDefinedSchema.zip", "EDM.xsd",
        "schematron/schematron.xsl");
    //then
    File directory = new File(SchemaProvider.TMP_DIR,
        "schemas" + File.separator + "localhost_userDefinedSchema");
    File zipFile = new File(directory, "zip.zip");
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
    //given
    SchemaProvider provider = new SchemaProvider(PREDEFINED_SCHEMAS_LOCATIONS);
    //when
    Schema s = provider.getSchema("EDM-INTERNAL");
    //then
    assertEquals("localhost_internal_test_schema", s.getName());
    assertEquals(entryFileLocation(provider, "localhost_internal_test_schema", "EDM-INTERNAL.xsd"),
        s.getPath());
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
    File tempDirectory = new File(SchemaProvider.TMP_DIR, "schemas");
    File zipFile = new File(tempDirectory, s.getName().toLowerCase() + "/zip.zip");
    assertTrue(zipFile.exists());
  }

  private String entryFileLocation(SchemaProvider schemaProvider, String schemaName,
      String fileLocation) {
    return directoryLocation(schemaProvider, schemaName) + fileLocation;
  }

  private String directoryLocation(SchemaProvider schemaProvider, String schemaName) {
    return schemaProvider.getSchemasDirectory() + schemaName + File.separator;
  }
}