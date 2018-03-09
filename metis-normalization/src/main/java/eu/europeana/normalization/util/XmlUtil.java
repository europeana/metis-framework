package eu.europeana.normalization.util;

import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Utility methods for working with XML DOMs (org.w3c.dom)
 *
 * @author Nuno Freire (nfreire@gmail.com)
 * @since 23 de Fev de 2011
 */
public class XmlUtil {

  /**
   * Document builder factory
   */
  private static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

  static {
    factory.setNamespaceAware(true);
  }

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
   * @param elementName the name of the child elements
   * @return the first child Element with a given name
   */
  public static Element getElementByTagName(Element n, String elementName) {
    NodeList subNodes = n.getElementsByTagName(elementName);
    int sz = subNodes.getLength();
    if (sz > 0) {
      return (Element) subNodes.item(0);
    }
    return null;
  }

  public static String getElementTextByTagName(Element n, String elementName) {
    Element subEl = getElementByTagName(n, elementName);
    if (subEl != null) {
      return getElementText(subEl);
    }
    return null;
  }

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
   * Creates a DOM from a file representation of an xml record
   *
   * @param reader the xml reader
   * @return the DOM document
   */
  public static Document parseDom(Reader reader) throws XmlException {
    try {
      DocumentBuilder builder = factory.newDocumentBuilder();
      return builder.parse(new org.xml.sax.InputSource(reader));
    } catch (Exception e) {
      throw new XmlException("Could not parse DOM for '" + reader.toString() + "'!", e);
    }
  }

  /**
   * Converts a dom to a String
   *
   * @param dom dom to convert
   * @return the dom as a String
   */
  public static String writeDomToString(Document dom) throws XmlException {
    return writeDomToString(dom, null);
  }

  /**
   * Converts a dom to a String
   *
   * @param dom dom to convert
   * @param outputProperties the properties for the String representation of the XML
   * @return the dom as a String
   */
  private static String writeDomToString(Document dom, Properties outputProperties) throws XmlException {
    return writeNodeToString(dom, outputProperties);
  }

  private static String writeNodeToString(Node dom, Properties outputProperties) throws XmlException {
    try {
      StringWriter ret = new StringWriter();
      TransformerFactory transFact = TransformerFactory.newInstance();
// transFact.setAttribute("indent-number", 2);
      Transformer transformer = transFact.newTransformer();
      if (outputProperties != null) {
        transformer.setOutputProperties(outputProperties);
      }
      DOMSource source = new DOMSource(dom);
      StreamResult result = new StreamResult(ret);
      transformer.transform(source, result);
      return ret.toString();
    } catch (Exception e) {
      throw new XmlException("Could not write dom to string!", e);
    }
  }
}
