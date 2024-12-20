package eu.europeana.validation.service;

import java.io.IOException;
import java.io.Serial;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;

/**
 * Helper class for EDM service exposing two validator and a DOMParser
 */
final class EDMParser {

  private static EDMParser edmParser;
  private static final ConcurrentMap<String, Schema> CACHE = new ConcurrentHashMap<>();
  private static final DocumentBuilderFactory PARSE_FACTORY = DocumentBuilderFactory.newInstance();
  private static final Logger LOGGER = LoggerFactory.getLogger(EDMParser.class);

  static {
    try {
      PARSE_FACTORY.setNamespaceAware(true);
      PARSE_FACTORY
          .setFeature("http://apache.org/xml/features/validation/schema-full-checking", false);
      PARSE_FACTORY.setFeature("http://apache.org/xml/features/honour-all-schemaLocations", true);
      PARSE_FACTORY.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      PARSE_FACTORY.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    } catch (ParserConfigurationException e) {
      LOGGER.error("Unable to create DocumentBuilderFactory", e);
    }
  }

  private EDMParser() {
  }

  /**
   * Get an EDM Parser using DOM
   *
   * @return EDM parser.
   * @throws EDMParseSetupException In case the parser could not be created.
   */
  public DocumentBuilder getEdmParser() throws EDMParseSetupException {
    try {
      return PARSE_FACTORY.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      throw new EDMParseSetupException("Unable to configure parser", e);
    }
  }

  /**
   * Get a JAXP schema validator (singleton)
   *
   * @param path The path location of the schema. This has to be a sanitized input otherwise the method could become unsecure.
   * @param resolver the resolver used for the schema
   * @return JAXP schema validator.
   * @throws EDMParseSetupException In case the validator could not be created.
   */
  public Validator getEdmValidator(String path, LSResourceResolver resolver)
      throws EDMParseSetupException {
    // False positive. The parser has all security settings applied (see getSchema).
    @SuppressWarnings("squid:S2755")
    final Validator result = getSchema(path, resolver).newValidator();
    return result;
  }

  private Schema getSchema(String path, LSResourceResolver resolver) throws EDMParseSetupException {

    final Schema schema;
    try {
      schema = CACHE.computeIfAbsent(path, s -> {
        try {
          SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
          factory.setResourceResolver(resolver);
          factory.setFeature("http://apache.org/xml/features/validation/schema-full-checking", false);
          factory.setFeature("http://apache.org/xml/features/honour-all-schemaLocations", true);
          // Protection from XXE
          factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
          factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
          return factory.newSchema(new StreamSource(Files.newInputStream(Paths.get(s))));
        } catch (SAXException | IOException e) {
          throw new RuntimeEDMParseSetupException("Failed to create schema for path: " + s, e);
        }
      });
    } catch (RuntimeEDMParseSetupException e) {
      throw new EDMParseSetupException(e.getMessage(), e);
    }
    return schema;
  }

  /**
   * Get a parser instance as a singleton
   *
   * @return parser instance.
   */
  public static EDMParser getInstance() {
    synchronized (EDMParser.class) {
      if (edmParser == null) {
        edmParser = new EDMParser();
      }
      return edmParser;
    }
  }

  private static class RuntimeEDMParseSetupException extends RuntimeException {

    @Serial private static final long serialVersionUID = 6802348788522122630L;

    RuntimeEDMParseSetupException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  /**
   * Exception indicating something went wrong setting up EDM Parsing.
   */
  public static class EDMParseSetupException extends Exception {

    @Serial private static final long serialVersionUID = 3854029647081914787L;

    EDMParseSetupException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
