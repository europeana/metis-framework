package eu.europeana.validation.service;

import java.io.IOException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;

/**
 * Helper class for EDM service exposing two validator and a DOMParser
 */
final class EDMParser {

  private static EDMParser p;
  private static final ConcurrentMap<String, Schema> CACHE = new ConcurrentHashMap<>();
  private static final DocumentBuilderFactory PARSE_FACTORY;
  private static final Logger LOGGER = LoggerFactory.getLogger(EDMParser.class);

  static {
    DocumentBuilderFactory temp = null;
    try {
      temp = DocumentBuilderFactory.newInstance();
      temp.setNamespaceAware(true);
      temp.setFeature("http://apache.org/xml/features/validation/schema-full-checking", false);
      temp.setFeature("http://apache.org/xml/features/honour-all-schemaLocations", true);
      temp.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    } catch (ParserConfigurationException e) {
      LOGGER.error("Unable to create DocumentBuilderFactory", e);
    }
    PARSE_FACTORY = temp;
  }

  private EDMParser() {
  }

  /**
   * Get an EDM Parser using DOM
   *
   * @return EDM parser.
   */
  public DocumentBuilder getEdmParser() {
    try {
      return PARSE_FACTORY.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      LOGGER.error("Unable to configure parser", e);
    }
    return null;
  }

  /**
   * Get a JAXP schema validator (singleton)
   *
   * @param path The path location of the schema
   * @param resolver the resolver used for the schema
   * @return JAXP schema validator.
   */
  public javax.xml.validation.Validator getEdmValidator(String path, LSResourceResolver resolver) {
    try {
      Schema schema = getSchema(path, resolver);
      return schema.newValidator();
    } catch (SAXException | IOException e) {
      LOGGER.error("Unable to create validator", e);
    }
    return null;
  }

  private Schema getSchema(String path, LSResourceResolver resolver)
      throws SAXException, IOException {

    if (!CACHE.containsKey(path)) {
      SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      factory.setResourceResolver(resolver);
      factory.setFeature("http://apache.org/xml/features/validation/schema-full-checking",
          false);
      factory.setFeature("http://apache.org/xml/features/honour-all-schemaLocations", true);
      Schema schema = factory.newSchema(new StreamSource(Files.newInputStream(Paths.get(path))));
      CACHE.put(path, schema);
    }
    return CACHE.get(path);
  }

  /**
   * Get a parser instance as a singleton
   *
   * @return parser instance.
   */
  public static EDMParser getInstance() {
    synchronized (EDMParser.class) {
      if (p == null) {
        p = new EDMParser();
      }
      return p;
    }
  }
}
