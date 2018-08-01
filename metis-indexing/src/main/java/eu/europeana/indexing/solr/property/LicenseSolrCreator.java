package eu.europeana.indexing.solr.property;

import java.util.Date;
import java.util.function.Predicate;

import org.apache.solr.common.SolrInputDocument;

import eu.europeana.corelib.definitions.edm.entity.License;
import eu.europeana.indexing.solr.EdmLabel;

/**
 * Property Solr Creator for 'cc:License' tags.
 */
public class LicenseSolrCreator implements PropertySolrCreator<License> {

  private final Predicate<License> isAggregationResolver;

  /**
   * Constructor.
   * 
   * @param isAggregationResolver Predicate that evaluates whether the given license (URI) is an
   *        aggregation license.
   */
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

    Date ccDeprecatedOnDate = new Date(license.getCcDeprecatedOn().getTime());
    doc.addField(deprecatedLabel.toString(), ccDeprecatedOnDate);
    SolrPropertyUtils.addValue(doc, inheritedLabel, license.getOdrlInheritFrom());
  }
}
