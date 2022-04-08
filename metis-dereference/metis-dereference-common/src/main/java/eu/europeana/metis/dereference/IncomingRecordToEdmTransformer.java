package eu.europeana.metis.dereference;

import static eu.europeana.metis.utils.CommonStringValues.CRLF_PATTERN;

import eu.europeana.metis.exception.BadContentException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.BasicTransformerFactory;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Convert an incoming record to EDM.
 */
public class IncomingRecordToEdmTransformer {

  private static final Logger LOGGER = LoggerFactory.getLogger(IncomingRecordToEdmTransformer.class);
  private static final Pattern XML_DECLARATION_CHECKER = Pattern.compile("\\A<\\?[^?]*\\?>\\s*\\z");

  /**
   * Vocabulary XSLs require the resource ID as a parameter. This is the parameter name.
   **/
  private static final String TARGET_ID_PARAMETER_NAME = "targetId";

  private final Templates template;
  private final DocumentBuilderFactory documentBuilderFactory;

  /**
   * Create a converter for the transformation.
   *
   * @param xslt The xslt representing the conversion to perform.
   * @throws TransformerException if the transformer could not be initialized
   * @throws ParserConfigurationException if the xml builder could not be initialized
   */
  public IncomingRecordToEdmTransformer(String xslt) throws TransformerException, ParserConfigurationException {
    final Source xsltSource = new StreamSource(new StringReader(xslt));
    // Ensure that the Saxon library is used by choosing the right transformer factory.
    final TransformerFactory transformerFactory = new BasicTransformerFactory();
    transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    this.template = transformerFactory.newTemplates(xsltSource);

    documentBuilderFactory = DocumentBuilderFactory.newInstance();
    documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    documentBuilderFactory.setNamespaceAware(true);
  }

  /**
   * Transform the given xmlRecord.
   *
   * @param xmlRecord The incoming xmlRecord (that comes from the vocabulary).
   * @param resourceId The xmlRecord ID of the incoming xmlRecord.
   * @return The EDM xmlRecord, or null if the xmlRecord couldn't be transformed.
   * @throws BadContentException if there was a problem performing the transformation.
   */
  public Optional<String> transform(String xmlRecord, String resourceId) throws BadContentException {
    // Set up the transformer
    final Source source = new StreamSource(new StringReader(xmlRecord));
    final StringWriter transformedXmlWriter = new StringWriter();
    final Transformer transformer;
    try {
      transformer = template.newTransformer();
      transformer.setParameter(TARGET_ID_PARAMETER_NAME, resourceId);
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());

      // Perform the transformation.
      transformer.transform(source, new StreamResult(transformedXmlWriter));
    } catch (TransformerException e) {
      throw new BadContentException("Transformation failure", e);
    }
    return getValidatedXml(resourceId, transformedXmlWriter.toString());
  }

  /**
   * Returns an optional which is empty if the provided xml is a validated empty xml or contains the xml itself if it's a valid
   * parsable xml.
   *
   * @param resourceId the resource id
   * @param xml the xml
   * @return the optional being empty or with the xml contents
   * @throws BadContentException if the xml parsing failed
   */
  @NotNull
  private Optional<String> getValidatedXml(String resourceId, String xml) throws BadContentException {
    final Optional<String> xmlResponse;
    if (isEmptyXml(xml)) {
      xmlResponse = Optional.empty();
      if (LOGGER.isInfoEnabled()) {
        LOGGER.info("Transformed entity {} results to an empty XML.",
            CRLF_PATTERN.matcher(resourceId).replaceAll(""));
      }
    } else {
      try {
        assertXmlValidity(xml);
        xmlResponse = Optional.of(xml);
      } catch (ParserConfigurationException | IOException | SAXException e) {
        throw new BadContentException("Transformed xml is not valid", e);
      }
    }

    return xmlResponse;
  }

  /**
   * Asserts if the provided xml is valid and can be parsed.
   *
   * @param xml the xml string
   * @throws ParserConfigurationException if xml parsing failed
   * @throws IOException if xml parsing failed
   * @throws SAXException if xml parsing failed
   */
  private void assertXmlValidity(String xml) throws ParserConfigurationException, IOException, SAXException {
    documentBuilderFactory.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
  }

  /**
   * Checks if the provided xml is empty.
   * <p>
   * Emptiness is verifying if the only the xml header declaration is present. Note: if this method returns true, the input is not
   * technically a valid XML as it doesn't have a root node.
   * </p>
   *
   * @param xml the input XML.
   * @return true if xml is empty
   */
  private boolean isEmptyXml(String xml) {
    return XML_DECLARATION_CHECKER.matcher(xml).matches();
  }
}
