package eu.europeana.indexing.utils;

import eu.europeana.indexing.common.exception.IndexerRelatedIndexingException;
import eu.europeana.indexing.common.exception.IndexingException;
import eu.europeana.indexing.common.exception.RecordRelatedIndexingException;
import eu.europeana.metis.schema.convert.RdfConversionUtils;
import eu.europeana.metis.schema.convert.SerializationException;
import eu.europeana.metis.schema.jibx.RDF;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;
import org.apache.commons.io.IOUtils;

/**
 * This class deserializes String representations of RDF (XML) to instances of {@link RDF}.
 *
 * @author jochen
 */
public class RDFDeserializer {

  private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

  private RdfConversionUtils rdfConversionUtils;
  private final Supplier<RdfConversionUtils> rdfConversionUtilsSupplier;

  /**
   * Constructor.
   */
  public RDFDeserializer() {
    this(RdfConversionUtils::new);
  }

  /**
   * Constructor for testing purposes.
   *
   * @param rdfConversionUtilsSupplier Supplies an instance of {@link RdfConversionUtils}
   * used to deserialize strings to instances of {@link RDF}. Will be called once during every
   * call to convert a string.
   */
  RDFDeserializer(Supplier<RdfConversionUtils> rdfConversionUtilsSupplier) {
    this.rdfConversionUtilsSupplier = rdfConversionUtilsSupplier;
  }

  /**
   * Converts a string (XML of RDF) to an RDF object.
   *
   * @param record The record as an XML string.
   * @return The RDF instance.
   * @throws IndexingException In case there was a problem with the parsing or conversion.
   */
  public RDF convertToRdf(String record) throws IndexingException {
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
    try {
      rdf = getRdfConversionUtils().convertInputStreamToRdf(record);
    } catch (SerializationException e) {
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

  private RdfConversionUtils getRdfConversionUtils() throws IndexerRelatedIndexingException {
    synchronized (this) {
      if (this.rdfConversionUtils == null) {
        try {
          this.rdfConversionUtils = rdfConversionUtilsSupplier.get();
        } catch (Exception e) {
          throw new IndexerRelatedIndexingException("Error creating the JibX factory.", e);
        }
      }
      return this.rdfConversionUtils;
    }
  }
}
