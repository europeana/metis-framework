package eu.europeana.indexing;

import java.io.IOException;
import org.apache.solr.client.solrj.SolrClient;
import eu.europeana.corelib.mongo.server.EdmMongoServer;
import eu.europeana.indexing.exception.IndexerConfigurationException;

/**
 * This class is an implementation of {@link AbstractConnectionProvider} that sets up the connection using
 * provided Solr and Mongo clients. Note: the caller is responsible for closing those connections.
 * 
 * @author jochen
 *
 */
class ClientsConnectionProvider extends AbstractConnectionProvider {

  private final EdmMongoServer mongoClient;
  private final SolrClient solrClient;

  /**
   * Constructor.
   * 
   * @param mongoClient The Mongo client to be used. Cannot be null.
   * @param solrClient The Solr client to be used. Cannot be null.
   * @throws IndexerConfigurationException In case either of the two clients are null.
   */
  ClientsConnectionProvider(EdmMongoServer mongoClient, SolrClient solrClient)
      throws IndexerConfigurationException {
    if (mongoClient == null) {
      throw new IndexerConfigurationException("The provided Mongo client is null.");
    }
    if (solrClient == null) {
      throw new IndexerConfigurationException("The provided Solr client is null.");
    }
    this.mongoClient = mongoClient;
    this.solrClient = solrClient;
  }

  @Override
  protected SolrClient getSolrClient() {
    return solrClient;
  }

  @Override
  protected EdmMongoServer getMongoClient() {
    return mongoClient;
  }

  @Override
  public void close() throws IOException {
    // Nothing to do: the two clients are to be closed by the caller.
  }
}
