package eu.europeana.indexing.fullbean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.indexing.base.IndexingTestUtils;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.metis.schema.convert.RdfConversionUtils;
import eu.europeana.metis.schema.convert.SerializationException;
import org.junit.jupiter.api.Test;

class RdfToFullBeanConverterTest {

  @Test
  void convertRdfToFullBean() throws SerializationException {
    RdfToFullBeanConverter rdfToFullBeanConverter = new RdfToFullBeanConverter();
    final RdfConversionUtils conversionUtils = new RdfConversionUtils();
    final RdfWrapper inputRdf = new RdfWrapper(conversionUtils.convertStringToRdf(
        IndexingTestUtils.getResourceFileContent("europeana_record_rdf.xml")));

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
    assertEquals("277_local_09012024_1543", fullBean.getEuropeanaCollectionName()[0]);
  }

  private static void assertProxies(FullBeanImpl fullBean) {
    assertEquals("/proxy/europeana/277/CMC_HA_1185", fullBean.getProxies().get(0).getAbout());
    assertEquals("/proxy/provider/277/CMC_HA_1185", fullBean.getProxies().get(1).getAbout());
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
    assertEquals("/277/CMC_HA_1185", fullBean.getProvidedCHOs().getFirst().getAbout());
    assertEquals(0, fullBean.getProvidedCHOs().getFirst().getOwlSameAs().length);
  }

  private static void assertEuropeanaAggregation(FullBeanImpl fullBean) {
    assertNotNull(fullBean.getEuropeanaAggregation());
    assertEquals("http://www.cmcassociates.co.uk/Skara_Brae/images/panorama_photos/link7-8grassyknoll/_I2P1856_th.jpg",
        fullBean.getEuropeanaAggregation().getEdmPreview());
    assertEquals("/aggregation/europeana/277/CMC_HA_1185", fullBean.getEuropeanaAggregation().getAbout());
    assertNull(fullBean.getEuropeanaAggregation().getDqvHasQualityAnnotation());
  }

  private static void assertAggregations(FullBeanImpl fullBean) {
    assertEquals("http://www.cmcassociates.co.uk/Skara_Brae/images/panorama_photos/link7-8grassyknoll/_I2P1856.JPG",
        fullBean.getAggregations().getFirst().getEdmIsShownBy());
    assertEquals("http://www.cmcassociates.co.uk/Skara_Brae/landing/sb_pass_photo.html",
        fullBean.getAggregations().getFirst().getEdmIsShownAt());
    assertEquals("http://www.cmcassociates.co.uk/Skara_Brae/images/panorama_photos/link7-8grassyknoll/_I2P1856_th.jpg",
        fullBean.getAggregations().getFirst().getEdmObject());
    assertEquals("/aggregation/provider/277/CMC_HA_1185", fullBean.getAggregations().getFirst().getAbout());
  }

  private static void assertConcepts(FullBeanImpl fullBean) {
    assertEquals("http://vocab.getty.edu/aat/300000810", fullBean.getConcepts().get(0).getAbout());
    assertEquals("http://vocab.getty.edu/aat/300008372", fullBean.getConcepts().get(1).getAbout());
    assertEquals("http://data.europeana.eu/concept/48", fullBean.getConcepts().get(2).getAbout());
    assertEquals("http://data.europeana.eu/concept/25", fullBean.getConcepts().get(3).getAbout());
  }

  private static void assertOrganizations(FullBeanImpl fullBean) {
    assertEquals("http://data.europeana.eu/organization/1482250000004502043", fullBean.getOrganizations().get(0).getAbout());
    assertEquals("http://data.europeana.eu/organization/1482250000004671084", fullBean.getOrganizations().get(1).getAbout());
  }

  private static void assertTimeSpans(FullBeanImpl fullBean) {
    assertEquals("HA/1185#timespan", fullBean.getTimespans().get(0).getAbout());
    assertEquals("#19XX", fullBean.getTimespans().get(1).getAbout());
    assertEquals("http://data.europeana.eu/timespan/20", fullBean.getTimespans().get(2).getAbout());
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
    assertEquals("iid:6744/SP.1", fullBean.getPlaces().getFirst().getAbout());
    assertEquals(59.04861f, fullBean.getPlaces().getFirst().getLatitude());
    assertEquals(-3.343056f, fullBean.getPlaces().getFirst().getLongitude());
    assertEquals(7.0f, fullBean.getPlaces().getFirst().getAltitude());
  }
}
