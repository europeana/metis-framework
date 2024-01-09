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

    assertEquals("/277/CMC_HA_1185", fullBean.getAbout());
    assertEquals(10, fullBean.getEuropeanaCompleteness());
    assertEquals(2, fullBean.getOrganizations().size());
    assertEquals(0, fullBean.getAgents().size());
    assertEquals(3, fullBean.getTimespans().size());
    assertEquals(4, fullBean.getConcepts().size());
    assertEquals(1, fullBean.getAggregations().size());
    assertEquals(1, fullBean.getProvidedCHOs().size());
    assertNotNull(fullBean.getEuropeanaAggregation());
    assertNull(fullBean.getEuropeanaAggregation().getDqvHasQualityAnnotation());
    assertEquals(2, fullBean.getProxies().size());
    assertEquals(0, fullBean.getLicenses().size());
    assertEquals(0, fullBean.getServices().size());
    assertEquals(0, fullBean.getQualityAnnotations().size());
    assertEquals("277_local_09012024_1543", fullBean.getEuropeanaCollectionName()[0]);
  }
}
