package eu.europeana.metis.schema.convert;

import eu.europeana.metis.schema.jibx.RDF;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.stream.IntStream;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for converting {@link RDF} to String and vice versa.
 */
public final class RdfConversionUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(RdfConversionUtils.class);
  private static final int INDENTATION_SPACE = 2;
  private static final String UTF8 = StandardCharsets.UTF_8.name();
  private static IBindingFactory rdfBindingFactory;

  static {
    try {
      rdfBindingFactory = BindingDirectory.getFactory(RDF.class);
    } catch (JiBXException e) {
      LOGGER.error("Unable to create binding factory", e);
    }
  }

  private RdfConversionUtils() {
  }

  private static IBindingFactory getRdfBindingFactory() {
    if (rdfBindingFactory != null) {
      return rdfBindingFactory;
    }
    throw new IllegalStateException("No binding factory available.");
  }

  public static String getQualifiedElementNameForClass(Class<?> objectClass) {
    final int mappedClassIndex = IntStream.range(0, rdfBindingFactory.getMappedClasses().length).filter(
                                              i -> rdfBindingFactory.getMappedClasses()[i].equals(objectClass.getCanonicalName()))
                                          .findFirst().orElseThrow();
    final String elementNamespace = rdfBindingFactory.getElementNamespaces()[mappedClassIndex];
    final String elementName = rdfBindingFactory.getElementNames()[mappedClassIndex];
    final int namespaceIndex = IntStream.range(0, rdfBindingFactory.getNamespaces().length)
                                        .filter(i -> rdfBindingFactory.getNamespaces()[i].equals(elementNamespace))
                                        .findFirst().orElseThrow();
    final String prefix = rdfBindingFactory.getPrefixes()[namespaceIndex];
    return String.format("%s:%s", prefix, elementName);
  }

  /**
   * Convert an {@link RDF} to a UTF-8 encoded XML
   *
   * @param rdf The RDF object to convert
   * @return An XML string representation of the RDF object
   * @throws SerializationException if during marshalling there is a failure
   */
  public static String convertRdfToString(RDF rdf) throws SerializationException {
    try {
      return new String(convertRdfToBytes(rdf), UTF8);
    } catch (UnsupportedEncodingException e) {
      throw new IllegalStateException("Unexpected exception - should not occur.", e);
    }
  }

  /**
   * Convert an {@link RDF} to a UTF-8 encoded XML
   *
   * @param rdf The RDF object to convert
   * @return An XML string representation of the RDF object
   * @throws SerializationException if during marshalling there is a failure
   */
  public static byte[] convertRdfToBytes(RDF rdf) throws SerializationException {
    try {
      IMarshallingContext context = getRdfBindingFactory().createMarshallingContext();
      context.setIndent(INDENTATION_SPACE);
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      context.marshalDocument(rdf, UTF8, null, out);
      return out.toByteArray();
    } catch (JiBXException e) {
      throw new SerializationException(
          "Something went wrong with converting to or from the RDF format.", e);
    }
  }

  /**
   * Convert a UTF-8 encoded XML to {@link RDF}
   *
   * @param xml the xml string
   * @return the RDF object
   * @throws SerializationException if during unmarshalling there is a failure
   */
  public static RDF convertStringToRdf(String xml) throws SerializationException {
    try (final InputStream inputStream = new ByteArrayInputStream(
        xml.getBytes(StandardCharsets.UTF_8))) {
      return convertInputStreamToRdf(inputStream);
    } catch (IOException e) {
      throw new SerializationException("Unexpected issue with byte stream.", e);
    }
  }

  /**
   * Convert a UTF-8 encoded XML to {@link RDF}
   *
   * @param inputStream The xml. The stream is not closed.
   * @return the RDF object
   * @throws SerializationException if during unmarshalling there is a failure
   */
  public static RDF convertInputStreamToRdf(InputStream inputStream) throws SerializationException {
    try {
      final IUnmarshallingContext context = getRdfBindingFactory().createUnmarshallingContext();
      return (RDF) context.unmarshalDocument(inputStream, UTF8);
    } catch (JiBXException e) {
      throw new SerializationException(
          "Something went wrong with converting to or from the RDF format.", e);
    }
  }
}
