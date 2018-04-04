package eu.europeana.indexing.solr;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.solr.common.SolrInputDocument;
import eu.europeana.corelib.definitions.edm.entity.Agent;
import eu.europeana.corelib.definitions.edm.entity.Concept;
import eu.europeana.corelib.definitions.edm.entity.License;
import eu.europeana.corelib.definitions.edm.entity.Place;
import eu.europeana.corelib.definitions.edm.entity.Proxy;
import eu.europeana.corelib.definitions.edm.entity.Timespan;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.entity.AggregationImpl;
import eu.europeana.corelib.solr.entity.LicenseImpl;
import eu.europeana.corelib.solr.entity.ServiceImpl;

public class SolrDocumentCreator {

  /**
   * Convert a FullBean to a SolrInputDocument
   * 
   * @param fBean The FullBean to convert to a SolrInputDocument
   * @return The SolrInputDocument representation of the FullBean
   */
  public SolrInputDocument generate(FullBeanImpl fBean) {

    final SolrInputDocument doc = new SolrInputDocument();

    final Set<String> licenceIds;
    if (fBean.getLicenses() != null) {
      licenceIds =
          fBean.getLicenses().stream().map(LicenseImpl::getAbout).collect(Collectors.toSet());
    } else {
      licenceIds = Collections.emptySet();
    }

    new ProvidedChoSolrCreator().create(doc, fBean.getProvidedCHOs().get(0));
    new AggregationSolrCreator().create(doc, fBean.getAggregations().get(0), licenceIds);
    new EuropeanaAggregationSolrCreator().create(doc, fBean.getEuropeanaAggregation());
    for (Proxy prx : fBean.getProxies()) {
      new ProxySolrCreator().create(doc, prx);
    }
    if (fBean.getConcepts() != null) {
      for (Concept concept : fBean.getConcepts()) {
        new ConceptSolrCreator().create(doc, concept);
      }
    }
    if (fBean.getTimespans() != null) {
      for (Timespan ts : fBean.getTimespans()) {
        new TimespanSolrCreator().create(doc, ts);
      }
    }
    if (fBean.getAgents() != null) {
      for (Agent agent : fBean.getAgents()) {
        new AgentSolrCreator().create(doc, agent);
      }
    }
    if (fBean.getPlaces() != null) {
      for (Place place : fBean.getPlaces()) {
        new PlaceSolrCreator().create(doc, place);
      }
    }
    if (fBean.getLicenses() != null) {
      final Set<String> defRights = fBean.getAggregations().stream()
          .map(AggregationImpl::getEdmRights).filter(Objects::nonNull)
          .flatMap(SolrUtils::getRightsFromMap).collect(Collectors.toSet());
      for (License lic : fBean.getLicenses()) {
        final boolean isAggregation = defRights.contains(lic.getAbout());
        new LicenseSolrCreator().create(doc, lic, isAggregation);
      }
    }
    if (fBean.getServices() != null) {
      for (ServiceImpl service : fBean.getServices()) {
        new ServiceSolrCreator().create(doc, service);
      }
    }
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
