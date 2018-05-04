package eu.europeana.indexing;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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

  private final AbstractConnectionProvider connectionProvider;

  private final IndexingSupplier<IBindingFactory> rdfBindingFactorySupplier;

  /**
   * Constructor.
   * 
   * @param connectionProvider The connection provider for this indexer.
   */
  IndexerImpl(AbstractConnectionProvider connectionProvider) {
    this(connectionProvider, IndexerImpl::getRdfBindingFactory);
  }

  /**
   * Constructor for testing purposes.
   * 
   * @param connectionProvider The connection provider for this indexer.
   * @param rdfBindingFactorySupplier Supplies an instance of {@link IBindingFactory} (RDF Binding
   *        Factory) used to parse strings to instances of {@link RDF}. Will be called once during
   *        every index.
   */
  IndexerImpl(AbstractConnectionProvider connectionProvider,
      IndexingSupplier<IBindingFactory> rdfBindingFactorySupplier) {
    this.connectionProvider = connectionProvider;
    this.rdfBindingFactorySupplier = rdfBindingFactorySupplier;
  }

  @Override
  public void indexRdfs(List<RDF> records) throws IndexingException {
    LOGGER.info("Processing {} records...", records.size());
    try {
      final FullBeanPublisher publisher = connectionProvider.getFullBeanPublisher();
      for (RDF record : records) {
        publisher.publish(record);
      }
      LOGGER.info("Successfully processed {} records.", records.size());
    } catch (IndexingException e) {
      LOGGER.warn("Error while indexing a record.", e);
      throw e;
    }
  }

  @Override
  public void indexRdf(RDF record) throws IndexingException {
    indexRdfs(Collections.singletonList(record));
  }

  @Override
  public void index(List<String> records) throws IndexingException {
    LOGGER.info("Parsing {} records...", records.size());
    final IBindingFactory rdfBindingFactory = rdfBindingFactorySupplier.get();
    final List<RDF> rdfs = new ArrayList<>();
    for (String record : records) {
      rdfs.add(convertStringToRdf(record, rdfBindingFactory));
    }
    indexRdfs(rdfs);
  }

  @Override
  public void index(String record) throws IndexingException {
    index(Collections.singletonList(record));
  }

  @Override
  public void close() throws IOException {
    this.connectionProvider.close();
  }

  private RDF convertStringToRdf(String record, IBindingFactory rdfBindingFactory)
      throws IndexingException {

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
