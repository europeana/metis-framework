package eu.europeana.indexing;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
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

  private final IndexingConnectionProvider connectionProvider;

  IndexerImpl(IndexingSettings settings) throws IndexerConfigurationException {
    this.connectionProvider = new IndexingConnectionProvider(settings);
  }

  @Override
  public void index(String record) throws IndexingException {
    index(Collections.singletonList(record));
  }

  @Override
  public void index(List<String> records) throws IndexingException {
    LOGGER.info("Processing {} records...", records.size());
    final FullBeanPublisher publisher = connectionProvider.getFullBeanPublisher();
    for (String record : records) {
      publisher.publish(convertStringToFullBean(record));
    }
    LOGGER.info("Successfully processed {} records.", records.size());
  }

  FullBeanImpl convertStringToFullBean(String record) throws IndexingException {
    return new FullBeanCreator().convertStringToFullBean(record);
  }

  @Override
  public void close() throws IOException {
    this.connectionProvider.close();
  }
}
