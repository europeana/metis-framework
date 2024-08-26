package eu.europeana.indexing;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import eu.europeana.metis.mongo.dao.RecordDao;
import eu.europeana.metis.mongo.dao.RecordRedirectDao;
import java.io.IOException;
import org.apache.solr.client.solrj.SolrClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ClientsConnectionProviderTest {

  @Test
  void testConstructorWithAllParameters() throws IOException {
    RecordDao recordDao = Mockito.mock(RecordDao.class);
    RecordDao tombstoneRecordDao = Mockito.mock(RecordDao.class);
    RecordRedirectDao recordRedirectDao = Mockito.mock(RecordRedirectDao.class);
    try (SolrClient solrClient = Mockito.mock(SolrClient.class)) {

      ClientsConnectionProvider clientsConnectionProvider = new ClientsConnectionProvider(recordDao, tombstoneRecordDao,
          recordRedirectDao, solrClient);

      assertEquals(recordDao, clientsConnectionProvider.getRecordDao());
      assertEquals(tombstoneRecordDao, clientsConnectionProvider.getTombstoneRecordDao());
      assertEquals(recordRedirectDao, clientsConnectionProvider.getRecordRedirectDao());
      assertEquals(solrClient, clientsConnectionProvider.getSolrClient());
    }
  }

  @Test
  void testConstructorWithRequiredParametersOnly() throws IOException {
    RecordDao recordDao = Mockito.mock(RecordDao.class);
    RecordRedirectDao recordRedirectDao = Mockito.mock(RecordRedirectDao.class);
    try (SolrClient solrClient = Mockito.mock(SolrClient.class)) {

      ClientsConnectionProvider clientsConnectionProvider = new ClientsConnectionProvider(recordDao, recordRedirectDao,
          solrClient);

      assertEquals(recordDao, clientsConnectionProvider.getRecordDao());
      assertNull(clientsConnectionProvider.getTombstoneRecordDao());
      assertEquals(recordRedirectDao, clientsConnectionProvider.getRecordRedirectDao());
      assertEquals(solrClient, clientsConnectionProvider.getSolrClient());
    }
  }

  @Test
  void testConstructorThrowsExceptionForNullRecordDao() throws IOException {
    RecordDao recordDao = Mockito.mock(RecordDao.class);
    RecordRedirectDao recordRedirectDao = Mockito.mock(RecordRedirectDao.class);
    try (SolrClient solrClient = Mockito.mock(SolrClient.class)) {

      assertThrows(NullPointerException.class, () ->
          new ClientsConnectionProvider(null, recordRedirectDao, solrClient)
      );

      assertThrows(NullPointerException.class, () ->
          new ClientsConnectionProvider(recordDao, recordRedirectDao, null)
      );
    }
  }

  @Test
  void testCloseDoesNothing() throws IOException {
    RecordDao recordDao = Mockito.mock(RecordDao.class);
    RecordDao tombstoneRecordDao = Mockito.mock(RecordDao.class);
    RecordRedirectDao recordRedirectDao = Mockito.mock(RecordRedirectDao.class);
    try (SolrClient solrClient = Mockito.mock(SolrClient.class)) {

      ClientsConnectionProvider provider = new ClientsConnectionProvider(recordDao, tombstoneRecordDao, recordRedirectDao,
          solrClient);
      assertDoesNotThrow(provider::close);
    }
  }
}
