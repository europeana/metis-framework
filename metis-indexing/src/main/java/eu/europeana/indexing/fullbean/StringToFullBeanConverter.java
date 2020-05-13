package eu.europeana.indexing.fullbean;

import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.indexing.exception.IndexerRelatedIndexingException;
import eu.europeana.indexing.exception.IndexingException;
import eu.europeana.indexing.exception.RecordRelatedIndexingException;
import eu.europeana.indexing.utils.RdfWrapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;
import org.apache.commons.io.IOUtils;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;

/**
 * This class converts String representations of RDF (XML) to instances of {@link FullBeanImpl}.
 *
 * @author jochen
 */
public class StringToFullBeanConverter extends RdfToFullBeanConverter {

  private static IBindingFactory globalRdfBindingFactory;

  private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

  private final IndexingSupplier<IBindingFactory> rdfBindingFactorySupplier;

  /**
   * Constructor.
   */
  public StringToFullBeanConverter() {
    this(StringToFullBeanConverter::getRdfBindingFactory);
  }

  /**
   * Constructor for testing purposes.
   *
   * @param rdfBindingFactorySupplier Supplies an instance of {@link IBindingFactory} (RDF Binding
   * Factory) used to parse strings to instances of {@link RDF}. Will be called once during every
   * call to convert a string.
   */
  StringToFullBeanConverter(IndexingSupplier<IBindingFactory> rdfBindingFactorySupplier) {
    this.rdfBindingFactorySupplier = rdfBindingFactorySupplier;
  }

  /**
   * Converts a string (XML of RDF) to Full Bean.
   *
   * @param record The record as an XML string.
   * @return The Full Bean.
   * @throws IndexingException In case there was a problem with the parsing or conversion.
   */
  public FullBeanImpl convertStringToFullBean(String record) throws IndexingException {
    return convertRdfToFullBean(new RdfWrapper(convertStringToRdf(record)));
  }

  /**
   * Converts a string (XML of RDF) to an RDF object.
   *
   * @param record The record as an XML string.
   * @return The RDF instance.
   * @throws IndexingException In case there was a problem with the parsing or conversion.
   */
  public RDF convertStringToRdf(String record) throws IndexingException {
    try (InputStream stream = IOUtils.toInputStream(record, DEFAULT_CHARSET)) {
      return convertToRdf(stream);
    } catch (IOException e) {
      throw new RecordRelatedIndexingException("Could not read string value.", e);
    }
  }

  /**
   * Converts an input stream (XML of RDF) to an RDF object.
   *
   * @param record The record as an input stream. This stream is not closed.
   * @return The RDF instance.
   * @throws IndexingException In case there was a problem with the parsing or conversion.
   */
  public RDF convertToRdf(InputStream record) throws IndexingException {

    // Convert string to RDF
    final RDF rdf;
    final IBindingFactory rdfBindingFactory = rdfBindingFactorySupplier.get();
    try {
      final IUnmarshallingContext context = rdfBindingFactory.createUnmarshallingContext();
      rdf = (RDF) context.unmarshalDocument(record, DEFAULT_CHARSET.name());
    } catch (JiBXException e) {
      throw new RecordRelatedIndexingException("Could not convert record to RDF.", e);
    }

    // Sanity check - shouldn't happen
    if (rdf == null) {
      throw new RecordRelatedIndexingException(
              "Could not convert record to RDF: null was returned.");
    }

    // Done.
    return rdf;
  }

  private static IBindingFactory getRdfBindingFactory()
      throws IndexerRelatedIndexingException {
    synchronized (StringToFullBeanConverter.class) {
      if (globalRdfBindingFactory == null) {
        try {
          globalRdfBindingFactory = BindingDirectory.getFactory(RDF.class);
        } catch (JiBXException e) {
          throw new IndexerRelatedIndexingException("Error creating the JibX factory.", e);
        }
      }
      return globalRdfBindingFactory;
    }
  }

  /**
   * Similar to the Java interface {@link Supplier}, but one that may throw an {@link
   * IndexerRelatedIndexingException}.
   *
   * @param <T> The type of the object to be supplied.
   * @author jochen
   */
  @FunctionalInterface
  interface IndexingSupplier<T> {

    /**
     * Gets a result.
     *
     * @return A result.
     * @throws IndexerRelatedIndexingException In case something went wrong while getting the
     * result.
     */
    T get() throws IndexerRelatedIndexingException;
  }
}
