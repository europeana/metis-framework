package eu.europeana.indexing.solr.property;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.corelib.definitions.edm.entity.Place;
import eu.europeana.corelib.definitions.solr.DocType;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.corelib.solr.entity.PlaceImpl;
import eu.europeana.corelib.solr.entity.ProxyImpl;
import java.sql.Date;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.apache.solr.common.SolrInputDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FullBeanSolrPropertiesTest {


  private FullBeanSolrProperties fullBeanSolrProperties;
  private SolrInputDocument solrInputDocument;
  private FullBeanImpl fullBean;


  @BeforeEach
  void setup() {
    solrInputDocument = new SolrInputDocument();
    fullBean = new FullBeanImpl();
    fullBeanSolrProperties = new FullBeanSolrProperties();
  }


  @Test
  void fullBeanSolrSetPropertiesTest() {

    // proxy setup
    ProxyImpl proxy = new ProxyImpl();
    proxy.setAbout("About Proxy");
    proxy.setEdmType(DocType.TEXT.getEnumNameValue());
    proxy.setEdmHasType(Map.of("edm types", List.of(DocType.TEXT.getEnumNameValue())));
    proxy.setEuropeanaProxy(true);
    proxy.setEdmCurrentLocation(Map.of("Somewhere", List.of("Nowhere")));
    proxy.setDctermsSpatial(Map.of("Somewhere", List.of("Nowhere")));

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

}
