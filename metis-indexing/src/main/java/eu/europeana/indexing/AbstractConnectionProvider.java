package eu.europeana.indexing;

import java.io.Closeable;
import org.apache.solr.client.solrj.SolrClient;
import eu.europeana.corelib.mongo.server.EdmMongoServer;
import eu.europeana.indexing.mongo.FullBeanDao;

/**
 * <p>
 * This class is maintainable for providing an instance of {@link Indexer} with the required
 * connections to persistence storage. It is responsible for maintaining the connections and
 * providing access to a Publisher object (instance of {@link FullBeanPublisher}) for publishing
 * Full Beans to be accessed by external agents.
 * </p>
 * <p>
 * Please note that this class is {@link Closeable} and must be closed to release it's resources.
 * </p>
 * 
 * @author jochen
 *
 */
abstract class AbstractConnectionProvider implements Closeable {

  /**
   * Provides a Publisher object for publishing Full Beans so that they may be found by users.
   * 
   * @return A publisher.
   */
  final FullBeanPublisher getFullBeanPublisher() {
    return new FullBeanPublisher(new FullBeanDao(getMongoClient()), getSolrClient());
  }

  /**
   * Provides a Solr client object for connecting with the Solr database.
   * 
   * @return A Solr client.
   */
  abstract SolrClient getSolrClient();

  /**
   * Provides a Mongo client object for connecting with the Mongo database.
   * 
   * @return A Mongo client.
   */
  abstract EdmMongoServer getMongoClient();
}
