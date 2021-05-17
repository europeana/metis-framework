package eu.europeana.metis.harvesting.oaipmh;

import com.lyncode.xml.XmlReader;
import com.lyncode.xml.exceptions.XmlReaderException;
import com.lyncode.xml.matchers.XmlEventMatchers;
import eu.europeana.metis.harvesting.HarvesterException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
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
import org.dspace.xoai.serviceprovider.parsers.HeaderParser;
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

  private static final String HEADER_XPATH = "/*[local-name()='OAI-PMH']" +
          "/*[local-name()='GetRecord']" +
          "/*[local-name()='record']" +
          "/*[local-name()='header']";

  private static XPathExpression metadataExpression;
  private static XPathExpression headerExpression;

  private final byte[] oaiRecord;

  private static void initializeExpressions() throws HarvesterException {
    try {
      synchronized (OaiRecordParser.class) {
        if (metadataExpression == null || headerExpression == null) {
          final XPathFactoryImpl xpathFactory = new XPathFactoryImpl();
          xpathFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
          final XPath xpath = xpathFactory.newXPath();
          metadataExpression = xpath.compile(METADATA_XPATH);
          headerExpression = xpath.compile(HEADER_XPATH);
        }
      }
    } catch (RuntimeException | XPathExpressionException | XPathFactoryConfigurationException e) {
      LOGGER.error("Exception while compiling the xpath.");
      throw new HarvesterException("Error while compiling the xpath.", e);
    }
  }

  /**
   * Constructor.
   *
   * @param oaiRecord The record to parse as a string value.
   * @throws HarvesterException In case there was a problem with setting up the parser.
   */
  OaiRecordParser(byte[] oaiRecord) throws HarvesterException {
    initializeExpressions();
    this.oaiRecord = oaiRecord;
  }

  /**
   * Obtain the embedded (RDF) record with it's OAI header.
   *
   * @return The record along with the OAI header.
   * @throws HarvesterException in case there is a problem with the expression.
   */
  OaiRecord getOaiRecord() throws HarvesterException {
    try {
      final byte[] headerBytes = getAsBytes(headerExpression);
      if (headerBytes.length == 0) {
        throw new HarvesterException("Empty record header!");
      }
      final XmlReader xmlReader = new XmlReader(new ByteArrayInputStream(headerBytes))
              .next(XmlEventMatchers.anElement());
      final OaiRecordHeader header = OaiRecordHeader.convert(new HeaderParser().parse(xmlReader));
      final byte[] record = getAsBytes(metadataExpression);
      if (record.length == 0 && !header.isDeleted()) {
        throw new HarvesterException("Empty (non-deleted) record!");
      }
      return new OaiRecord(header, record);
    } catch (XPathExpressionException | TransformerException | RuntimeException | XmlReaderException e) {
      throw new HarvesterException("Cannot xpath XML!", e);
    }
  }

  private byte[] getAsBytes(XPathExpression xPathExpression)
          throws TransformerException, HarvesterException, XPathExpressionException {
    final InputSource inputSource = new SAXSource(new InputSource(
            new StringReader(new String(oaiRecord, StandardCharsets.UTF_8)))).getInputSource();
    // Note that this is created safely, so this is a false positive by SonarQube.
    @SuppressWarnings("findsecbugs:XXE_XPATH")
    final ArrayList<NodeInfo> nodes = (ArrayList<NodeInfo>) xPathExpression
            .evaluate(inputSource, XPathConstants.NODESET);
    final int length = nodes.size();
    if (length < 1) {
      return new byte[0];
    } else if (length > 1) {
      throw new HarvesterException("More than one XML!");
    }
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      Result outputTarget = new StreamResult(outputStream);
      TransformerFactory transformerFactory = new net.sf.saxon.TransformerFactoryImpl();

      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.transform(nodes.get(0), outputTarget);

      return outputStream.toByteArray();
    } catch (IOException e) {
      // Cannot really happen.
      throw new HarvesterException("Unexpected exception", e);
    }
  }
}
