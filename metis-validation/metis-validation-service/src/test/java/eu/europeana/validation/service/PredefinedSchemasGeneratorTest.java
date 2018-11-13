package eu.europeana.validation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Properties;
import org.junit.jupiter.api.Test;

class PredefinedSchemasGeneratorTest {

  @Test
  void properSchemasShouldBeCreated() {

    Properties props = new Properties();
    props.setProperty("predefinedSchemas", "EDM-INTERNAL,EDM-EXTERNAL");
    //
    props.setProperty("predefinedSchemas.EDM-INTERNAL.url",
        "http://ftp.eanadev.org/schema_zips/europeana_schemas.zip");
    props.setProperty("predefinedSchemas.EDM-INTERNAL.rootLocation", "EDM-INTERNAL.xsd");
    props.setProperty("predefinedSchemas.EDM-INTERNAL.schematronLocation",
        "schematron/schematron-internal.xsl");
    //
    props.setProperty("predefinedSchemas.EDM-EXTERNAL.url",
        "http://ftp.eanadev.org/schema_zips/europeana_schemas.zip");
    props.setProperty("predefinedSchemas.EDM-EXTERNAL.rootLocation", "EDM.xsd");
    props.setProperty("predefinedSchemas.EDM-EXTERNAL.schematronLocation",
        "schematron/schematron.xsl");
    //
    //
    PredefinedSchemas predefinedSchemas = PredefinedSchemasGenerator.generate(props);
    //
    assertNotNull(predefinedSchemas.get("EDM-INTERNAL"));
    assertNotNull(predefinedSchemas.get("EDM-EXTERNAL"));

    assertTrue(predefinedSchemas.contains("EDM-INTERNAL"));
    assertEquals("EDM-INTERNAL", predefinedSchemas.get("EDM-INTERNAL").getKey());
    assertEquals("http://ftp.eanadev.org/schema_zips/europeana_schemas.zip",
        predefinedSchemas.get("EDM-INTERNAL").getLocation());
    assertEquals("EDM-INTERNAL.xsd",
        predefinedSchemas.get("EDM-INTERNAL").getRootFileLocation());
    assertEquals("schematron/schematron-internal.xsl",
        predefinedSchemas.get("EDM-INTERNAL").getSchematronFileLocation());

    assertTrue(predefinedSchemas.contains("EDM-EXTERNAL"));
    assertEquals("EDM-EXTERNAL", predefinedSchemas.get("EDM-EXTERNAL").getKey());
    assertEquals("http://ftp.eanadev.org/schema_zips/europeana_schemas.zip",
        predefinedSchemas.get("EDM-EXTERNAL").getLocation());
    assertEquals("EDM.xsd", predefinedSchemas.get("EDM-EXTERNAL").getRootFileLocation());
    assertEquals("schematron/schematron.xsl",
        predefinedSchemas.get("EDM-EXTERNAL").getSchematronFileLocation());
  }

  @Test
  void emptySchemasShouldBeCreated() {
    //prepare props
    Properties props = new Properties();
    //
    props.setProperty("predefinedSchemas.EDM-INTERNAL.url",
        "http://ftp.eanadev.org/schema_zips/europeana_schemas.zip");
    props.setProperty("predefinedSchemas.EDM-INTERNAL.rootLocation", "EDM-INTERNAL.xsd");
    props.setProperty("predefinedSchemas.EDM-INTERNAL.schematronLocation",
        "schematron/schematron-internal.xsl");
    //
    props.setProperty("predefinedSchemas.EDM-EXTERNAL.url",
        "http://ftp.eanadev.org/schema_zips/europeana_schemas.zip");
    props.setProperty("predefinedSchemas.EDM-EXTERNAL.rootLocation", "EDM.xsd");
    props.setProperty("predefinedSchemas.EDM-INTERNAL.schematronLocation",
        "schematron/schematron.xsl");
    //
    //
    PredefinedSchemas predefinedSchemas = PredefinedSchemasGenerator.generate(props);
    //
    assertFalse(predefinedSchemas.contains("EDM-EXTERNAL"));
    assertFalse(predefinedSchemas.contains("EDM-INTERNAL"));
    assertNull(predefinedSchemas.get("EDM-INTERNAL"));
    assertNull(predefinedSchemas.get("EDM-EXTERNAL"));
  }
}