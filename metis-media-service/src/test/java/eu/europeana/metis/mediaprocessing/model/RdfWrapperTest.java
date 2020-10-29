package eu.europeana.metis.mediaprocessing.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

import eu.europeana.metis.schema.jibx.Aggregation;
import eu.europeana.metis.schema.jibx.HasView;
import eu.europeana.metis.schema.jibx.IsShownAt;
import eu.europeana.metis.schema.jibx.IsShownBy;
import eu.europeana.metis.schema.jibx.RDF;
import eu.europeana.metis.schema.jibx.WebResourceType;
import eu.europeana.metis.schema.jibx._Object;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class RdfWrapperTest {

  @Test
  void testGetRdf() {
    final RDF rdf = new RDF();
    assertSame(rdf, new RdfWrapper(rdf).getRdf());
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
