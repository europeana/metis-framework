package eu.europeana.indexing.solr.property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.corelib.definitions.edm.entity.EuropeanaAggregation;
import eu.europeana.corelib.definitions.edm.entity.License;
import eu.europeana.corelib.definitions.edm.entity.QualityAnnotation;
import eu.europeana.corelib.definitions.edm.entity.WebResource;
import eu.europeana.corelib.solr.entity.EuropeanaAggregationImpl;
import eu.europeana.corelib.solr.entity.LicenseImpl;
import eu.europeana.corelib.solr.entity.QualityAnnotationImpl;
import eu.europeana.corelib.solr.entity.WebResourceImpl;
import eu.europeana.indexing.solr.EdmLabel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.apache.solr.common.SolrInputDocument;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link EuropeanaAggregationSolrCreator} class
 */

class EuropeanaAggregationSolrCreatorTest {

  private EuropeanaAggregationSolrCreator europeanaAggregationSolrCreator;
  private EuropeanaAggregation europeanaAggregation;
  private SolrInputDocument solrInputDocument;


  @BeforeEach
  void setup() {

    solrInputDocument = new SolrInputDocument();
    europeanaAggregation = new EuropeanaAggregationImpl();
    List<License> licenses = new ArrayList<>();
    License license = new LicenseImpl();
    license.setAbout("about license");
    licenses.add(license);
    Function<String, QualityAnnotation> qualityAnnotations = (String s) -> {
      QualityAnnotation qa = new QualityAnnotationImpl();
      qa.setAbout("about quality annotation");
      return qa;
    };
    europeanaAggregationSolrCreator = new EuropeanaAggregationSolrCreator(licenses, qualityAnnotations);
  }

  @Test
  void addEuropeanaAggregationToSolrDocument() {

    europeanaAggregation.setId(new ObjectId(String.valueOf(ObjectId.get())));
    europeanaAggregation.setAbout("about europeana aggregation");
    WebResource webResource = new WebResourceImpl();
    webResource.setAbout("about web resource");
    webResource.setId(new ObjectId(String.valueOf(ObjectId.get())));
    List<WebResource> webResources = new ArrayList<>();
    webResources.add(webResource);
    europeanaAggregation.setWebResources(webResources);
    europeanaAggregation.setEdmCountry(Map.of("key_for_country", List.of("US")));
    europeanaAggregation.setEdmLanguage(Map.of("key_for_language", List.of("en_US")));
    europeanaAggregation.setEdmPreview("http://www.europeana.eu/portal/en/preview.html");

    // the actual method we are testing
    europeanaAggregationSolrCreator.addToDocument(solrInputDocument, europeanaAggregation);

    // assertions
    assertTrue(solrInputDocument.containsKey(EdmLabel.EUROPEANA_AGGREGATION_EDM_COUNTRY + ".key_for_country"));

    assertTrue(solrInputDocument.containsKey(EdmLabel.EUROPEANA_AGGREGATION_EDM_LANGUAGE + ".key_for_language"));

    assertTrue(solrInputDocument.containsKey(EdmLabel.EUROPEANA_AGGREGATION_EDM_PREVIEW.toString()));

    assertEquals("US", solrInputDocument.getFieldValue(EdmLabel.EUROPEANA_AGGREGATION_EDM_COUNTRY + ".key_for_country"));

    assertEquals("en_US", solrInputDocument.getFieldValue(EdmLabel.EUROPEANA_AGGREGATION_EDM_LANGUAGE + ".key_for_language"));

    assertEquals("http://www.europeana.eu/portal/en/preview.html",
        solrInputDocument.getFieldValue(EdmLabel.EUROPEANA_AGGREGATION_EDM_PREVIEW.toString()));

    assertEquals(5, solrInputDocument.size());

  }
}