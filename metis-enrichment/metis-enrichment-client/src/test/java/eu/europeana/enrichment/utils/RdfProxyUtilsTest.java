package eu.europeana.enrichment.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.enrichment.api.internal.ProxyFieldType;
import eu.europeana.metis.schema.jibx.Coverage;
import eu.europeana.metis.schema.jibx.EuropeanaProxy;
import eu.europeana.metis.schema.jibx.EuropeanaType.Choice;
import eu.europeana.metis.schema.jibx.ProxyType;
import eu.europeana.metis.schema.jibx.RDF;
import eu.europeana.metis.schema.jibx.ResourceOrLiteralType.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RdfProxyUtilsTest {

  private static RDF TEST_RDF ;
  private final static ProxyType PROXY_EUROPEANA = new ProxyType();

  @BeforeEach
  void setUp() {

    TEST_RDF = new RDF();

    EuropeanaProxy europeanaProxy = new EuropeanaProxy();
    europeanaProxy.setEuropeanaProxy(true);

    PROXY_EUROPEANA.setAbout("/proxy/europeana/260/_kmo_av_sid_45006");
    PROXY_EUROPEANA.setEuropeanaProxy(europeanaProxy);

  }

  @Test
  void testAppendLinkToEuropeanaProxy(){

    TEST_RDF.setProxyList(Collections.singletonList(PROXY_EUROPEANA));

    String link = "http://dummylink.com";
    Set<ProxyFieldType> linkTypes = new HashSet<>();
    linkTypes.add(ProxyFieldType.DC_COVERAGE);

    RdfProxyUtils.appendLinkToEuropeanaProxy(TEST_RDF, link, linkTypes);

    assertEquals(1, TEST_RDF.getProxyList().get(0).getChoiceList().size());
    assertEquals(link, TEST_RDF.getProxyList().get(0).getChoiceList().get(0).getCoverage().getResource().getResource());
  }

  @Test
  void testAppendLinkToEuropeanaProxyAddSameChoiceType(){

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

    RdfProxyUtils.appendLinkToEuropeanaProxy(TEST_RDF, link, linkTypes);

    assertEquals(2, TEST_RDF.getProxyList().get(0).getChoiceList().size());
    assertEquals("http://differentdummylink.com",
        TEST_RDF.getProxyList().get(0).getChoiceList().get(0).getCoverage().getResource().getResource());
    assertEquals(link, TEST_RDF.getProxyList().get(0).getChoiceList().get(1).getCoverage().getResource().getResource());

  }

  @Test
  void testAppendLinkToEuropeanaProxyAlreadyExists(){

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

    RdfProxyUtils.appendLinkToEuropeanaProxy(TEST_RDF, link, linkTypes);

    assertEquals(1, TEST_RDF.getProxyList().get(0).getChoiceList().size());
    assertEquals(link, TEST_RDF.getProxyList().get(0).getChoiceList().get(0).getCoverage().getResource().getResource());

  }

  @Test
  void testGetProviderProxy(){

    final ProxyType proxyProvider = new ProxyType();

    EuropeanaProxy providerProxy = new EuropeanaProxy();
    providerProxy.setEuropeanaProxy(false);

    proxyProvider.setAbout("/proxy/provider/260/_kmo_av_sid_45006");
    proxyProvider.setEuropeanaProxy(providerProxy);

    ArrayList<ProxyType> proxyList = new ArrayList<>();
    proxyList.add(PROXY_EUROPEANA);
    proxyList.add(proxyProvider);

    TEST_RDF.setProxyList(proxyList);

    List<ProxyType> output = RdfProxyUtils.getProviderProxies(TEST_RDF);
    assertNotNull(output);
    assertEquals(1, output.size());
    assertNotNull(output.get(0));
    assertFalse(output.get(0).getEuropeanaProxy().isEuropeanaProxy());
    assertEquals(proxyProvider, output.get(0));
  }

  @Test
  void testGetProviderProxyWithoutProvider(){
    TEST_RDF.setProxyList(Collections.singletonList(PROXY_EUROPEANA));
    List<ProxyType> output = RdfProxyUtils.getProviderProxies(TEST_RDF);
    assertNotNull(output);
    assertTrue(output.isEmpty());
  }
}
