package eu.europeana.metis.mediaprocessing.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.corelib.definitions.jibx.Aggregation;
import eu.europeana.corelib.definitions.jibx.HasView;
import eu.europeana.corelib.definitions.jibx.IsShownAt;
import eu.europeana.corelib.definitions.jibx.IsShownBy;
import eu.europeana.corelib.definitions.jibx.RDF;
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
}
