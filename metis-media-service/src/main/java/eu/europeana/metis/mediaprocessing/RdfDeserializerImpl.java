package eu.europeana.metis.mediaprocessing;

import static eu.europeana.metis.mediaprocessing.RdfXpathConstants.EDM_HAS_VIEW;
import static eu.europeana.metis.mediaprocessing.RdfXpathConstants.EDM_IS_SHOWN_AT;
import static eu.europeana.metis.mediaprocessing.RdfXpathConstants.EDM_IS_SHOWN_BY;
import static eu.europeana.metis.mediaprocessing.RdfXpathConstants.EDM_OBJECT;
import static eu.europeana.metis.mediaprocessing.RdfXpathConstants.EDM_WEBRESOURCE;
import static eu.europeana.metis.mediaprocessing.RdfXpathConstants.SVCS_SERVICE;

import eu.europeana.metis.mediaprocessing.exception.RdfDeserializationException;
import eu.europeana.metis.mediaprocessing.model.EnrichedRdf;
import eu.europeana.metis.mediaprocessing.model.EnrichedRdfImpl;
import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import eu.europeana.metis.mediaprocessing.model.UrlType;
import eu.europeana.metis.schema.convert.RdfConversionUtils;
import eu.europeana.metis.schema.convert.SerializationException;
import eu.europeana.metis.utils.RdfNamespaceContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.xml.XMLConstants;
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

/**
 * This implements RDF deserialization functionality. The code that obtains the individual resources does not assume that we can
 * convert the record to an EDM internal format. Link checking must also run on EDM external. We therefore use XPath expressions
 * to obtain the required data.
 * <p>
 */
class RdfDeserializerImpl implements RdfDeserializer {

  private static final String OEMBED_NAMESPACE = "https://oembed.com/";
  private static final String XPATH_OEMBED_SERVICES =
      SVCS_SERVICE + "[dcterms:conformsTo/@rdf:resource = \"" + OEMBED_NAMESPACE + "\"]";
  private static final String XPATH_OEMBED_WEB_RESOURCES = EDM_WEBRESOURCE
      + "[svcs:has_service/@rdf:resource = " + XPATH_OEMBED_SERVICES + "/@rdf:about]";
  private static final String XPATH_IS_OEMBED_RESOURCE_CONDITION = "[. = "
      + XPATH_OEMBED_WEB_RESOURCES + "/@rdf:about]";
  private static final String OEMBED_XPATH_CONDITION_IS_SHOWN_BY =
      EDM_IS_SHOWN_BY + XPATH_IS_OEMBED_RESOURCE_CONDITION;
  private static final String OEMBED_XPATH_CONDITION_HAS_VIEW =
      EDM_HAS_VIEW + XPATH_IS_OEMBED_RESOURCE_CONDITION;

  private final XPathExpressionWrapper getObjectExpression = new XPathExpressionWrapper(
      xPath -> xPath.compile(EDM_OBJECT));
  private final XPathExpressionWrapper getHasViewExpression = new XPathExpressionWrapper(
      xPath -> xPath.compile(EDM_HAS_VIEW));
  private final XPathExpressionWrapper getIsShownAtExpression = new XPathExpressionWrapper(
      xPath -> xPath.compile(EDM_IS_SHOWN_AT));
  private final XPathExpressionWrapper getIsShownByExpression = new XPathExpressionWrapper(
      xPath -> xPath.compile(EDM_IS_SHOWN_BY));
  private final XPathExpressionWrapper getOEmbedExpression = new XPathExpressionWrapper(
      xPath -> xPath.compile(OEMBED_XPATH_CONDITION_HAS_VIEW + " | " + OEMBED_XPATH_CONDITION_IS_SHOWN_BY));

  private final RdfConversionUtils rdfConversionUtils = new RdfConversionUtils();

  private static List<RdfResourceEntry> convertToResourceEntries(
      Map<String, ResourceInfo> urlWithTypes) {
    return urlWithTypes.entrySet().stream().map(RdfDeserializerImpl::convertToResourceEntry)
                       .toList();
  }

  private static RdfResourceEntry convertToResourceEntry(Map.Entry<String, ResourceInfo> entry) {
    return new RdfResourceEntry(entry.getKey(), entry.getValue().urlTypes(),
        entry.getValue().configuredForOembed());
  }

  private static <R> R performDeserialization(byte[] input, DeserializationOperation<R> operation)
      throws RdfDeserializationException {
    try (InputStream inputStream = new ByteArrayInputStream(input)) {
      return operation.performDeserialization(inputStream);
    } catch (IOException e) {
      throw new RdfDeserializationException("Problem with reading byte array - Shouldn't happen.", e);
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
    return getMainThumbnailResourceForMediaExtraction(deserializeToDocument(inputStream))
        .orElse(null);
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
    final Document deserializedDocument = deserializeToDocument(inputStream);
    final Map<String, ResourceInfo> allResources = getResourceEntries(deserializedDocument,
        UrlType.URL_TYPES_FOR_MEDIA_EXTRACTION);

    // Find the main thumbnail resource if it exists and remove it from the result.
    getMainThumbnailResourceForMediaExtraction(deserializedDocument).map(RdfResourceEntry::getResourceUrl)
                                                                    .ifPresent(allResources::remove);

    // Done.
    return convertToResourceEntries(allResources);
  }

  @Override
  public List<RdfResourceEntry> getResourceEntriesForLinkChecking(byte[] input)
      throws RdfDeserializationException {
    return performDeserialization(input, this::getResourceEntriesForLinkChecking);
  }

  @Override
  public List<RdfResourceEntry> getResourceEntriesForLinkChecking(InputStream inputStream)
      throws RdfDeserializationException {
    return convertToResourceEntries(getResourceEntries(deserializeToDocument(inputStream),
        UrlType.URL_TYPES_FOR_LINK_CHECKING));
  }

  @Override
  public EnrichedRdf getRdfForResourceEnriching(byte[] input) throws RdfDeserializationException {
    return performDeserialization(input, this::getRdfForResourceEnriching);
  }

  @Override
  public EnrichedRdf getRdfForResourceEnriching(InputStream inputStream)
      throws RdfDeserializationException {
    try {
      return new EnrichedRdfImpl(rdfConversionUtils.convertInputStreamToRdf(inputStream));
    } catch (SerializationException e) {
      throw new RdfDeserializationException("Problem with deserializing record to RDF.", e);
    }
  }

  private Optional<RdfResourceEntry> getMainThumbnailResourceForMediaExtraction(Document document)
      throws RdfDeserializationException {

    // Get the entries of the required types.
    final Map<String, ResourceInfo> resourceEntries = getResourceEntries(document,
        Collections.singleton(UrlType.URL_TYPE_FOR_MAIN_THUMBNAIL_RESOURCE));

    // If there is not exactly one, we return an empty optional.
    if (resourceEntries.size() != 1) {
      return Optional.empty();
    }

    // So there is exactly one. Convert and return.
    return Optional.of(convertToResourceEntries(resourceEntries).get(0));
  }

  private Set<String> getUrls(Document document, UrlType type) throws RdfDeserializationException {

    // Determine the right expression to apply.
    final XPathExpressionWrapper expression =
        switch (type) {
          case OBJECT -> getObjectExpression;
          case HAS_VIEW -> getHasViewExpression;
          case IS_SHOWN_AT -> getIsShownAtExpression;
          case IS_SHOWN_BY -> getIsShownByExpression;
        };

    // Evaluate the expression and convert the node list to a set of attribute values.
    final NodeList nodes = expression.evaluate(document);
    return IntStream.range(0, nodes.getLength()).mapToObj(nodes::item).map(Node::getNodeValue)
                    .collect(Collectors.toSet());
  }

  private Document deserializeToDocument(InputStream inputStream) throws RdfDeserializationException {

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

  private Set<String> getOEmbedUrls(Document document) throws RdfDeserializationException {
    final NodeList oEmbedNodes = getOEmbedExpression.evaluate(document);
    return IntStream.range(0, oEmbedNodes.getLength())
                    .mapToObj(oEmbedNodes::item)
                    .map(Node::getNodeValue)
                    .collect(Collectors.toSet());
  }

  @FunctionalInterface
  private interface DeserializationOperation<R> {

    /**
     * Perform deserialization r.
     *
     * @param inputStream the input stream
     * @return the r
     * @throws RdfDeserializationException the rdf deserialization exception
     */
    R performDeserialization(InputStream inputStream) throws RdfDeserializationException;
  }

  private static class XPathExpressionWrapper extends
      AbstractThreadSafeWrapper<XPathExpression, RdfDeserializationException> {

    /**
     * Instantiates a new X path expression wrapper.
     *
     * @param expressionCreator the expression creator
     */
    XPathExpressionWrapper(
        ThrowingFunction<XPath, XPathExpression, XPathExpressionException> expressionCreator) {
      super(() -> {
        final XPathFactory factory;
        synchronized (XPathFactory.class) {
          factory = XPathFactory.newInstance();
        }
        final XPath xPath = factory.newXPath();
        xPath.setNamespaceContext(new RdfNamespaceContext());
        try {
          return expressionCreator.apply(xPath);
        } catch (XPathExpressionException e) {
          throw new RdfDeserializationException("Could not initialize xpath expression.", e);
        }
      });
    }

    /**
     * Evaluate node list.
     *
     * @param document the document
     * @return the node list
     * @throws RdfDeserializationException the rdf deserialization exception
     */
    NodeList evaluate(Document document) throws RdfDeserializationException {
      return process(compiledExpression -> {
        try {
          return (NodeList) compiledExpression.evaluate(document, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
          throw new RdfDeserializationException("Problem with deserializing RDF.", e);
        }
      });
    }
  }
  /**
   * Gets resource entries.
   *
   * @param document the document
   * @param allowedUrlTypes the allowed url types
   * @return the resource entries
   * @throws RdfDeserializationException the rdf deserialization exception
   */
  Map<String, ResourceInfo> getResourceEntries(Document document,
      Set<UrlType> allowedUrlTypes) throws RdfDeserializationException {

    // Get the resources and their types.
    final Map<String, Set<UrlType>> urls = new HashMap<>();
    for (UrlType type : allowedUrlTypes) {
      final Set<String> urlsForType = getUrls(document, type);
      for (String url : urlsForType) {
        urls.computeIfAbsent(url, k -> new HashSet<>()).add(type);
      }
    }

    // For each resource, check whether they are configured for oEmbed.
    final Map<String, ResourceInfo> result = HashMap.newHashMap(urls.size());
    final Set<String> oEmbedUrls = getOEmbedUrls(document);
    for (Entry<String, Set<UrlType>> entry : urls.entrySet()) {
      boolean isConfiguredForOembed = oEmbedUrls.contains(entry.getKey());
      result.put(entry.getKey(), new ResourceInfo(entry.getValue(), isConfiguredForOembed));
    }

    // Done
    return result;
  }

  record ResourceInfo(Set<UrlType> urlTypes, boolean configuredForOembed) {
  }
}
