package eu.europeana.indexing.fullbean;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.entity.AbstractEdmEntityImpl;
import eu.europeana.corelib.solr.entity.AggregationImpl;
import eu.europeana.corelib.solr.entity.PlaceImpl;
import eu.europeana.corelib.solr.entity.ProvidedCHOImpl;
import eu.europeana.indexing.base.IndexingTestUtils;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.metis.schema.convert.RdfConversionUtils;
import eu.europeana.metis.schema.convert.SerializationException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class RdfToFullBeanConverterTest {

  @Test
  void convertRdfToFullBean() throws SerializationException {
    RdfToFullBeanConverter rdfToFullBeanConverter = new RdfToFullBeanConverter();
    final RdfConversionUtils conversionUtils = new RdfConversionUtils();
    final RdfWrapper inputRdf = new RdfWrapper(
        conversionUtils.convertStringToRdf(
            IndexingTestUtils.getResourceFileContent("europeana_record_rdf_conversion.xml")
        )
    );

    FullBeanImpl fullBean = rdfToFullBeanConverter.convertRdfToFullBean(inputRdf);

    assertAbout(fullBean);
    assertAgents(fullBean);
    assertAggregations(fullBean);
    assertConcepts(fullBean);
    assertEuropeanaAggregation(fullBean);
    assertEuropeanaCollection(fullBean);
    assertLicenses(fullBean);
    assertOrganizations(fullBean);
    assertPlaces(fullBean);
    assertProvidedCHOs(fullBean);
    assertProxies(fullBean);
    assertQualityAnnotations(fullBean);
    assertServices(fullBean);
    assertSizes(fullBean);
    assertTimeSpans(fullBean);
    assertTimestamps(fullBean);
  }

  private static void assertTimestamps(FullBeanImpl fullBean) {
    assertEquals("1970-01-01T00:00:00Z", fullBean.getTimestampCreated().toInstant().toString());
    assertEquals("1970-01-01T00:00:00Z", fullBean.getTimestampUpdated().toInstant().toString());
  }

  private static void assertAbout(FullBeanImpl fullBean) {
    assertEquals("/277/CMC_HA_1185", fullBean.getAbout());
  }

  private static void assertEuropeanaCollection(FullBeanImpl fullBean) {
    assertEquals(Set.of("277_local_09012024_1543"), Arrays.stream(fullBean.getEuropeanaCollectionName())
                                                          .collect(Collectors.toSet()));
  }

  private static void assertProxies(FullBeanImpl fullBean) {
    assertEquals(Set.of("/proxy/europeana/277/CMC_HA_1185", "/proxy/provider/277/CMC_HA_1185"),
        fullBean.getProxies()
                .stream()
                .map(AbstractEdmEntityImpl::getAbout)
                .collect(Collectors.toSet()));
  }

  private static void assertAgents(FullBeanImpl fullBean) {
    assertNotNull(fullBean.getAgents());
    assertEquals(0, fullBean.getAgents().size());
  }

  private static void assertLicenses(FullBeanImpl fullBean) {
    assertNotNull(fullBean.getLicenses());
    assertEquals(0, fullBean.getLicenses().size());
  }

  private static void assertServices(FullBeanImpl fullBean) {
    assertNotNull(fullBean.getServices());
    assertEquals(0, fullBean.getServices().size());
  }

  private static void assertQualityAnnotations(FullBeanImpl fullBean) {
    assertNotNull(fullBean.getQualityAnnotations());
    assertEquals(0, fullBean.getQualityAnnotations().size());
  }

  private static void assertProvidedCHOs(FullBeanImpl fullBean) {
    assertEquals(Set.of("/277/CMC_HA_1185"), fullBean.getProvidedCHOs()
                                                     .stream()
                                                     .map(AbstractEdmEntityImpl::getAbout)
                                                     .collect(Collectors.toSet()));
  }

  private static void assertEuropeanaAggregation(FullBeanImpl fullBean) {
    assertNotNull(fullBean.getEuropeanaAggregation());
    assertEquals("http://www.cmcassociates.co.uk/Skara_Brae/images/panorama_photos/link7-8grassyknoll/_I2P1856_th.jpg",
        fullBean.getEuropeanaAggregation().getEdmPreview());
    assertEquals("/aggregation/europeana/277/CMC_HA_1185", fullBean.getEuropeanaAggregation().getAbout());
  }

  private static void assertAggregations(FullBeanImpl fullBean) {
    assertEquals(Set.of("http://www.cmcassociates.co.uk/Skara_Brae/images/panorama_photos/link7-8grassyknoll/_I2P1856.JPG"),
        fullBean.getAggregations()
                .stream()
                .map(AggregationImpl::getEdmIsShownBy)
                .collect(Collectors.toSet()));
    assertEquals(Set.of("http://www.cmcassociates.co.uk/Skara_Brae/landing/sb_pass_photo.html"),
        fullBean.getAggregations()
                .stream()
                .map(AggregationImpl::getEdmIsShownAt)
                .collect(Collectors.toSet()));
    assertEquals(Set.of("http://www.cmcassociates.co.uk/Skara_Brae/images/panorama_photos/link7-8grassyknoll/_I2P1856_th.jpg"),
        fullBean.getAggregations()
                .stream()
                .map(AggregationImpl::getEdmObject)
                .collect(Collectors.toSet()));
    assertEquals(Set.of("/aggregation/provider/277/CMC_HA_1185"),
        fullBean.getAggregations()
                .stream()
                .map(AggregationImpl::getAbout)
                .collect(Collectors.toSet()));
  }

  private static void assertConcepts(FullBeanImpl fullBean) {
    assertEquals(Set.of("http://vocab.getty.edu/aat/300000810",
            "http://vocab.getty.edu/aat/300008372",
            "http://data.europeana.eu/concept/48",
            "http://data.europeana.eu/concept/25"),
        fullBean.getConcepts()
                .stream()
                .map(AbstractEdmEntityImpl::getAbout)
                .collect(Collectors.toSet()));
  }

  private static void assertOrganizations(FullBeanImpl fullBean) {
    assertEquals(Set.of("http://data.europeana.eu/organization/1482250000004502043",
            "http://data.europeana.eu/organization/1482250000004671084"),
        fullBean.getOrganizations()
                .stream()
                .map(AbstractEdmEntityImpl::getAbout)
                .collect(Collectors.toSet()));
  }

  private static void assertTimeSpans(FullBeanImpl fullBean) {
    assertEquals(Set.of("HA/1185#timespan", "#19XX",
            "http://data.europeana.eu/timespan/20"),
        fullBean.getTimespans()
                .stream()
                .map(AbstractEdmEntityImpl::getAbout)
                .collect(Collectors.toSet()));
  }

  private static void assertSizes(FullBeanImpl fullBean) {
    assertEquals(10, fullBean.getEuropeanaCompleteness());
    assertEquals(2, fullBean.getOrganizations().size());
    assertEquals(0, fullBean.getAgents().size());
    assertEquals(3, fullBean.getTimespans().size());
    assertEquals(4, fullBean.getConcepts().size());
    assertEquals(1, fullBean.getAggregations().size());
    assertEquals(1, fullBean.getProvidedCHOs().size());
    assertEquals(2, fullBean.getProxies().size());
    assertEquals(0, fullBean.getLicenses().size());
    assertEquals(0, fullBean.getServices().size());
    assertEquals(0, fullBean.getQualityAnnotations().size());
  }

  private static void assertPlaces(FullBeanImpl fullBean) {
    PlaceImpl place = new PlaceImpl();
    place.setAbout("iid:6744/SP.1");
    place.setLatitude(59.04861f);
    place.setLongitude(-3.343056f);
    place.setAltitude(7.0f);
    assertTrue(fullBean.getPlaces().contains(place));
  }
}
