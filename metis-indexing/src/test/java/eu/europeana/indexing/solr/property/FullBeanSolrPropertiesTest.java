package eu.europeana.indexing.solr.property;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.junit.jupiter.api.Assertions.assertEquals;

import eu.europeana.corelib.definitions.edm.entity.Place;
import eu.europeana.corelib.definitions.solr.DocType;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.corelib.solr.entity.PlaceImpl;
import eu.europeana.corelib.solr.entity.ProxyImpl;
import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.solr.common.SolrInputDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FullBeanSolrPropertiesTest {

  private FullBeanSolrProperties fullBeanSolrProperties;
  private SolrInputDocument solrInputDocument;


  @BeforeEach
  void setup() {
    solrInputDocument = new SolrInputDocument();
    fullBeanSolrProperties = new FullBeanSolrProperties();
  }


  @Test
  void fullBeanSolrSetPropertiesTest() {

    FullBeanImpl fullBean = new FullBeanImpl();

    // proxy setup
    ProxyImpl proxy = new ProxyImpl();
    proxy.setAbout("About Proxy");
    proxy.setEdmType(DocType.TEXT.getEnumNameValue());
    proxy.setEdmHasType(Map.of("edm types", List.of(DocType.TEXT.getEnumNameValue())));
    proxy.setEuropeanaProxy(true);
    proxy.setEdmCurrentLocation(Map.of("Somewhere", List.of("Nowhere")));
    proxy.setDctermsSpatial(Map.of("Somewhere", List.of("Nowhere")));
    proxy.setDcCoverage(Map.of("Somewhere", List.of("Nowhere")));
    fullBean.setProxies(List.of(proxy));

    // agent setup
    AgentImpl agent = new AgentImpl();
    agent.setAbout("About Agent");
    fullBean.setAgents(List.of(agent));

    // place setup
    Place place = new PlaceImpl();
    place.setAbout("Nowhere");
    place.setLatitude(10.0f);
    place.setLongitude(33.0f);
    place.setAltitude(7.0f);
    fullBean.setPlaces(List.of(place));

    fullBean.setEuropeanaCompleteness(0);
    fullBean.setEuropeanaCollectionName(new String[]{"Europeana Collection Name"});
    fullBean.setTimestampCreated(Date.from(Instant.now().truncatedTo(DAYS)));

    fullBean.setTimestampUpdated(Date.from(Instant.now().truncatedTo(DAYS).plus(1, DAYS)));

    // method to test
    fullBeanSolrProperties.setProperties(solrInputDocument, fullBean);

    // assertions
    assertEquals("TEXT", solrInputDocument.getFieldValue("proxy_edm_type"));
    assertEquals("10,33", solrInputDocument.getFieldValue("currentLocation_wgs"));
    assertEquals("10,33", solrInputDocument.getFieldValue("coverageLocation_wgs"));
    assertEquals("10,33", solrInputDocument.getFieldValue("location_wgs"));
    assertEquals(0, solrInputDocument.getFieldValue("europeana_completeness"));
    assertEquals("Europeana Collection Name", solrInputDocument.getFieldValue("europeana_collectionName"));
    assertEquals(solrInputDocument.getFieldValue("timestamp_created"), Date.from(Instant.now().truncatedTo(DAYS)));
    assertEquals(solrInputDocument.getFieldValue("timestamp_update"),
        Date.from(Instant.now().truncatedTo(DAYS).plus(1, DAYS)));
    assertEquals(8, solrInputDocument.size());

  }

  @Test
  void fullBeanSolrSetPropertiesNullProxyTest() {

    FullBeanImpl fullBean = new FullBeanImpl();
    // proxy setup
    ProxyImpl proxy1 = new ProxyImpl();
    proxy1.setAbout("About Proxy1");
    proxy1.setEdmType(DocType.TEXT.getEnumNameValue());
    proxy1.setEdmHasType(Map.of("edm types", List.of(DocType.TEXT.getEnumNameValue())));
    proxy1.setEuropeanaProxy(true);
    proxy1.setEdmCurrentLocation(Map.of("Somewhere", List.of("Nowhere")));
    proxy1.setDctermsSpatial(Map.of("Somewhere", List.of("Nowhere")));

    ProxyImpl proxy2 = new ProxyImpl();
    proxy2.setAbout("About Proxy2");
    proxy2.setEdmType(DocType._3D.getEnumNameValue());
    proxy2.setEdmHasType(Map.of("edm types", List.of(DocType._3D.getEnumNameValue())));
    proxy2.setEuropeanaProxy(true);
    proxy2.setEdmCurrentLocation(Map.of("Somewhere", List.of("Nowhere")));
    proxy2.setDctermsSpatial(Map.of("Somewhere", List.of("Nowhere")));

    List<ProxyImpl> proxies = new ArrayList<>();
    proxies.add(proxy1);
    proxies.add(proxy2);
    proxies.add(null);

    fullBean.setProxies(proxies);

    // agent setup
    AgentImpl agent = new AgentImpl();
    agent.setAbout("About Agent");
    fullBean.setAgents(List.of(agent));

    // place setup
    Place place1 = new PlaceImpl();
    place1.setAbout("Nowhere");
    place1.setLatitude(10.0f);
    place1.setLongitude(33.0f);
    place1.setAltitude(7.0f);

    Place place2 = new PlaceImpl();
    place2.setAbout("Nowhere");
    place2.setLatitude(40.0f);
    place2.setLongitude(53.0f);
    place2.setAltitude(36.0f);

    fullBean.setPlaces(List.of(place1, place2));

    fullBean.setEuropeanaCompleteness(0);
    fullBean.setEuropeanaCollectionName(new String[]{"Europeana Collection Name"});
    fullBean.setTimestampCreated(Date.from(Instant.now().truncatedTo(DAYS)));

    fullBean.setTimestampUpdated(Date.from(Instant.now().truncatedTo(DAYS).plus(1, DAYS)));

    // method to test
    fullBeanSolrProperties.setProperties(solrInputDocument, fullBean);

    // assertions
    // List.of("10","33") doesn't work as expected, it adds a whitespace between the two values
    List<String> wgs = new ArrayList();
    wgs.add("10,33");
    assertEquals(List.of("TEXT", "3D"), solrInputDocument.getFieldValues("proxy_edm_type"));
    assertEquals(wgs, solrInputDocument.getFieldValues("currentLocation_wgs"));
    assertEquals(wgs, solrInputDocument.getFieldValues("coverageLocation_wgs"));
    assertEquals(wgs, solrInputDocument.getFieldValues("location_wgs"));
    assertEquals(0, solrInputDocument.getFieldValue("europeana_completeness"));
    assertEquals("Europeana Collection Name", solrInputDocument.getFieldValue("europeana_collectionName"));
    assertEquals(solrInputDocument.getFieldValue("timestamp_created"), Date.from(Instant.now().truncatedTo(DAYS)));
    assertEquals(solrInputDocument.getFieldValue("timestamp_update"),
        Date.from(Instant.now().truncatedTo(DAYS).plus(1, DAYS)));
    assertEquals(8, solrInputDocument.size());

  }

}
