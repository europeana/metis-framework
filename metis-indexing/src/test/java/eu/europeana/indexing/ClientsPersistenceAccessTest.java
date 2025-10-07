package eu.europeana.indexing;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import eu.europeana.metis.mongo.dao.RecordDao;
import eu.europeana.metis.mongo.dao.RecordRedirectDao;
import java.io.IOException;
import org.apache.solr.client.solrj.SolrClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ClientsPersistenceAccessTest {

  @Test
  void testConstructorThrowsExceptionForNullRecordDao() throws IOException {
    RecordDao recordDao = Mockito.mock(RecordDao.class);
    RecordRedirectDao recordRedirectDao = Mockito.mock(RecordRedirectDao.class);
    try (SolrClient solrClient = Mockito.mock(SolrClient.class)) {

      assertThrows(NullPointerException.class, () ->
          new ClientsPersistenceAccess(null, null, recordRedirectDao, solrClient)
      );

      assertThrows(NullPointerException.class, () ->
          new ClientsPersistenceAccess(recordDao, null, recordRedirectDao, null)
      );
    }
  }

  @Test
  void testCloseDoesNothing() throws IOException {
    RecordDao recordDao = Mockito.mock(RecordDao.class);
    RecordDao tombstoneRecordDao = Mockito.mock(RecordDao.class);
    RecordRedirectDao recordRedirectDao = Mockito.mock(RecordRedirectDao.class);
    try (SolrClient solrClient = Mockito.mock(SolrClient.class)) {

      ClientsPersistenceAccess provider = new ClientsPersistenceAccess(recordDao, tombstoneRecordDao, recordRedirectDao,
          solrClient);
      assertDoesNotThrow(provider::close);
    }
  }
}
