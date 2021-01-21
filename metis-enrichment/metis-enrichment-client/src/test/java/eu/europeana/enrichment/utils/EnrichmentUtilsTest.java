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

import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EnrichmentUtilsTest {

  private final static RDF TEST_RDF = spy(new RDF());
  private final static ProxyType PROXY_EUROPEANA = new ProxyType();
  private final static ProxyType PROXY_PROVIDER = new ProxyType();
  private final static EuropeanaAggregationType EUROPEANA_AGGREGATION_TYPE = new EuropeanaAggregationType();

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
        .filter(x -> x.getEuropeanaProxy().isEuropeanaProxy())
        .collect(Collectors.toList()).get(0);

    assertEquals(proxyResult.getYearList().size(), 4);
    assertEquals(proxyResult.getYearList().get(0).getString(), "1990");
    assertEquals(proxyResult.getYearList().get(1).getString(), "1991");
    assertEquals(proxyResult.getYearList().get(2).getString(), "1992");
    assertEquals(proxyResult.getYearList().get(3).getString(), "1993");

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
        .collect(Collectors.toList()).get(0);

    assertEquals(proxyResult.getYearList().size(), 1);
    assertEquals(proxyResult.getYearList().get(0).getString(), "1990");

  }

  @Test
  void testSetAdditionalDataCompletenessNone(){

    ArrayList<ProxyType> proxyList = new ArrayList<>();
    proxyList.add(PROXY_EUROPEANA);
    proxyList.add(PROXY_PROVIDER);

    TEST_RDF.setProxyList(proxyList);

    EnrichmentUtils.setAdditionalData(TEST_RDF);

    EuropeanaAggregationType aggregationTypeResult = TEST_RDF.getEuropeanaAggregationList().get(0);

    assertEquals(aggregationTypeResult.getCompleteness().getString(), "0");
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

    EuropeanaAggregationType aggregationTypeResult = TEST_RDF.getEuropeanaAggregationList().get(0);

    assertTrue(Integer.parseInt(aggregationTypeResult.getCompleteness().getString()) > 0);
  }

  @Test
  void testSetAdditionalDataEmptyProxies() {
    RDF newRdf = new RDF();
    RDF toCompare = new RDF();
    ProxyType emptyEuropeanaProxy = new ProxyType();
    ProxyType emptyProviderProxy = new ProxyType();
    EuropeanaAggregationType emptyAggregation = new EuropeanaAggregationType();

    ArrayList<ProxyType> proxyList = new ArrayList<>();
    proxyList.add(emptyEuropeanaProxy);
    proxyList.add(emptyProviderProxy);

    newRdf.setEuropeanaAggregationList(Collections.singletonList(emptyAggregation));
    newRdf.setProxyList(proxyList);
    toCompare.setEuropeanaAggregationList(Collections.singletonList(emptyAggregation));
    toCompare.setProxyList(proxyList);

    EnrichmentUtils.setAdditionalData(newRdf);

    assertArrayEquals(newRdf.getProxyList().toArray(), toCompare.getProxyList().toArray());
    assertEquals(newRdf.getEuropeanaAggregationList().get(0), toCompare.getEuropeanaAggregationList().get(0));
  }
}
