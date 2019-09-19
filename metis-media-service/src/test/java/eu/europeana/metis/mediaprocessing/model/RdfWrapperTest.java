package eu.europeana.metis.mediaprocessing.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.corelib.definitions.jibx.Aggregation;
import eu.europeana.corelib.definitions.jibx.HasView;
import eu.europeana.corelib.definitions.jibx.IsShownAt;
import eu.europeana.corelib.definitions.jibx.IsShownBy;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.definitions.jibx.WebResourceType;
import eu.europeana.corelib.definitions.jibx._Object;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class RdfWrapperTest {

  @Test
  void testGetRdf() {
    final RDF rdf = new RDF();
    assertSame(rdf, new RdfWrapper(rdf).getRdf());
  }

  @Test
  void testGetResourceUrlsWithDifferentResources() {

    // Prepare RDF with all links
    final RDF rdf = new RDF();

    // Add object link to new aggregation
    final Aggregation aggregation1 = new Aggregation();
    rdf.getAggregationList().add(aggregation1);
    final _Object object = new _Object();
    object.setResource("object resource");
    aggregation1.setObject(object);

    // Add hasView instances to same aggregation
    final HasView hasView1 = new HasView();
    hasView1.setResource("has view resource 1");
    aggregation1.getHasViewList().add(hasView1);
    final HasView hasView2 = new HasView();
    hasView1.setResource("has view resource 2");
    aggregation1.getHasViewList().add(hasView2);

    // Add isShownBy to same aggregation
    final IsShownBy isShownBy1 = new IsShownBy();
    isShownBy1.setResource("is shown by resource");
    aggregation1.setIsShownBy(isShownBy1);

    // Add isShownBy to new aggregation
    final Aggregation aggregation2 = new Aggregation();
    rdf.getAggregationList().add(aggregation2);
    final IsShownBy isShownBy2 = new IsShownBy();
    isShownBy2.setResource("is shown by resource 2");
    aggregation2.setIsShownBy(isShownBy2);

    // Add isShownAt to same aggregation
    final IsShownAt isShownAt = new IsShownAt();
    isShownAt.setResource("is shown at resource");
    aggregation2.setIsShownAt(isShownAt);

    // Test method for all url types
    final Map<String, List<UrlType>> resultAllTypes = new RdfWrapper(rdf)
        .getResourceUrls(new HashSet<>(Arrays.asList(UrlType.values())));
    assertEquals(6, resultAllTypes.size());
    assertEquals(Collections.singletonList(UrlType.OBJECT),
        resultAllTypes.get(object.getResource()));
    assertEquals(Collections.singletonList(UrlType.HAS_VIEW),
        resultAllTypes.get(hasView1.getResource()));
    assertEquals(Collections.singletonList(UrlType.HAS_VIEW),
        resultAllTypes.get(hasView2.getResource()));
    assertEquals(Collections.singletonList(UrlType.IS_SHOWN_BY),
        resultAllTypes.get(isShownBy1.getResource()));
    assertEquals(Collections.singletonList(UrlType.IS_SHOWN_BY),
        resultAllTypes.get(isShownBy2.getResource()));
    assertEquals(Collections.singletonList(UrlType.IS_SHOWN_AT),
        resultAllTypes.get(isShownAt.getResource()));

    // Test method for selection of url types
    final Map<String, List<UrlType>> resultSelectedTypes = new RdfWrapper(rdf)
        .getResourceUrls(new HashSet<>(Arrays.asList(UrlType.IS_SHOWN_AT, UrlType.HAS_VIEW)));
    assertEquals(3, resultSelectedTypes.size());
    assertEquals(Collections.singletonList(UrlType.HAS_VIEW),
        resultSelectedTypes.get(hasView1.getResource()));
    assertEquals(Collections.singletonList(UrlType.HAS_VIEW),
        resultSelectedTypes.get(hasView2.getResource()));
    assertEquals(Collections.singletonList(UrlType.IS_SHOWN_AT),
        resultSelectedTypes.get(isShownAt.getResource()));

    // Test method for no url types
    assertTrue(new RdfWrapper(rdf).getResourceUrls(Collections.emptySet()).isEmpty());
  }

  @Test
  void testGetResourceUrlsWithSameResources() {

    // Prepare RDF with all links
    final RDF rdf = new RDF();
    final String commonResource = "common resource";

    // Add object link to new aggregation
    final Aggregation aggregation1 = new Aggregation();
    rdf.getAggregationList().add(aggregation1);
    final _Object object = new _Object();
    object.setResource(commonResource);
    aggregation1.setObject(object);

    // Add hasView instances to same aggregation
    final HasView hasView = new HasView();
    hasView.setResource(commonResource);
    aggregation1.getHasViewList().add(hasView);

    // Add isShownBy to same aggregation
    final IsShownBy isShownBy = new IsShownBy();
    isShownBy.setResource(commonResource);
    aggregation1.setIsShownBy(isShownBy);

    // Add isShownAt to new aggregation
    final Aggregation aggregation2 = new Aggregation();
    rdf.getAggregationList().add(aggregation2);
    final IsShownAt isShownAt = new IsShownAt();
    isShownAt.setResource(commonResource);
    aggregation2.setIsShownAt(isShownAt);

    // Test method for all url types
    final Map<String, List<UrlType>> resultAllTypes = new RdfWrapper(rdf)
        .getResourceUrls(new HashSet<>(Arrays.asList(UrlType.values())));
    assertEquals(1, resultAllTypes.size());
    assertEquals(Arrays.asList(UrlType.values()), resultAllTypes.get(commonResource));

    // Test method for selected url types
    final Set<UrlType> selectedTypes = new HashSet<>(
        Arrays.asList(UrlType.IS_SHOWN_BY, UrlType.OBJECT));
    final Map<String, List<UrlType>> resultSelectedTypes = new RdfWrapper(rdf)
        .getResourceUrls(selectedTypes);
    assertEquals(1, resultSelectedTypes.size());
    assertEquals(selectedTypes, new HashSet<>(resultSelectedTypes.get(commonResource)));
  }

  @Test
  void testGetResourceUrlsWithoutData() {
    final RDF rdf = new RDF();
    rdf.setAggregationList(null);
    assertTrue(new RdfWrapper(rdf).getResourceUrls(Collections.emptySet()).isEmpty());
    rdf.setAggregationList(new ArrayList<>());
    assertTrue(new RdfWrapper(rdf).getResourceUrls(Collections.emptySet()).isEmpty());
  }

  @Test
  void testGetFirstResourceOfType (){

    // Setup tests - create RDF and aggregations
    final Aggregation firstAggregation = new Aggregation();
    final Aggregation middleAggregation = new Aggregation();
    final Aggregation lastAggregation =new Aggregation();
    final RDF rdf = new RDF();
    rdf.setAggregationList(Arrays.asList(null, firstAggregation, middleAggregation, lastAggregation));
    final RdfWrapper instance = new RdfWrapper(rdf);

    // Test - try with non-existing values
    assertFalse(instance.getFirstResourceOfType(UrlType.OBJECT).isPresent());
    assertFalse(instance.getFirstResourceOfType(UrlType.HAS_VIEW).isPresent());
    assertFalse(instance.getFirstResourceOfType(UrlType.IS_SHOWN_AT).isPresent());
    assertFalse(instance.getFirstResourceOfType(UrlType.IS_SHOWN_BY).isPresent());

    // Setup tests - object links
    final String firstObject = "firstObject";
    final String lastObject = "lastObject";
    firstAggregation.setObject(new _Object());
    firstAggregation.getObject().setResource(firstObject);
    middleAggregation.setObject(new _Object());
    middleAggregation.getObject().setResource(lastObject);
    lastAggregation.setObject(null);

    // Setup tests - hasView links
    final String firstHasView = "firstHasView";
    final String middleHasView = "middleHasView";
    final String lastHasView = "lastHasView";
    firstAggregation.setHasViewList(null);
    middleAggregation
        .setHasViewList(Arrays.asList(null, new HasView(), new HasView(), new HasView()));
    middleAggregation.getHasViewList().get(2).setResource(firstHasView);
    middleAggregation.getHasViewList().get(3).setResource(middleHasView);
    lastAggregation.setHasViewList(Collections.singletonList(new HasView()));
    lastAggregation.getHasViewList().get(0).setResource(lastHasView);

    // Setup tests - isShownAt links
    final String firstIsShownAt = "firstIsShownAt";
    final String lastIsShownAt = "lastIsShownAt";
    firstAggregation.setIsShownAt(new IsShownAt());
    middleAggregation.setIsShownAt(new IsShownAt());
    middleAggregation.getIsShownAt().setResource(firstIsShownAt);
    lastAggregation.setIsShownAt(new IsShownAt());
    lastAggregation.getIsShownAt().setResource(lastIsShownAt);

    // Setup tests - isShownBy links
    final String firstIsShownBy = "firstIsShownBy";
    final String lastIsShownBy = "lastIsShownBy";
    firstAggregation.setIsShownBy(new IsShownBy());
    firstAggregation.getIsShownBy().setResource(" ");
    middleAggregation.setIsShownBy(new IsShownBy());
    middleAggregation.getIsShownBy().setResource(firstIsShownBy);
    lastAggregation.setIsShownBy(new IsShownBy());
    lastAggregation.getIsShownBy().setResource(lastIsShownBy);

    // Test - try with existing values
    assertEquals(firstObject, instance.getFirstResourceOfType(UrlType.OBJECT).orElse(null));
    assertEquals(firstHasView, instance.getFirstResourceOfType(UrlType.HAS_VIEW).orElse(null));
    assertEquals(firstIsShownAt, instance.getFirstResourceOfType(UrlType.IS_SHOWN_AT).orElse(null));
    assertEquals(firstIsShownBy, instance.getFirstResourceOfType(UrlType.IS_SHOWN_BY).orElse(null));
  }

  @Test
  void testGetWebResource() {

    // Prepare RDF with all links
    final RDF rdf = new RDF();
    final RdfWrapper instance = new RdfWrapper(rdf);

    // Test null or empty list
    rdf.setWebResourceList(null);
    assertFalse(instance.getWebResource("about").isPresent());
    rdf.setWebResourceList(new ArrayList<>());
    assertFalse(instance.getWebResource("about").isPresent());

    // Test with actual values - setup the test
    final String existingAbout1 = "existingAbout1";
    final String existingAbout2 = "existingAbout2";
    final String absentAbout = "absentAbout";
    rdf.setWebResourceList(
        Arrays.asList(new WebResourceType(), null, new WebResourceType(), new WebResourceType()));
    rdf.getWebResourceList().get(0).setAbout(null);
    rdf.getWebResourceList().get(2).setAbout(existingAbout1);
    rdf.getWebResourceList().get(3).setAbout(existingAbout2);

    // Test with actual values - do the test
    assertFalse(instance.getWebResource(absentAbout).isPresent());
    assertSame(rdf.getWebResourceList().get(2),
        instance.getWebResource(existingAbout1).orElse(null));
    assertSame(rdf.getWebResourceList().get(3),
        instance.getWebResource(existingAbout2).orElse(null));
  }
}
