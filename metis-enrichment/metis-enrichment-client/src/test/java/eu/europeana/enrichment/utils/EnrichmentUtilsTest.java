package eu.europeana.enrichment.utils;

import eu.europeana.metis.schema.convert.RdfConversionUtils;
import eu.europeana.metis.schema.convert.SerializationException;
import eu.europeana.metis.schema.jibx.RDF;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class EnrichmentUtilsTest {

  private static RDF testRdf;

  @BeforeAll
  static void setUp() throws FileNotFoundException, SerializationException {

    testRdf = RdfConversionUtils.convertInputStreamToRdf(
        new FileInputStream("src/test/resources/sample_enrichment.xml"));

  }

  @Test
  void testSetAdditionalData(){

  }

}
