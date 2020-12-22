package eu.europeana.normalization.util;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Utility methods for working with XML DOMs (org.w3c.dom)
 *
 * @author Nuno Freire (nfreire@gmail.com)
 */
public final class XmlUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(XmlUtil.class);
  private static final DocumentBuilderFactory FACTORY = DocumentBuilderFactory.newInstance();

  static {
    try {
      FACTORY.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      FACTORY.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      FACTORY.setNamespaceAware(true);
    } catch (ParserConfigurationException e) {
      LOGGER.error("Could not initialize static block XmlUtil", e);
    }
  }

  private XmlUtil() {
  }

  /**
   * Add the prefix to the node name and returns the result.
   *
   * @param nodeName The name of the node.
   * @param prefix The prefix.
   * @return The name with the prefix.
   */
  public static String addPrefixToNodeName(String nodeName, String prefix) {
    return prefix == null ? nodeName : String.format("%s:%s", prefix, nodeName);
  }

  /**
   * Returns a list for all the Element childs of a node
   *
   * @param n the node get the children from
   * @return The list
   */
  public static List<Element> elements(Element n) {
    return getAsElementList(n.getChildNodes());
  }

  /**
   * Returns a list for the Element's childs, with a particular name, of a node
   *
   * @param n the node get the children from
   * @param elementName the name of the child elements
   * @return The list.
   */
  public static List<Element> elements(Element n, String elementName) {
    return getAsElementList(n.getElementsByTagName(elementName));
  }

  /**
   * Convert the given instance of {@link NodeList} to an instance of {@link List} with {@link
   * Element} objects. Entries in the original list that are not instances of {@link Element} will
   * be ignored.
   *
   * @param nodeList The list of nodes to convert.
   * @return The converted list.
   */
  public static List<Element> getAsElementList(NodeList nodeList) {
    return IntStream.range(0, nodeList.getLength()).mapToObj(nodeList::item)
        .filter(node -> (node instanceof Element)).map(node -> (Element) node)
        .collect(Collectors.toList());
  }

  /**
   * Gets the first child Element with a given name
   *
   * @param n the node get the children from
   * @param elementName the name of the child element
   * @return the first child Element with a given name
   */
  public static Element getElementByTagName(Element n, String elementName) {
    return (Element) n.getElementsByTagName(elementName).item(0);
  }

  public static Element getLastElementByTagName(Element n, String elementName) {
    final NodeList elementsByTagName = n.getElementsByTagName(elementName);
    return elementsByTagName.getLength() == 0 ? null
        : (Element) elementsByTagName.item(elementsByTagName.getLength() - 1);
  }

  /**
   * Gets the text content of the first child Element with a given name
   *
   * @param n the node get the text from
   * @param elementName the name of the child element
   * @return The text in the various text child nodes, concatenated.
   */
  public static String getElementTextByTagName(Element n, String elementName) {
    final Element subEl = getElementByTagName(n, elementName);
    return subEl == null ? null : getElementText(subEl);
  }

  /**
   * Gets the text content of the given element.
   *
   * @param element the element get the text from
   * @return The text in the various text child nodes, concatenated. Is not null, but can be empty.
   */
  public static String getElementText(Element element) {
    final NodeList childNodes = element.getChildNodes();
    final StringBuilder result = new StringBuilder();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node node = childNodes.item(i);
      if (node.getNodeType() == Node.TEXT_NODE) {
        result.append(node.getNodeValue());
      }
    }
    return result.toString();
  }

  /**
   * Deserialize input into a DOM tree.
   *
   * @param reader the reader containing the input.
   * @return the DOM document
   * @throws XmlException In case of parsing or IO errors.
   */
  public static Document parseDom(Reader reader) throws XmlException {
    try {
      DocumentBuilder builder = FACTORY.newDocumentBuilder();
      return builder.parse(new org.xml.sax.InputSource(reader));
    } catch (ParserConfigurationException | SAXException | IOException e) {
      throw new XmlException("Could not parse DOM for '" + reader.toString() + "'!", e);
    }
  }

  /**
   * Serialize a DOM tree to an XML string.
   *
   * @param dom The DOM tree to convert
   * @return A string containing the XML representation of the DOM tree.
   * @throws XmlException In case something went wrong while performing the transformation.
   */
  public static String writeDomToString(Document dom) throws XmlException {
    final StringWriter ret = new StringWriter();
    writeDom(dom, ret);
    return ret.toString();
  }

  /**
   * Serialize a DOM tree to an XML byte array.
   *
   * @param dom The DOM tree to convert
   * @return A string containing the XML representation of the DOM tree.
   * @throws XmlException In case something went wrong while performing the transformation.
   */
  public static byte[] writeDomToByteArray(Document dom) throws XmlException {
    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    final Writer writer= new BufferedWriter(new OutputStreamWriter(outputStream));
    writeDom(dom, writer);
    return outputStream.toByteArray();
  }

  private static void writeDom(Document dom, Writer writer) throws XmlException {
    try {
      final TransformerFactory transformerFactory = TransformerFactory.newInstance();
      transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
      transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
      final Transformer transformer = transformerFactory.newTransformer();
      final DOMSource source = new DOMSource(dom);
      final StreamResult result = new StreamResult(writer);
      transformer.transform(source, result);
    } catch (RuntimeException | TransformerException e) {
      throw new XmlException("Could not write dom to string!", e);
    }
  }
}
