package eu.europeana.indexing.solr.property;

import org.apache.solr.common.SolrInputDocument;
import eu.europeana.corelib.definitions.edm.entity.EuropeanaAggregation;
import eu.europeana.indexing.solr.EdmLabel;

/**
 * Property Solr Creator for 'edm:EuropeanaAggregation' tags.
 */
public class EuropeanaAggregationSolrCreator implements PropertySolrCreator<EuropeanaAggregation> {

  @Override
  public void addToDocument(SolrInputDocument doc, EuropeanaAggregation aggr) {
    SolrPropertyUtils.addValue(doc, EdmLabel.EDM_EUROPEANA_AGGREGATION, aggr.getAbout());
    SolrPropertyUtils.addValues(doc, EdmLabel.EUROPEANA_AGGREGATION_DC_CREATOR, aggr.getDcCreator());
    SolrPropertyUtils.addValues(doc, EdmLabel.EUROPEANA_AGGREGATION_EDM_COUNTRY, aggr.getEdmCountry());
    SolrPropertyUtils.addValues(doc, EdmLabel.EUROPEANA_AGGREGATION_EDM_LANGUAGE, aggr.getEdmLanguage());
    SolrPropertyUtils.addValues(doc, EdmLabel.EUROPEANA_AGGREGATION_ORE_AGGREGATES, aggr.getAggregates());
    SolrPropertyUtils.addValues(doc, EdmLabel.EUROPEANA_AGGREGATION_EDM_HASVIEW, aggr.getEdmHasView());
    SolrPropertyUtils.addValue(doc, EdmLabel.EUROPEANA_AGGREGATION_ORE_AGGREGATEDCHO, aggr.getAggregatedCHO());
    SolrPropertyUtils.addValue(doc, EdmLabel.EUROPEANA_AGGREGATION_EDM_LANDINGPAGE, aggr.getEdmLandingPage());
    SolrPropertyUtils.addValue(doc, EdmLabel.EUROPEANA_AGGREGATION_EDM_ISSHOWNBY, aggr.getEdmIsShownBy());
    new WebResourceSolrCreator(license -> false).addAllToDocument(doc, aggr.getWebResources());
  }
}
