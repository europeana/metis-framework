package eu.europeana.enrichment.utils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import eu.europeana.metis.schema.jibx.Aggregation;
import eu.europeana.metis.schema.jibx.Created;
import eu.europeana.metis.schema.jibx.Date;
import eu.europeana.metis.schema.jibx.EuropeanaAggregationType;
import eu.europeana.metis.schema.jibx.EuropeanaProxy;
import eu.europeana.metis.schema.jibx.EuropeanaType.Choice;
import eu.europeana.metis.schema.jibx.Issued;
import eu.europeana.metis.schema.jibx.ProxyType;
import eu.europeana.metis.schema.jibx.RDF;
import eu.europeana.metis.schema.jibx.Temporal;
import eu.europeana.metis.schema.jibx.Title;
import eu.europeana.metis.schema.jibx._Object;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EnrichmentUtilsTest {

  private static final RDF TEST_RDF = spy(new RDF());
  private static final ProxyType PROXY_EUROPEANA = new ProxyType();
  private static final ProxyType PROXY_PROVIDER = new ProxyType();
  private static final EuropeanaAggregationType EUROPEANA_AGGREGATION_TYPE = new EuropeanaAggregationType();

  @BeforeEach
  void setUp() {

    EuropeanaProxy europeanaProxy = new EuropeanaProxy();
    EuropeanaProxy providerProxy = new EuropeanaProxy();
    europeanaProxy.setEuropeanaProxy(true);
    providerProxy.setEuropeanaProxy(false);

    PROXY_EUROPEANA.setAbout("/proxy/europeana/260/_kmo_av_sid_45006");
    PROXY_EUROPEANA.setEuropeanaProxy(europeanaProxy);

    PROXY_PROVIDER.setAbout("/proxy/provider/260/_kmo_av_sid_45006");
    PROXY_PROVIDER.setEuropeanaProxy(providerProxy);

    EUROPEANA_AGGREGATION_TYPE.setAbout("/aggregation/europeana/260/_kmo_av_sid_45006");

    TEST_RDF.setEuropeanaAggregationList(Collections.singletonList(EUROPEANA_AGGREGATION_TYPE));

    doReturn(Collections.singletonList(new Aggregation())).when(TEST_RDF).getAggregationList();
  }

  @Test
  void testSetAdditionalDataAllYearFieldValues(){

    Choice dateChoice = new Choice();
    Date date = new Date();
    Choice temporalChoice = new Choice();
    Temporal temporal = new Temporal();
    Choice createdChoice = new Choice();
    Created created = new Created();
    Choice issuedChoice = new Choice();
    Issued issued = new Issued();

    date.setString("1990");
    dateChoice.setDate(date);

    temporal.setString("1991");
    temporalChoice.setTemporal(temporal);

    created.setString("1992");
    createdChoice.setCreated(created);

    issued.setString("1993");
    issuedChoice.setIssued(issued);

    ArrayList<Choice> choices = new ArrayList<>();
    choices.add(dateChoice);
    choices.add(temporalChoice);
    choices.add(createdChoice);
    choices.add(issuedChoice);

    PROXY_PROVIDER.setChoiceList(choices);

    ArrayList<ProxyType> proxyList = new ArrayList<>();
    proxyList.add(PROXY_EUROPEANA);
    proxyList.add(PROXY_PROVIDER);

    TEST_RDF.setProxyList(proxyList);

    EnrichmentUtils.setAdditionalData(TEST_RDF);

    ProxyType proxyResult = TEST_RDF.getProxyList().stream()
        .filter(x -> x.getEuropeanaProxy().isEuropeanaProxy()).toList().getFirst();

    assertEquals(4, proxyResult.getYearList().size());
    assertEquals("1990", proxyResult.getYearList().get(0).getString());
    assertEquals("1991", proxyResult.getYearList().get(1).getString());
    assertEquals("1992", proxyResult.getYearList().get(2).getString());
    assertEquals("1993", proxyResult.getYearList().get(3).getString());

  }

  @Test
  void testSetAdditionalDataYearFieldWithoutDuplicates(){
    Choice dateChoice = new Choice();
    Date date = new Date();
    Choice temporalChoice = new Choice();
    Temporal temporal = new Temporal();

    date.setString("1990");
    dateChoice.setDate(date);

    temporal.setString("1990");
    temporalChoice.setTemporal(temporal);

    ArrayList<Choice> choices = new ArrayList<>();
    choices.add(dateChoice);
    choices.add(temporalChoice);

    PROXY_PROVIDER.setChoiceList(choices);

    ArrayList<ProxyType> proxyList = new ArrayList<>();
    proxyList.add(PROXY_EUROPEANA);
    proxyList.add(PROXY_PROVIDER);

    TEST_RDF.setProxyList(proxyList);

    EnrichmentUtils.setAdditionalData(TEST_RDF);

    ProxyType proxyResult = TEST_RDF.getProxyList().stream()
        .filter(x -> x.getEuropeanaProxy().isEuropeanaProxy())
        .toList().getFirst();

    assertEquals(1, proxyResult.getYearList().size());
    assertEquals("1990", proxyResult.getYearList().getFirst().getString());

  }

  @Test
  void testSetAdditionalDataCompletenessNone(){

    ArrayList<ProxyType> proxyList = new ArrayList<>();
    proxyList.add(PROXY_EUROPEANA);
    proxyList.add(PROXY_PROVIDER);

    TEST_RDF.setProxyList(proxyList);

    EnrichmentUtils.setAdditionalData(TEST_RDF);

    EuropeanaAggregationType aggregationTypeResult = TEST_RDF.getEuropeanaAggregationList().getFirst();

    assertEquals("0", aggregationTypeResult.getCompleteness().getString());
  }

  @Test
  void testSetAdditionalDataCompletenessMoreThanZero() {
    Aggregation aggregation = spy(new Aggregation());

    Choice dateChoice = new Choice();
    Date date = new Date();
    Choice titleChoice = new Choice();
    Title title = new Title();

    date.setString("1990");
    dateChoice.setDate(date);
    title.setString(
        "The Sudbury Neutrino Observatory: Observation of Flavor Change for Solar Neutrinos");
    titleChoice.setTitle(title);

    ArrayList<Choice> choices = new ArrayList<>();
    choices.add(dateChoice);
    choices.add(titleChoice);

    PROXY_PROVIDER.setChoiceList(choices);

    ArrayList<ProxyType> proxyList = new ArrayList<>();
    proxyList.add(PROXY_EUROPEANA);
    proxyList.add(PROXY_PROVIDER);

    TEST_RDF.setProxyList(proxyList);

    _Object object = new _Object();
    object.setResource("/260/_kmo_av_sid_45006");
    doReturn(object).when(aggregation).getObject();
    doReturn(Collections.singletonList(aggregation)).when(TEST_RDF).getAggregationList();

    EnrichmentUtils.setAdditionalData(TEST_RDF);

    EuropeanaAggregationType aggregationTypeResult = TEST_RDF.getEuropeanaAggregationList().getFirst();

    assertTrue(Integer.parseInt(aggregationTypeResult.getCompleteness().getString()) > 0);
  }

  @Test
  void testSetAdditionalDataEmptyProxies() {
    RDF newRdf = new RDF();
    RDF toCompare = new RDF();
    ProxyType emptyEuropeanaProxy = new ProxyType();
    emptyEuropeanaProxy.setEuropeanaProxy(new EuropeanaProxy());
    emptyEuropeanaProxy.getEuropeanaProxy().setEuropeanaProxy(true);
    ProxyType emptyProviderProxy = new ProxyType();
    EuropeanaAggregationType emptyAggregation = new EuropeanaAggregationType();

    List<ProxyType> proxyList = List.of(emptyEuropeanaProxy, emptyProviderProxy);

    newRdf.setEuropeanaAggregationList(Collections.singletonList(emptyAggregation));
    newRdf.setProxyList(proxyList);
    toCompare.setEuropeanaAggregationList(Collections.singletonList(emptyAggregation));
    toCompare.setProxyList(proxyList);

    EnrichmentUtils.setAdditionalData(newRdf);

    assertArrayEquals(toCompare.getProxyList().toArray(), newRdf.getProxyList().toArray());
    assertEquals(toCompare.getEuropeanaAggregationList().getFirst(), newRdf.getEuropeanaAggregationList().getFirst());
  }
}
