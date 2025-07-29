package eu.europeana.indexing.search.v2.property;

import java.util.Date;
import java.util.function.Predicate;

import org.apache.solr.common.SolrInputDocument;

import eu.europeana.corelib.definitions.edm.entity.License;
import eu.europeana.indexing.common.persistence.solr.v2.SolrV2Field;

/**
 * Property Solr Creator for 'cc:License' tags.
 */
public class LicenseSolrCreator implements PropertySolrCreator<License> {

  private final Predicate<License> isAggregationResolver;

  /**
   * Constructor.
   *
   * @param isAggregationResolver Predicate that evaluates whether the given license (URI) is an
   * aggregation license.
   */
  public LicenseSolrCreator(Predicate<License> isAggregationResolver) {
    this.isAggregationResolver = isAggregationResolver;
  }

  @Override
  public void addToDocument(SolrInputDocument doc, License license) {

    final boolean isAggregation = isAggregationResolver.test(license);

    final SolrV2Field licenseLabel =
        isAggregation ? SolrV2Field.PROVIDER_AGGREGATION_CC_LICENSE : SolrV2Field.WR_CC_LICENSE;
    SolrPropertyUtils.addValue(doc, licenseLabel, license.getAbout());

    if (license.getCcDeprecatedOn() != null) {
      final SolrV2Field deprecatedLabel = isAggregation ?
              SolrV2Field.PROVIDER_AGGREGATION_CC_DEPRECATED_ON : SolrV2Field.WR_CC_DEPRECATED_ON;
      final Date ccDeprecatedOnDate = new Date(license.getCcDeprecatedOn().getTime());
      doc.addField(deprecatedLabel.toString(), ccDeprecatedOnDate);
    }

    if (isAggregation) {
      SolrPropertyUtils.addValue(doc, SolrV2Field.PROVIDER_AGGREGATION_ODRL_INHERITED_FROM,
          license.getOdrlInheritFrom());
    }
  }
}
