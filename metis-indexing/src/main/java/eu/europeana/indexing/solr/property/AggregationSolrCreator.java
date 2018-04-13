package eu.europeana.indexing.solr.property;

import java.util.function.Predicate;
import org.apache.solr.common.SolrInputDocument;
import eu.europeana.corelib.definitions.edm.entity.Aggregation;
import eu.europeana.indexing.solr.EdmLabel;

/**
 * Property Solr Creator for 'ore:Aggregation' tags.
 *
 * @author Yorgos.Mamakis@ europeana.eu
 */
public class AggregationSolrCreator implements PropertySolrCreator<Aggregation> {

  private final Predicate<String> hasLicense;

  /**
   * Constructor.
   * 
   * @param hasLicense Predicate to evaluate whether there is a license available for any given web
   *        resource (URI).
   */
  public AggregationSolrCreator(Predicate<String> hasLicense) {
    this.hasLicense = hasLicense;
  }

  @Override
  public void addToDocument(SolrInputDocument doc, Aggregation aggr) {
    SolrPropertyUtils.addValue(doc, EdmLabel.PROVIDER_AGGREGATION_ORE_AGGREGATION, aggr.getAbout());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROVIDER_AGGREGATION_DC_RIGHTS, aggr.getDcRights());
    if (!SolrPropertyUtils.hasLicenseForRights(aggr.getEdmRights(), hasLicense)) {
      SolrPropertyUtils.addValues(doc, EdmLabel.PROVIDER_AGGREGATION_EDM_RIGHTS, aggr.getEdmRights());
    }
    SolrPropertyUtils.addValues(doc, EdmLabel.PROVIDER_AGGREGATION_EDM_DATA_PROVIDER,
        aggr.getEdmDataProvider());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROVIDER_AGGREGATION_EDM_INTERMEDIATE_PROVIDER,
        aggr.getEdmIntermediateProvider());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROVIDER_AGGREGATION_EDM_PROVIDER, aggr.getEdmProvider());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROVIDER_AGGREGATION_ORE_AGGREGATES, aggr.getAggregates());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROVIDER_AGGREGATION_EDM_HASVIEW, aggr.getHasView());
    SolrPropertyUtils.addValue(doc, EdmLabel.PROVIDER_AGGREGATION_EDM_AGGREGATED_CHO,
        aggr.getAggregatedCHO());
    SolrPropertyUtils.addValue(doc, EdmLabel.PROVIDER_AGGREGATION_EDM_IS_SHOWN_AT, aggr.getEdmIsShownAt());
    SolrPropertyUtils.addValue(doc, EdmLabel.PROVIDER_AGGREGATION_EDM_IS_SHOWN_BY, aggr.getEdmIsShownBy());
    SolrPropertyUtils.addValue(doc, EdmLabel.PROVIDER_AGGREGATION_EDM_OBJECT, aggr.getEdmObject());
    SolrPropertyUtils.addValue(doc, EdmLabel.EDM_UGC, aggr.getEdmUgc());
    doc.addField(EdmLabel.PREVIEW_NO_DISTRIBUTE.toString(), aggr.getEdmPreviewNoDistribute());
    new WebResourceSolrCreator(hasLicense).addAllToDocument(doc, aggr.getWebResources());
  }
}
