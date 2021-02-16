package eu.europeana.metis.harvesting.oaipmh;

import eu.europeana.metis.harvesting.HarvesterException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactoryConfigurationException;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.xpath.XPathFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

/**
 * <p>This class provides functionality for parsing a full OAI-PMH record (including header).</p>
 * <p>We are using a non-standard XPatch implementation. The standard one contains some
 * static content that sometimes causes the threading issues. The exception that we encountered and
 * are now avoiding is:
 * <code> javax.xml.xpath.XPathExpressionException: org.xml.sax.SAXException: FWK005 parse may
 * not be called while parsing</code>.</p>
 */
class OaiRecordParser {

  private static final Logger LOGGER = LoggerFactory.getLogger(OaiRecordParser.class);

  private static final String METADATA_XPATH = "/*[local-name()='OAI-PMH']" +
          "/*[local-name()='GetRecord']" +
          "/*[local-name()='record']" +
          "/*[local-name()='metadata']" +
          "/child::*";

  private static final String IS_DELETED_XPATH = "string(/*[local-name()='OAI-PMH']" +
          "/*[local-name()='GetRecord']" +
          "/*[local-name()='record']" +
          "/*[local-name()='header']" +
          "/@status)";

  private static XPathExpression metadataExpression;
  private static XPathExpression isDeletedExpression;

  private final String oaiRecord;

  /**
   * Constructor.
   *
   * @param oaiRecord The record to parse as a string value.
   * @throws HarvesterException In case there was a problem with setting up the parser.
   */
  OaiRecordParser(String oaiRecord) throws HarvesterException {
    this.oaiRecord = oaiRecord;
    try {
      synchronized (OaiHarvesterImpl.class) {
        if (metadataExpression == null || isDeletedExpression == null) {
          final XPathFactoryImpl xpathFactory = new XPathFactoryImpl();
          xpathFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
          final XPath xpath = xpathFactory.newXPath();
          metadataExpression = xpath.compile(METADATA_XPATH);
          isDeletedExpression = xpath.compile(IS_DELETED_XPATH);
        }
      }
    } catch (RuntimeException | XPathExpressionException | XPathFactoryConfigurationException e) {
      LOGGER.error("Exception while compiling the xpath.");
      throw new HarvesterException("Error while compiling the xpath.", e);
    }
  }

  /**
   * Obtain the embedded RDF record
   *
   * @return The embedded RDF record as a stream.
   * @throws HarvesterException in case there is a problem with the expression.
   */
  InputStream getRdfRecord() throws HarvesterException {
    try {
      final InputSource inputSource = getInputSource();
      final ArrayList<NodeInfo> result = (ArrayList<NodeInfo>) metadataExpression
              .evaluate(inputSource, XPathConstants.NODESET);
      return convertToStream(result);
    } catch (XPathExpressionException | TransformerException | RuntimeException e) {
      throw new HarvesterException("Cannot xpath XML!", e);
    }
  }

  /**
   * Find whether the record is marked as deleted.
   *
   * @return whether the method is marked as deleted.
   * @throws HarvesterException in case there is a problem with the expression.
   */
  boolean recordIsDeleted() throws HarvesterException {
    try {
      return "deleted".equalsIgnoreCase(evaluateExpressionAsString(isDeletedExpression));
    } catch (XPathExpressionException e) {
      throw new HarvesterException("Cannot xpath XML!", e);
    }
  }

  private InputSource getInputSource() {
    return new SAXSource(new InputSource(new StringReader(oaiRecord))).getInputSource();
  }

  private String evaluateExpressionAsString(XPathExpression expr) throws XPathExpressionException {
    final InputSource inputSource = getInputSource();
    return expr.evaluate(inputSource);
  }

  private InputStream convertToStream(ArrayList<NodeInfo> nodes)
          throws TransformerException, HarvesterException {
    final int length = nodes.size();
    if (length < 1) {
      throw new HarvesterException("Empty XML!");
    } else if (length > 1) {

      throw new HarvesterException("More than one XML!");
    }
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      Result outputTarget = new StreamResult(outputStream);
      TransformerFactory transformerFactory = new net.sf.saxon.TransformerFactoryImpl();

      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.transform(nodes.get(0), outputTarget);

      return new ByteArrayInputStream(outputStream.toByteArray());
    } catch (IOException e) {
      // Cannot really happen.
      throw new HarvesterException("Unexpected exception", e);
    }
  }
}
