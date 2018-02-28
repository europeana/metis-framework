package eu.europeana.metis.dereference.service.utils;

import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import eu.europeana.metis.dereference.Vocabulary;

/**
 * Convert an incoming record to EDM.
 */
public class IncomingRecordToEdmConverter {

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
    final Source xsltSource = new StreamSource(new StringReader(vocabulary.getXslt()));
    this.template = TransformerFactory.newInstance().newTemplates(xsltSource);
  }

  /**
   * Convert the given record.
   * 
   * @param record The incoming record (that comes from the vocabulary).
   * @param recordId The record ID of the incoming record.
   * @return The EDM record.
   * @throws TransformerException In case record could not be parsed or converted.
   */
  public String convert(String record, String recordId) throws TransformerException {
    final Source source = new StreamSource(new StringReader(record));
    final StringWriter stringWriter = new StringWriter();
    final Transformer transformer = template.newTransformer();
    transformer.setParameter(TARGET_ID_PARAMETER_NAME, recordId);
    transformer.transform(source, new StreamResult(stringWriter));
    return stringWriter.toString();
  }
}
