package eu.europeana.indexing.solr.property;

import static org.apache.commons.collections4.CollectionUtils.union;
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
import eu.europeana.indexing.utils.RdfTier;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.solr.common.SolrInputDocument;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link EuropeanaAggregationSolrCreator} class
 */

class EuropeanaAggregationSolrCreatorTest {

  @Test
  void addToDocument() {
    SolrInputDocument solrInputDocument = new SolrInputDocument();
    License license1 = new LicenseImpl();
    license1.setAbout("license1About");
    license1.setOdrlInheritFrom("license1OdrlInheritFrom");
    License license2 = new LicenseImpl();
    license2.setAbout("license2About");
    license2.setOdrlInheritFrom("license2OdrlInheritFrom");

    QualityAnnotation qualityAnnotationContent1 = new QualityAnnotationImpl();
    qualityAnnotationContent1.setAbout("qualityAnnotationContent1About");
    qualityAnnotationContent1.setBody(RdfTier.CONTENT_TIER_0.getUri());
    QualityAnnotation qualityAnnotationContent2 = new QualityAnnotationImpl();
    qualityAnnotationContent2.setAbout("qualityAnnotationContent2About");
    qualityAnnotationContent2.setBody(RdfTier.CONTENT_TIER_1.getUri());
    QualityAnnotation qualityAnnotationMetadata1 = new QualityAnnotationImpl();
    qualityAnnotationMetadata1.setAbout("qualityAnnotationMetadata1About");
    qualityAnnotationMetadata1.setBody(RdfTier.METADATA_TIER_0.getUri());

    final Map<String, QualityAnnotation> qualityAnnotationMap = Map.of(qualityAnnotationContent1.getAbout(),
        qualityAnnotationContent1, qualityAnnotationContent2.getAbout(), qualityAnnotationContent2,
        qualityAnnotationMetadata1.getAbout(), qualityAnnotationMetadata1);

    final EuropeanaAggregationSolrCreator europeanaAggregationSolrCreator = new EuropeanaAggregationSolrCreator(
        List.of(license1, license2), qualityAnnotationMap::get);

    WebResource webResource1 = new WebResourceImpl();
    webResource1.setAbout("webResource1About");
    webResource1.setIsNextInSequence("webResource1IsNextInSequence");
    webResource1.setWebResourceEdmRights(
        Map.of("webResource1EdmRightsKey", List.of("webResource1EdmRightsValue", license2.getAbout())));
    webResource1.setWebResourceDcRights(
        Map.of("webResource1DcRightsKey", List.of("webResource1DcRightsValue", license1.getAbout())));
    final List<String> webResource1SvcsHasService = List.of("webResource1SvcsHasService");
    webResource1.setSvcsHasService(webResource1SvcsHasService.toArray(new String[0]));
    final List<String> webResource1DctermsIsReferencedBy = List.of("webResource1DctermsIsReferencedBy");
    webResource1.setDctermsIsReferencedBy(webResource1DctermsIsReferencedBy.toArray(new String[0]));

    WebResource webResource2 = new WebResourceImpl();
    webResource2.setAbout("webResource2About");
    webResource2.setIsNextInSequence("webResource2IsNextInSequence");
    webResource2.setWebResourceEdmRights(Map.of("webResource2EdmRightsKey", List.of("webResource2EdmRightsValue")));
    webResource2.setWebResourceDcRights(Map.of("webResource2DcRightsKey", List.of("webResource2DcRightsValue")));
    final List<String> webResource2SvcsHasService = List.of("webResource2SvcsHasService");
    webResource2.setSvcsHasService(webResource2SvcsHasService.toArray(new String[0]));
    final List<String> webResource2DctermsIsReferencedBy = List.of("webResource2DctermsIsReferencedBy");
    webResource2.setDctermsIsReferencedBy(webResource2DctermsIsReferencedBy.toArray(new String[0]));

    EuropeanaAggregation europeanaAggregation = new EuropeanaAggregationImpl();
    europeanaAggregation.setId(new ObjectId(String.valueOf(ObjectId.get())));
    europeanaAggregation.setAbout("europeanaAggregationAbout");
    europeanaAggregation.setWebResources(List.of(webResource1, webResource2));
    europeanaAggregation.setEdmCountry(Map.of("edmCountryKey", List.of("countryValue")));
    europeanaAggregation.setEdmLanguage(Map.of("edmLanguageKey", List.of("languageValue")));
    europeanaAggregation.setEdmPreview("previewValue");
    europeanaAggregation.setDqvHasQualityAnnotation(
        new String[]{qualityAnnotationContent1.getAbout(), qualityAnnotationContent2.getAbout(),
            qualityAnnotationMetadata1.getAbout()});

    europeanaAggregationSolrCreator.addToDocument(solrInputDocument, europeanaAggregation);

    //Verify Europeana Aggregation fields
    verifyMap(solrInputDocument, EdmLabel.EUROPEANA_AGGREGATION_EDM_COUNTRY, europeanaAggregation.getEdmCountry());
    verifyMap(solrInputDocument, EdmLabel.EUROPEANA_AGGREGATION_EDM_LANGUAGE, europeanaAggregation.getEdmLanguage());
    assertEquals(europeanaAggregation.getEdmPreview(),
        solrInputDocument.getFieldValue(EdmLabel.EUROPEANA_AGGREGATION_EDM_PREVIEW.toString()));

    //Verify web resources fields
    verifyCollection(solrInputDocument, EdmLabel.EDM_WEB_RESOURCE, List.of(webResource1.getAbout(), webResource2.getAbout()));
    verifyCollection(solrInputDocument, EdmLabel.WR_EDM_IS_NEXT_IN_SEQUENCE,
        List.of(webResource1.getIsNextInSequence(), webResource2.getIsNextInSequence()));
    verifyCollection(solrInputDocument, EdmLabel.WR_SVCS_HAS_SERVICE,
        union(webResource1SvcsHasService, webResource2SvcsHasService));
    verifyCollection(solrInputDocument, EdmLabel.WR_DCTERMS_ISREFERENCEDBY,
        union(webResource1DctermsIsReferencedBy, webResource2DctermsIsReferencedBy));
    verifyCollection(solrInputDocument, EdmLabel.WR_CC_ODRL_INHERITED_FROM,
        List.of(license1.getOdrlInheritFrom(), license2.getOdrlInheritFrom()));

    //Verify webResource1
    verifyMap(solrInputDocument, EdmLabel.WR_EDM_RIGHTS, webResource1.getWebResourceEdmRights());
    verifyMap(solrInputDocument, EdmLabel.WR_DC_RIGHTS, webResource1.getWebResourceDcRights());
    //Verify webResource2
    verifyMap(solrInputDocument, EdmLabel.WR_EDM_RIGHTS, webResource2.getWebResourceEdmRights());
    verifyMap(solrInputDocument, EdmLabel.WR_DC_RIGHTS, webResource2.getWebResourceDcRights());

    //Verify QualityAnnotation Fields
    verifyCollection(solrInputDocument, EdmLabel.CONTENT_TIER,
        List.of(RdfTier.CONTENT_TIER_0.getTier().toString(), RdfTier.CONTENT_TIER_1.getTier().toString()));
    verifyCollection(solrInputDocument, EdmLabel.METADATA_TIER, List.of(RdfTier.METADATA_TIER_0.getTier().toString()));
  }

  void verifyCollection(SolrInputDocument solrInputDocument, EdmLabel edmLabel, Collection<String> collection) {
    final Collection<Object> fieldValues = solrInputDocument.getFieldValues(edmLabel.toString());
    assertTrue(fieldValues.containsAll(collection));
    assertEquals(collection.size(), fieldValues.size());
  }

  void verifyMap(SolrInputDocument solrInputDocument, EdmLabel edmLabel, Map<String, List<String>> map) {
    map.forEach((key, value) -> assertTrue(solrInputDocument.getFieldValues(computeSolrField(edmLabel, key))
                                                            .containsAll(value)));
  }

  private String computeSolrField(EdmLabel label, String value) {
    return label.toString() + "." + value;
  }
}