package eu.europeana.indexing.solr;

import org.apache.solr.common.SolrInputDocument;
import eu.europeana.corelib.definitions.edm.entity.License;

public class LicenseSolrCreator {

  public void create(SolrInputDocument doc, License agent, boolean isAggregation) {
    final EdmLabel licenseLabel =
        isAggregation ? EdmLabel.PROVIDER_AGGREGATION_CC_LICENSE : EdmLabel.WR_CC_LICENSE;
    final EdmLabel deprecatedLabel = isAggregation ? EdmLabel.PROVIDER_AGGREGATION_CC_DEPRECATED_ON
        : EdmLabel.WR_CC_DEPRECATED_ON;
    final EdmLabel inheritedLabel =
        isAggregation ? EdmLabel.PROVIDER_AGGREGATION_ODRL_INHERITED_FROM
            : EdmLabel.WR_ODRL_INHERITED_FROM;
    SolrUtils.addValue(doc, licenseLabel, agent.getAbout());
    doc.addField(deprecatedLabel.toString(), agent.getCcDeprecatedOn());
    SolrUtils.addValue(doc, inheritedLabel, agent.getOdrlInheritFrom());
  }
}
