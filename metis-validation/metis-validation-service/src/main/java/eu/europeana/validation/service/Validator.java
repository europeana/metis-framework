package eu.europeana.validation.service;

import eu.europeana.validation.model.Schema;
import eu.europeana.validation.model.ValidationResult;
import eu.europeana.validation.service.EDMParser.EDMParseSetupException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * EDM Validator class Created by gmamakis on 18-12-15.
 */
public class Validator implements Callable<ValidationResult> {

  private static final String NODE_ID_ATTR = "nodeId";
  private static final Logger LOGGER = LoggerFactory.getLogger(Validator.class);
  private static final ConcurrentMap<String, Templates> templatesCache = new ConcurrentHashMap<>();

  private final String schema;
  private final String rootFileLocation;
  private final String schematronFileLocation;
  private final InputStream document;
  private final SchemaProvider schemaProvider;
  private final ClasspathResourceResolver resolver;

  /**
   * Constructor specifying the schema to validate against and the document
   *
   * @param schema schema that will be used for validation
   * @param rootFileLocation location of the schema root file
   * @param schematronFileLocation location of the schematron file
   * @param document document that will be validated - this will be read but not closed
   * @param schemaProvider the class that provides the schemas. Make sure it is initialized with
   * safe schema location paths.
   * @param resolver the resolver used for parsing split xsds
   */
  public Validator(String schema, String rootFileLocation, String schematronFileLocation,
      InputStream document, SchemaProvider schemaProvider, ClasspathResourceResolver resolver) {
    this.schema = schema;
    this.rootFileLocation = rootFileLocation;
    this.schematronFileLocation = schematronFileLocation;
    this.document = document;
    this.schemaProvider = schemaProvider;
    this.resolver = resolver;
  }

  /**
   * Get schema object specified with its name
   *
   * @param schemaName name of the schema
   * @return the schema object
   */
  private Schema getSchemaByName(String schemaName) throws SchemaProviderException {
    Schema schemaObject;
    if (schemaProvider.isPredefined(schemaName)) {
      schemaObject = schemaProvider.getSchema(schemaName);
    } else {
      if (rootFileLocation == null) {
        throw new SchemaProviderException("Missing root file location for custom schema");
      } else {
        schemaObject = schemaProvider
            .getSchema(schemaName, rootFileLocation, schematronFileLocation);
      }
    }
    if (schemaObject == null) {
      throw new SchemaProviderException("Could not find specified schema does not exist");
    }
    return schemaObject;
  }

  /**
   * Validate method using JAXP
   *
   * @return The outcome of the Validation
   */
  private ValidationResult validate() {
    LOGGER.debug("Validation started");
    try {
      final Schema savedSchema = getSchemaByName(schema);
      final InputSource source = new InputSource();
      source.setByteStream(document);
      return validate(source, savedSchema.getPath(), savedSchema.getSchematronPath(), resolver);
    } catch (SchemaProviderException e) {
      return constructValidationError(null, e);
    } finally {
      LOGGER.debug("Validation ended");
    }
  }

  private static ValidationResult validate(InputSource source, String schemaPath,
          String schematronPath, ClasspathResourceResolver resolver) {

    // Set up the validation.
    resolver.setPrefix(StringUtils.substringBeforeLast(schemaPath, File.separator));
    final DocumentBuilder edmParser;
    final javax.xml.validation.Validator edmValidator;
    final Transformer transformer;
    try {
      edmParser = EDMParser.getInstance().getEdmParser();
      edmValidator = EDMParser.getInstance().getEdmValidator(schemaPath, resolver);
      transformer = StringUtils.isNotEmpty(schematronPath) ? getTransformer(schematronPath) : null;
    } catch (EDMParseSetupException | IOException | TransformerConfigurationException e) {
      LOGGER.error("Problem setting up parsing and validation: {}", e.getMessage(), e);
      return constructValidationError(null, e);
    }

    // Perform the validation
    String rdfAbout = null;
    try {
      // False positive. The parser has all security settings applied (see EDMParser).
      @SuppressWarnings("findsecbugs:XXE_DOCUMENT")
      final Document doc = edmParser.parse(source);
      rdfAbout = getRdfAbout(doc);
      edmValidator.validate(new DOMSource(doc));
      if (transformer != null) {
        final DOMResult result = new DOMResult();
        transformer.transform(new DOMSource(doc), result);
        final NodeList nresults = result.getNode().getFirstChild().getChildNodes();
        final ValidationResult errorResult = checkNodeListForErrors(nresults, rdfAbout);
        if (errorResult != null) {
          return errorResult;
        }
      }
    } catch (IOException | SAXException | TransformerException e) {
      return constructValidationError(rdfAbout, e);
    }

    // Done: no errors to report.
    return constructOk();
  }

  private static String getRdfAbout(Document document) {
    final NodeList nodes = document
            .getElementsByTagNameNS("http://www.europeana.eu/schemas/edm/", "ProvidedCHO");
    for (int i = 0; i < nodes.getLength(); i++) {
      final Element element = (Element) nodes.item(i);
      final Attr aboutAttribute = element
              .getAttributeNodeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "about");
      final String aboutValue = Optional.ofNullable(aboutAttribute).map(Attr::getValue)
              .map(String.class::cast).orElse(null);
      if (aboutValue != null) {
        return aboutValue;
      }
    }
    return null;
  }

  private static ValidationResult checkNodeListForErrors(NodeList nresults, String rdfAbout) {
    for (int i = 0; i < nresults.getLength(); i++) {
      Node nresult = nresults.item(i);
      if ("failed-assert".equals(nresult.getLocalName())) {
        String nodeId = nresult.getAttributes().getNamedItem(NODE_ID_ATTR) == null ? null
            : nresult.getAttributes().getNamedItem(NODE_ID_ATTR).getTextContent();
        return constructValidationError(rdfAbout,
            "Schematron error: " + nresult.getTextContent().trim(), nodeId);
      }
    }
    return null;
  }

  private static Transformer getTransformer(String schematronPath)
      throws IOException, TransformerConfigurationException {
    StringReader reader;

    if (!templatesCache.containsKey(schematronPath)) {
      reader = new StringReader(
          FileUtils.readFileToString(new File(schematronPath), StandardCharsets.UTF_8.name()));
      final TransformerFactory transformerFactory = TransformerFactory.newInstance();
      transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      Templates template = TransformerFactory.newInstance()
          .newTemplates(new StreamSource(reader));
      templatesCache.put(schematronPath, template);
    }

    return templatesCache.get(schematronPath).newTransformer();
  }

  private static ValidationResult constructValidationError(String rdfAbout, Exception e) {
    ValidationResult res = new ValidationResult();
    res.setMessage(e.getMessage());
    res.setRecordId(rdfAbout);
    if (StringUtils.isEmpty(res.getRecordId())) {
      res.setRecordId("Missing record identifier for EDM record");
    }

    res.setSuccess(false);
    return res;
  }

  private static ValidationResult constructValidationError(String rdfAbout, String message,
      String nodeId) {
    ValidationResult res = new ValidationResult();
    res.setMessage(message);
    res.setRecordId(rdfAbout);
    if (StringUtils.isEmpty(res.getRecordId())) {
      res.setRecordId("Missing record identifier for EDM record");
    }
    res.setNodeId(Objects.requireNonNullElse(nodeId, "Missing node identifier"));
    res.setSuccess(false);
    return res;
  }

  private static ValidationResult constructOk() {
    ValidationResult res = new ValidationResult();

    res.setSuccess(true);
    return res;
  }

  @Override
  public ValidationResult call() {
    return validate();
  }
}
