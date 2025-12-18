package eu.europeana.metis.mediaprocessing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.metis.mediaprocessing.RdfDeserializerImpl.ResourceInfo;
import eu.europeana.metis.mediaprocessing.exception.RdfDeserializationException;
import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import eu.europeana.metis.mediaprocessing.model.RdfResourceKind;
import eu.europeana.metis.mediaprocessing.model.UrlType;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class RdfDeserializerImplTest {

  private static final String RDF_NAMESPACE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
  private static final String ORE_NAMESPACE = "http://www.openarchives.org/ore/terms/";
  private static final String EDM_NAMESPACE = "http://www.europeana.eu/schemas/edm/";
  private static final String SVCS_NAMESPACE = "http://rdfs.org/sioc/services#";
  private static final String DCTERMS_NAMESPACE = "http://purl.org/dc/terms/";

  private static String addEdmOEmbedResourceType(Document document, Element aggregation, String typeName, String resourceValue) {
    final Element object = document.createElementNS(EDM_NAMESPACE, typeName);
    object.setAttributeNS(RDF_NAMESPACE, "resource", resourceValue);
    aggregation.appendChild(object);
    final Element webResource = document.createElementNS(EDM_NAMESPACE, "WebResource");
    webResource.setAttributeNS(RDF_NAMESPACE, "about", resourceValue);
    final Element hasService = document.createElementNS(SVCS_NAMESPACE, "has_service");
    final String oEmbedResourceService = "http://resource/services/oembed/";
    hasService.setAttributeNS(RDF_NAMESPACE, "resource", oEmbedResourceService);
    webResource.appendChild(hasService);
    object.setAttributeNS(RDF_NAMESPACE, "resource", resourceValue);
    final Element service = document.createElementNS(SVCS_NAMESPACE, "Service");
    service.setAttributeNS(RDF_NAMESPACE, "about", oEmbedResourceService);
    final Element conformsTo = document.createElementNS(DCTERMS_NAMESPACE, "conformsTo");
    conformsTo.setAttributeNS(RDF_NAMESPACE,"resource", "https://oembed.com/");
    service.appendChild(conformsTo);
    object.getParentNode().getParentNode().appendChild(webResource);
    object.getParentNode().getParentNode().appendChild(service);
    return resourceValue;
  }

  private static String addEdmResourceType(Document document, Element aggregation, String typeName,
      String resourceValue) {
    final Element object = document.createElementNS(EDM_NAMESPACE, typeName);
    object.setAttributeNS(RDF_NAMESPACE, "resource", resourceValue);
    aggregation.appendChild(object);
    return resourceValue;
  }

  private static String addEdmObject(Document document, Element aggregation, String resourceValue) {
    return addEdmResourceType(document, aggregation, "object", resourceValue);
  }

  private static String addEdmHasView(Document document, Element aggregation,
      String resourceValue) {
    return addEdmResourceType(document, aggregation, "hasView", resourceValue);
  }

  private static String addEdmIsShownBy(Document document, Element aggregation,
      String resourceValue) {
    return addEdmResourceType(document, aggregation, "isShownBy", resourceValue);
  }

  private static String addEdmIsShownAt(Document document, Element aggregation,
      String resourceValue) {
    return addEdmResourceType(document, aggregation, "isShownAt", resourceValue);
  }

  @Test
  void testGetResourceUrlsWithDifferentResources()
      throws RdfDeserializationException, ParserConfigurationException {

    // Create document with root rdf
    final Document document = DocumentBuilderFactory.newInstance()
                                                    .newDocumentBuilder()
                                                    .newDocument();
    final Element rdf = document.createElementNS(RDF_NAMESPACE, "RDF");
    document.appendChild(rdf);

    // Add one aggregation with some links
    final Element aggregation1 = document.createElementNS(ORE_NAMESPACE, "Aggregation");
    rdf.appendChild(aggregation1);
    final String object = addEdmObject(document, aggregation1, "object resource");
    final String hasView1 = addEdmHasView(document, aggregation1, "has view resource 1");
    final String hasView2 = addEdmHasView(document, aggregation1, "has view resource 2");
    final String isShownBy1 = addEdmIsShownBy(document, aggregation1, "is shown by resource 1");

    // Add another aggregation with some more links
    final Element aggregation2 = document.createElementNS(ORE_NAMESPACE, "Aggregation");
    rdf.appendChild(aggregation2);
    final String isShownBy2 = addEdmIsShownBy(document, aggregation2, "is shown by resource 2");
    final String isShownAt = addEdmIsShownAt(document, aggregation2, "is shown at resource");

    // Test method for all url types
    final Map<String, ResourceInfo> resultAllTypes = new RdfDeserializerImpl().getResourceEntries(document);
    assertEquals(6, resultAllTypes.size());
    assertEquals(Collections.singleton(UrlType.OBJECT), resultAllTypes.get(object).urlTypes());
    assertEquals(Collections.singleton(UrlType.HAS_VIEW), resultAllTypes.get(hasView1).urlTypes());
    assertEquals(Collections.singleton(UrlType.HAS_VIEW), resultAllTypes.get(hasView2).urlTypes());
    assertEquals(Collections.singleton(UrlType.IS_SHOWN_BY), resultAllTypes.get(isShownBy1).urlTypes());
    assertEquals(Collections.singleton(UrlType.IS_SHOWN_BY), resultAllTypes.get(isShownBy2).urlTypes());
    assertEquals(Collections.singleton(UrlType.IS_SHOWN_AT), resultAllTypes.get(isShownAt).urlTypes());
  }

  @Test
  void testGetResourceUrlsWithSameResources()
      throws RdfDeserializationException, ParserConfigurationException {

    // Create document with root rdf
    final Document document = DocumentBuilderFactory.newInstance()
                                                    .newDocumentBuilder()
                                                    .newDocument();
    final Element rdf = document.createElementNS(RDF_NAMESPACE, "RDF");
    document.appendChild(rdf);
    final String commonResource = "common resource";

    // Add one aggregation with the same link multiple times
    final Element aggregation1 = document.createElementNS(ORE_NAMESPACE, "Aggregation");
    rdf.appendChild(aggregation1);
    addEdmObject(document, aggregation1, commonResource);
    addEdmHasView(document, aggregation1, commonResource);
    addEdmIsShownBy(document, aggregation1, commonResource);

    // Add another aggregation with the same link
    final Element aggregation2 = document.createElementNS(ORE_NAMESPACE, "Aggregation");
    rdf.appendChild(aggregation2);
    addEdmIsShownAt(document, aggregation2, commonResource);

    // Test method for all url types
    final Map<String, ResourceInfo> resultAllTypes = new RdfDeserializerImpl().getResourceEntries(document);
    assertEquals(1, resultAllTypes.size());
    assertEquals(Set.of(UrlType.values()), resultAllTypes.get(commonResource).urlTypes());
  }

  @Test
  void testGetResourceUrlsFromOEmbedCondition()
      throws RdfDeserializationException, ParserConfigurationException {

    // given Create document with root rdf
    final Document document = DocumentBuilderFactory.newInstance()
                                                    .newDocumentBuilder()
                                                    .newDocument();
    final Element rdf = document.createElementNS(RDF_NAMESPACE, "RDF");
    document.appendChild(rdf);
    final Element aggregation1 = document.createElementNS(ORE_NAMESPACE, "Aggregation");
    rdf.appendChild(aggregation1);
    final String hasView = addEdmOEmbedResourceType(document, aggregation1, "hasView", "has view resource");
    final String isShownBy = addEdmOEmbedResourceType(document, aggregation1, "isShownBy", "is shown by resource");

    // when test object extraction
    final Map<String, ResourceInfo> resultAllTypes = new RdfDeserializerImpl().getResourceEntries(document);

    // then check the oEmbedResources where successfully identified.
    assertEquals(2, resultAllTypes.size());
    assertEquals(Collections.singleton(UrlType.HAS_VIEW), resultAllTypes.get(hasView).urlTypes());
    assertEquals(RdfResourceKind.OEMBEDDED, resultAllTypes.get(hasView).rdfResourceKind());
    assertEquals(Collections.singleton(UrlType.IS_SHOWN_BY), resultAllTypes.get(isShownBy).urlTypes());
    assertEquals(RdfResourceKind.OEMBEDDED, resultAllTypes.get(isShownBy).rdfResourceKind());
  }

  @Test
  void testGetOEmbeddableObjectsFromSample_MatchingService() throws RdfDeserializationException {
    // given
    final InputStream inputStream = getClass().getClassLoader().getResourceAsStream("__files/rdf_with_oembed_sample.xml");

    // when
    final List<RdfResourceEntry> rdfResourceEntry = new RdfDeserializerImpl().getRemainingResourcesForMediaExtraction(
        inputStream);

    // then
    assertEquals(2, rdfResourceEntry.size());
    assertTrue(rdfResourceEntry.stream().anyMatch(r -> r.getResourceUrl().equals(
        "https://vimeo.com/api/oembed.json?url=https%3A%2F%2Fcdn.pixabay.com%2Fvideo%2F2023%2F10%2F22%2F186070-876973719_small.mp4")
        && RdfResourceKind.OEMBEDDED.equals(r.getResourceKind())));
    assertTrue(rdfResourceEntry.stream().anyMatch(r -> r.getResourceUrl().equals(
        "http://www.flickr.com/services/oembed/?url=https%3A%2F%2Fwww.flickr.com%2Fphotos%2Fbees%2F2341623661%2F&format=json")
        && RdfResourceKind.OEMBEDDED.equals(r.getResourceKind())));
  }

  @Test
  void testGetOEmbeddableObjectsFromSample_NoMatchingService() throws RdfDeserializationException {
    // given
    final InputStream inputStream = getClass().getClassLoader().getResourceAsStream("__files/rdf_with_oembed_sample_II.xml");

    // when
    final List<RdfResourceEntry> rdfResourceEntry = new RdfDeserializerImpl().getRemainingResourcesForMediaExtraction(
        inputStream);

    // then
    assertEquals(3, rdfResourceEntry.size());
    assertTrue(rdfResourceEntry.stream().anyMatch(r -> r.getResourceUrl()
        .equals("https://vimeo.com/api/oembed.json?url=https%3A%2F%2Fvimeo.com%2F42947250")
        && !RdfResourceKind.OEMBEDDED.equals(r.getResourceKind())));
    assertTrue(rdfResourceEntry.stream().anyMatch(r -> r.getResourceUrl()
        .equals("https://vimeo.com/api/oembed.json?url=https%3A%2F%2Fvimeo.com%2F24416915")
        && RdfResourceKind.OEMBEDDED.equals(r.getResourceKind())));
    assertTrue(rdfResourceEntry.stream().anyMatch(r -> r.getResourceUrl()
        .equals("http://www.cmcassociates.co.uk/Skara_Brae/landing/sb_pass_pano.html")
        && !RdfResourceKind.OEMBEDDED.equals(r.getResourceKind())));
    assertTrue(rdfResourceEntry.stream().noneMatch(r -> r.getResourceUrl()
        .equals("https://vimeo.com/api/oembed.json?url=https%3A%2F%2Fvimeo.com%2F24416915")
        && r.getUrlTypes().contains(UrlType.OBJECT)));
  }

  @Test
  void testGetIIIFObjectFromSample_MatchingService() throws RdfDeserializationException {
    // given
    final InputStream inputStream = getClass().getClassLoader().getResourceAsStream("__files/rdf_with_iiif_sample.xml");

    // when
    final List<RdfResourceEntry> rdfResourceEntry = new RdfDeserializerImpl().getRemainingResourcesForMediaExtraction(
        inputStream);

    // then
    assertEquals(4, rdfResourceEntry.size());
    assertTrue(rdfResourceEntry.stream().anyMatch(r -> r.getResourceUrl().equals(
        "https://stacks.stanford.edu/image/iiif/zw031pj2507/zw031pj2507_0001/full/full/0/default.jpg")
        && r.getUrlTypes().contains(UrlType.IS_SHOWN_BY)
        && RdfResourceKind.IIIF.equals(r.getResourceKind())));
    assertTrue(rdfResourceEntry.stream().anyMatch(r -> r.getResourceUrl().equals(
        "https://stacks.stanford.edu/image/iiif/zw031pj2507/zw031pj2507_0002/full/full/0/default.jpg")
        && r.getUrlTypes().contains(UrlType.HAS_VIEW)
        && RdfResourceKind.IIIF.equals(r.getResourceKind())));
    assertTrue(rdfResourceEntry.stream().anyMatch(r -> r.getResourceUrl().equals(
        "https://purl.stanford.edu/zw031pj2507")
        && r.getUrlTypes().contains(UrlType.IS_SHOWN_AT)
        && RdfResourceKind.STANDARD.equals(r.getResourceKind())));
    assertTrue(rdfResourceEntry.stream().anyMatch(r -> r.getResourceUrl().equals(
        "https://stacks.stanford.edu/file/zw031pj2507/zw031pj2507_0002.xml")
        && r.getUrlTypes().contains(UrlType.HAS_VIEW)
        && RdfResourceKind.STANDARD.equals(r.getResourceKind())));
  }

  @Test
  void testGetIIIFObjectFromSample_NoMatchingService() throws RdfDeserializationException {
    // given
    final InputStream inputStream = getClass().getClassLoader().getResourceAsStream("__files/rdf_with_iiif_sample_II.xml");

    // when
    final List<RdfResourceEntry> rdfResourceEntry = new RdfDeserializerImpl().getRemainingResourcesForMediaExtraction(
        inputStream);

    // then
    assertEquals(4, rdfResourceEntry.size());
    // this is not detected due no matching service
    assertTrue(rdfResourceEntry.stream().anyMatch(r -> r.getResourceUrl().equals(
        "https://stacks.stanford.edu/image/iiif/zw031pj2507/zw031pj2507_0001/full/full/0/default.jpg")
        && r.getUrlTypes().contains(UrlType.IS_SHOWN_BY)
        && RdfResourceKind.STANDARD.equals(r.getResourceKind())));
    assertTrue(rdfResourceEntry.stream().anyMatch(r -> r.getResourceUrl().equals(
        "https://stacks.stanford.edu/image/iiif/zw031pj2507/zw031pj2507_0002/full/full/0/default.jpg")
        && r.getUrlTypes().contains(UrlType.HAS_VIEW)
        && RdfResourceKind.IIIF.equals(r.getResourceKind())));
    assertTrue(rdfResourceEntry.stream().anyMatch(r -> r.getResourceUrl().equals(
        "https://purl.stanford.edu/zw031pj2507")
        && r.getUrlTypes().contains(UrlType.IS_SHOWN_AT)
        && RdfResourceKind.STANDARD.equals(r.getResourceKind())));
    assertTrue(rdfResourceEntry.stream().anyMatch(r -> r.getResourceUrl().equals(
        "https://stacks.stanford.edu/file/zw031pj2507/zw031pj2507_0002.xml")
        && r.getUrlTypes().contains(UrlType.HAS_VIEW)
        && RdfResourceKind.STANDARD.equals(r.getResourceKind())));
  }

  @Test
  void testSampleWithComplexIsFormatOfGraph() throws RdfDeserializationException, IOException {

    // when
    final byte[] input = IOUtils.resourceToByteArray("__files/rdf_with_complex_isformatof_graph.xml", getClass().getClassLoader());
    final RdfResourceEntry mainRdfResourceEntry = new RdfDeserializerImpl().getMainThumbnailResourceForMediaExtraction(input);
    final List<RdfResourceEntry> otherRdfResourceEntries = new RdfDeserializerImpl().getRemainingResourcesForMediaExtraction(input);

    // then
    assertNotNull(mainRdfResourceEntry);
    assertTrue(mainRdfResourceEntry.getResourceUrl().equals("https://example.com/resourceB1")
        && mainRdfResourceEntry.getUrlTypes().equals(Set.of(UrlType.OBJECT, UrlType.HAS_VIEW))
        && RdfResourceKind.STANDARD.equals(mainRdfResourceEntry.getResourceKind()));
    assertEquals(5, otherRdfResourceEntries.size());
    assertTrue(otherRdfResourceEntries.stream().anyMatch(r ->
        r.getResourceUrl().equals("https://example.com/resourceA1")
            && r.getUrlTypes()
            .equals(Set.of(UrlType.IS_SHOWN_BY, UrlType.IS_SHOWN_AT, UrlType.HAS_VIEW))
            && RdfResourceKind.STANDARD.equals(r.getResourceKind())));
    assertTrue(otherRdfResourceEntries.stream().anyMatch(r ->
        r.getResourceUrl().equals("https://example.com/resourceA2")
            && r.getUrlTypes().equals(Set.of(UrlType.IS_SHOWN_BY, UrlType.HAS_VIEW))
            && RdfResourceKind.IIIF.equals(r.getResourceKind())));
    assertTrue(otherRdfResourceEntries.stream().anyMatch(r ->
        r.getResourceUrl().equals("https://example.com/resourceB2")
            && r.getUrlTypes().equals(Set.of(UrlType.HAS_VIEW))
            && RdfResourceKind.OEMBEDDED.equals(r.getResourceKind())));
    assertTrue(otherRdfResourceEntries.stream().anyMatch(r ->
        r.getResourceUrl().equals("https://example.com/resourceB3")
            && r.getUrlTypes().equals(Set.of(UrlType.HAS_VIEW, UrlType.OBJECT))
            && RdfResourceKind.STANDARD.equals(r.getResourceKind())));
    assertTrue(otherRdfResourceEntries.stream().anyMatch(r ->
        r.getResourceUrl().equals("https://example.com/resourceC")
            && r.getUrlTypes().equals(Set.of(UrlType.IS_SHOWN_BY, UrlType.HAS_VIEW))
            && RdfResourceKind.STANDARD.equals(r.getResourceKind())));
  }
}
