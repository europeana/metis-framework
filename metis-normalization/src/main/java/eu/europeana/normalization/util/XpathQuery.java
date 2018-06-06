package eu.europeana.normalization.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import eu.europeana.normalization.util.Namespace.Element;

/**
 * This object represents an XPath query, containing an expression and the namespaces that occur in
 * it. This query can be executed on a DOM tree. Internally it uses the
 * {@link javax.xml.xpath.XPath} API.
 * 
 * @author jochen
 *
 */
public final class XpathQuery {

  /** The element rdf:RDF that may be used to create queries. **/
  public static final Element RDF_TAG = Namespace.RDF.getElement("RDF");

  private static final XPathFactory XPATH_FACTORY = XPathFactory.newInstance();

  private final Map<String, String> namespaceMap;
  private final String expression;

  private XpathQuery(Map<String, String> namespaceMap, String expression) {
    this.namespaceMap = namespaceMap;
    this.expression = expression;
  }

  /**
   * This is a convenience method for creating an XPath query. It uses
   * {@link String#format(String, Object...)} to format the given input format to put in the
   * elements.
   * 
   * @param expressionFormat A format string with '%s' placeholders (see
   *        {@link java.util.Formatter}).
   * @param elements The elements corresponding to the placeholders in the expression format.
   * @return An XPath query.
   */
  public static XpathQuery create(String expressionFormat, Element... elements) {
    final String expression = String.format(expressionFormat, (Object[]) elements).trim();
    final Map<String, String> namespaces = Arrays.stream(elements).map(Element::getNamespace)
        .distinct().collect(Collectors.toMap(Namespace::getTagPrefix, Namespace::getUri));
    return new XpathQuery(namespaces, expression);
  }

  /**
   * This is a convenience method for combining XPath queries into one. That means it will match on
   * the union of the two previous queries. It does so by concatenating the expressions using the
   * '|' deliminators and merging the namespace maps.
   * 
   * @param queries The queries to combine.
   * @return A query representing the combination of the input queries.
   * @throws IllegalArgumentException In case the queries could not be combined because the
   *         namespaces conflict (i.e. there are two namespaces with the same prefix but a different
   *         URI).
   */
  public static XpathQuery combine(XpathQuery... queries) {
    final String expression =
        Arrays.stream(queries).map(query -> query.expression).collect(Collectors.joining(" | "));
    final Map<String, String> namespaces = new HashMap<>();
    for (XpathQuery query : queries) {
      for (Entry<String, String> namespace : query.namespaceMap.entrySet()) {
        addNamespace(namespaces, namespace);
      }
    }
    return new XpathQuery(namespaces, expression);
  }

  private static void addNamespace(Map<String, String> namespaces,
      Entry<String, String> namespace) {
    final String currentUri = namespaces.get(namespace.getKey());
    if (currentUri != null && !currentUri.equals(namespace.getValue())) {
      throw new IllegalArgumentException(
          "The same prefix " + namespace.getKey() + " is used for two different namespaces.", null);
    }
    namespaces.put(namespace.getKey(), namespace.getValue());
  }

  private XPathExpression toXPath() throws XPathExpressionException {
    XPath xpath = XPATH_FACTORY.newXPath();
    xpath.setNamespaceContext(new SimpleNamespaceContext());
    return xpath.compile(this.expression);
  }

  /**
   * This method executes the query on a DOM tree.
   * 
   * @param dom The DOM tree on which to execute the query.
   * @return The list of nodes that satisfy the query. Is not null, but could of course be empty.
   * @throws XPathExpressionException In case the expression couldn't be evaluated.
   */
  public NodeList execute(Document dom) throws XPathExpressionException {
    return (NodeList) toXPath().evaluate(dom, XPathConstants.NODESET);
  }

  private class SimpleNamespaceContext implements NamespaceContext {

    @Override
    public String getNamespaceURI(String prefix) {

      // In case prefix is null.
      if (prefix == null) {
        throw new IllegalArgumentException();
      }

      // In case prefix is in map.
      final String resultFromMap = namespaceMap.get(prefix);
      if (resultFromMap != null) {
        return resultFromMap;
      }

      // Other default options.
      final String result;
      switch (prefix) {
        case XMLConstants.XML_NS_PREFIX:
          result = XMLConstants.XML_NS_URI;
          break;
        case XMLConstants.XMLNS_ATTRIBUTE:
          result = XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
          break;
        default:
          result = XMLConstants.NULL_NS_URI;
          break;
      }
      return result;
    }

    @Override
    public String getPrefix(String uri) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<String> getPrefixes(String uri) {
      throw new UnsupportedOperationException();
    }
  }
}
