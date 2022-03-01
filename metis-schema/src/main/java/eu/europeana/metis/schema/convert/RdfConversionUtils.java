package eu.europeana.metis.schema.convert;

import eu.europeana.metis.schema.jibx.RDF;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;

/**
 * Utility class for converting {@link RDF} to String and vice versa.
 */
public final class RdfConversionUtils {

  private static final int INDENTATION_SPACE = 2;
  private static final String UTF8 = StandardCharsets.UTF_8.name();
  private static IBindingFactory rdfBindingFactory;
  private static Map<String, RdfXmlElementMetadata> rdfXmlElementMetadataMap;
  @SuppressWarnings("java:S5852") //This regex is safe, and it's only meant for internal use without use input
  private static final Pattern complexTypePattern = Pattern.compile("^\\{(.*)}:(.*)$");

  static {
    initializeStaticComponents();
  }

  private RdfConversionUtils() {
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
      IMarshallingContext context = rdfBindingFactory.createMarshallingContext();
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
   * Get the xml representation of a class that will contain the namespace prefix and the element name. E.g. dc:subject
   * <p>This class uses the internal static map that should be generated with regards to the RDF jibx classes</p>
   *
   * @param objectClass the jibx object class to search for
   * @return the xml representation
   */
  public static String getQualifiedElementNameForClass(Class<?> objectClass) {
    final RdfXmlElementMetadata rdfXmlElementMetadata = rdfXmlElementMetadataMap.get(objectClass.getCanonicalName());
    Objects.requireNonNull(rdfXmlElementMetadata,
        String.format("Element metadata not found for class: %s", objectClass.getCanonicalName()));
    return String.format("%s:%s", rdfXmlElementMetadata.getPrefix(), rdfXmlElementMetadata.getName());
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
      final IUnmarshallingContext context = rdfBindingFactory.createUnmarshallingContext();
      return (RDF) context.unmarshalDocument(inputStream, UTF8);
    } catch (JiBXException e) {
      throw new SerializationException(
          "Something went wrong with converting to or from the RDF format.", e);
    }
  }

  /**
   * Collect all information that we can get for jibx classes from the {@link IBindingFactory}.
   */
  private static Map<String, RdfXmlElementMetadata> initializeRdfXmlElementMetadataMap() {
    Map<String, RdfXmlElementMetadata> rdfXmlElementMetadataMap = new HashMap<>();
    for (int i = 0; i < rdfBindingFactory.getMappedClasses().length; i++) {
      final String canonicalName;
      final String elementNamespace;
      final String elementName;
      final Matcher matcher = complexTypePattern.matcher(rdfBindingFactory.getMappedClasses()[i]);
      if (matcher.matches()) {
        //Complex type search
        elementNamespace = matcher.group(1);
        elementName = matcher.group(2);
        final Pattern canonicalClassNamePattern = Pattern.compile(String.format("^(.*)\\.(%s)$", elementName));
        canonicalName = Arrays.stream(rdfBindingFactory.getAbstractMappings()).flatMap(Arrays::stream)
                              .filter(Objects::nonNull)
                              .filter(input -> canonicalClassNamePattern.matcher(input).matches())
                              .findFirst().orElse(null);
      } else {
        //Simple type search
        elementNamespace = rdfBindingFactory.getElementNamespaces()[i];
        elementName = rdfBindingFactory.getElementNames()[i];
        canonicalName = rdfBindingFactory.getMappedClasses()[i];
      }
      checkAndStoreMetadataInMap(rdfXmlElementMetadataMap, canonicalName, elementNamespace, elementName);
    }
    return rdfXmlElementMetadataMap;
  }

  static class RdfXmlElementMetadata {

    final String canonicalClassName;
    final String prefix;
    final String namespace;
    final String name;

    public RdfXmlElementMetadata(String canonicalClassName, String prefix, String namespace, String name) {
      this.canonicalClassName = canonicalClassName;
      this.prefix = prefix;
      this.namespace = namespace;
      this.name = name;
    }

    public String getCanonicalClassName() {
      return canonicalClassName;
    }

    public String getPrefix() {
      return prefix;
    }

    public String getNamespace() {
      return namespace;
    }

    public String getName() {
      return name;
    }
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

  private static void checkAndStoreMetadataInMap(final Map<String, RdfXmlElementMetadata> rdfXmlElementMetadataMap,
      String canonicalName, String elementNamespace, String elementName) {
    //Store only if we could find the canonical name properly
    if (canonicalName != null) {
      final int namespaceIndex = IntStream.range(0, rdfBindingFactory.getNamespaces().length)
                                          .filter(j -> rdfBindingFactory.getNamespaces()[j].equals(elementNamespace))
                                          .findFirst().orElseThrow();
      final String prefix = rdfBindingFactory.getPrefixes()[namespaceIndex];
      final RdfXmlElementMetadata rdfXmlElementMetadata = new RdfXmlElementMetadata(canonicalName, prefix, elementNamespace,
          elementName);
      rdfXmlElementMetadataMap.put(rdfXmlElementMetadata.getCanonicalClassName(), rdfXmlElementMetadata);
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

  static void initializeStaticComponents() {
    try {
      rdfBindingFactory = BindingDirectory.getFactory(RDF.class);
      rdfXmlElementMetadataMap = initializeRdfXmlElementMetadataMap();
    } catch (JiBXException e) {
      throw new IllegalStateException("No binding factory available.", e);
    }
  }
}
