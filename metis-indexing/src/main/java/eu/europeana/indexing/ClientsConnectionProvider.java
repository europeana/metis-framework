package eu.europeana.indexing;

import eu.europeana.corelib.mongo.server.EdmMongoServer;
import eu.europeana.indexing.exception.SetupRelatedIndexingException;
import eu.europeana.metis.mongo.RecordRedirectDao;
import org.apache.solr.client.solrj.SolrClient;

/**
 * This class is an implementation of {@link AbstractConnectionProvider} that sets up the connection
 * using provided Solr and Mongo clients. Note: the caller is responsible for closing those
 * connections.
 *
 * @author jochen
 */
final class ClientsConnectionProvider implements AbstractConnectionProvider {

  private final EdmMongoServer edmMongoClient;
  private final RecordRedirectDao recordRedirectDao;
  private final SolrClient solrClient;

  /**
   * Constructor.
   *
   * @param edmMongoClient The Mongo client to be used. Cannot be null.
   * @param recordRedirectDao The record redirect dao.
   * @param solrClient The Solr client to be used. Cannot be null.
   * @throws SetupRelatedIndexingException In case either of the two clients are null.
   */
  ClientsConnectionProvider(EdmMongoServer edmMongoClient, RecordRedirectDao recordRedirectDao,
      SolrClient solrClient)
      throws SetupRelatedIndexingException {
    if (edmMongoClient == null) {
      throw new SetupRelatedIndexingException("The provided Mongo client is null.");
    }
    if (recordRedirectDao == null) {
      throw new SetupRelatedIndexingException("The provided Record redirect dao is null.");
    }
    if (solrClient == null) {
      throw new SetupRelatedIndexingException("The provided Solr client is null.");
    }
    this.edmMongoClient = edmMongoClient;
    this.recordRedirectDao = recordRedirectDao;
    this.solrClient = solrClient;
  }

  @Override
  public SolrClient getSolrClient() {
    return solrClient;
  }

  @Override
  public EdmMongoServer getEdmMongoClient() {
    return edmMongoClient;
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
