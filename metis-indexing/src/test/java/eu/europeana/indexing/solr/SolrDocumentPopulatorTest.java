package eu.europeana.indexing.solr;

import static eu.europeana.indexing.solr.EdmLabel.COVERAGE_LOCATION_WGS;
import static eu.europeana.indexing.solr.EdmLabel.CURRENT_LOCATION_WGS;
import static eu.europeana.indexing.solr.EdmLabel.EUROPEANA_ID;
import static eu.europeana.indexing.solr.EdmLabel.LOCATION_WGS;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.indexing.fullbean.RdfToFullBeanConverter;
import eu.europeana.indexing.tiers.ClassifierFactory;
import eu.europeana.indexing.utils.RdfTierUtils;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.metis.schema.convert.RdfConversionUtils;
import eu.europeana.metis.schema.jibx.RDF;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import org.apache.commons.collections.CollectionUtils;
import org.apache.solr.common.SolrInputDocument;
import org.junit.jupiter.api.Test;

class SolrDocumentPopulatorTest {

  @Test
  void populateWithProperties_PlaceCoordinates() throws Exception {
    ClassLoader classLoader = SolrDocumentPopulatorTest.class.getClassLoader();
    File file = new File(Objects.requireNonNull(classLoader.getResource("europeana_record_with_geospatial_data.xml")).getFile());
    String xml = new String(Files.readAllBytes(file.toPath()));
    final RDF rdf = new RdfConversionUtils().convertStringToRdf(xml);

    // Perform the tier classification
    final RdfWrapper rdfWrapper = new RdfWrapper(rdf);
    RdfTierUtils.setTier(rdf, ClassifierFactory.getMediaClassifier().classify(rdfWrapper).getTier());
    RdfTierUtils.setTier(rdf, ClassifierFactory.getMetadataClassifier().classify(rdfWrapper).getTier());

    final RdfToFullBeanConverter fullBeanConverter = new RdfToFullBeanConverter();
    final FullBeanImpl fullBean = fullBeanConverter.convertRdfToFullBean(rdfWrapper);

    // Create Solr document.
    final SolrDocumentPopulator documentPopulator = new SolrDocumentPopulator();
    final SolrInputDocument document = new SolrInputDocument();
    documentPopulator.populateWithProperties(document, fullBean);
    documentPopulator.populateWithFacets(document, rdfWrapper);

    assertTrue(document.get(EUROPEANA_ID.toString()).getValues().contains(fullBean.getAbout()));
    assertTrue(CollectionUtils.isEqualCollection(document.get(CURRENT_LOCATION_WGS.toString()).getValues(),
        List.of("50.75,4.5")));
    assertTrue(CollectionUtils.isEqualCollection(document.get(COVERAGE_LOCATION_WGS.toString()).getValues(),
        List.of("50,50", "40,40")));
    assertTrue(CollectionUtils.isEqualCollection(document.get(LOCATION_WGS.toString()).getValues(),
        List.of("50,50", "40,40", "50.75,4.5")));
  }

  @Test
  void populateWithProperties_WGS84Coordinates() throws Exception {
    ClassLoader classLoader = SolrDocumentPopulatorTest.class.getClassLoader();
    File file = new File(
        Objects.requireNonNull(classLoader.getResource("europeana_record_with_geospatial_data_wgs84.xml")).getFile());
    String xml = new String(Files.readAllBytes(file.toPath()));
    final RDF rdf = new RdfConversionUtils().convertStringToRdf(xml);

    // Perform the tier classification
    final RdfWrapper rdfWrapper = new RdfWrapper(rdf);
    RdfTierUtils.setTier(rdf, ClassifierFactory.getMediaClassifier().classify(rdfWrapper).getTier());
    RdfTierUtils.setTier(rdf, ClassifierFactory.getMetadataClassifier().classify(rdfWrapper).getTier());

    final RdfToFullBeanConverter fullBeanConverter = new RdfToFullBeanConverter();
    final FullBeanImpl fullBean = fullBeanConverter.convertRdfToFullBean(rdfWrapper);

    // Create Solr document.
    final SolrDocumentPopulator documentPopulator = new SolrDocumentPopulator();
    final SolrInputDocument document = new SolrInputDocument();
    documentPopulator.populateWithProperties(document, fullBean);
    documentPopulator.populateWithFacets(document, rdfWrapper);

    assertTrue(document.get(EUROPEANA_ID.toString()).getValues().contains(fullBean.getAbout()));
    assertTrue(CollectionUtils.isEqualCollection(document.get(CURRENT_LOCATION_WGS.toString()).getValues(),
        List.of("50.75,4.5")));
    assertTrue(CollectionUtils.isEqualCollection(document.get(COVERAGE_LOCATION_WGS.toString()).getValues(),
        List.of("50,50", "40,40", "40.123456,40.1234567")));
    assertTrue(CollectionUtils.isEqualCollection(document.get(LOCATION_WGS.toString()).getValues(),
        List.of("50,50", "40,40", "40.123456,40.1234567", "50.75,4.5")));
  }
}