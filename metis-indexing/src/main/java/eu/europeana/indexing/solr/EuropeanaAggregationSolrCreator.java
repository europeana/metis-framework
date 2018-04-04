package eu.europeana.indexing.solr;

import java.util.Collections;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.SolrInputDocument;
import eu.europeana.corelib.definitions.edm.entity.EuropeanaAggregation;
import eu.europeana.corelib.definitions.edm.entity.WebResource;

/**
 *
 * @author Yorgos.Mamakis@ europeana.eu
 */
public class EuropeanaAggregationSolrCreator {

  private static final String PORTAL_PREFIX = "http://europeana.eu/portal/record/";
  private static final String PORTAL_SUFFIX = ".html";

  public void create(SolrInputDocument doc, EuropeanaAggregation aggr) {
    SolrUtils.addValue(doc, EdmLabel.EDM_EUROPEANA_AGGREGATION, aggr.getAbout());
    SolrUtils.addValues(doc, EdmLabel.EUROPEANA_AGGREGATION_DC_CREATOR, aggr.getDcCreator());
    SolrUtils.addValues(doc, EdmLabel.EUROPEANA_AGGREGATION_EDM_COUNTRY, aggr.getEdmCountry());
    SolrUtils.addValues(doc, EdmLabel.EUROPEANA_AGGREGATION_EDM_LANGUAGE, aggr.getEdmLanguage());
    SolrUtils.addValues(doc, EdmLabel.EUROPEANA_AGGREGATION_ORE_AGGREGATES,
        aggr.getAggregates());
    SolrUtils.addValues(doc, EdmLabel.EUROPEANA_AGGREGATION_EDM_HASVIEW,
        aggr.getEdmHasView());
    SolrUtils.addValue(doc, EdmLabel.EUROPEANA_AGGREGATION_ORE_AGGREGATEDCHO,
        aggr.getAggregatedCHO());
    SolrUtils.addValue(doc, EdmLabel.EUROPEANA_AGGREGATION_EDM_LANDINGPAGE, PORTAL_PREFIX
        + StringUtils.substringAfter(aggr.getAggregatedCHO(), "/item/") + PORTAL_SUFFIX);
    SolrUtils.addValue(doc, EdmLabel.EUROPEANA_AGGREGATION_EDM_ISSHOWNBY,
        aggr.getEdmIsShownBy());
    SolrUtils.addValue(doc, EdmLabel.EUROPEANA_AGGREGATION_EDM_PREVIEW, aggr.getEdmPreview());
    if (aggr.getWebResources() != null) {
      for (WebResource wr : aggr.getWebResources()) {
        new WebResourceSolrCreator().create(doc, wr, Collections.emptySet());
      }
    }
  }
}
