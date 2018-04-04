package eu.europeana.indexing.solr;

import java.util.function.Predicate;
import org.apache.solr.common.SolrInputDocument;
import eu.europeana.corelib.definitions.edm.entity.Aggregation;

/**
 *
 * @author Yorgos.Mamakis@ europeana.eu
 */
public class AggregationSolrCreator extends PropertySolrCreator<Aggregation> {

  private final Predicate<String> hasLicense;

  public AggregationSolrCreator(Predicate<String> hasLicense) {
    this.hasLicense = hasLicense;
  }

  @Override
  public void addToDocument(SolrInputDocument doc, Aggregation aggr) {
    SolrUtils.addValue(doc, EdmLabel.PROVIDER_AGGREGATION_ORE_AGGREGATION, aggr.getAbout());
    SolrUtils.addValues(doc, EdmLabel.PROVIDER_AGGREGATION_DC_RIGHTS, aggr.getDcRights());
    if (!SolrUtils.hasLicenseForRights(aggr.getEdmRights(), hasLicense)) {
      SolrUtils.addValues(doc, EdmLabel.PROVIDER_AGGREGATION_EDM_RIGHTS, aggr.getEdmRights());
    }
    SolrUtils.addValues(doc, EdmLabel.PROVIDER_AGGREGATION_EDM_DATA_PROVIDER,
        aggr.getEdmDataProvider());
    SolrUtils.addValues(doc, EdmLabel.PROVIDER_AGGREGATION_EDM_INTERMEDIATE_PROVIDER,
        aggr.getEdmIntermediateProvider());
    SolrUtils.addValues(doc, EdmLabel.PROVIDER_AGGREGATION_EDM_PROVIDER, aggr.getEdmProvider());
    SolrUtils.addValues(doc, EdmLabel.PROVIDER_AGGREGATION_ORE_AGGREGATES, aggr.getAggregates());
    SolrUtils.addValues(doc, EdmLabel.PROVIDER_AGGREGATION_EDM_HASVIEW, aggr.getHasView());
    SolrUtils.addValue(doc, EdmLabel.PROVIDER_AGGREGATION_EDM_AGGREGATED_CHO,
        aggr.getAggregatedCHO());
    SolrUtils.addValue(doc, EdmLabel.PROVIDER_AGGREGATION_EDM_IS_SHOWN_AT, aggr.getEdmIsShownAt());
    SolrUtils.addValue(doc, EdmLabel.PROVIDER_AGGREGATION_EDM_IS_SHOWN_BY, aggr.getEdmIsShownBy());
    SolrUtils.addValue(doc, EdmLabel.PROVIDER_AGGREGATION_EDM_OBJECT, aggr.getEdmObject());
    SolrUtils.addValue(doc, EdmLabel.EDM_UGC, aggr.getEdmUgc());
    doc.addField(EdmLabel.PREVIEW_NO_DISTRIBUTE.toString(), aggr.getEdmPreviewNoDistribute());
    new WebResourceSolrCreator(hasLicense).addAllToDocument(doc, aggr.getWebResources());
  }
}
