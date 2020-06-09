package eu.europeana.metis.dereference;

import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * Convert an incoming record to EDM.
 */
public class IncomingRecordToEdmConverter {

  private static final String EMPTY_XML_REGEX = "\\A(<\\?.*?\\?>|<!--.*?-->|\\s)*\\Z";
  private static final Pattern EMPTY_XML_CHECKER = Pattern.compile(EMPTY_XML_REGEX, Pattern.DOTALL);

  /** Vocabulary XSLs require the resource ID as a parameter. This is the parameter name. **/
  private static final String TARGET_ID_PARAMETER_NAME = "targetId";

  private final Templates template;

  /**
   * Create a converter for the given vocabulary.
   *
   * @param vocabulary The vocabulary for which to perform the conversion.
   * @throws TransformerException In case the input could not be parsed or the conversion could not
   *         be set up.
   */
  public IncomingRecordToEdmConverter(Vocabulary vocabulary) throws TransformerException {
    this(vocabulary.getXslt());
  }

  /**
   * Create a converter for the transformation.
   *
   * @param xslt The xslt representing the conversion to perform.
   * @throws TransformerException In case the input could not be parsed or the conversion could not
   *         be set up.
   */
  public IncomingRecordToEdmConverter(String xslt) throws TransformerException {
    final Source xsltSource = new StreamSource(new StringReader(xslt));
    final TransformerFactory factory = TransformerFactory.newInstance();
    factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    this.template = factory.newTemplates(xsltSource);
  }

  /**
   * Convert the given record.
   * 
   * @param record The incoming record (that comes from the vocabulary).
   * @param recordId The record ID of the incoming record.
   * @return The EDM record, or null if the record couldn't be transformed.
   * @throws TransformerException In case there is a problem performing the transformation.
   */
  public String convert(String record, String recordId) throws TransformerException {

    // Set up the transformer
    final Source source = new StreamSource(new StringReader(record));
    final StringWriter stringWriter = new StringWriter();
    final Transformer transformer = template.newTransformer();
    transformer.setParameter(TARGET_ID_PARAMETER_NAME, recordId);
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());

    // Perform the transformation.
    transformer.transform(source, new StreamResult(stringWriter));
    final String result = stringWriter.toString();

    // Check whether there is a result (any tag in the file).
    return isEmptyXml(result) ? null : result;
 }

  /**
   * This method analyzes the XML file and decides whether or not it has any content. Excluded are
   * space characters, the XML header and XML comments. Note: if this method returns true, the input
   * is not technically a valid XML as it doesn't have a root node.
   * 
   * @param file The input XML.
   * @return Whether the XML has any content.
   */
  static boolean isEmptyXml(String file) {
    return EMPTY_XML_CHECKER.matcher(file).matches();
  }
}
