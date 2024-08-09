package eu.europeana.enrichment.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.enrichment.api.internal.ProxyFieldType;
import eu.europeana.metis.schema.jibx.AgentType;
import eu.europeana.metis.schema.jibx.Concept;
import eu.europeana.metis.schema.jibx.Coverage;
import eu.europeana.metis.schema.jibx.EuropeanaProxy;
import eu.europeana.metis.schema.jibx.EuropeanaType.Choice;
import eu.europeana.metis.schema.jibx.PlaceType;
import eu.europeana.metis.schema.jibx.ProxyType;
import eu.europeana.metis.schema.jibx.RDF;
import eu.europeana.metis.schema.jibx.ResourceOrLiteralType.Resource;
import eu.europeana.metis.schema.jibx.TimeSpanType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.compress.utils.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RdfEntityUtilsTest {

  private static RDF TEST_RDF;
  private static ProxyType PROXY_EUROPEANA;

  @BeforeEach
  void setUp() {

    TEST_RDF = new RDF();

    EuropeanaProxy europeanaProxy = new EuropeanaProxy();
    europeanaProxy.setEuropeanaProxy(true);

    PROXY_EUROPEANA = new ProxyType();
    PROXY_EUROPEANA.setAbout("/proxy/europeana/260/_kmo_av_sid_45006");
    PROXY_EUROPEANA.setEuropeanaProxy(europeanaProxy);

  }

  @Test
  void testAppendLinkToEuropeanaProxy() {

    TEST_RDF.setProxyList(Collections.singletonList(PROXY_EUROPEANA));

    String link = "http://dummylink.com";
    Set<ProxyFieldType> linkTypes = new HashSet<>();
    linkTypes.add(ProxyFieldType.DC_COVERAGE);

    RdfEntityUtils.appendLinkToEuropeanaProxy(TEST_RDF, link, linkTypes);

    assertEquals(1, TEST_RDF.getProxyList().getFirst().getChoiceList().size());
    assertEquals(link,
        TEST_RDF.getProxyList().getFirst().getChoiceList().getFirst().getCoverage().getResource()
                .getResource());
  }

  @Test
  void testAppendLinkToEuropeanaProxyAddSameChoiceType() {

    Choice choice = new Choice();
    Coverage coverage = new Coverage();
    Resource resource = new Resource();
    resource.setResource("http://differentdummylink.com");
    coverage.setResource(resource);
    choice.setCoverage(coverage);

    List<Choice> choices = new ArrayList<>();
    choices.add(choice);
    PROXY_EUROPEANA.setChoiceList(choices);

    TEST_RDF.setProxyList(Collections.singletonList(PROXY_EUROPEANA));

    String link = "http://dummylink.com";
    Set<ProxyFieldType> linkTypes = new HashSet<>();
    linkTypes.add(ProxyFieldType.DC_COVERAGE);

    RdfEntityUtils.appendLinkToEuropeanaProxy(TEST_RDF, link, linkTypes);

    assertEquals(2, TEST_RDF.getProxyList().getFirst().getChoiceList().size());
    assertEquals("http://differentdummylink.com",
        TEST_RDF.getProxyList().getFirst().getChoiceList().get(0).getCoverage().getResource()
                .getResource());
    assertEquals(link,
        TEST_RDF.getProxyList().getFirst().getChoiceList().get(1).getCoverage().getResource()
                .getResource());

  }

  @Test
  void testAppendLinkToEuropeanaProxyAlreadyExists() {

    String link = "http://dummylink.com";

    Choice choice = new Choice();
    Coverage coverage = new Coverage();
    Resource resource = new Resource();
    resource.setResource(link);
    coverage.setResource(resource);
    choice.setCoverage(coverage);

    List<Choice> choices = new ArrayList<>();
    choices.add(choice);
    PROXY_EUROPEANA.setChoiceList(choices);

    TEST_RDF.setProxyList(Collections.singletonList(PROXY_EUROPEANA));

    Set<ProxyFieldType> linkTypes = new HashSet<>();
    linkTypes.add(ProxyFieldType.DC_COVERAGE);

    RdfEntityUtils.appendLinkToEuropeanaProxy(TEST_RDF, link, linkTypes);

    assertEquals(1, TEST_RDF.getProxyList().getFirst().getChoiceList().size());
    assertEquals(link,
        TEST_RDF.getProxyList().getFirst().getChoiceList().getFirst().getCoverage().getResource().getResource());

  }

  @Test
  void testGetProviderProxy() {

    final ProxyType proxyProvider = new ProxyType();

    EuropeanaProxy providerProxy = new EuropeanaProxy();
    providerProxy.setEuropeanaProxy(false);

    proxyProvider.setAbout("/proxy/provider/260/_kmo_av_sid_45006");
    proxyProvider.setEuropeanaProxy(providerProxy);

    ArrayList<ProxyType> proxyList = new ArrayList<>();
    proxyList.add(PROXY_EUROPEANA);
    proxyList.add(proxyProvider);

    TEST_RDF.setProxyList(proxyList);

    List<ProxyType> output = RdfEntityUtils.getProviderProxies(TEST_RDF);
    assertNotNull(output);
    assertEquals(1, output.size());
    assertNotNull(output.getFirst());
    assertFalse(output.getFirst().getEuropeanaProxy().isEuropeanaProxy());
    assertEquals(proxyProvider, output.getFirst());
  }

  @Test
  void testGetProviderProxyWithoutProvider() {
    TEST_RDF.setProxyList(Collections.singletonList(PROXY_EUROPEANA));
    List<ProxyType> output = RdfEntityUtils.getProviderProxies(TEST_RDF);
    assertNotNull(output);
    assertTrue(output.isEmpty());
  }

  @Test
  void testRemoveMatchingEntities() {
    TEST_RDF.setProxyList(Collections.singletonList(PROXY_EUROPEANA));

    String agentLink = "http://data.europeana.eu/agent/example1";
    String conceptLink = "http://data.europeana.eu/concept/example1";
    String placeLink = "http://data.europeana.eu/place/example1";
    String timespanLink = "http://data.europeana.eu/timespan/example1";
    RdfEntityUtils.appendLinkToEuropeanaProxy(TEST_RDF, agentLink,
        Sets.newHashSet(ProxyFieldType.DC_CREATOR));
    RdfEntityUtils.appendLinkToEuropeanaProxy(TEST_RDF, conceptLink,
        Sets.newHashSet(ProxyFieldType.DC_SUBJECT));
    RdfEntityUtils.appendLinkToEuropeanaProxy(TEST_RDF, placeLink,
        Sets.newHashSet(ProxyFieldType.DC_COVERAGE));
    RdfEntityUtils.appendLinkToEuropeanaProxy(TEST_RDF, timespanLink,
        Sets.newHashSet(ProxyFieldType.DCTERMS_CREATED));

    final AgentType agentType = new AgentType();
    agentType.setAbout(agentLink);
    TEST_RDF.setAgentList(new ArrayList<>(Collections.singleton(agentType)));

    final Concept concept = new Concept();
    concept.setAbout(conceptLink);
    TEST_RDF.setConceptList(new ArrayList<>(Collections.singleton(concept)));

    final PlaceType placeType = new PlaceType();
    placeType.setAbout(placeLink);
    TEST_RDF.setPlaceList(new ArrayList<>(Collections.singleton(placeType)));

    final TimeSpanType timeSpanType = new TimeSpanType();
    timeSpanType.setAbout(timespanLink);
    TEST_RDF.setTimeSpanList(new ArrayList<>(Collections.singleton(timeSpanType)));

    assertEquals(4, TEST_RDF.getProxyList().getFirst().getChoiceList().size());
    assertEquals(agentLink,
        TEST_RDF.getProxyList().getFirst().getChoiceList().getFirst().getCreator().getResource()
                .getResource());
    assertEquals(1, TEST_RDF.getAgentList().size());

    assertEquals(conceptLink,
        TEST_RDF.getProxyList().getFirst().getChoiceList().get(1).getSubject().getResource()
                .getResource());
    assertEquals(1, TEST_RDF.getConceptList().size());

    assertEquals(placeLink,
        TEST_RDF.getProxyList().getFirst().getChoiceList().get(2).getCoverage().getResource()
                .getResource());
    assertEquals(1, TEST_RDF.getPlaceList().size());

    assertEquals(timespanLink,
        TEST_RDF.getProxyList().getFirst().getChoiceList().get(3).getCreated().getResource()
                .getResource());
    assertEquals(1, TEST_RDF.getTimeSpanList().size());

    //Find the correct links
    final Set<String> links = new HashSet<>();
    links.add(agentLink);
    links.add(conceptLink);
    links.add(placeLink);
    links.add(timespanLink);
    RdfEntityUtils.removeMatchingEntities(TEST_RDF, links);

    assertEquals(0, TEST_RDF.getProxyList().getFirst().getChoiceList().size());
    assertEquals(0, TEST_RDF.getAgentList().size());
    assertEquals(0, TEST_RDF.getConceptList().size());
    assertEquals(0, TEST_RDF.getPlaceList().size());
    assertEquals(0, TEST_RDF.getTimeSpanList().size());
  }
}
