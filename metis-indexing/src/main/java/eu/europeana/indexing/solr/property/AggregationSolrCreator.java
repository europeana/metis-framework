package eu.europeana.indexing.solr.property;

import eu.europeana.corelib.definitions.edm.entity.Aggregation;
import eu.europeana.corelib.solr.entity.LicenseImpl;
import eu.europeana.indexing.solr.EdmLabel;
import java.util.ArrayList;
import java.util.List;
import org.apache.solr.common.SolrInputDocument;

/**
 * Property Solr Creator for 'ore:Aggregation' tags.
 *
 * @author Yorgos.Mamakis@ europeana.eu
 */
public class AggregationSolrCreator implements PropertySolrCreator<Aggregation> {

  private final List<LicenseImpl> licenses;

  /**
   * Constructor.
   *
   * @param licenses the list of licenses for the record.
   */
  public AggregationSolrCreator(List<LicenseImpl> licenses) {
    this.licenses = new ArrayList<>(licenses);
  }

  @Override
  public void addToDocument(SolrInputDocument doc, Aggregation aggr) {
    SolrPropertyUtils.addValues(doc, EdmLabel.PROVIDER_AGGREGATION_DC_RIGHTS, aggr.getDcRights());
    if (!SolrPropertyUtils.hasLicenseForRights(aggr.getEdmRights(),
        item -> licenses.stream().anyMatch(license -> license.getAbout().equals(item)))) {
      SolrPropertyUtils
          .addValues(doc, EdmLabel.PROVIDER_AGGREGATION_EDM_RIGHTS, aggr.getEdmRights());
    }
    SolrPropertyUtils.addValues(doc, EdmLabel.PROVIDER_AGGREGATION_EDM_DATA_PROVIDER,
        aggr.getEdmDataProvider());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROVIDER_AGGREGATION_EDM_INTERMEDIATE_PROVIDER,
        aggr.getEdmIntermediateProvider());
    SolrPropertyUtils
        .addValues(doc, EdmLabel.PROVIDER_AGGREGATION_EDM_PROVIDER, aggr.getEdmProvider());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROVIDER_AGGREGATION_EDM_HASVIEW, aggr.getHasView());
    SolrPropertyUtils
        .addValue(doc, EdmLabel.PROVIDER_AGGREGATION_EDM_IS_SHOWN_AT, aggr.getEdmIsShownAt());
    SolrPropertyUtils
        .addValue(doc, EdmLabel.PROVIDER_AGGREGATION_EDM_IS_SHOWN_BY, aggr.getEdmIsShownBy());
    SolrPropertyUtils.addValue(doc, EdmLabel.PROVIDER_AGGREGATION_EDM_OBJECT, aggr.getEdmObject());
    SolrPropertyUtils.addValue(doc, EdmLabel.EDM_UGC, aggr.getEdmUgc());
    doc.addField(EdmLabel.PREVIEW_NO_DISTRIBUTE.toString(), aggr.getEdmPreviewNoDistribute());
    new WebResourceSolrCreator(licenses).addAllToDocument(doc, aggr.getWebResources());
  }
}
