package eu.europeana.indexing.mongo;

import static org.junit.jupiter.api.Assertions.*;

import eu.europeana.indexing.exception.IndexingException;
import eu.europeana.metis.mongo.connection.MongoProperties;
import eu.europeana.metis.schema.jibx.RDF;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MongoIndexerTest {

  @Mock
  private MongoProperties properties;

  @InjectMocks
  private MongoIndexer indexer;

  @Test
  void IllegalArgumentExceptionTest() {
    IllegalArgumentException expected = assertThrows(IllegalArgumentException.class, () ->indexer.indexRecord((RDF) null));
    assertEquals("Input RDF cannot be null.",expected.getMessage());
  }

  @Test
  void indexRecord() throws IndexingException {
//    final RDF inputRdf = new RDF();
//    indexer.indexRecord(inputRdf);
  }

  @Test
  void testIndexRecord() {
  }
}
