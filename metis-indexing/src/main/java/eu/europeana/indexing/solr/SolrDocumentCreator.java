package eu.europeana.indexing.solr;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.solr.common.SolrInputDocument;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.entity.AggregationImpl;
import eu.europeana.corelib.solr.entity.LicenseImpl;

public class SolrDocumentCreator {

  /**
   * Convert a FullBean to a SolrInputDocument
   * 
   * @param fBean The FullBean to convert to a SolrInputDocument
   * @return The SolrInputDocument representation of the FullBean
   */
  public SolrInputDocument generate(FullBeanImpl fBean) {

    final SolrInputDocument doc = new SolrInputDocument();

    final Set<String> licenseIds;
    if (fBean.getLicenses() != null) {
      licenseIds =
          fBean.getLicenses().stream().map(LicenseImpl::getAbout).collect(Collectors.toSet());
    } else {
      licenseIds = Collections.emptySet();
    }

    new ProvidedChoSolrCreator().addToDocument(doc, fBean.getProvidedCHOs().get(0));
    new AggregationSolrCreator(licenseIds::contains).addToDocument(doc,
        fBean.getAggregations().get(0));
    new EuropeanaAggregationSolrCreator().addToDocument(doc, fBean.getEuropeanaAggregation());
    new ProxySolrCreator().addAllToDocument(doc, fBean.getProxies());
    new ConceptSolrCreator().addAllToDocument(doc, fBean.getConcepts());
    new TimespanSolrCreator().addAllToDocument(doc, fBean.getTimespans());
    new AgentSolrCreator().addAllToDocument(doc, fBean.getAgents());
    new PlaceSolrCreator().addAllToDocument(doc, fBean.getPlaces());
    new ServiceSolrCreator().addAllToDocument(doc, fBean.getServices());

    final Set<String> defRights =
        fBean.getAggregations().stream().map(AggregationImpl::getEdmRights).filter(Objects::nonNull)
            .flatMap(SolrUtils::getRightsFromMap).collect(Collectors.toSet());
    new LicenseSolrCreator(license -> defRights.contains(license.getAbout())).addAllToDocument(doc,
        fBean.getLicenses());

    doc.addField(EdmLabel.EUROPEANA_COMPLETENESS.toString(), fBean.getEuropeanaCompleteness());
    doc.addField(EdmLabel.EUROPEANA_COLLECTIONNAME.toString(),
        fBean.getEuropeanaCollectionName()[0]);
    doc.addField("timestamp_created", fBean.getTimestampCreated());
    doc.addField("timestamp_update", fBean.getTimestampUpdated());

    extractCRFFields(doc);
    
    return doc;
  }

  private void extractCRFFields(SolrInputDocument doc) {
    // TODO JOCHEN
  }
}
