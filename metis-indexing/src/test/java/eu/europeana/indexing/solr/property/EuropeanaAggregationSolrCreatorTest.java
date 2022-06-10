package eu.europeana.indexing.solr.property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import eu.europeana.corelib.definitions.edm.entity.EuropeanaAggregation;
import eu.europeana.corelib.definitions.edm.entity.License;
import eu.europeana.corelib.definitions.edm.entity.QualityAnnotation;
import eu.europeana.corelib.definitions.edm.entity.WebResource;
import eu.europeana.corelib.solr.entity.EuropeanaAggregationImpl;
import eu.europeana.corelib.solr.entity.LicenseImpl;
import eu.europeana.corelib.solr.entity.QualityAnnotationImpl;
import eu.europeana.corelib.solr.entity.WebResourceImpl;
import eu.europeana.indexing.solr.EdmLabel;
import java.sql.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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

  private SolrInputDocument solrInputDocument;


  @BeforeEach
  void setup() {

    solrInputDocument = new SolrInputDocument();

    License license = new LicenseImpl();
    license.setId(new ObjectId(String.valueOf(ObjectId.get())));
    license.setAbout("about license");
    license.setOdrlInheritFrom("http://www.europeana.eu/portal/en/rights.html");
    license.setCcDeprecatedOn(Date.from(Instant.now().plus(364, ChronoUnit.DAYS)));

    Function<String, QualityAnnotation> qualityAnnotations = any -> {
      QualityAnnotation qa = new QualityAnnotationImpl();
      qa.setId(new ObjectId(String.valueOf(ObjectId.get())));
      qa.setAbout("about quality annotation");
      qa.setBody("body quality annotation");
      qa.setCreated(Date.from(Instant.now()).toString());
      qa.setTarget(new String[]{"t1", "t2", "t3"});
      return qa;
    };

    europeanaAggregationSolrCreator = new EuropeanaAggregationSolrCreator(List.of(license), qualityAnnotations);

  }

  @Test
  void addEuropeanaAggregationToSolrDocumentWithEmptyWebResource() {

    EuropeanaAggregation europeanaAggregation = new EuropeanaAggregationImpl();
    europeanaAggregation.setId(new ObjectId(String.valueOf(ObjectId.get())));
    europeanaAggregation.setAbout("about europeana aggregation");
    WebResource webResource = new WebResourceImpl();
    webResource.setAbout("about web resource");
    webResource.setId(new ObjectId(String.valueOf(ObjectId.get())));
    europeanaAggregation.setWebResources(List.of(webResource));
    europeanaAggregation.setEdmCountry(Map.of("key_for_country", List.of("US")));
    europeanaAggregation.setEdmLanguage(Map.of("key_for_language", List.of("en_US")));
    europeanaAggregation.setEdmPreview("http://www.europeana.eu/portal/en/preview.html");

    // the actual method we are testing
    europeanaAggregationSolrCreator.addToDocument(solrInputDocument, europeanaAggregation);

    // assertions
    assertEquals("US", solrInputDocument.getFieldValue(EdmLabel.EUROPEANA_AGGREGATION_EDM_COUNTRY + ".key_for_country"));
    assertEquals("en_US", solrInputDocument.getFieldValue(EdmLabel.EUROPEANA_AGGREGATION_EDM_LANGUAGE + ".key_for_language"));
    assertEquals("http://www.europeana.eu/portal/en/preview.html",
        solrInputDocument.getFieldValue(EdmLabel.EUROPEANA_AGGREGATION_EDM_PREVIEW.toString()));
    assertEquals(5, solrInputDocument.size());
  }

  @Test
  void addEuropeanaAggregationToSolrDocumentWithWebResources() {

    EuropeanaAggregation europeanaAggregation = new EuropeanaAggregationImpl();
    europeanaAggregation.setId(new ObjectId(String.valueOf(ObjectId.get())));
    europeanaAggregation.setAbout("about europeana aggregation");
    europeanaAggregation.setEdmCountry(Map.of("key_for_country", List.of("US")));
    europeanaAggregation.setEdmLanguage(Map.of("key_for_language", List.of("en_US")));
    europeanaAggregation.setEdmPreview("http://www.europeana.eu/portal/en/preview.html");
    europeanaAggregation.setDqvHasQualityAnnotation(new String[]{"about quality annotation"});
    europeanaAggregation.setAggregates(new String[]{"about aggregated CHO"});
    europeanaAggregation.setAggregatedCHO("about aggregated CHO");

    WebResource webResource = new WebResourceImpl();
    webResource.setId(new ObjectId(String.valueOf(ObjectId.get())));
    webResource.setAbout("about web resource");
    webResource.setIsNextInSequence("false");
    webResource.setWebResourceDcRights(Map.of("edmDcRights", List.of("http://www.europeana.eu/portal/en/rights.html")));
    webResource.setWebResourceEdmRights(Map.of("edmRights", List.of("http://www.europeana.eu/portal/en/rights.html")));
    webResource.setDctermsIsPartOf(Map.of("about license", List.of("http://www.europeana.eu/portal/en/rights.html")));
    webResource.setDctermsIsReferencedBy(new String[]{"about license"});
    webResource.setOwlSameAs(new String[]{"about license"});
    webResource.setDctermsHasPart(Map.of("about license", List.of("http://www.europeana.eu/portal/en/rights.html")));

    europeanaAggregation.setWebResources(List.of(webResource));

    // the actual method we are testing
    europeanaAggregationSolrCreator.addToDocument(solrInputDocument, europeanaAggregation);

    // assertions

    assertEquals("US", solrInputDocument.getFieldValue(EdmLabel.EUROPEANA_AGGREGATION_EDM_COUNTRY + ".key_for_country"));
    assertEquals("en_US", solrInputDocument.getFieldValue(EdmLabel.EUROPEANA_AGGREGATION_EDM_LANGUAGE + ".key_for_language"));
    assertEquals("http://www.europeana.eu/portal/en/preview.html",
        solrInputDocument.getFieldValue(EdmLabel.EUROPEANA_AGGREGATION_EDM_PREVIEW.toString()));
    assertEquals("about web resource", solrInputDocument.getFieldValue(EdmLabel.EDM_WEB_RESOURCE.toString()));
    assertEquals("false", solrInputDocument.getFieldValue(EdmLabel.WR_EDM_IS_NEXT_IN_SEQUENCE.toString()));
    assertEquals("http://www.europeana.eu/portal/en/rights.html",
        solrInputDocument.getFieldValue(EdmLabel.WR_EDM_RIGHTS + ".edmRights"));
    assertEquals("http://www.europeana.eu/portal/en/rights.html",
        solrInputDocument.getFieldValue(EdmLabel.WR_DC_RIGHTS + ".edmDcRights"));
    assertEquals("about license", solrInputDocument.getFieldValue(EdmLabel.WR_DCTERMS_ISREFERENCEDBY.toString()));

    assertNull(solrInputDocument.getFieldValue(EdmLabel.WR_CC_ODRL_INHERITED_FROM.toString()));

    assertEquals(9, solrInputDocument.size());
  }


}