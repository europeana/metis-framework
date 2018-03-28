package eu.europeana.indexing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;

/**
 * Implementation of {@link Indexer}.
 * 
 * TODO JOCHEN integrate publishing service with this class.
 * 
 * @author jochen
 *
 */
class IndexerImpl implements Indexer {

  private static final Logger LOGGER = LoggerFactory.getLogger(IndexerImpl.class);

  private final FullBeanPublisher publisher;

  IndexerImpl(FullBeanPublisher publishingService) {
    this.publisher = publishingService;
  }

  @Override
  public void index(String record) throws IndexingException {
    LOGGER.info("Processing record...");

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Record to process: " + record);
    }

    final FullBeanImpl fullBean = convertStringToFullBean(record);
    publisher.publish(fullBean);

    LOGGER.info("Successfully processed record.");
  }

  FullBeanImpl convertStringToFullBean(String record) throws IndexingException {
    return new FullBeanCreator().convertStringToFullBean(record);
  }
}
