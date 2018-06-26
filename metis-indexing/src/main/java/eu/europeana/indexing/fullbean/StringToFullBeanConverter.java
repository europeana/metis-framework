package eu.europeana.indexing.fullbean;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;
import org.apache.commons.io.IOUtils;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.indexing.exception.IndexingException;

/**
 * This class converts String representations of RDF (XML) to instances of {@link FullBeanImpl}.
 * 
 * @author jochen
 *
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
   *        Factory) used to parse strings to instances of {@link RDF}. Will be called once during
   *        every call to convert a string.
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
    return convertRdfToFullBean(convertStringToRdf(record));
  }

  /**
   * Converts a string (XML of RDF) to an RDF object.
   * 
   * @param record The record as an XML string.
   * @return The RDF instance.
   * @throws IndexingException In case there was a problem with the parsing or conversion.
   */
  public RDF convertStringToRdf(String record) throws IndexingException {

    // Convert string to RDF
    final RDF rdf;
    final IBindingFactory rdfBindingFactory = rdfBindingFactorySupplier.get();
    try {
      final IUnmarshallingContext context = rdfBindingFactory.createUnmarshallingContext();
      rdf = (RDF) context.unmarshalDocument(IOUtils.toInputStream(record, DEFAULT_CHARSET),
          DEFAULT_CHARSET.name());
    } catch (JiBXException e) {
      throw new IndexingException("Could not convert record to RDF.", e);
    }

    // Sanity check - shouldn't happen
    if (rdf == null) {
      throw new IndexingException("Could not convert record to RDF: null was returned.");
    }

    // Done.
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
