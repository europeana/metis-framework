package eu.europeana.indexing.solr;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import eu.europeana.indexing.mongo.MongoIndexer;
import eu.europeana.metis.mongo.connection.MongoProperties;
import eu.europeana.metis.schema.jibx.RDF;
import eu.europeana.metis.solr.connection.SolrProperties;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SolrIndexerTest {

  @Mock
  private SolrProperties properties;

  @InjectMocks
  private SolrIndexer indexer;

  @Test
  void IllegalArgumentExceptionTest() {
    IllegalArgumentException expected = assertThrows(IllegalArgumentException.class, () ->indexer.indexRecord((RDF) null));
    assertEquals("Input RDF cannot be null.",expected.getMessage());
  }

  @Test
  void indexRecord() throws Exception {
    final RDF inputRdf = new RDF();
   // when(properties.getSolrHosts()).thenReturn(List.of(new URI("http://localhost:8983")));
   // indexer.indexRecord(inputRdf);
  }

  @Test
  void testIndexRecord() {
  }
}
