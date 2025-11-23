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
import eu.europeana.metis.mediaprocessing.model.RdfResourceKind;
import eu.europeana.metis.mediaprocessing.model.UrlType;
import eu.europeana.metis.schema.convert.RdfConversionUtils;
import eu.europeana.metis.schema.convert.SerializationException;
import eu.europeana.metis.utils.RdfNamespaceContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
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
import org.w3c.dom.Element;
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

  private static final String RDF_NAMESPACE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

  private static final Set<UrlType> OEMBED_SUPPORTED_URL_TYPES = EnumSet
      .of(UrlType.IS_SHOWN_BY, UrlType.HAS_VIEW);
  private static final Set<UrlType> IIIF_SUPPORTED_URL_TYPES = EnumSet
      .of(UrlType.IS_SHOWN_BY, UrlType.HAS_VIEW, UrlType.OBJECT);

  private static final String XPATH_DCTERMS_CONFORMS_TO = "dcterms:conformsTo";
  private static final String XPATH_DCTERMS_IS_FORMAT_OF = "dcterms:isFormatOf";
  private static final String XPATH_RDF_ABOUT = "@rdf:about";
  private static final String XPATH_RDF_RESOURCE = "@rdf:resource";
  private static final String XPATH_SVCS_HAS_SERVICE = "svcs:has_service";

  private static final String IIIF_NAMESPACE = "http://iiif.io/api/image";
  private static final String XPATH_IIIF_SERVICES = SVCS_SERVICE + "[" +
      XPATH_DCTERMS_CONFORMS_TO + "/" + XPATH_RDF_RESOURCE + " = \"" + IIIF_NAMESPACE + "\"]";
  private static final String XPATH_IIIF_SERVICE_REFERENCES = EDM_WEBRESOURCE + "/"
      + XPATH_SVCS_HAS_SERVICE  + "[" +XPATH_RDF_RESOURCE+ " = " + XPATH_IIIF_SERVICES + "/"
      + XPATH_RDF_ABOUT + "]";

  private static final String OEMBED_NAMESPACE = "https://oembed.com/";
  private static final String XPATH_OEMBED_SERVICES = SVCS_SERVICE + "["
      + XPATH_DCTERMS_CONFORMS_TO  + "/" +XPATH_RDF_RESOURCE+ " = \"" + OEMBED_NAMESPACE + "\"]";
  private static final String XPATH_OEMBED_SERVICE_REFERENCES = EDM_WEBRESOURCE + "/"
      + XPATH_SVCS_HAS_SERVICE  + "[" +XPATH_RDF_RESOURCE+ " = " + XPATH_OEMBED_SERVICES + "/"
      + XPATH_RDF_ABOUT + "]";

  private static final String XPATH_DCTERMS_IS_FORMAT_OF_REFERENCES = EDM_WEBRESOURCE + "/"
      + XPATH_DCTERMS_IS_FORMAT_OF + "[" + XPATH_RDF_RESOURCE + "]";

  private final XPathExpressionWrapper getIIIFExpression = new XPathExpressionWrapper(xPath ->
      xPath.compile(XPATH_IIIF_SERVICE_REFERENCES));
  private final XPathExpressionWrapper getOEmbedExpression = new XPathExpressionWrapper(xPath ->
      xPath.compile(XPATH_OEMBED_SERVICE_REFERENCES));
  private final XPathExpressionWrapper getIsFormatOfExpression = new XPathExpressionWrapper(xPath ->
      xPath.compile(XPATH_DCTERMS_IS_FORMAT_OF_REFERENCES));

  private final XPathExpressionWrapper getObjectExpression = new XPathExpressionWrapper(
      xPath -> xPath.compile(EDM_OBJECT));
  private final XPathExpressionWrapper getHasViewExpression = new XPathExpressionWrapper(
      xPath -> xPath.compile(EDM_HAS_VIEW));
  private final XPathExpressionWrapper getIsShownAtExpression = new XPathExpressionWrapper(
      xPath -> xPath.compile(EDM_IS_SHOWN_AT));
  private final XPathExpressionWrapper getIsShownByExpression = new XPathExpressionWrapper(
      xPath -> xPath.compile(EDM_IS_SHOWN_BY));

  private final RdfConversionUtils rdfConversionUtils = new RdfConversionUtils();

  private static List<RdfResourceEntry> convertToResourceEntries(
      Map<String, ResourceInfo> urlWithTypes) {
    return urlWithTypes.entrySet()
                       .stream()
                       .map(RdfDeserializerImpl::convertToResourceEntry)
                       .toList();
  }

  private static RdfResourceEntry convertToResourceEntry(Map.Entry<String, ResourceInfo> entry) {
    return new RdfResourceEntry(entry.getKey(), entry.getValue().urlTypes(),
        entry.getValue().rdfResourceKind(), entry.getValue().svcsHasServiceValue());
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
    getMainThumbnailResourceForMediaExtraction(deserializedDocument)
        .map(RdfResourceEntry::getResourceUrl).ifPresent(allResources::remove);

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
    return Optional.of(convertToResourceEntries(resourceEntries).getFirst());
  }

  private Set<String> getDirectlyReferencedUrlsForType(Document document, UrlType type)
      throws RdfDeserializationException {

    // Determine the right expression to apply.
    final XPathExpressionWrapper expression = switch (type) {
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

  private Map<String, String> getResourceUrls(Document document, XPathExpressionWrapper expression)
      throws RdfDeserializationException {
    final NodeList resultNodes = expression.evaluate(document);
    final Map<String, String> result = new HashMap<>();
    IntStream.range(0, resultNodes.getLength()).mapToObj(resultNodes::item).forEach(node -> {
      final String url = ((Element) node.getParentNode()).getAttributeNS(RDF_NAMESPACE, "about");
      final String serviceReference = ((Element) node).getAttributeNS(RDF_NAMESPACE, "resource");
      result.computeIfAbsent(url, key -> serviceReference);
    });
    return result;
  }

  private Map<String, Set<String>> getIsFormatOfReferences(Document document)
      throws RdfDeserializationException {
    final NodeList resultNodes = getIsFormatOfExpression.evaluate(document);
    final Map<String, Set<String>> result = new HashMap<>();
    IntStream.range(0, resultNodes.getLength()).mapToObj(resultNodes::item).forEach(node -> {
      final String source = ((Element) node.getParentNode()).getAttributeNS(RDF_NAMESPACE, "about");
      final String target = ((Element) node).getAttributeNS(RDF_NAMESPACE, "resource");
      result.computeIfAbsent(source, key -> new HashSet<>()).add(target);
    });
    return result;
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

    // Get the oEmbed and IIIF web resources and their service references. Get isFormatOf relations.
    final Map<String, String> oEmbedUrls = getResourceUrls(document, getOEmbedExpression);
    final Map<String, String> iiifUrls = getResourceUrls(document, getIIIFExpression);
    final Map<String, Set<String>> isFormatOfReferences = getIsFormatOfReferences(document);

    // Get all reachable resources and their types.
    final Map<String, Set<UrlType>> resourcesWithTypes = new HashMap<>();
    for (UrlType type : allowedUrlTypes) {

      // Set up graph walking algorithm - bootstrap with directly linked resources.
      final Set<String> urlsForType = getDirectlyReferencedUrlsForType(document, type);
      final Deque<String> urlsToProcess = new LinkedList<>(urlsForType);

      // Process the list.
      while (!urlsToProcess.isEmpty()) {

        // Get the next url.
        final String url = urlsToProcess.pop();

        // If it is an oembed or iiif URL, check that the type is supported.
        final boolean notSupportedOEmbed = oEmbedUrls.containsKey(url) &&
            !OEMBED_SUPPORTED_URL_TYPES.contains(type);
        final boolean notSupportedIIIF = iiifUrls.containsKey(url) &&
            !IIIF_SUPPORTED_URL_TYPES.contains(type);
        if (notSupportedOEmbed || notSupportedIIIF) {
          continue;
        }

        // Set the type. Propagate if needed by adding any connected URLs to the list.
        boolean typeAdded = resourcesWithTypes
            .computeIfAbsent(url, k -> EnumSet.noneOf(UrlType.class)).add(type);
        if (typeAdded) {
          Optional.ofNullable(isFormatOfReferences.get(url)).ifPresent(urlsToProcess::addAll);
        }
      }
    }

    // Build the result by including the resource kind and service reference.
    final Map<String, ResourceInfo> result = HashMap.newHashMap(resourcesWithTypes.size());
    for (Entry<String, Set<UrlType>> entry : resourcesWithTypes.entrySet()) {
      final RdfResourceKind rdfResourceKind;
      final String svcsHasServiceValue;
      if (oEmbedUrls.containsKey(entry.getKey())) {
        rdfResourceKind = RdfResourceKind.OEMBEDDED;
        svcsHasServiceValue = oEmbedUrls.get(entry.getKey());
      } else if (iiifUrls.containsKey(entry.getKey())) {
        rdfResourceKind = RdfResourceKind.IIIF;
        svcsHasServiceValue = iiifUrls.get(entry.getKey());
      } else {
        rdfResourceKind = RdfResourceKind.STANDARD;
        svcsHasServiceValue = null;
      }
      result.put(entry.getKey(), new ResourceInfo(entry.getValue(), rdfResourceKind, svcsHasServiceValue));
    }

    // Done
    return result;
  }

  record ResourceInfo(Set<UrlType> urlTypes, RdfResourceKind rdfResourceKind, String svcsHasServiceValue) {
  }
}
