package eu.europeana.indexing;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import eu.europeana.corelib.edm.utils.construct.AgentUpdater;
import eu.europeana.corelib.edm.utils.construct.AggregationUpdater;
import eu.europeana.corelib.edm.utils.construct.ConceptUpdater;
import eu.europeana.corelib.edm.utils.construct.EuropeanaAggregationUpdater;
import eu.europeana.corelib.edm.utils.construct.LicenseUpdater;
import eu.europeana.corelib.edm.utils.construct.PlaceUpdater;
import eu.europeana.corelib.edm.utils.construct.ProvidedChoUpdater;
import eu.europeana.corelib.edm.utils.construct.ProxyUpdater;
import eu.europeana.corelib.edm.utils.construct.ServiceUpdater;
import eu.europeana.corelib.edm.utils.construct.SolrDocumentHandler;
import eu.europeana.corelib.edm.utils.construct.TimespanUpdater;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.corelib.solr.entity.AggregationImpl;
import eu.europeana.corelib.solr.entity.ConceptImpl;
import eu.europeana.corelib.solr.entity.EuropeanaAggregationImpl;
import eu.europeana.corelib.solr.entity.LicenseImpl;
import eu.europeana.corelib.solr.entity.PlaceImpl;
import eu.europeana.corelib.solr.entity.ProvidedCHOImpl;
import eu.europeana.corelib.solr.entity.ProxyImpl;
import eu.europeana.corelib.solr.entity.ServiceImpl;
import eu.europeana.corelib.solr.entity.TimespanImpl;

class FullBeanPublisher {

  private final FullBeanDao fullBeanDao;
  private final SolrServer solrServer;

  public FullBeanPublisher(FullBeanDao fullBeanDao, SolrServer solrServer) {
    this.fullBeanDao = fullBeanDao;
    this.solrServer = solrServer;
  }

  public void publish(FullBeanImpl fBean) throws IndexingException {

    SolrDocumentHandler solrDocHandler = new SolrDocumentHandler(solrServer);
    SolrInputDocument solrInputDoc;

    try {
      solrInputDoc = solrDocHandler.generate(fBean);
    } catch (SolrServerException e) {
      throw new IndexingException("Could not generate Solr input document.", e);
    }

    try {
      solrServer.add(solrInputDoc);
    } catch (IOException | SolrServerException e) {
      throw new IndexingException("Could not add Solr input document to Solr server.", e);
    }

    try {
      saveEdmClasses(fBean);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      throw new IndexingException("Could not save/update EDM classes of FullBean to Mongo.", e);
    }

    if (fBean.getAbout() == null) {
      fullBeanDao.save(fBean);
    } else {
      fullBeanDao.updateFullBean(fBean);
    }
  }

  private void saveEdmClasses(FullBeanImpl fullBean)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {

    final boolean isFirstSave = fullBean.getAbout() == null;

    final List<AgentImpl> agents =
        fullBeanDao.update(fullBean.getAgents(), AgentImpl.class, new AgentUpdater(), true);
    final List<PlaceImpl> places =
        fullBeanDao.update(fullBean.getPlaces(), PlaceImpl.class, new PlaceUpdater(), true);
    final List<ConceptImpl> concepts =
        fullBeanDao.update(fullBean.getConcepts(), ConceptImpl.class, new ConceptUpdater(), true);
    final List<TimespanImpl> timespans = fullBeanDao.update(fullBean.getTimespans(),
        TimespanImpl.class, new TimespanUpdater(), true);
    final List<LicenseImpl> licenses =
        fullBeanDao.update(fullBean.getLicenses(), LicenseImpl.class, new LicenseUpdater(), true);
    final List<ServiceImpl> services =
        fullBeanDao.update(fullBean.getServices(), ServiceImpl.class, new ServiceUpdater(), true);

    if (isFirstSave) {
      executeFirstSave(fullBean);
    } else {
      executeUpdate(fullBean);
    }

    fullBean.setAgents(agents);
    fullBean.setPlaces(places);
    fullBean.setConcepts(concepts);
    fullBean.setTimespans(timespans);
    fullBean.setLicenses(licenses);
    fullBean.setServices(services);
  }

  private void executeFirstSave(FullBeanImpl fullBean) {
    fullBeanDao.save(fullBean.getProvidedCHOs());
    fullBeanDao.save(fullBean.getEuropeanaAggregation());
    fullBeanDao.save(fullBean.getProxies());
    fullBeanDao.save(fullBean.getAggregations());

    if (fullBean.getEuropeanaAggregation().getWebResources() != null)
      fullBeanDao.save(fullBean.getEuropeanaAggregation().getWebResources());

    for (AggregationImpl aggr : fullBean.getAggregations()) {
      if (aggr.getWebResources() != null)
        fullBeanDao.save(aggr.getWebResources());
    }
  }

  private void executeUpdate(FullBeanImpl fullBean)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    final List<ProvidedCHOImpl> pChos = fullBeanDao.update(fullBean.getProvidedCHOs(),
        ProvidedCHOImpl.class, new ProvidedChoUpdater(), false);
    final List<AggregationImpl> aggregations = fullBeanDao.update(fullBean.getAggregations(),
        AggregationImpl.class, new AggregationUpdater(), false);
    final EuropeanaAggregationImpl europeanaAggregation =
        fullBeanDao.update((EuropeanaAggregationImpl) fullBean.getEuropeanaAggregation(),
            EuropeanaAggregationImpl.class, new EuropeanaAggregationUpdater(), false);
    final List<ProxyImpl> proxies =
        fullBeanDao.update(fullBean.getProxies(), ProxyImpl.class, new ProxyUpdater(), false);

    fullBean.setProvidedCHOs(pChos);
    fullBean.setAggregations(aggregations);
    if (europeanaAggregation != null) {
      fullBean.setEuropeanaAggregation(europeanaAggregation);
    }
    fullBean.setProxies(proxies);
  }
}
