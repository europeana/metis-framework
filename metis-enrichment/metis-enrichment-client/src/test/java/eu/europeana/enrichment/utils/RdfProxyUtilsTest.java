package eu.europeana.enrichment.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


import eu.europeana.metis.schema.jibx.Coverage;
import eu.europeana.metis.schema.jibx.EuropeanaType.Choice;
import eu.europeana.metis.schema.jibx.ResourceOrLiteralType.Resource;
import eu.europeana.enrichment.api.internal.FieldType;
import eu.europeana.metis.schema.jibx.EuropeanaProxy;
import eu.europeana.metis.schema.jibx.ProxyType;
import eu.europeana.metis.schema.jibx.RDF;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class RdfProxyUtilsTest {

  private final static ProxyType PROXY_EUROPEANA = new ProxyType();

  @Test
  void testAppendLinkToEuropeanaProxy(){

    RDF rdf = new RDF();

    EuropeanaProxy europeanaProxy = new EuropeanaProxy();
    europeanaProxy.setEuropeanaProxy(true);

    PROXY_EUROPEANA.setAbout("/proxy/europeana/260/_kmo_av_sid_45006");
    PROXY_EUROPEANA.setEuropeanaProxy(europeanaProxy);

    rdf.setProxyList(Collections.singletonList(PROXY_EUROPEANA));

    String link = "http://dummylink.com";
    Set<FieldType> linkTypes = new HashSet<>();
    linkTypes.add(FieldType.DC_COVERAGE);

    RdfProxyUtils.appendLinkToEuropeanaProxy(rdf, link, linkTypes);

    assertEquals(1, rdf.getProxyList().get(0).getChoiceList().size());
    assertEquals(link, rdf.getProxyList().get(0).getChoiceList().get(0).getCoverage().getResource().getResource());
  }

  @Test
  void testAppendLinkToEuropeanaProxyAddSameChoiceType(){

    RDF rdf = new RDF();

    EuropeanaProxy europeanaProxy = new EuropeanaProxy();
    europeanaProxy.setEuropeanaProxy(true);

    PROXY_EUROPEANA.setAbout("/proxy/europeana/260/_kmo_av_sid_45006");
    PROXY_EUROPEANA.setEuropeanaProxy(europeanaProxy);

    Choice choice = new Choice();
    Coverage coverage = new Coverage();
    Resource resource = new Resource();
    resource.setResource("http://differentdummylink.com");
    coverage.setResource(resource);
    choice.setCoverage(coverage);

    List<Choice> choices = new ArrayList<>();
    choices.add(choice);
    PROXY_EUROPEANA.setChoiceList(choices);

    rdf.setProxyList(Collections.singletonList(PROXY_EUROPEANA));

    String link = "http://dummylink.com";
    Set<FieldType> linkTypes = new HashSet<>();
    linkTypes.add(FieldType.DC_COVERAGE);

    RdfProxyUtils.appendLinkToEuropeanaProxy(rdf, link, linkTypes);

    assertEquals(2, rdf.getProxyList().get(0).getChoiceList().size());
    assertEquals("http://differentdummylink.com",
        rdf.getProxyList().get(0).getChoiceList().get(0).getCoverage().getResource().getResource());
    assertEquals(link, rdf.getProxyList().get(0).getChoiceList().get(1).getCoverage().getResource().getResource());

  }

  @Test
  void testAppendLinkToEuropeanaProxyAlreadyExists(){

    RDF rdf = new RDF();

    EuropeanaProxy europeanaProxy = new EuropeanaProxy();
    europeanaProxy.setEuropeanaProxy(true);

    PROXY_EUROPEANA.setAbout("/proxy/europeana/260/_kmo_av_sid_45006");
    PROXY_EUROPEANA.setEuropeanaProxy(europeanaProxy);

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

    rdf.setProxyList(Collections.singletonList(PROXY_EUROPEANA));

    Set<FieldType> linkTypes = new HashSet<>();
    linkTypes.add(FieldType.DC_COVERAGE);

    RdfProxyUtils.appendLinkToEuropeanaProxy(rdf, link, linkTypes);

    assertEquals(1, rdf.getProxyList().get(0).getChoiceList().size());
    assertEquals(link, rdf.getProxyList().get(0).getChoiceList().get(0).getCoverage().getResource().getResource());

  }

  @Test
  void testGetProviderProxy(){

    RDF rdf = new RDF();

    final ProxyType proxyProvider = new ProxyType();

    EuropeanaProxy europeanaProxy = new EuropeanaProxy();
    EuropeanaProxy providerProxy = new EuropeanaProxy();
    europeanaProxy.setEuropeanaProxy(true);
    providerProxy.setEuropeanaProxy(false);

    proxyProvider.setAbout("/proxy/provider/260/_kmo_av_sid_45006");
    proxyProvider.setEuropeanaProxy(providerProxy);

    PROXY_EUROPEANA.setAbout("/proxy/europeana/260/_kmo_av_sid_45006");
    PROXY_EUROPEANA.setEuropeanaProxy(europeanaProxy);

    ArrayList<ProxyType> proxyList = new ArrayList<>();
    proxyList.add(PROXY_EUROPEANA);
    proxyList.add(proxyProvider);

    rdf.setProxyList(proxyList);

    ProxyType output = RdfProxyUtils.getProviderProxy(rdf);

    assertNotNull(output);
    assertFalse(output.getEuropeanaProxy().isEuropeanaProxy());
    assertEquals(proxyProvider, output);

  }

  @Test
  void testGetProviderProxyWithoutProvider(){

    RDF rdf = new RDF();

    EuropeanaProxy europeanaProxy = new EuropeanaProxy();
    europeanaProxy.setEuropeanaProxy(true);

    PROXY_EUROPEANA.setAbout("/proxy/europeana/260/_kmo_av_sid_45006");
    PROXY_EUROPEANA.setEuropeanaProxy(europeanaProxy);

    ArrayList<ProxyType> proxyList = new ArrayList<>();
    proxyList.add(PROXY_EUROPEANA);

    rdf.setProxyList(proxyList);

    Exception exception = assertThrows(RuntimeException.class, () -> {
      RdfProxyUtils.getProviderProxy(rdf);;
    });

    assertEquals(exception.getMessage(), "Could not find provider proxy.");


  }
}
