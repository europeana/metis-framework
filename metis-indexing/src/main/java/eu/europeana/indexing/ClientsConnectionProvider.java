package eu.europeana.indexing;

import eu.europeana.metis.mongo.dao.RecordDao;
import eu.europeana.metis.mongo.dao.RecordRedirectDao;
import java.util.Objects;
import org.apache.solr.client.solrj.SolrClient;

/**
 * This class is an implementation of {@link AbstractConnectionProvider} that sets up the connection using provided Solr and Mongo
 * clients. Note: the caller is responsible for closing those connections.
 *
 * @author jochen
 */
record ClientsConnectionProvider(RecordDao recordDao, RecordDao tombstoneRecordDao, RecordRedirectDao recordRedirectDao,
                                 SolrClient solrClient) implements AbstractConnectionProvider {

  /**
   * Constructor.
   *
   * @param recordDao The Mongo dao to be used. Cannot be null.
   * @param tombstoneRecordDao The Mongo tombstone dao to be used. Cannot be null.
   * @param recordRedirectDao The record redirect dao.
   * @param solrClient The Solr client to be used. Cannot be null.
   */
  public ClientsConnectionProvider {
    Objects.requireNonNull(recordDao);
    Objects.requireNonNull(solrClient);
  }

  /**
   * Constructor.
   *
   * @param recordDao The Mongo dao to be used. Cannot be null.
   * @param recordRedirectDao The record redirect dao.
   * @param solrClient The Solr client to be used. Cannot be null.
   */
  public ClientsConnectionProvider(RecordDao recordDao, RecordRedirectDao recordRedirectDao, SolrClient solrClient) {
    this(recordDao, null, recordRedirectDao, solrClient);
  }

  @Override
    public void close() {
      // Nothing to do: the two clients are to be closed by the caller.
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
}
