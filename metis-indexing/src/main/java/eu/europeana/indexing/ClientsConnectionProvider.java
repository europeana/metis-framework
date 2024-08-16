package eu.europeana.indexing;

import eu.europeana.metis.mongo.dao.RecordDao;
import eu.europeana.indexing.exception.SetupRelatedIndexingException;
import eu.europeana.metis.mongo.dao.RecordRedirectDao;
import org.apache.solr.client.solrj.SolrClient;

/**
 * This class is an implementation of {@link AbstractConnectionProvider} that sets up the connection
 * using provided Solr and Mongo clients. Note: the caller is responsible for closing those
 * connections.
 *
 * @author jochen
 */
final class ClientsConnectionProvider implements AbstractConnectionProvider {

  private final RecordDao recordDao;
  private final RecordDao tombstoneRecordDao;
  private final RecordRedirectDao recordRedirectDao;
  private final SolrClient solrClient;

  /**
   * Constructor.
   *
   * @param recordDao The Mongo dao to be used. Cannot be null.
   * @param recordDao The Mongo tombstone dao to be used. Cannot be null.
   * @param recordRedirectDao The record redirect dao.
   * @param solrClient The Solr client to be used. Cannot be null.
   * @throws SetupRelatedIndexingException In case either of the two clients are null.
   */
  ClientsConnectionProvider(RecordDao recordDao, RecordDao tombstoneRecordDao, RecordRedirectDao recordRedirectDao,
      SolrClient solrClient)
      throws SetupRelatedIndexingException {
    if (recordDao == null) {
      throw new SetupRelatedIndexingException("The provided Mongo dao is null.");
    }
    if (tombstoneRecordDao == null) {
      throw new SetupRelatedIndexingException("The provided Mongo tombstone dao is null.");
    }
    if (solrClient == null) {
      throw new SetupRelatedIndexingException("The provided Solr client is null.");
    }
    this.recordDao = recordDao;
    this.tombstoneRecordDao = tombstoneRecordDao;
    this.recordRedirectDao = recordRedirectDao;
    this.solrClient = solrClient;
  }

  @Override
  public SolrClient getSolrClient() {
    return solrClient;
  }

  @Override
  public RecordDao getRecordDao() {
    return recordDao;
  }

  @Override
  public RecordDao getTombstoneRecordDao() {
    return tombstoneRecordDao;
  }

  @Override
  public RecordRedirectDao getRecordRedirectDao() {
    return recordRedirectDao;
  }

  @Override
  public void close() {
    // Nothing to do: the two clients are to be closed by the caller.
  }
}
