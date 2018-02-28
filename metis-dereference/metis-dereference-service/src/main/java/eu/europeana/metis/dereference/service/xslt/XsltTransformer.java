package eu.europeana.metis.dereference.service.xslt;

import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * Apply an XSLT to incoming data and convert to string Created by ymamakis on 2/11/16.
 */
public class XsltTransformer {

  private final Templates template;

  /**
   * Create a transformer for the given XSL transformation.
   * 
   * @param xslt The XSL transformation as a string.
   * @throws TransformerException In case the input could not be parsed or the transformation could
   *         not be set up.
   */
  public XsltTransformer(String xslt) throws TransformerException {
    final Source xsltSource = new StreamSource(new StringReader(xslt));
    this.template = TransformerFactory.newInstance().newTemplates(xsltSource);
  }

  /**
   * Apply the XSLT transformation
   * 
   * @param record The incoming unmapped entity
   * @return The mapped entity to EDM
   * @throws TransformerException In case record could not be parsed or transformed.
   */
  public String transform(String record) throws TransformerException {
    final Source source = new StreamSource(new StringReader(record));
    final StringWriter stringWriter = new StringWriter();
    template.newTransformer().transform(source, new StreamResult(stringWriter));
    return stringWriter.toString();
  }
}
