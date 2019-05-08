package eu.europeana.indexing.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import eu.europeana.corelib.definitions.jibx.AboutType;
import eu.europeana.corelib.definitions.jibx.AgentType;
import eu.europeana.corelib.definitions.jibx.Aggregation;
import eu.europeana.corelib.definitions.jibx.CollectionName;
import eu.europeana.corelib.definitions.jibx.Concept;
import eu.europeana.corelib.definitions.jibx.DatasetName;
import eu.europeana.corelib.definitions.jibx.EuropeanaAggregationType;
import eu.europeana.corelib.definitions.jibx.License;
import eu.europeana.corelib.definitions.jibx.PlaceType;
import eu.europeana.corelib.definitions.jibx.ProvidedCHOType;
import eu.europeana.corelib.definitions.jibx.ProxyType;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.definitions.jibx.Service;
import eu.europeana.corelib.definitions.jibx.TimeSpanType;
import eu.europeana.corelib.definitions.jibx.WebResourceType;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class RdfWrapperTest {

  @Test
  void testFilteringEntities() {
    testFilteringEntities(WebResourceType.class, RDF::getWebResourceList,
        RdfWrapper::getWebResources);
    testFilteringEntities(AgentType.class, RDF::getAgentList, RdfWrapper::getAgents);
    testFilteringEntities(Concept.class, RDF::getConceptList, RdfWrapper::getConcepts);
    testFilteringEntities(License.class, RDF::getLicenseList, RdfWrapper::getLicenses);
    testFilteringEntities(PlaceType.class, RDF::getPlaceList, RdfWrapper::getPlaces);
    testFilteringEntities(Service.class, RDF::getServiceList, RdfWrapper::getServices);
    testFilteringEntities(TimeSpanType.class, RDF::getTimeSpanList, RdfWrapper::getTimeSpans);
  }

  private <T extends AboutType> void testFilteringEntities(Class<T> type,
      Function<RDF, List<T>> getter, Function<RdfWrapper, List<T>> wrapperMethod) {

    // Create entities
    final T entity0 = mock(type);
    doReturn(" ").when(entity0).getAbout();
    final T entity1 = mock(type);
    doReturn("nonemptyabout").when(entity1).getAbout();
    final T entity2 = mock(type);
    doReturn(null).when(entity2).getAbout();

    // Test rdf that returns a real list
    final RDF rdf = mock(RDF.class);
    when(getter.apply(rdf)).thenReturn(Arrays.asList(entity0, entity1, entity2));
    assertEquals(Collections.singletonList(entity1), wrapperMethod.apply(new RdfWrapper(rdf)));

    // Test rdf that returns null
    when(getter.apply(rdf)).thenReturn(null);
    assertTrue(wrapperMethod.apply(new RdfWrapper(rdf)).isEmpty());
  }

  @Test
  void testListedEntities() {
    testListedEntities(ProvidedCHOType.class, RDF::getProvidedCHOList, RdfWrapper::getProvidedCHOs);
    testListedEntities(ProxyType.class, RDF::getProxyList, RdfWrapper::getProxies);
    testListedEntities(Aggregation.class, RDF::getAggregationList, RdfWrapper::getAggregations);
  }

  private <T> void testListedEntities(Class<T> type, Function<RDF, List<T>> getter,
      Function<RdfWrapper, List<T>> wrapperMethod) {

    // Create entities
    final T entity1 = mock(type);
    final T entity2 = mock(type);

    // Test rdf that returns a real list
    final RDF rdf = mock(RDF.class);
    when(getter.apply(rdf)).thenReturn(Arrays.asList(entity1, entity2));
    assertEquals(Arrays.asList(entity1, entity2), wrapperMethod.apply(new RdfWrapper(rdf)));

    // Test rdf that returns null
    when(getter.apply(rdf)).thenReturn(null);
    assertTrue(wrapperMethod.apply(new RdfWrapper(rdf)).isEmpty());
  }

  @Test
  void testGetWebResourcesWithProcessing() {

    // Create entities
    final WebResourceType entity0 = mock(WebResourceType.class);
    doReturn(" ").when(entity0).getAbout();
    final WebResourceType entity1 = mock(WebResourceType.class);
    doReturn("nonemptyabout").when(entity1).getAbout();
    final WebResourceType entity2 = mock(WebResourceType.class);
    doReturn(null).when(entity2).getAbout();

    // Test rdf that returns a real list
    final RDF rdf = mock(RDF.class);
    when(rdf.getWebResourceList()).thenReturn(Arrays.asList(entity0, entity1, entity2));
    assertEquals(Collections.singletonList(entity1.getAbout()),
        new RdfWrapper(rdf).getWebResources().stream().map(WebResourceType::getAbout)
            .collect(Collectors.toList()));

    // Test rdf that returns null
    when(rdf.getWebResourceList()).thenReturn(null);
    assertTrue(new RdfWrapper(rdf).getWebResources().isEmpty());
  }

  @Test
  void testGetEuropeanaAggregation() {

    // Create entities
    final EuropeanaAggregationType entity1 = mock(EuropeanaAggregationType.class);
    final EuropeanaAggregationType entity2 = mock(EuropeanaAggregationType.class);

    // Test rdf that returns a real list
    final RDF rdf = mock(RDF.class);
    when(rdf.getEuropeanaAggregationList()).thenReturn(Arrays.asList(entity1, entity2));
    assertEquals(entity1, new RdfWrapper(rdf).getEuropeanaAggregation().get());

    // Test rdf that returns null
    when(rdf.getEuropeanaAggregationList()).thenReturn(null);
    assertFalse(new RdfWrapper(rdf).getEuropeanaAggregation().isPresent());
  }

  @Test
  void testGetDatasetName() {

    // Set up to return a europeana aggregation
    final EuropeanaAggregationType entity = mock(EuropeanaAggregationType.class);
    final RdfWrapper rdf = spy(new RdfWrapper(new RDF()));
    when(rdf.getEuropeanaAggregation()).thenReturn(Optional.of(entity));

    // Test if dataset name exists and collection name does not
    final DatasetName datasetName = new DatasetName();
    datasetName.setString("test1");
    when(entity.getDatasetName()).thenReturn(datasetName);
    assertEquals(datasetName.getString(), rdf.getDatasetName());

    // Test if dataset name and collection name exist
    final CollectionName collectionName = new CollectionName();
    collectionName.setString("test2");
    when(entity.getCollectionName()).thenReturn(collectionName);
    assertEquals(datasetName.getString(), rdf.getDatasetName());

    // Test if dataset name does not exist, but collection name does.
    datasetName.setString("");
    assertEquals(collectionName.getString(), rdf.getDatasetName());
    datasetName.setString(null);
    assertEquals(collectionName.getString(), rdf.getDatasetName());
    when(entity.getDatasetName()).thenReturn(null);
    assertEquals(collectionName.getString(), rdf.getDatasetName());

    // Test if neither dataset name nor collection name exists
    collectionName.setString("");
    assertTrue(rdf.getDatasetName().isEmpty());
    collectionName.setString(null);
    assertTrue(rdf.getDatasetName().isEmpty());
    when(entity.getCollectionName()).thenReturn(null);
    assertTrue(rdf.getDatasetName().isEmpty());

    // If there is no europeana aggregation
    when(rdf.getEuropeanaAggregation()).thenReturn(Optional.of(entity));
    assertTrue(rdf.getDatasetName().isEmpty());
  }

  @Test
  void testGetAbout() {

    // Create entities
    final ProvidedCHOType entity0 = mock(ProvidedCHOType.class);
    doReturn(" ").when(entity0).getAbout();
    final ProvidedCHOType entity1 = mock(ProvidedCHOType.class);
    doReturn("nonemptyabout").when(entity1).getAbout();
    final ProvidedCHOType entity2 = mock(ProvidedCHOType.class);
    doReturn(null).when(entity2).getAbout();


    // Test rdf that returns a real list
    final RDF rdf = mock(RDF.class);
    when(rdf.getProvidedCHOList()).thenReturn(Arrays.asList(entity0, entity1, entity2));
    assertEquals(entity1.getAbout(), new RdfWrapper(rdf).getAbout());

    // Test rdf that returns list without viable candidates
    when(rdf.getProvidedCHOList()).thenReturn(Arrays.asList(entity0, entity2));
    assertNull(new RdfWrapper(rdf).getAbout());

    // Test rdf that returns null
    when(rdf.getProvidedCHOList()).thenReturn(null);
    assertNull(new RdfWrapper(rdf).getAbout());
  }
}
