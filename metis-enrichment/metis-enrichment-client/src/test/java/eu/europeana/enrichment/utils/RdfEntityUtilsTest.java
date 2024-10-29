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

  private RDF testRdf;
  private ProxyType proxyEuropeana;

  @BeforeEach
  void setUp() {

    testRdf = new RDF();

    EuropeanaProxy europeanaProxy = new EuropeanaProxy();
    europeanaProxy.setEuropeanaProxy(true);

    proxyEuropeana = new ProxyType();
    proxyEuropeana.setAbout("/proxy/europeana/260/_kmo_av_sid_45006");
    proxyEuropeana.setEuropeanaProxy(europeanaProxy);

  }

  @Test
  void testAppendLinkToEuropeanaProxy() {

    testRdf.setProxyList(Collections.singletonList(proxyEuropeana));

    String link = "http://dummylink.com";
    Set<ProxyFieldType> linkTypes = new HashSet<>();
    linkTypes.add(ProxyFieldType.DC_COVERAGE);

    RdfEntityUtils.appendLinkToEuropeanaProxy(testRdf, link, linkTypes);

    assertEquals(1, testRdf.getProxyList().getFirst().getChoiceList().size());
    assertEquals(link,
        testRdf.getProxyList().getFirst().getChoiceList().getFirst().getCoverage().getResource()
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
    proxyEuropeana.setChoiceList(choices);

    testRdf.setProxyList(Collections.singletonList(proxyEuropeana));

    String link = "http://dummylink.com";
    Set<ProxyFieldType> linkTypes = new HashSet<>();
    linkTypes.add(ProxyFieldType.DC_COVERAGE);

    RdfEntityUtils.appendLinkToEuropeanaProxy(testRdf, link, linkTypes);

    assertEquals(2, testRdf.getProxyList().getFirst().getChoiceList().size());
    assertEquals("http://differentdummylink.com",
        testRdf.getProxyList().getFirst().getChoiceList().get(0).getCoverage().getResource()
               .getResource());
    assertEquals(link,
        testRdf.getProxyList().getFirst().getChoiceList().get(1).getCoverage().getResource()
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
    proxyEuropeana.setChoiceList(choices);

    testRdf.setProxyList(Collections.singletonList(proxyEuropeana));

    Set<ProxyFieldType> linkTypes = new HashSet<>();
    linkTypes.add(ProxyFieldType.DC_COVERAGE);

    RdfEntityUtils.appendLinkToEuropeanaProxy(testRdf, link, linkTypes);

    assertEquals(1, testRdf.getProxyList().getFirst().getChoiceList().size());
    assertEquals(link,
        testRdf.getProxyList().getFirst().getChoiceList().getFirst().getCoverage().getResource().getResource());

  }

  @Test
  void testGetProviderProxy() {

    final ProxyType proxyProvider = new ProxyType();

    EuropeanaProxy providerProxy = new EuropeanaProxy();
    providerProxy.setEuropeanaProxy(false);

    proxyProvider.setAbout("/proxy/provider/260/_kmo_av_sid_45006");
    proxyProvider.setEuropeanaProxy(providerProxy);

    ArrayList<ProxyType> proxyList = new ArrayList<>();
    proxyList.add(proxyEuropeana);
    proxyList.add(proxyProvider);

    testRdf.setProxyList(proxyList);

    List<ProxyType> output = RdfEntityUtils.getProviderProxies(testRdf);
    assertNotNull(output);
    assertEquals(1, output.size());
    assertNotNull(output.getFirst());
    assertFalse(output.getFirst().getEuropeanaProxy().isEuropeanaProxy());
    assertEquals(proxyProvider, output.getFirst());
  }

  @Test
  void testGetProviderProxyWithoutProvider() {
    testRdf.setProxyList(Collections.singletonList(proxyEuropeana));
    List<ProxyType> output = RdfEntityUtils.getProviderProxies(testRdf);
    assertNotNull(output);
    assertTrue(output.isEmpty());
  }

  @Test
  void testRemoveMatchingEntities() {
    testRdf.setProxyList(Collections.singletonList(proxyEuropeana));

    String agentLink = "http://data.europeana.eu/agent/example1";
    String conceptLink = "http://data.europeana.eu/concept/example1";
    String placeLink = "http://data.europeana.eu/place/example1";
    String timespanLink = "http://data.europeana.eu/timespan/example1";
    RdfEntityUtils.appendLinkToEuropeanaProxy(testRdf, agentLink,
        Sets.newHashSet(ProxyFieldType.DC_CREATOR));
    RdfEntityUtils.appendLinkToEuropeanaProxy(testRdf, conceptLink,
        Sets.newHashSet(ProxyFieldType.DC_SUBJECT));
    RdfEntityUtils.appendLinkToEuropeanaProxy(testRdf, placeLink,
        Sets.newHashSet(ProxyFieldType.DC_COVERAGE));
    RdfEntityUtils.appendLinkToEuropeanaProxy(testRdf, timespanLink,
        Sets.newHashSet(ProxyFieldType.DCTERMS_CREATED));

    final AgentType agentType = new AgentType();
    agentType.setAbout(agentLink);
    testRdf.setAgentList(new ArrayList<>(Collections.singleton(agentType)));

    final Concept concept = new Concept();
    concept.setAbout(conceptLink);
    testRdf.setConceptList(new ArrayList<>(Collections.singleton(concept)));

    final PlaceType placeType = new PlaceType();
    placeType.setAbout(placeLink);
    testRdf.setPlaceList(new ArrayList<>(Collections.singleton(placeType)));

    final TimeSpanType timeSpanType = new TimeSpanType();
    timeSpanType.setAbout(timespanLink);
    testRdf.setTimeSpanList(new ArrayList<>(Collections.singleton(timeSpanType)));

    assertEquals(4, testRdf.getProxyList().getFirst().getChoiceList().size());
    assertEquals(agentLink,
        testRdf.getProxyList().getFirst().getChoiceList().getFirst().getCreator().getResource()
               .getResource());
    assertEquals(1, testRdf.getAgentList().size());

    assertEquals(conceptLink,
        testRdf.getProxyList().getFirst().getChoiceList().get(1).getSubject().getResource()
               .getResource());
    assertEquals(1, testRdf.getConceptList().size());

    assertEquals(placeLink,
        testRdf.getProxyList().getFirst().getChoiceList().get(2).getCoverage().getResource()
               .getResource());
    assertEquals(1, testRdf.getPlaceList().size());

    assertEquals(timespanLink,
        testRdf.getProxyList().getFirst().getChoiceList().get(3).getCreated().getResource()
               .getResource());
    assertEquals(1, testRdf.getTimeSpanList().size());

    //Find the correct links
    final Set<String> links = new HashSet<>();
    links.add(agentLink);
    links.add(conceptLink);
    links.add(placeLink);
    links.add(timespanLink);
    RdfEntityUtils.removeMatchingEntities(testRdf, links);

    assertEquals(0, testRdf.getProxyList().getFirst().getChoiceList().size());
    assertEquals(0, testRdf.getAgentList().size());
    assertEquals(0, testRdf.getConceptList().size());
    assertEquals(0, testRdf.getPlaceList().size());
    assertEquals(0, testRdf.getTimeSpanList().size());
  }
}
