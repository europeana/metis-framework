package eu.europeana.normalization.util;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
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

  /**
   * Document builder factory
   */
  private static final DocumentBuilderFactory FACTORY = DocumentBuilderFactory.newInstance();

  static {
    FACTORY.setNamespaceAware(true);
  }

  private XmlUtil() {}

  /**
   * An iterable for all the Element childs of a node
   *
   * @param n the node get the children from
   * @return An iterable for all the Element childs of a node
   */
  public static Iterable<Element> elements(Element n) {
    int sz = n.getChildNodes().getLength();
    ArrayList<Element> elements = new ArrayList<>(sz);
    for (int idx = 0; idx < sz; idx++) {
      Node node = n.getChildNodes().item(idx);
      if (node instanceof Element) {
        elements.add((Element) node);
      }
    }
    return elements;
  }

  /**
   * An Iterable for the Element's childs, with a particular name, of a node
   *
   * @param n the node get the children from
   * @param elementName the name of the child elements
   * @return An Iterable for the Element's children, with a particular name, of a node
   */
  public static Iterable<Element> elements(Element n, String elementName) {
    NodeList subNodes = n.getElementsByTagName(elementName);
    int sz = subNodes.getLength();
    ArrayList<Element> elements = new ArrayList<>(sz);
    for (int idx = 0; idx < sz; idx++) {
      Node node = subNodes.item(idx);
      elements.add((Element) node);
    }
    return elements;
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

  /**
   * Gets the text content of the first child Element with a given name
   *
   * @param n the node get the text from
   * @param elementName the name of the child element
   * @return The text in the various text child nodes, concatenated.
   */
  public static String getElementTextByTagName(Element n, String elementName) {
    final Element subEl = getElementByTagName(n, elementName);
    return subEl != null ? getElementText(subEl) : null;
  }

  /**
   * Gets the text content of the given element.
   *
   * @param n the node get the text from
   * @return The text in the various text child nodes, concatenated.
   */
  public static String getElementText(Element n) {
    NodeList childNodes = n.getChildNodes();
    StringBuilder result = new StringBuilder();
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
    return writeDomToString(dom, null);
  }

  /**
   * Serialize a DOM tree to an XML string.
   *
   * @param dom The DOM tree to convert
   * @param outputProperties the properties for the String representation of the XML. Can be null.
   * @return A string containing the XML representation of the DOM tree.
   * @throws XmlException In case something went wrong while performing the transformation.
   */
  private static String writeDomToString(Document dom, Properties outputProperties)
      throws XmlException {
    try {
      StringWriter ret = new StringWriter();
      TransformerFactory transFact = TransformerFactory.newInstance();
      Transformer transformer = transFact.newTransformer();
      if (outputProperties != null) {
        transformer.setOutputProperties(outputProperties);
      }
      DOMSource source = new DOMSource(dom);
      StreamResult result = new StreamResult(ret);
      transformer.transform(source, result);
      return ret.toString();
    } catch (RuntimeException | TransformerException e) {
      throw new XmlException("Could not write dom to string!", e);
    }
  }
}
