package eu.europeana.indexing;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.indexing.exception.IndexerConfigurationException;
import eu.europeana.indexing.exception.IndexingException;

/**
 * Implementation of {@link Indexer}.
 * 
 * @author jochen
 *
 */
class IndexerImpl implements Indexer {

  private static final Logger LOGGER = LoggerFactory.getLogger(IndexerImpl.class);

  private final IndexingConnectionProvider connectionProvider;

  private final Supplier<FullBeanConverter> fullBeanConverterSupplier;

  /**
   * Constructor.
   * 
   * @param settings The settings for this indexer.
   * @throws IndexerConfigurationException In case an exception occurred while setting up the
   *         indexer.
   */
  IndexerImpl(IndexingSettings settings) throws IndexerConfigurationException {
    this(settings, FullBeanConverter::new);
  }

  /**
   * Constructor for testing purposes.
   * 
   * @param settings The settings for this indexer.
   * @param fullBeanConverterSupplier Supplies an instance of {@link FullBeanConverter} used to parse
   *        strings to instances of {@link FullBeanImpl}. Will be called once during every index.
   * @throws IndexerConfigurationException In case an exception occurred while setting up the
   *         indexer.
   */
  IndexerImpl(IndexingSettings settings, Supplier<FullBeanConverter> fullBeanConverterSupplier)
      throws IndexerConfigurationException {
    this.connectionProvider = new IndexingConnectionProvider(settings);
    this.fullBeanConverterSupplier = fullBeanConverterSupplier;
  }

  @Override
  public void index(String record) throws IndexingException {
    index(Collections.singletonList(record));
  }

  @Override
  public void index(List<String> records) throws IndexingException {
    LOGGER.info("Processing {} records...", records.size());
    final FullBeanConverter fullBeanConverter = fullBeanConverterSupplier.get();
    try {
      final FullBeanPublisher publisher = connectionProvider.getFullBeanPublisher();
      for (String record : records) {
        publisher.publish(fullBeanConverter.convertFromRdfString(record));
      }
      LOGGER.info("Successfully processed {} records.", records.size());
    } catch (IndexingException e) {
      LOGGER.warn("Error while indexing a record.", e);
      throw e;
    }
  }

  @Override
  public void close() {
    this.connectionProvider.close();
  }
}
