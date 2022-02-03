package eu.europeana.metis.solr.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.LBHttpSolrClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Unit test for {@link CompoundSolrClient}
 *
 * @author Jorge Ortiz
 * @since 03-02-2022
 */
class CompoundSolrClientTest {

  private CompoundSolrClient compoundSolrClient;

  @Test
  void getSolrClient() {
    final LBHttpSolrClient lbHttpSolrClient = mock(LBHttpSolrClient.class);
    final CloudSolrClient cloudSolrClient = mock(CloudSolrClient.class);

    compoundSolrClient = new CompoundSolrClient(lbHttpSolrClient, cloudSolrClient);

    assertNotNull(compoundSolrClient.getSolrClient());
    assertEquals(cloudSolrClient, compoundSolrClient.getSolrClient());
  }

  @Test
  void getSolrClientCloudSolrClientIsNull() {
    final LBHttpSolrClient lbHttpSolrClient = mock(LBHttpSolrClient.class);

    compoundSolrClient = new CompoundSolrClient(lbHttpSolrClient, null);

    assertNotNull(compoundSolrClient.getSolrClient());
    assertEquals(lbHttpSolrClient, compoundSolrClient.getSolrClient());
  }

  @Test
  void close() throws IOException {
    final LBHttpSolrClient lbHttpSolrClient = mock(LBHttpSolrClient.class);
    final CloudSolrClient cloudSolrClient = mock(CloudSolrClient.class);
    compoundSolrClient = new CompoundSolrClient(lbHttpSolrClient, cloudSolrClient);

    compoundSolrClient.close();

    verify(lbHttpSolrClient).close();
    verify(cloudSolrClient).close();
  }
}