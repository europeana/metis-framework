package eu.europeana.metis.harvesting.oaipmh;

import eu.europeana.metis.harvesting.HarvesterException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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

import io.gdcc.xoai.xmlio.XmlReader;
import io.gdcc.xoai.xmlio.exceptions.XmlReaderException;
import io.gdcc.xoai.xmlio.matchers.XmlEventMatchers;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.xpath.XPathFactoryImpl;
import io.gdcc.xoai.serviceprovider.parsers.HeaderParser;
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
public class OaiRecordParser {

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
   * @throws HarvesterException In case there was a problem with setting up the parser.
   */
  public OaiRecordParser() throws HarvesterException {
    initializeExpressions();
  }

  /**
   * Obtain the embedded (RDF) record with it's OAI header.
   *
   * @param oaiRecord The record to parse as a string value.
   * @return The record along with the OAI header.
   * @throws HarvesterException in case there is a problem with the expression.
   */
  public OaiRecord parseOaiRecord(byte[] oaiRecord) throws HarvesterException {
    synchronized (OaiRecordParser.class) {
      try {
        return parseOaiRecordInternal(oaiRecord);
      } catch (XPathExpressionException | TransformerException | RuntimeException | XmlReaderException e) {
        throw new HarvesterException("Cannot xpath XML!", e);
      }
    }
  }

  private OaiRecord parseOaiRecordInternal(byte[] oaiRecord)
          throws HarvesterException, TransformerException, XPathExpressionException, XmlReaderException {

    // Read the record header.
    final OaiRecordHeader header;
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      if (!getAsBytes(oaiRecord, headerExpression, outputStream)) {
        throw new HarvesterException("Empty record header!");
      }
      try (ByteArrayInputStream input = new ByteArrayInputStream(outputStream.toByteArray())) {
        final XmlReader xmlReader = new XmlReader(input).next(XmlEventMatchers.anElement());
        header = OaiRecordHeader.convert(new HeaderParser().parse(xmlReader));
      }
    } catch (IOException e) {
      // Cannot really happen.
      throw new HarvesterException("Unexpected exception", e);
    }

    // Read the embedded record.
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      if (!getAsBytes(oaiRecord, metadataExpression, outputStream) && !header.isDeleted()) {
        throw new HarvesterException("Empty (non-deleted) record!");
      }
      return new OaiRecord(header, outputStream::toByteArray);
    } catch (IOException e) {
      // Cannot really happen.
      throw new HarvesterException("Unexpected exception", e);
    }
  }

  private static boolean getAsBytes(byte[] oaiRecord, XPathExpression xPathExpression,
          OutputStream destination)
          throws TransformerException, HarvesterException, XPathExpressionException {

    // Read the nodes from the record.
    final InputSource inputSource = new SAXSource(new InputSource(
            new StringReader(new String(oaiRecord, StandardCharsets.UTF_8)))).getInputSource();
    // Note that this is created safely, so this is a false positive by SonarQube.
    @SuppressWarnings("findsecbugs:XXE_XPATH")
    final ArrayList<NodeInfo> nodes = (ArrayList<NodeInfo>) xPathExpression
            .evaluate(inputSource, XPathConstants.NODESET);
    final int length = nodes.size();
    if (length < 1) {
      return false;
    } else if (length > 1) {
      throw new HarvesterException("More than one XML!");
    }

    // Convert the nodes to bytes.
    final Result outputTarget = new StreamResult(destination);
    final TransformerFactory transformerFactory = new net.sf.saxon.TransformerFactoryImpl();
    final Transformer transformer = transformerFactory.newTransformer();
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.transform(nodes.getFirst(), outputTarget);
    return true;
  }
}
