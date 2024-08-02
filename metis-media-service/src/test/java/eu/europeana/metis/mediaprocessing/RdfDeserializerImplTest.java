package eu.europeana.metis.mediaprocessing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.metis.mediaprocessing.exception.RdfDeserializationException;
import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import eu.europeana.metis.mediaprocessing.model.UrlType;
import eu.europeana.metis.schema.jibx.WebResourceType;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class RdfDeserializerImplTest {

  private static final String RDF_NAMESPACE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
  private static final String ORE_NAMESPACE = "http://www.openarchives.org/ore/terms/";
  private static final String EDM_NAMESPACE = "http://www.europeana.eu/schemas/edm/";
  private static final String SVCS_NAMESPACE = "http://rdfs.org/sioc/services#";

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

  @Disabled
  @Test
  void testGetResourceUrlsWithDifferentResources()
          throws RdfDeserializationException, ParserConfigurationException {

    // Create document with root rdf
    final Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
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
    final Map<String, Set<UrlType>> resultAllTypes = new RdfDeserializerImpl()
            .getResourceEntries(document, Set.of(UrlType.values()));
    assertEquals(6, resultAllTypes.size());
    assertEquals(Collections.singleton(UrlType.OBJECT), resultAllTypes.get(object));
    assertEquals(Collections.singleton(UrlType.HAS_VIEW), resultAllTypes.get(hasView1));
    assertEquals(Collections.singleton(UrlType.HAS_VIEW), resultAllTypes.get(hasView2));
    assertEquals(Collections.singleton(UrlType.IS_SHOWN_BY), resultAllTypes.get(isShownBy1));
    assertEquals(Collections.singleton(UrlType.IS_SHOWN_BY), resultAllTypes.get(isShownBy2));
    assertEquals(Collections.singleton(UrlType.IS_SHOWN_AT), resultAllTypes.get(isShownAt));

    // Test method for selection of url types
    final Map<String, Set<UrlType>> resultSelectedTypes = new RdfDeserializerImpl()
            .getResourceEntries(document, Set.of(UrlType.IS_SHOWN_AT, UrlType.HAS_VIEW));
    assertEquals(3, resultSelectedTypes.size());
    assertEquals(Collections.singleton(UrlType.HAS_VIEW), resultSelectedTypes.get(hasView1));
    assertEquals(Collections.singleton(UrlType.HAS_VIEW), resultSelectedTypes.get(hasView2));
    assertEquals(Collections.singleton(UrlType.IS_SHOWN_AT), resultSelectedTypes.get(isShownAt));

    // Test method for no url types
    assertTrue(
            new RdfDeserializerImpl().getResourceEntries(document, Collections.emptySet())
                    .isEmpty());
  }

  @Disabled
  @Test
  void testGetResourceUrlsWithSameResources()
          throws RdfDeserializationException, ParserConfigurationException {

    // Create document with root rdf
    final Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
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
    final Map<String, Set<UrlType>> resultAllTypes = new RdfDeserializerImpl()
            .getResourceEntries(document, Set.of(UrlType.values()));
    assertEquals(1, resultAllTypes.size());
    assertEquals(Set.of(UrlType.values()), resultAllTypes.get(commonResource));

    // Test method for selected url types
    final Set<UrlType> selectedTypes = Set.of(UrlType.IS_SHOWN_BY, UrlType.OBJECT);
    final Map<String, Set<UrlType>> resultSelectedTypes = new RdfDeserializerImpl()
            .getResourceEntries(document, selectedTypes);
    assertEquals(1, resultSelectedTypes.size());
    assertEquals(selectedTypes, resultSelectedTypes.get(commonResource));
  }

  @Test
  void testGetResourceUrlsWithoutData()
          throws RdfDeserializationException, ParserConfigurationException {
    final Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .newDocument();
    final Element rdf = document.createElementNS(RDF_NAMESPACE, "RDF");
    document.appendChild(rdf);
    assertTrue(new RdfDeserializerImpl().getResourceEntries(document, Collections.emptySet())
            .isEmpty());
  }

  @Test
  void testGetOEmbeddableObjects() throws IOException, RdfDeserializationException {
    RdfDeserializerImpl rdfDeserializer = new RdfDeserializerImpl();
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream("__files/rdf_with_oembed_sample.xml");
    RdfResourceEntry rdfResourceEntry = rdfDeserializer.getMainThumbnailResourceForMediaExtraction(inputStream);
  }
}
