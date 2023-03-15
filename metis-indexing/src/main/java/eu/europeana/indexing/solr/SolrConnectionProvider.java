package eu.europeana.indexing.solr;

import eu.europeana.indexing.AbstractConnectionProvider;
import eu.europeana.indexing.exception.SetupRelatedIndexingException;
import eu.europeana.metis.mongo.dao.RecordDao;
import eu.europeana.metis.mongo.dao.RecordRedirectDao;
import eu.europeana.metis.solr.client.CompoundSolrClient;
import eu.europeana.metis.solr.connection.SolrClientProvider;
import java.io.IOException;
import org.apache.solr.client.solrj.SolrClient;

/**
 * The type Solr connection provider.
 */
public final class SolrConnectionProvider implements AbstractConnectionProvider {

  private final CompoundSolrClient solrClient;

  /**
   * Instantiates a new Solr connection provider.
   *
   * @param settings the solr settings
   * @throws SetupRelatedIndexingException the setup related indexing exception
   */
  public SolrConnectionProvider(SolrIndexingSettings settings)
      throws SetupRelatedIndexingException {
    this.solrClient = new SolrClientProvider<>(settings.getSolrProperties()).createSolrClient();
  }

  @Override
  public SolrClient getSolrClient() {
    return this.solrClient.getSolrClient();
  }

  @Override
  public RecordDao getRecordDao() {
    return null;
  }

  @Override
  public RecordRedirectDao getRecordRedirectDao() {
    return null;
  }

  @Override
  public void close() throws IOException {
    this.solrClient.close();
  }
}
