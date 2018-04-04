package eu.europeana.indexing.solr;

import java.util.Set;
import org.apache.solr.common.SolrInputDocument;
import eu.europeana.corelib.definitions.edm.entity.Aggregation;
import eu.europeana.corelib.definitions.edm.entity.WebResource;

/**
 *
 * @author Yorgos.Mamakis@ europeana.eu
 */
public class AggregationSolrCreator {

  public void create(SolrInputDocument doc, Aggregation aggr, Set<String> licIds) {
    SolrUtils.addValue(doc, EdmLabel.PROVIDER_AGGREGATION_ORE_AGGREGATION, aggr.getAbout());
    SolrUtils.addValues(doc, EdmLabel.PROVIDER_AGGREGATION_DC_RIGHTS, aggr.getDcRights());
    if (!SolrUtils.hasLicenseForRights(aggr.getEdmRights(), licIds)) {
      SolrUtils.addValues(doc, EdmLabel.PROVIDER_AGGREGATION_EDM_RIGHTS, aggr.getEdmRights());
    }
    SolrUtils.addValues(doc, EdmLabel.PROVIDER_AGGREGATION_EDM_DATA_PROVIDER,
        aggr.getEdmDataProvider());
    SolrUtils.addValues(doc, EdmLabel.PROVIDER_AGGREGATION_EDM_INTERMEDIATE_PROVIDER,
        aggr.getEdmIntermediateProvider());
    SolrUtils.addValues(doc, EdmLabel.PROVIDER_AGGREGATION_EDM_PROVIDER, aggr.getEdmProvider());
    SolrUtils.addValues(doc, EdmLabel.PROVIDER_AGGREGATION_ORE_AGGREGATES,
        aggr.getAggregates());
    SolrUtils.addValues(doc, EdmLabel.PROVIDER_AGGREGATION_EDM_HASVIEW, aggr.getHasView());
    SolrUtils.addValue(doc, EdmLabel.PROVIDER_AGGREGATION_EDM_AGGREGATED_CHO,
        aggr.getAggregatedCHO());
    SolrUtils.addValue(doc, EdmLabel.PROVIDER_AGGREGATION_EDM_IS_SHOWN_AT,
        aggr.getEdmIsShownAt());
    SolrUtils.addValue(doc, EdmLabel.PROVIDER_AGGREGATION_EDM_IS_SHOWN_BY,
        aggr.getEdmIsShownBy());
    SolrUtils.addValue(doc, EdmLabel.PROVIDER_AGGREGATION_EDM_OBJECT, aggr.getEdmObject());
    SolrUtils.addValue(doc, EdmLabel.EDM_UGC, aggr.getEdmUgc());
    doc.addField(EdmLabel.PREVIEW_NO_DISTRIBUTE.toString(), aggr.getEdmPreviewNoDistribute());
    if (aggr.getWebResources() != null) {
      for (WebResource wr : aggr.getWebResources()) {
        new WebResourceSolrCreator().create(doc, wr, licIds);
      }
    }
  }
}
