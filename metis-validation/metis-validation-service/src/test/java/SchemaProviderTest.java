import com.github.tomakehurst.wiremock.junit.WireMockRule;
import eu.europeana.validation.model.Schema;
import eu.europeana.validation.service.PredefinedSchemas;
import eu.europeana.validation.service.SchemaProvider;
import eu.europeana.validation.service.SchemaProviderException;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

/**
 * Created by pwozniak on 12/21/17
 */
public class SchemaProviderTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(9999));


    private static final PredefinedSchemas PREDEFINED_SCHEMAS_LOCATIONS = new PredefinedSchemas();

    static {
        PREDEFINED_SCHEMAS_LOCATIONS.add("EDM-INTERNAL", "http://localhost:9999/internal_test_schema.zip","EDM-INTERNAL.xsd","schematron/schematron-internal.xsl");
        PREDEFINED_SCHEMAS_LOCATIONS.add("EDM-EXTERNAL", "http://localhost:9999/external_test_schema.zip","EDM.xsd","schematron/schematron.xsl");
    }

    @Test
    public void shouldCreateCorrectSchemaForEdmInternal() throws SchemaProviderException {

        wireMockRule.stubFor(get(urlEqualTo("/internal_test_schema.zip"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBodyFile("test_schema.zip")));
        //given
        SchemaProvider provider = new SchemaProvider(PREDEFINED_SCHEMAS_LOCATIONS);
        //when
        Schema s = provider.getSchema("EDM-INTERNAL");
        //then
        Assert.assertEquals("localhost_internal_test_schema", s.getName());
        Assert.assertEquals(entryFileLocation(provider, "localhost_internal_test_schema","EDM-INTERNAL.xsd"), s.getPath());
        Assert.assertNotNull(s.getSchematronPath());
        assertZipFileExistence(s);
    }

    @Test
    public void shouldCreateCorrectSchemaForEdmExternal() throws SchemaProviderException {
        wireMockRule.stubFor(get(urlEqualTo("/external_test_schema.zip"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBodyFile("test_schema.zip")));
        //given
        SchemaProvider provider = new SchemaProvider(PREDEFINED_SCHEMAS_LOCATIONS);
        //when
        Schema schema = provider.getSchema("EDM-EXTERNAL");
        //then
        Assert.assertEquals("localhost_external_test_schema", schema.getName());
        Assert.assertEquals(entryFileLocation(provider, "localhost_external_test_schema","EDM.xsd"), schema.getPath());
        Assert.assertNotNull(schema.getSchematronPath());
        assertZipFileExistence(schema);
    }

    @Test
    public void shouldCreateCorrectSchemaForCustomSchema() throws SchemaProviderException {
        wireMockRule.stubFor(get(urlEqualTo("/custom_schema.zip"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBodyFile("test_schema.zip")));
        //given
        SchemaProvider provider = new SchemaProvider(PREDEFINED_SCHEMAS_LOCATIONS);
        //when
        Schema schema = provider.getSchema("http://localhost:9999/custom_schema.zip","DC.xsd", null);
        //then
        Assert.assertEquals("localhost_custom_schema", schema.getName());
        Assert.assertEquals(entryFileLocation(provider, "localhost_custom_schema", "DC.xsd"), schema.getPath());
        Assert.assertNull(schema.getSchematronPath());
        assertZipFileExistence(schema);
    }

    @Test(expected = SchemaProviderException.class)
    public void shouldThrowExceptionForNonExistingRootFile() throws SchemaProviderException {
        wireMockRule.stubFor(get(urlEqualTo("/custom_schema.zip"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBodyFile("test_schema.zip")));
        //given
        SchemaProvider provider = new SchemaProvider(PREDEFINED_SCHEMAS_LOCATIONS);
        //when
        Schema schema = provider.getSchema("http://localhost:9999/custom_schema.zip","nonExisting.xsd", null);
        //then
        Assert.assertEquals("localhost_custom_schema", schema.getName());
        Assert.assertEquals(entryFileLocation(provider, "localhost_custom_schema", "nonExisting.xsd"), schema.getPath());
        Assert.assertNull(schema.getSchematronPath());
        assertZipFileExistence(schema);
    }

    @Test(expected = SchemaProviderException.class)
    public void exceptionShouldBeThrownForMalformedUrl() throws SchemaProviderException {
        //given
        SchemaProvider provider = new SchemaProvider(PREDEFINED_SCHEMAS_LOCATIONS);
        //when
        Schema schema = provider.getSchema("malformedUrl","EDM.xsd", null);
        //then
        Assert.assertEquals("EDM-EXTERNAL", schema.getName());
        Assert.assertEquals(entryFileLocation(provider, "edm-external", "EDM.xsd"), schema.getPath());
        Assert.assertNull(schema.getSchematronPath());
        assertZipFileExistence(schema);
    }

    @Test
    public void zipFileShouldBeCreatedInCorrectLocationForEdmInternal() throws SchemaProviderException {
        wireMockRule.resetAll();
        wireMockRule.stubFor(get(urlEqualTo("/internal_test_schema.zip"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBodyFile("test_schema.zip")));
        //given
        SchemaProvider provider = new SchemaProvider(PREDEFINED_SCHEMAS_LOCATIONS);
        //when
        provider.getSchema("EDM-INTERNAL");
        //then
        File directory = new File(SchemaProvider.TMP_DIR, "schemas" + File.separator + "localhost_internal_test_schema");
        File zipFile = new File(directory, "zip.zip");
        Assert.assertTrue(zipFile.exists());
    }

    @Test
    public void zipFileShouldBeCreatedInCorrectLocationForEdmExternal() throws SchemaProviderException {
        wireMockRule.resetAll();
        wireMockRule.stubFor(get(urlEqualTo("/external_test_schema.zip"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBodyFile("test_schema.zip")));
        //given
        SchemaProvider provider = new SchemaProvider(PREDEFINED_SCHEMAS_LOCATIONS);
        //when
        provider.getSchema("EDM-EXTERNAL");
        //then
        File directory = new File(SchemaProvider.TMP_DIR, "schemas" + File.separator + "localhost_external_test_schema");
        File zipFile = new File(directory, "zip.zip");
        Assert.assertTrue(zipFile.exists());
    }

    @Test
    public void zipFileShouldBeCreatedInCorrectLocationForCustomZip() throws SchemaProviderException {
        wireMockRule.resetAll();
        wireMockRule.stubFor(get(urlEqualTo("/userDefinedSchema.zip"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBodyFile("test_schema.zip")));
        //given
        SchemaProvider provider = new SchemaProvider(PREDEFINED_SCHEMAS_LOCATIONS);
        //when
        provider.getSchema("http://localhost:9999/userDefinedSchema.zip","EDM.xsd", "schematron/schematron.xsl");
        //then
        File directory = new File(SchemaProvider.TMP_DIR, "schemas" + File.separator + "localhost_userDefinedSchema");
        File zipFile = new File(directory, "zip.zip");
        Assert.assertTrue(zipFile.exists());
    }

    @Test
    public void zipFileShouldBeDownloadedWhenNotAvailable() throws IOException, SchemaProviderException {
        wireMockRule.stubFor(get(urlEqualTo("/internal_test_schema.zip"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBodyFile("test_schema.zip")));
        clearSchemasDir();
        //given
        SchemaProvider provider = new SchemaProvider(PREDEFINED_SCHEMAS_LOCATIONS);
        //when
        Schema s = provider.getSchema("EDM-INTERNAL");
        //then
        Assert.assertEquals("localhost_internal_test_schema", s.getName());
        Assert.assertEquals(entryFileLocation(provider, "localhost_internal_test_schema", "EDM-INTERNAL.xsd"), s.getPath());
        Assert.assertNotNull(s.getSchematronPath());
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
        Assert.assertTrue(zipFile.exists());
    }

    private String entryFileLocation(SchemaProvider schemaProvider, String schemaName, String fileLocation) {
        return directoryLocation(schemaProvider, schemaName) + fileLocation;
    }

    private String directoryLocation(SchemaProvider schemaProvider, String schemaName) {
        return schemaProvider.getSchemasDirectory() + schemaName + File.separator;
    }
}