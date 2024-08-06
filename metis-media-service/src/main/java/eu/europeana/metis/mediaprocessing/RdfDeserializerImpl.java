package eu.europeana.metis.mediaprocessing;

import eu.europeana.metis.mediaprocessing.exception.RdfDeserializationException;
import eu.europeana.metis.mediaprocessing.model.EnrichedRdf;
import eu.europeana.metis.mediaprocessing.model.EnrichedRdfImpl;
import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import eu.europeana.metis.mediaprocessing.model.UrlType;
import eu.europeana.metis.schema.jibx.RDF;
import eu.europeana.metis.utils.RdfNamespaceContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
 *
 * TODO use {@link eu.europeana.metis.schema.convert.RdfConversionUtils} - no org.jibx.runtime.*
 * import should remain.
 */
class RdfDeserializerImpl implements RdfDeserializer {

  private final UnmarshallingContextWrapper unmarshallingContext = new UnmarshallingContextWrapper();

  private static final String OEMBED_XPATH_CONDITION_IS_SHOWN_BY = "/rdf:RDF/ore:Aggregation/edm:isShownBy/@rdf:resource[/rdf:RDF/ore:Aggregation/edm:isShownBy/@rdf:resource =/rdf:RDF/edm:WebResource[svcs:has_service/@rdf:resource = /rdf:RDF/svcs:Service/@rdf:about and /rdf:RDF/svcs:Service/dcterms:conformsTo/@rdf:resource = \"https://oembed.com/\"]/@rdf:about]";
  private static final String OEMBED_XPATH_CONDITION_HAS_VIEW = "/rdf:RDF/ore:Aggregation/edm:hasView/@rdf:resource[/rdf:RDF/ore:Aggregation/edm:hasView/@rdf:resource=/rdf:RDF/edm:WebResource[svcs:has_service/@rdf:resource = /rdf:RDF/svcs:Service/@rdf:about and /rdf:RDF/svcs:Service/dcterms:conformsTo/@rdf:resource = \"https://oembed.com/\"]/@rdf:about]";

  private final XPathExpressionWrapper getObjectExpression = new XPathExpressionWrapper(
      xPath -> xPath.compile("/rdf:RDF/ore:Aggregation/edm:object/@rdf:resource"));
  private final XPathExpressionWrapper getHasViewExpression = new XPathExpressionWrapper(
      xPath -> xPath.compile("/rdf:RDF/ore:Aggregation/edm:hasView/@rdf:resource | " + OEMBED_XPATH_CONDITION_HAS_VIEW));
  private final XPathExpressionWrapper getIsShownAtExpression = new XPathExpressionWrapper(
      xPath -> xPath.compile("/rdf:RDF/ore:Aggregation/edm:isShownAt/@rdf:resource"));
  private final XPathExpressionWrapper getIsShownByExpression = new XPathExpressionWrapper(
      xPath -> xPath.compile("/rdf:RDF/ore:Aggregation/edm:isShownBy/@rdf:resource | " + OEMBED_XPATH_CONDITION_IS_SHOWN_BY));

  private static class XPathExpressionWrapper extends
          AbstractThreadSafeWrapper<XPathExpression, RdfDeserializationException> {

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

  private static class UnmarshallingContextWrapper extends
          AbstractThreadSafeWrapper<IUnmarshallingContext, RdfDeserializationException> {

    public UnmarshallingContextWrapper() {
      super(() -> {
        try {
          return RdfBindingFactoryProvider.getBindingFactory().createUnmarshallingContext();
        } catch (JiBXException e) {
          throw new RdfDeserializationException("Problem creating deserializer.", e);
        }
      });
    }

    public RDF deserializeToRdf(InputStream inputStream) throws RdfDeserializationException {
      return process(context -> {
        try {
          return (RDF) context.unmarshalDocument(inputStream, "UTF-8");
        } catch (JiBXException e) {
          throw new RdfDeserializationException("Problem with deserializing record to RDF.", e);
        }
      });
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

  private Optional<RdfResourceEntry> getMainThumbnailResourceForMediaExtraction(Document record)
          throws RdfDeserializationException {

    // Get the entries of the required types.
    final Map<String, Set<UrlType>> resourceEntries = getResourceEntries(record,
            Collections.singleton(UrlType.URL_TYPE_FOR_MAIN_THUMBNAIL_RESOURCE));

    // If there is not exactly one, we return an empty optional.
    if (resourceEntries.size() != 1) {
      return Optional.empty();
    }

    // So there is exactly one. Convert and return.
    return Optional.of(convertToResourceEntries(resourceEntries).get(0));
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
    final Map<String, Set<UrlType>> allResources = getResourceEntries(record,
            UrlType.URL_TYPES_FOR_MEDIA_EXTRACTION);

    // Find the main thumbnail resource if it exists and remove it from the result.
    getMainThumbnailResourceForMediaExtraction(record).map(RdfResourceEntry::getResourceUrl)
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

  private static List<RdfResourceEntry> convertToResourceEntries(
          Map<String, Set<UrlType>> urlWithTypes) {
    return urlWithTypes.entrySet().stream().map(RdfDeserializerImpl::convertToResourceEntry)
                       .toList();
  }

  private static RdfResourceEntry convertToResourceEntry(Map.Entry<String, Set<UrlType>> entry) {
    return new RdfResourceEntry(entry.getKey(), entry.getValue());
  }

  Map<String, Set<UrlType>> getResourceEntries(Document document,
          Set<UrlType> allowedUrlTypes) throws RdfDeserializationException {
    final Map<String, Set<UrlType>> urls = new HashMap<>();
    for (UrlType type : allowedUrlTypes) {
      final Set<String> urlsForType = getUrls(document, type);
      for (String url : urlsForType) {
        urls.computeIfAbsent(url, k -> new HashSet<>()).add(type);
      }
    }
    return urls;
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
      // False positive. The parser has all security settings applied (see below).
      @SuppressWarnings("squid:S2755")
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
    return new EnrichedRdfImpl(unmarshallingContext.deserializeToRdf(inputStream));
  }

  private static <R> R performDeserialization(byte[] input, DeserializationOperation<R> operation)
          throws RdfDeserializationException {
    try (InputStream inputStream = new ByteArrayInputStream(input)) {
      return operation.performDeserialization(inputStream);
    } catch (IOException e) {
      throw new RdfDeserializationException("Problem with reading byte array - Shouldn't happen.", e);
    }
  }

  @FunctionalInterface
  private interface DeserializationOperation<R> {

    R performDeserialization(InputStream inputStream) throws RdfDeserializationException;
  }
}
