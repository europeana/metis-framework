package eu.europeana.metis.mediaprocessing;

import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.metis.mediaprocessing.exception.RdfConverterException;
import eu.europeana.metis.mediaprocessing.exception.RdfDeserializationException;
import eu.europeana.metis.mediaprocessing.model.EnrichedRdf;
import eu.europeana.metis.mediaprocessing.model.EnrichedRdfImpl;
import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import eu.europeana.metis.mediaprocessing.model.UrlType;
import java.io.ByteArrayInputStream;
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
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This implements RDF deserialization functionality. The code that obtains the individual resources
 * does not assume that we can convert the record to an EDM internal format. Link checking must also
 * run on EDM external. We therefore use XPath expressions to obtain the required data.
 */
class RdfDeserializerImpl implements RdfDeserializer {

  private final IUnmarshallingContext context;

  private final XPathExpression getObjectExpression;
  private final XPathExpression getHasViewExpression;
  private final XPathExpression getIsShownAtExpression;
  private final XPathExpression getIsShownByExpression;

  /**
   * Constructor.
   *
   * @throws RdfConverterException In case something went wrong constructing this object.
   */
  RdfDeserializerImpl() throws RdfConverterException {

    // Create the unmarshalling context
    try {
      context = RdfBindingFactoryProvider.getBindingFactory().createUnmarshallingContext();
    } catch (JiBXException e) {
      throw new RdfConverterException("Problem creating deserializer.", e);
    }

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
  public RdfResourceEntry getMainThumbnailResourceForMediaExtraction(byte[] input)
          throws RdfDeserializationException {
    return performDeserialization(input, this::getMainThumbnailResourceForMediaExtraction);
  }

  @Override
  public RdfResourceEntry getMainThumbnailResourceForMediaExtraction(InputStream inputStream)
          throws RdfDeserializationException {
    return getMainThumbnailResourceForMediaExtraction(deserializeToDocument(inputStream));
  }

  private RdfResourceEntry getMainThumbnailResourceForMediaExtraction(Document record)
          throws RdfDeserializationException {

    // Get the entries of the required types.
    final Map<String, List<UrlType>> resourceEntries = getResourceEntries(record,
            Collections.singleton(UrlType.URL_TYPE_FOR_MAIN_THUMBNAIL_RESOURCE));

    // If there is not exactly one, we return null.
    if (resourceEntries.size() != 1) {
      return null;
    }

    // So there is exactly one. Convert and return.
    return convertToResourceEntries(resourceEntries).get(0);
  }

  @Override
  public List<RdfResourceEntry> getRemainingResourcesForMediaExtraction(byte[] input)
          throws RdfDeserializationException {
    return performDeserialization(input, this::getRemainingResourcesForMediaExtraction);
  }

  @Override
  public List<RdfResourceEntry> getRemainingResourcesForMediaExtraction(InputStream inputStream)
          throws RdfDeserializationException {

    // Get all the resource entries.
    final Document record = deserializeToDocument(inputStream);
    final Map<String, List<UrlType>> allResources = getResourceEntries(record,
            UrlType.URL_TYPES_FOR_MEDIA_EXTRACTION);

    // Find the main thumbnail resource and remove it from the result.
    final RdfResourceEntry toExclude = getMainThumbnailResourceForMediaExtraction(record);
    if (toExclude != null) {
      allResources.remove(toExclude.getResourceUrl());
    }

    // Done.
    return convertToResourceEntries(allResources);
  }

  @Override
  public List<String> getResourceEntriesForLinkChecking(byte[] input)
      throws RdfDeserializationException {
    return performDeserialization(input, this::getResourceEntriesForLinkChecking);
  }

  @Override
  public List<String> getResourceEntriesForLinkChecking(InputStream inputStream)
      throws RdfDeserializationException {
    return new ArrayList<>(
            getResourceEntries(inputStream, UrlType.URL_TYPES_FOR_LINK_CHECKING).keySet());
  }

  private static List<RdfResourceEntry> convertToResourceEntries(
          Map<String, List<UrlType>> urlWithTypes) {
    return urlWithTypes.entrySet().stream().map(RdfDeserializerImpl::convertToResourceEntry)
            .collect(Collectors.toList());
  }

  private static RdfResourceEntry convertToResourceEntry(Map.Entry<String, List<UrlType>> entry) {
    return new RdfResourceEntry(entry.getKey(), entry.getValue());
  }

  Map<String, List<UrlType>> getResourceEntries(InputStream inputStream,
          Set<UrlType> allowedUrlTypes) throws RdfDeserializationException {
    return getResourceEntries(deserializeToDocument(inputStream), allowedUrlTypes);
  }

  private Map<String, List<UrlType>> getResourceEntries(Document document,
          Set<UrlType> allowedUrlTypes) throws RdfDeserializationException {

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

  private Set<String> getUrls(Document document, UrlType type) throws RdfDeserializationException {

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

  private Document deserializeToDocument(InputStream inputStream)
          throws RdfDeserializationException {

    // Parse document to schema-agnostic XML document (but make parsing namespace-aware).
    try {
      final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      factory.setNamespaceAware(true);
      return factory.newDocumentBuilder().parse(inputStream);
    } catch (SAXException | IOException | ParserConfigurationException e) {
      throw new RdfDeserializationException("Problem with deserializing record to XML document.", e);
    }
  }

  @Override
  public EnrichedRdf getRdfForResourceEnriching(byte[] input) throws RdfDeserializationException {
    return performDeserialization(input, this::getRdfForResourceEnriching);
  }

  @Override
  public EnrichedRdf getRdfForResourceEnriching(InputStream inputStream)
          throws RdfDeserializationException {
    return new EnrichedRdfImpl(deserializeToRdf(inputStream));
  }

  private static <R> R performDeserialization(byte[] input, DeserializationOperation<R> operation)
          throws RdfDeserializationException {
    try (InputStream inputStream = new ByteArrayInputStream(input)) {
      return operation.performDeserialization(inputStream);
    } catch (IOException e) {
      throw new RdfDeserializationException("Problem with reading byte array - Shouldn't happen.", e);
    }
  }

  private RDF deserializeToRdf(InputStream inputStream) throws RdfDeserializationException {
    try {
      return (RDF) context.unmarshalDocument(inputStream, "UTF-8");
    } catch (JiBXException e) {
      throw new RdfDeserializationException("Problem with deserializing record to RDF.", e);
    }
  }

  @FunctionalInterface
  private interface DeserializationOperation<R> {

    R performDeserialization(InputStream inputStream) throws RdfDeserializationException;
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
              .orElseGet(Collections::emptyList).iterator();
    }
  }
}
