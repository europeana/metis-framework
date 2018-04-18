package eu.europeana.validation.service;

import eu.europeana.validation.model.Schema;
import eu.europeana.validation.model.ValidationResult;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * EDM Validator class
 * Created by gmamakis on 18-12-15.
 */
public class Validator implements Callable<ValidationResult> {

  private static final String NODE_ID_ATTR = "nodeId";
  private static final Logger LOGGER = LoggerFactory.getLogger(Validator.class);
  private static ConcurrentMap<String, Templates> templatesCache;

  private String schema;
  private String rootFileLocation;
  private String schematronFileLocation;
  private String document;
  private SchemaProvider schemaProvider;
  private ClasspathResourceResolver resolver;

  static {
    templatesCache = new ConcurrentHashMap<>();
  }


  /**
   * Constructor specifying the schema to validate against and the document
   *
   * @param schema schema that will be used for validation
   * @param rootFileLocation location of the schema root file
   * @param schematronFileLocation location of the schematron file
   * @param document document that will be validated
   */
  public Validator(String schema, String rootFileLocation, String schematronFileLocation,
      String document, SchemaProvider schemaProvider, ClasspathResourceResolver resolver) {
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
    Schema schemaObject = null;
    if (schemaProvider.isPredefined(schemaName)) {
      schemaObject = schemaProvider.getSchema(schemaName);
    } else {
      if (rootFileLocation != null) {
        schemaObject = schemaProvider.getSchema(schemaName, rootFileLocation, schematronFileLocation);
      } else {
        throw new SchemaProviderException("Missing root file location for custom schema");
      }
    }
    if (schemaObject == null)
      throw new SchemaProviderException("Could not find specified schema does not exist");
    return schemaObject;
  }

  /**
   * Validate method using JAXP
   *
   * @return The outcome of the Validation
   */
  private ValidationResult validate() {
    LOGGER.info("Validation started");
    InputSource source = new InputSource();
    source.setByteStream(new ByteArrayInputStream(document.getBytes(StandardCharsets.UTF_8)));
    try {
      Schema savedSchema = getSchemaByName(schema);

      resolver.setPrefix(StringUtils.substringBeforeLast(savedSchema.getPath(), File.separator));

      Document doc = EDMParser.getInstance().getEdmParser().parse(source);
      EDMParser.getInstance().getEdmValidator(savedSchema.getPath(), resolver)
          .validate(new DOMSource(doc));
      if (StringUtils.isNotEmpty(savedSchema.getSchematronPath())) {
        Transformer transformer = getTransformer(savedSchema);

        DOMResult result = new DOMResult();
        transformer.transform(new DOMSource(doc), result);
        NodeList nresults = result.getNode().getFirstChild().getChildNodes();
        for (int i = 0; i < nresults.getLength(); i++) {
          Node nresult = nresults.item(i);
          if ("failed-assert".equals(nresult.getLocalName())) {
            String nodeId = nresult.getAttributes().getNamedItem(NODE_ID_ATTR) == null ? null
                : nresult.getAttributes().getNamedItem(NODE_ID_ATTR).getTextContent();
            return constructValidationError(document,
                "Schematron error: " + nresult.getTextContent().trim(), nodeId);
          }
        }
      }
    } catch (IOException | SchemaProviderException | SAXException | TransformerException e) {
      return constructValidationError(document, e);
    }
    LOGGER.info("Validation ended");
    return constructOk();
  }

  private Transformer getTransformer(Schema schema)
      throws IOException, TransformerConfigurationException {
    StringReader reader;
    String schematronPath = schema.getSchematronPath();

    if (!templatesCache.containsKey(schematronPath)) {
      reader = new StringReader(
          IOUtils.toString(new FileInputStream(schematronPath), StandardCharsets.UTF_8));
      Templates template = TransformerFactory.newInstance()
          .newTemplates(new StreamSource(reader));
      templatesCache.put(schematronPath, template);
    }

    return templatesCache.get(schematronPath).newTransformer();
  }

  private ValidationResult constructValidationError(String document, Exception e) {
    ValidationResult res = new ValidationResult();
    res.setMessage(e.getMessage());
    res.setRecordId(getRecordId(document));
    if (StringUtils.isEmpty(res.getRecordId())) {
      res.setRecordId("Missing record identifier for EDM record");
    }

    res.setSuccess(false);
    return res;
  }

  private ValidationResult constructValidationError(String document, String message,
      String nodeId) {
    ValidationResult res = new ValidationResult();
    res.setMessage(message);
    res.setRecordId(getRecordId(document));
    if (StringUtils.isEmpty(res.getRecordId())) {
      res.setRecordId("Missing record identifier for EDM record");
    }
    if (nodeId != null) {
      res.setNodeId(nodeId);
    } else {
      res.setNodeId("Missing node identifier");
    }

    res.setSuccess(false);
    return res;
  }

  private String getRecordId(String document) {
    Pattern pattern = Pattern.compile("ProvidedCHO\\s+rdf:about\\s?=\\s?\"(.+)\"\\s?>");
    Matcher matcher = pattern.matcher(document);
    if (matcher.find()) {
      return matcher.group(1);
    } else {
      return null;
    }
  }

  private ValidationResult constructOk() {
    ValidationResult res = new ValidationResult();

    res.setSuccess(true);
    return res;
  }

  @Override
  public ValidationResult call() {
    return validate();
  }

}


