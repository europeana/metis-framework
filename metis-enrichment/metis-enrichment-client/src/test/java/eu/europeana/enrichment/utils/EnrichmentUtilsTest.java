package eu.europeana.enrichment.utils;

import eu.europeana.metis.schema.convert.SerializationException;
import eu.europeana.metis.schema.jibx.EuropeanaProxy;
import eu.europeana.metis.schema.jibx.ProxyFor;
import eu.europeana.metis.schema.jibx.ProxyIn;
import eu.europeana.metis.schema.jibx.ProxyType;
import eu.europeana.metis.schema.jibx.RDF;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class EnrichmentUtilsTest {

  private static RDF testRdf;

  @BeforeAll
  static void setUp() throws FileNotFoundException, SerializationException {

//    testRdf = RdfConversionUtils.convertInputStreamToRdf(
//        new FileInputStream("src/test/resources/sample_enrichment.xml"));
    ProxyType proxy = new ProxyType();
    ProxyFor proxyFor = new ProxyFor();
    ProxyIn proxyIn = new ProxyIn();
    EuropeanaProxy europeanaProxy = new EuropeanaProxy();

    proxyFor.setResource("/260/_kmo_av_sid_45006");
    proxyIn.setResource("/aggregation/europeana/260/_kmo_av_sid_45006");
    List<ProxyIn> proxyInList = new ArrayList<>();
    proxyInList.add(proxyIn);
    europeanaProxy.setEuropeanaProxy(true);

    proxy.setProxyFor(proxyFor);
    proxy.setProxyInList(proxyInList);
    proxy.setEuropeanaProxy(europeanaProxy);

    ArrayList<ProxyType> proxyList = new ArrayList<>();
    proxyList.add(proxy);
    testRdf.setProxyList(proxyList);


  }

  @Test
  void testSetAdditionalData(){

    EnrichmentUtils.setAdditionalData(testRdf);

    System.out.println(testRdf.getAggregationList());

  }

}
