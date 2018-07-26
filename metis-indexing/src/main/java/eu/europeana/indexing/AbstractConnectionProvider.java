package eu.europeana.indexing;

import java.io.Closeable;
import java.io.IOException;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import eu.europeana.corelib.mongo.server.EdmMongoServer;

/**
 * <p>
 * Instances of this class are responsible for providing an instance of {@link Indexer} with access
 * to the required connections to persistence storage. It is responsible for maintaining the
 * connections and providing a Publisher object (instance of {@link FullBeanPublisher}) for
 * publishing Full Beans to be accessed by external agents.
 * </p>
 * <p>
 * Please note that this class is {@link Closeable} and must be closed to release it's resources.
 * </p>
 * 
 * @author jochen
 *
 */
public abstract class AbstractConnectionProvider implements Closeable {

  /**
   * Provides a Publisher object for publishing Full Beans so that they may be found by users.
   * 
   * @param preserveUpdateAndCreateTimesFromRdf This determines whether this publisher should use
   *        the updated and created times from the incoming RDFs, or whether it computes its own.
   * @return A publisher.
   */
  final FullBeanPublisher getFullBeanPublisher(boolean preserveUpdateAndCreateTimesFromRdf) {
    return new FullBeanPublisher(getMongoClient(), getSolrClient(), preserveUpdateAndCreateTimesFromRdf);
  }

  /**
   * This method will trigger a flush operation on pending changes/updates to the persistent data,
   * causing it to become permanent as well as available to other processes. Calling this method is
   * not obligatory, and indexing will work without it. This just allows the caller to determine the
   * moment when changes are written to disk rather than wait for this to be triggered by the
   * infrastructure/library itself at its own discretion.
   * 
   * @param blockUntilComplete If true, the call blocks until the flush is complete.
   * 
   * @throws IOException If there is a low-level I/O error.
   * @throws SolrServerException If there is an error on the server.
   */
  public final void triggerFlushOfPendingChanges(boolean blockUntilComplete)
      throws SolrServerException, IOException {
    getSolrClient().commit(blockUntilComplete, blockUntilComplete);
  }

  /**
   * Provides a remover object for removing all records in given datasets.
   * 
   * @return A dataset remover.
   */
  final DatasetRemover getDatasetRemover() {
    return new DatasetRemover(getMongoClient(), getSolrClient());
  }

  /**
   * Provides a Solr client object for connecting with the Solr database.
   * 
   * @return A Solr client.
   */
  public abstract SolrClient getSolrClient();

  /**
   * Provides a Mongo client object for connecting with the Mongo database.
   * 
   * @return A Mongo client.
   */
  public abstract EdmMongoServer getMongoClient();

}
