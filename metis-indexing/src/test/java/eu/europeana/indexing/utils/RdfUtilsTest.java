package eu.europeana.indexing.utils;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import eu.europeana.corelib.definitions.jibx.AboutType;
import eu.europeana.corelib.definitions.jibx.AgentType;
import eu.europeana.corelib.definitions.jibx.Concept;
import eu.europeana.corelib.definitions.jibx.License;
import eu.europeana.corelib.definitions.jibx.PlaceType;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.definitions.jibx.Service;
import eu.europeana.corelib.definitions.jibx.TimeSpanType;
import eu.europeana.corelib.definitions.jibx.WebResourceType;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.Test;

public class RdfUtilsTest {

  @Test
  public void testFilteringEntitiesWithEmtpyAbout() {
    testFilteringEntitiesWithEmtpyAbout(WebResourceType.class, RDF::getWebResourceList,
        rdf -> RdfUtils.getWebResourcesWithNonemptyAbout(rdf).collect(Collectors.toList()));
    testFilteringEntitiesWithEmtpyAbout(AgentType.class, RDF::getAgentList,
        RdfUtils::getAgentsWithNonemptyAbout);
    testFilteringEntitiesWithEmtpyAbout(Concept.class, RDF::getConceptList,
        RdfUtils::getConceptsWithNonemptyAbout);
    testFilteringEntitiesWithEmtpyAbout(License.class, RDF::getLicenseList,
        RdfUtils::getLicensesWithNonemptyAbout);
    testFilteringEntitiesWithEmtpyAbout(PlaceType.class, RDF::getPlaceList,
        RdfUtils::getPlacesWithNonemptyAbout);
    testFilteringEntitiesWithEmtpyAbout(Service.class, RDF::getServiceList,
        RdfUtils::getServicesWithNonemptyAbout);
    testFilteringEntitiesWithEmtpyAbout(TimeSpanType.class, RDF::getTimeSpanList,
        RdfUtils::getTimeSpansWithNonemptyAbout);
  }

  private <T extends AboutType> void testFilteringEntitiesWithEmtpyAbout(Class<T> type,
      Function<RDF, List<T>> getter, Function<RDF, List<T>> utilsMethod) {

    // Create entities
    final T entity1 = mock(type);
    doReturn("nonemptyabout").when(entity1).getAbout();
    final T entity2 = mock(type);
    doReturn(" ").when(entity2).getAbout();
    final T entity3 = mock(type);
    doReturn(null).when(entity3).getAbout();

    // Create rdf
    final RDF rdf = mock(RDF.class);
    when(getter.apply(rdf)).thenReturn(Arrays.asList(entity1, entity2, entity3));

    // Test
    assertEquals(Collections.singletonList(entity1), utilsMethod.apply(rdf));
  }
}
