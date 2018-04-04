package eu.europeana.indexing.solr;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.SolrInputDocument;
import eu.europeana.corelib.definitions.edm.entity.EuropeanaAggregation;

public class EuropeanaAggregationSolrCreator extends PropertySolrCreator<EuropeanaAggregation> {

  private static final String PORTAL_PREFIX = "http://europeana.eu/portal/record/";
  private static final String PORTAL_SUFFIX = ".html";

  @Override
  public void addToDocument(SolrInputDocument doc, EuropeanaAggregation aggr) {
    SolrUtils.addValue(doc, EdmLabel.EDM_EUROPEANA_AGGREGATION, aggr.getAbout());
    SolrUtils.addValues(doc, EdmLabel.EUROPEANA_AGGREGATION_DC_CREATOR, aggr.getDcCreator());
    SolrUtils.addValues(doc, EdmLabel.EUROPEANA_AGGREGATION_EDM_COUNTRY, aggr.getEdmCountry());
    SolrUtils.addValues(doc, EdmLabel.EUROPEANA_AGGREGATION_EDM_LANGUAGE, aggr.getEdmLanguage());
    SolrUtils.addValues(doc, EdmLabel.EUROPEANA_AGGREGATION_ORE_AGGREGATES, aggr.getAggregates());
    SolrUtils.addValues(doc, EdmLabel.EUROPEANA_AGGREGATION_EDM_HASVIEW, aggr.getEdmHasView());
    SolrUtils.addValue(doc, EdmLabel.EUROPEANA_AGGREGATION_ORE_AGGREGATEDCHO,
        aggr.getAggregatedCHO());
    SolrUtils.addValue(doc, EdmLabel.EUROPEANA_AGGREGATION_EDM_LANDINGPAGE, PORTAL_PREFIX
        + StringUtils.substringAfter(aggr.getAggregatedCHO(), "/item/") + PORTAL_SUFFIX);
    SolrUtils.addValue(doc, EdmLabel.EUROPEANA_AGGREGATION_EDM_ISSHOWNBY, aggr.getEdmIsShownBy());
    SolrUtils.addValue(doc, EdmLabel.EUROPEANA_AGGREGATION_EDM_PREVIEW, aggr.getEdmPreview());
    new WebResourceSolrCreator(license -> false).addAllToDocument(doc, aggr.getWebResources());
  }
}
