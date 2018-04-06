package eu.europeana.indexing.solr.property;

import java.util.function.Predicate;
import org.apache.solr.common.SolrInputDocument;
import eu.europeana.corelib.definitions.edm.entity.License;
import eu.europeana.indexing.solr.EdmLabel;

public class LicenseSolrCreator extends PropertySolrCreator<License> {

  private final Predicate<License> isAggregationResolver;

  public LicenseSolrCreator(Predicate<License> isAggregationResolver) {
    this.isAggregationResolver = isAggregationResolver;
  }

  @Override
  public void addToDocument(SolrInputDocument doc, License license) {
    final boolean isAggregation = isAggregationResolver.test(license);
    final EdmLabel licenseLabel =
        isAggregation ? EdmLabel.PROVIDER_AGGREGATION_CC_LICENSE : EdmLabel.WR_CC_LICENSE;
    final EdmLabel deprecatedLabel = isAggregation ? EdmLabel.PROVIDER_AGGREGATION_CC_DEPRECATED_ON
        : EdmLabel.WR_CC_DEPRECATED_ON;
    final EdmLabel inheritedLabel =
        isAggregation ? EdmLabel.PROVIDER_AGGREGATION_ODRL_INHERITED_FROM
            : EdmLabel.WR_ODRL_INHERITED_FROM;
    SolrPropertyUtils.addValue(doc, licenseLabel, license.getAbout());
    doc.addField(deprecatedLabel.toString(), license.getCcDeprecatedOn());
    SolrPropertyUtils.addValue(doc, inheritedLabel, license.getOdrlInheritFrom());
  }
}
