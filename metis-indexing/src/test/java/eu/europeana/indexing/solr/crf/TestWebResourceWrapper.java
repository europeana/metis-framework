package eu.europeana.indexing.solr.crf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.definitions.jibx.WebResourceType;
import eu.europeana.indexing.utils.RdfWrapper;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class TestWebResourceWrapper {

  @Test
  void testExtractWebResources() {

    // Create entities
    final WebResourceType entity0 = mock(WebResourceType.class);
    doReturn(" ").when(entity0).getAbout();
    final WebResourceType entity1 = mock(WebResourceType.class);
    doReturn("nonemptyabout").when(entity1).getAbout();
    final WebResourceType entity2 = mock(WebResourceType.class);
    doReturn(null).when(entity2).getAbout();

    // Test rdf that returns a real list
    final RDF rdf = mock(RDF.class);
    when(rdf.getWebResourceList()).thenReturn(Arrays.asList(entity0, entity1, entity2));
    assertEquals(Collections.singletonList(entity1.getAbout()),
        WebResourceWrapper.extractWebResources(new RdfWrapper(rdf)).stream()
            .map(WebResourceWrapper::getAbout).collect(Collectors.toList()));

    // Test rdf that returns null
    when(rdf.getWebResourceList()).thenReturn(null);
    assertTrue(WebResourceWrapper.extractWebResources(new RdfWrapper(rdf)).isEmpty());
  }
}
