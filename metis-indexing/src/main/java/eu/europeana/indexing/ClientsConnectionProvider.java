package eu.europeana.indexing;

import eu.europeana.corelib.mongo.server.EdmMongoServer;
import eu.europeana.indexing.exception.IndexingException;
import org.apache.solr.client.solrj.SolrClient;

/**
 * This class is an implementation of {@link AbstractConnectionProvider} that sets up the connection using
 * provided Solr and Mongo clients. Note: the caller is responsible for closing those connections.
 * 
 * @author jochen
 *
 */
final class ClientsConnectionProvider extends AbstractConnectionProvider {

  private final EdmMongoServer mongoClient;
  private final SolrClient solrClient;

  /**
   * Constructor.
   * 
   * @param mongoClient The Mongo client to be used. Cannot be null.
   * @param solrClient The Solr client to be used. Cannot be null.
   * @throws IndexingException In case either of the two clients are null.
   */
  ClientsConnectionProvider(EdmMongoServer mongoClient, SolrClient solrClient)
      throws IndexingException {
    if (mongoClient == null) {
      throw new IndexingException("The provided Mongo client is null.");
    }
    if (solrClient == null) {
      throw new IndexingException("The provided Solr client is null.");
    }
    this.mongoClient = mongoClient;
    this.solrClient = solrClient;
  }

  @Override
  public SolrClient getSolrClient() {
    return solrClient;
  }

  @Override
  public EdmMongoServer getMongoClient() {
    return mongoClient;
  }

  @Override
  public void close() {
    // Nothing to do: the two clients are to be closed by the caller.
  }
}
