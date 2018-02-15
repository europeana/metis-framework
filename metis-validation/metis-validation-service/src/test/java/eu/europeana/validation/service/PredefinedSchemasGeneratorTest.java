package eu.europeana.validation.service;

import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;

public class PredefinedSchemasGeneratorTest {

    @Test
    public void properSchemasShouldBeCreated(){

        Properties props = new Properties();
        props.setProperty("predefinedSchemas","EDM-INTERNAL,EDM-EXTERNAL");
        //
        props.setProperty("predefinedSchemas.EDM-INTERNAL.url","http://ftp.eanadev.org/schema_zips/europeana_schemas.zip");
        props.setProperty("predefinedSchemas.EDM-INTERNAL.rootLocation","EDM-INTERNAL.xsd");
        props.setProperty("predefinedSchemas.EDM-INTERNAL.schematronLocation","schematron/schematron-internal.xsl");
        //
        props.setProperty("predefinedSchemas.EDM-EXTERNAL.url","http://ftp.eanadev.org/schema_zips/europeana_schemas.zip");
        props.setProperty("predefinedSchemas.EDM-EXTERNAL.rootLocation","EDM.xsd");
        props.setProperty("predefinedSchemas.EDM-EXTERNAL.schematronLocation","schematron/schematron.xsl");
        //
        //
        PredefinedSchemas predefinedSchemas = PredefinedSchemasGenerator.generate(props);
        //
        Assert.assertNotNull(predefinedSchemas.get("EDM-INTERNAL"));
        Assert.assertNotNull(predefinedSchemas.get("EDM-EXTERNAL"));

        Assert.assertTrue(predefinedSchemas.contains("EDM-INTERNAL"));
        Assert.assertTrue("EDM-INTERNAL".equals(predefinedSchemas.get("EDM-INTERNAL").getKey()));
        Assert.assertTrue("http://ftp.eanadev.org/schema_zips/europeana_schemas.zip".equals(predefinedSchemas.get("EDM-INTERNAL").getLocation()));
        Assert.assertTrue("EDM-INTERNAL.xsd".equals(predefinedSchemas.get("EDM-INTERNAL").getRootFileLocation()));
        Assert.assertTrue("schematron/schematron-internal.xsl".equals(predefinedSchemas.get("EDM-INTERNAL").getSchematronFileLocation()));
        //
        Assert.assertTrue(predefinedSchemas.contains("EDM-EXTERNAL"));
        Assert.assertTrue("EDM-EXTERNAL".equals(predefinedSchemas.get("EDM-EXTERNAL").getKey()));
        Assert.assertTrue("http://ftp.eanadev.org/schema_zips/europeana_schemas.zip".equals(predefinedSchemas.get("EDM-EXTERNAL").getLocation()));
        Assert.assertTrue("EDM.xsd".equals(predefinedSchemas.get("EDM-EXTERNAL").getRootFileLocation()));
        Assert.assertTrue("schematron/schematron.xsl".equals(predefinedSchemas.get("EDM-EXTERNAL").getSchematronFileLocation()));
    }

    @Test
    public void emptySchemasShouldBeCreated(){
        //prepare props
        Properties props = new Properties();
        //
        props.setProperty("predefinedSchemas.EDM-INTERNAL.url","http://ftp.eanadev.org/schema_zips/europeana_schemas.zip");
        props.setProperty("predefinedSchemas.EDM-INTERNAL.rootLocation","EDM-INTERNAL.xsd");
        props.setProperty("predefinedSchemas.EDM-INTERNAL.schematronLocation","schematron/schematron-internal.xsl");
        //
        props.setProperty("predefinedSchemas.EDM-EXTERNAL.url","http://ftp.eanadev.org/schema_zips/europeana_schemas.zip");
        props.setProperty("predefinedSchemas.EDM-EXTERNAL.rootLocation","EDM.xsd");
        props.setProperty("predefinedSchemas.EDM-INTERNAL.schematronLocation","schematron/schematron.xsl");
        //
        //
        PredefinedSchemas predefinedSchemas = PredefinedSchemasGenerator.generate(props);
        //
        Assert.assertFalse(predefinedSchemas.contains("EDM-EXTERNAL"));
        Assert.assertFalse(predefinedSchemas.contains("EDM-INTERNAL"));
        Assert.assertNull(predefinedSchemas.get("EDM-INTERNAL"));
        Assert.assertNull(predefinedSchemas.get("EDM-EXTERNAL"));
    }
}