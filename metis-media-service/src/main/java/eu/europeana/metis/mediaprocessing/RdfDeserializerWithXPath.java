package eu.europeana.metis.mediaprocessing;

import eu.europeana.metis.mediaprocessing.exception.RdfConverterException;
import eu.europeana.metis.mediaprocessing.exception.RdfDeserializationException;
import eu.europeana.metis.mediaprocessing.model.UrlType;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

class RdfDeserializerWithXPath extends RdfDeserializerImpl {

  private final XPathExpression getObjectExpression;
  private final XPathExpression getHasViewExpression;
  private final XPathExpression getIsShownAtExpression;
  private final XPathExpression getIsShownByExpression;

  RdfDeserializerWithXPath() throws RdfConverterException {

    // Create xpath engine and set namespace context.
    final XPath xPath = XPathFactory.newInstance().newXPath();
    xPath.setNamespaceContext(new RdfNamespaceContext());

    // Create the various expressions we will need.
    try {
      getObjectExpression = xPath.compile("/rdf:RDF/ore:Aggregation/edm:object/@rdf:resource");
      getHasViewExpression = xPath.compile("/rdf:RDF/ore:Aggregation/edm:hasView/@rdf:resource");
      getIsShownAtExpression = xPath
          .compile("/rdf:RDF/ore:Aggregation/edm:isShownAt/@rdf:resource");
      getIsShownByExpression = xPath
          .compile("/rdf:RDF/ore:Aggregation/edm:isShownBy/@rdf:resource");
    } catch (XPathExpressionException e) {
      throw new RdfConverterException("Could not initialize xpath expressions.", e);
    }
  }

  @Override
  Map<String, List<UrlType>> getResourceEntries(InputStream inputStream,
      Set<UrlType> allowedUrlTypes) throws RdfDeserializationException {

    // Parse document to schema-agnostic XML document (but make parsing namespace-aware).
    final Document document;
    try {
      final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      document = factory.newDocumentBuilder().parse(inputStream);
    } catch (SAXException | IOException | ParserConfigurationException e) {
      throw new RdfDeserializationException("Problem with deserializing RDF.", e);
    }

    // Go by all types and add all urls with the given type.
    final Map<String, List<UrlType>> urls = new HashMap<>();
    Function<String, List<UrlType>> listProd = k -> new ArrayList<>();
    for (UrlType type : allowedUrlTypes) {
      final Set<String> urlsForType = getUrls(document, type);
      for (String url : urlsForType) {
        urls.computeIfAbsent(url, listProd).add(type);
      }
    }

    // Done.
    return urls;
  }

  private  Set<String> getUrls(Document document, UrlType type) throws RdfDeserializationException {

    // Determine the right expression to apply.
    final XPathExpression expression;
    switch (type) {
      case OBJECT:
        expression = getObjectExpression;
        break;
      case HAS_VIEW:
        expression = getHasViewExpression;
        break;
      case IS_SHOWN_AT:
        expression = getIsShownAtExpression;
        break;
      case IS_SHOWN_BY:
        expression = getIsShownByExpression;
        break;
      default:
        return Collections.emptySet();
    }

    // Get all matching attributes in a node list.
    final NodeList nodes;
    try {
      nodes = (NodeList) expression.evaluate(document, XPathConstants.NODESET);
    } catch (XPathExpressionException e) {
      throw new RdfDeserializationException("Problem with deserializing RDF.", e);
    }

    // Convert the node list to a set of attribute values.
    return IntStream.range(0, nodes.getLength()).mapToObj(nodes::item).map(Node::getNodeValue)
        .collect(Collectors.toSet());
  }

  private static class RdfNamespaceContext implements NamespaceContext {

    private static final Map<String, String> PREFIX_TO_NAMESPACE_MAP = new HashMap<>();

    static {
      PREFIX_TO_NAMESPACE_MAP.put(XMLConstants.DEFAULT_NS_PREFIX, XMLConstants.NULL_NS_URI);
      PREFIX_TO_NAMESPACE_MAP.put(XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI);
      PREFIX_TO_NAMESPACE_MAP
          .put(XMLConstants.XMLNS_ATTRIBUTE, XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
      PREFIX_TO_NAMESPACE_MAP.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
      PREFIX_TO_NAMESPACE_MAP.put("ore", "http://www.openarchives.org/ore/terms/");
      PREFIX_TO_NAMESPACE_MAP.put("edm", "http://www.europeana.eu/schemas/edm/");
    }

    @Override
    public String getNamespaceURI(String s) {
      if (s == null) {
        throw new IllegalArgumentException();
      }
      return Optional.ofNullable(PREFIX_TO_NAMESPACE_MAP.get(s)).orElse(XMLConstants.NULL_NS_URI);
    }

    @Override
    public String getPrefix(String s) {
      if (s == null) {
        throw new IllegalArgumentException();
      }
      return PREFIX_TO_NAMESPACE_MAP.entrySet().stream().filter(entry -> entry.getValue().equals(s))
          .map(Entry::getKey).findAny().orElse(null);
    }

    @Override
    public Iterator<String> getPrefixes(String s) {
      return Optional.ofNullable(getPrefix(s)).map(Collections::singletonList)
          .orElse(Collections.emptyList()).iterator();
    }
  }
}
