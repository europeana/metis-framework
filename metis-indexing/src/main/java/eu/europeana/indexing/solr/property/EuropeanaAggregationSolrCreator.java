package eu.europeana.indexing.solr.property;

import eu.europeana.corelib.definitions.edm.entity.EuropeanaAggregation;
import eu.europeana.corelib.solr.entity.LicenseImpl;
import eu.europeana.indexing.solr.EdmLabel;
import java.util.ArrayList;
import java.util.List;
import org.apache.solr.common.SolrInputDocument;

/**
 * Property Solr Creator for 'edm:EuropeanaAggregation' tags.
 */
public class EuropeanaAggregationSolrCreator implements PropertySolrCreator<EuropeanaAggregation> {

  private final List<LicenseImpl> licenses;

  /**
   * Constructor.
   *
   * @param licenses the list of licenses for the record.
   */
  public EuropeanaAggregationSolrCreator(List<LicenseImpl> licenses) {
    this.licenses = new ArrayList<>(licenses);;
  }

  @Override
  public void addToDocument(SolrInputDocument doc, EuropeanaAggregation europeanaAggregation) {
    SolrPropertyUtils
        .addValue(doc, EdmLabel.EDM_EUROPEANA_AGGREGATION, europeanaAggregation.getAbout());
    SolrPropertyUtils.addValues(doc, EdmLabel.EUROPEANA_AGGREGATION_DC_CREATOR,
        europeanaAggregation.getDcCreator());
    SolrPropertyUtils.addValues(doc, EdmLabel.EUROPEANA_AGGREGATION_EDM_COUNTRY,
        europeanaAggregation.getEdmCountry());
    SolrPropertyUtils.addValues(doc, EdmLabel.EUROPEANA_AGGREGATION_EDM_LANGUAGE,
        europeanaAggregation.getEdmLanguage());
    SolrPropertyUtils.addValues(doc, EdmLabel.EUROPEANA_AGGREGATION_ORE_AGGREGATES,
        europeanaAggregation.getAggregates());
    SolrPropertyUtils.addValues(doc, EdmLabel.EUROPEANA_AGGREGATION_EDM_HASVIEW,
        europeanaAggregation.getEdmHasView());
    SolrPropertyUtils.addValue(doc, EdmLabel.EUROPEANA_AGGREGATION_ORE_AGGREGATEDCHO,
        europeanaAggregation.getAggregatedCHO());
    SolrPropertyUtils.addValue(doc, EdmLabel.EUROPEANA_AGGREGATION_EDM_LANDINGPAGE,
        europeanaAggregation.getEdmLandingPage());
    SolrPropertyUtils.addValue(doc, EdmLabel.EUROPEANA_AGGREGATION_EDM_ISSHOWNBY,
        europeanaAggregation.getEdmIsShownBy());
    new WebResourceSolrCreator(licenses).addAllToDocument(doc, europeanaAggregation.getWebResources());
  }
}
