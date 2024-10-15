package eu.europeana.indexing.solr.property;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import eu.europeana.corelib.solr.entity.PlaceImpl;
import java.util.Arrays;
import org.apache.solr.common.SolrInputDocument;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link ProxySolrCreator} class
 */
class PropertySolrCreatorTest {

  @Test
  void testAddAllToDocument() {

    final PropertySolrCreator<PlaceImpl> creator = spy(PropertySolrCreator.class);
    doNothing().when(creator).addToDocument(any(SolrInputDocument.class), any(PlaceImpl.class));

    // Create two properties that will be used, and one that won't.
    final PlaceImpl property1 = new PlaceImpl();
    final PlaceImpl property2 = new PlaceImpl();
    final PlaceImpl property3 = new PlaceImpl();

    // Call creator method
    final SolrInputDocument document = new SolrInputDocument();
    creator.addAllToDocument(document, Arrays.asList(property1, property2));

    // Verify. Also make sure that the unused property is not called (basically testing the
    // integrity of the equals on PlaceImpl).
    verify(creator, times(2)).addToDocument(any(), any());
    verify(creator, times(2)).addToDocument(eq(document), any());
    verify(creator, times(1)).addToDocument(document, property1);
    verify(creator, times(1)).addToDocument(document, property2);
    verify(creator, never()).addToDocument(document, property3);
  }
}
