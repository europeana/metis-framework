package eu.europeana.indexing.solr.property;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.junit.jupiter.api.Assertions.assertEquals;

import eu.europeana.corelib.definitions.edm.entity.Place;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.corelib.solr.entity.PlaceImpl;
import eu.europeana.corelib.solr.entity.ProxyImpl;
import eu.europeana.indexing.solr.EdmLabel;
import eu.europeana.metis.schema.jibx.EdmType;
import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
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

    // proxy setup
    ProxyImpl proxy = new ProxyImpl();
    proxy.setAbout("About Proxy");
    proxy.setEuropeanaProxy(true);
    proxy.setEdmType(EdmType.TEXT.name());
    proxy.setEdmCurrentLocation(Map.of(EdmLabel.PROXY_EDM_CURRENT_LOCATION.name(), List.of(place.getAbout())));
    proxy.setDctermsSpatial(Map.of(EdmLabel.PROXY_DCTERMS_SPATIAL.name(), List.of(place.getAbout())));
    proxy.setDcCoverage(Map.of(EdmLabel.PROXY_DC_COVERAGE.name(), List.of(place.getAbout())));
    fullBean.setProxies(List.of(proxy));

    fullBean.setEuropeanaCompleteness(0);
    fullBean.setEuropeanaCollectionName(new String[]{"Europeana Collection Name"});
    fullBean.setTimestampCreated(Date.from(Instant.now().truncatedTo(DAYS)));
    fullBean.setTimestampUpdated(Date.from(Instant.now().truncatedTo(DAYS).plus(1, DAYS)));

    // method to test
    fullBeanSolrProperties.setProperties(solrInputDocument, fullBean);

    // assertions
    assertEquals(EdmType.TEXT.name(), solrInputDocument.getFieldValue(EdmLabel.PROVIDER_EDM_TYPE.toString()));
    assertEquals("10,33", solrInputDocument.getFieldValue(EdmLabel.CURRENT_LOCATION_WGS.toString()));
    assertEquals("10,33", solrInputDocument.getFieldValue(EdmLabel.COVERAGE_LOCATION_WGS.toString()));
    assertEquals("10,33", solrInputDocument.getFieldValue(EdmLabel.LOCATION_WGS.toString()));
    assertEquals(0, solrInputDocument.getFieldValue(EdmLabel.EUROPEANA_COMPLETENESS.toString()));
    assertEquals("Europeana Collection Name", solrInputDocument.getFieldValue(EdmLabel.EUROPEANA_COLLECTIONNAME.toString()));
    assertEquals(solrInputDocument.getFieldValue(EdmLabel.TIMESTAMP_CREATED.toString()),
        Date.from(Instant.now().truncatedTo(DAYS)));
    assertEquals(solrInputDocument.getFieldValue(EdmLabel.TIMESTAMP_UPDATED.toString()),
        Date.from(Instant.now().truncatedTo(DAYS).plus(1, DAYS)));
    assertEquals(8, solrInputDocument.size());

  }

  @Test
  void fullBeanSolrSetPropertiesMultipleProxiesTest() {

    FullBeanImpl fullBean = new FullBeanImpl();

    // agent setup
    AgentImpl agent = new AgentImpl();
    agent.setAbout("About Agent");
    fullBean.setAgents(List.of(agent));

    // place setup
    Place place1 = new PlaceImpl();
    place1.setAbout("Somewhere1");
    place1.setLatitude(10.0f);
    place1.setLongitude(33.0f);
    place1.setAltitude(7.0f);

    Place place2 = new PlaceImpl();
    place2.setAbout("Somewhere2");
    place2.setLatitude(40.0f);
    place2.setLongitude(53.0f);
    place2.setAltitude(36.0f);

    fullBean.setPlaces(List.of(place1, place2));

    // proxy setup
    ProxyImpl proxy1 = new ProxyImpl();
    proxy1.setAbout("About Proxy1");
    proxy1.setEuropeanaProxy(true);
    proxy1.setEdmType(EdmType.IMAGE.name());
    proxy1.setEdmCurrentLocation(Map.of(EdmLabel.PROXY_EDM_CURRENT_LOCATION.name(), List.of(place1.getAbout())));
    proxy1.setDctermsSpatial(Map.of(EdmLabel.PROXY_DCTERMS_SPATIAL.name(), List.of(place1.getAbout())));
    proxy1.setDcCoverage(Map.of(EdmLabel.PROXY_DC_COVERAGE.name(), List.of(place1.getAbout())));
    proxy1.setEdmHasType(Map.of(EdmType.IMAGE.name(), List.of(EdmType.IMAGE.toString())));

    ProxyImpl proxy2 = new ProxyImpl();
    proxy2.setAbout("About Proxy2");
    proxy2.setEuropeanaProxy(true);
    proxy2.setEdmType(EdmType.SOUND.name());
    proxy2.setEdmCurrentLocation(Map.of(EdmLabel.PROXY_EDM_CURRENT_LOCATION.name(), List.of(place2.getAbout())));
    proxy2.setDctermsSpatial(Map.of(EdmLabel.PROXY_DCTERMS_SPATIAL.name(), List.of(place2.getAbout())));
    proxy2.setDcCoverage(Map.of(EdmLabel.PROXY_DC_COVERAGE.name(), List.of(place2.getAbout())));
    proxy2.setEdmHasType(Map.of(EdmType.SOUND.name(), List.of(EdmType.SOUND.toString())));

    List<ProxyImpl> proxies = new ArrayList<>();
    proxies.add(proxy1);
    proxies.add(proxy2);
    proxies.add(null);
    fullBean.setProxies(proxies);

    fullBean.setEuropeanaCompleteness(0);
    fullBean.setEuropeanaCollectionName(new String[]{"Europeana Collection Name"});
    fullBean.setTimestampCreated(Date.from(Instant.now().truncatedTo(DAYS)));
    fullBean.setTimestampUpdated(Date.from(Instant.now().truncatedTo(DAYS).plus(1, DAYS)));

    // method to test
    fullBeanSolrProperties.setProperties(solrInputDocument, fullBean);

    // assertions
    assertEquals(EdmType.IMAGE.name(), solrInputDocument.getFieldValue(EdmLabel.PROVIDER_EDM_TYPE.toString()));
    assertEquals("40,53", solrInputDocument.getFieldValue(EdmLabel.CURRENT_LOCATION_WGS.toString()));
    assertEquals("40,53", solrInputDocument.getFieldValue(EdmLabel.COVERAGE_LOCATION_WGS.toString()));
    assertEquals("40,53", solrInputDocument.getFieldValue(EdmLabel.LOCATION_WGS.toString()));
    assertEquals(0, solrInputDocument.getFieldValue(EdmLabel.EUROPEANA_COMPLETENESS.toString()));
    assertEquals("Europeana Collection Name", solrInputDocument.getFieldValue(EdmLabel.EUROPEANA_COLLECTIONNAME.toString()));
    assertEquals(solrInputDocument.getFieldValue(EdmLabel.TIMESTAMP_CREATED.toString()),
        Date.from(Instant.now().truncatedTo(DAYS)));
    assertEquals(solrInputDocument.getFieldValue(EdmLabel.TIMESTAMP_UPDATED.toString()),
        Date.from(Instant.now().truncatedTo(DAYS).plus(1, DAYS)));
    assertEquals(8, solrInputDocument.size());
  }
}
