package eu.europeana.indexing;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import org.apache.commons.io.IOUtils;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.indexing.exception.IndexerConfigurationException;
import eu.europeana.indexing.exception.IndexingException;

/**
 * Implementation of {@link Indexer}.
 * 
 * @author jochen
 *
 */
class IndexerImpl implements Indexer {

  private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

  private static final Logger LOGGER = LoggerFactory.getLogger(IndexerImpl.class);

  private static IBindingFactory globalRdfBindingFactory;

  private final IndexingConnectionProvider connectionProvider;

  private final IndexingSupplier<IBindingFactory> rdfBindingFactorySupplier;

  /**
   * Constructor.
   * 
   * @param settings The settings for this indexer.
   * @throws IndexerConfigurationException In case an exception occurred while setting up the
   *         indexer.
   */
  IndexerImpl(IndexingSettings settings) throws IndexerConfigurationException {
    this(settings, IndexerImpl::getRdfBindingFactory);
  }

  /**
   * Constructor for testing purposes.
   * 
   * @param settings The settings for this indexer.
   * @param rdfBindingFactorySupplier Supplies an instance of {@link IBindingFactory} (RDF Binding
   *        Factory) used to parse strings to instances of {@link RDF}. Will be called once during
   *        every index.
   * @throws IndexerConfigurationException In case an exception occurred while setting up the
   *         indexer.
   */
  IndexerImpl(IndexingSettings settings,
      IndexingSupplier<IBindingFactory> rdfBindingFactorySupplier)
      throws IndexerConfigurationException {
    this.connectionProvider = new IndexingConnectionProvider(settings);
    this.rdfBindingFactorySupplier = rdfBindingFactorySupplier;
  }

  @Override
  public void index(String record) throws IndexingException {
    index(Collections.singletonList(record));
  }

  @Override
  public void index(List<String> records) throws IndexingException {
    LOGGER.info("Processing {} records...", records.size());
    final IBindingFactory rdfBindingFactory = rdfBindingFactorySupplier.get();
    try {
      final FullBeanPublisher publisher = connectionProvider.getFullBeanPublisher();
      for (String record : records) {
        final RDF rdf = convertStringToRdf(record, rdfBindingFactory);
        publisher.publish(rdf);
      }
      LOGGER.info("Successfully processed {} records.", records.size());
    } catch (IndexingException e) {
      LOGGER.warn("Error while indexing a record.", e);
      throw e;
    }
  }

  @Override
  public void close() throws IOException {
    this.connectionProvider.close();
  }

  private RDF convertStringToRdf(String record, IBindingFactory rdfBindingFactory) throws IndexingException {

    // Convert string to RDF
    final RDF rdf;
    try {
      IUnmarshallingContext context = rdfBindingFactory.createUnmarshallingContext();
      rdf = (RDF) context.unmarshalDocument(IOUtils.toInputStream(record, DEFAULT_CHARSET),
          DEFAULT_CHARSET.name());
    } catch (JiBXException e) {
      throw new IndexingException("Could not convert record to RDF.", e);
    }

    // Sanity check - shouldn't happen
    if (rdf == null) {
      throw new IndexingException("Could not convert record to RDF: null was returned.");
    }

    // Done
    return rdf;
  }

  private static synchronized IBindingFactory getRdfBindingFactory() throws IndexingException {
    if (globalRdfBindingFactory == null) {
      try {
        globalRdfBindingFactory = BindingDirectory.getFactory(RDF.class);
      } catch (JiBXException e) {
        throw new IndexingException("Error creating the JibX factory.", e);
      }
    }
    return globalRdfBindingFactory;
  }

  /**
   * Similar to the Java interface {@link Supplier}, but one that may throw an
   * {@link IndexingException}.
   * 
   * @author jochen
   *
   * @param <T> The type of the object to be supplied.
   */
  @FunctionalInterface
  interface IndexingSupplier<T> {

    /**
     * Gets a result.
     * 
     * @return A result.
     * @throws IndexingException In case something went wrong while getting the result.
     */
    public T get() throws IndexingException;
  }
}
