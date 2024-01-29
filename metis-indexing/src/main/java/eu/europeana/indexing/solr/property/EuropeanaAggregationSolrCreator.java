package eu.europeana.indexing.solr.property;

import eu.europeana.corelib.definitions.edm.entity.EuropeanaAggregation;
import eu.europeana.corelib.definitions.edm.entity.License;
import eu.europeana.indexing.solr.EdmLabel;
import java.util.ArrayList;
import java.util.List;
import org.apache.solr.common.SolrInputDocument;

/**
 * Property Solr Creator for 'edm:EuropeanaAggregation' tags.
 */
public class EuropeanaAggregationSolrCreator implements PropertySolrCreator<EuropeanaAggregation> {

  private final List<? extends License> licenses;

  /**
   * Constructor.
   *
   * @param licenses the list of licenses for the record. Is not null and does not contain null values.
   */
  public EuropeanaAggregationSolrCreator(List<? extends License> licenses) {
    this.licenses = new ArrayList<>(licenses);
  }

  @Override
  public void addToDocument(SolrInputDocument doc, EuropeanaAggregation europeanaAggregation) {
    SolrPropertyUtils.addValues(doc, EdmLabel.EUROPEANA_AGGREGATION_EDM_COUNTRY,
        europeanaAggregation.getEdmCountry());
    SolrPropertyUtils.addValues(doc, EdmLabel.EUROPEANA_AGGREGATION_EDM_LANGUAGE,
        europeanaAggregation.getEdmLanguage());
    SolrPropertyUtils.addValue(doc, EdmLabel.EUROPEANA_AGGREGATION_EDM_PREVIEW,
        europeanaAggregation.getEdmPreview());
    new WebResourceSolrCreator(licenses)
        .addAllToDocument(doc, europeanaAggregation.getWebResources());
  }
}
