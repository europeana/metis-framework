package eu.europeana.enrichment.utils;

import eu.europeana.enrichment.api.internal.FieldType;
import eu.europeana.metis.schema.jibx.RDF;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class RdfProxyUtilsTest {


  @Test
  void testAppendLinkToEuropeanaProxy(){

    RDF rdf = new RDF(); //TODO: add proxy lists
    //TODO: add europeana proxy
    String link = "http://dummylink.com";
    Set<FieldType> linkTypes = new HashSet<>();

  }

  @Test
  void testGetProviderProxy(){

  }
}
