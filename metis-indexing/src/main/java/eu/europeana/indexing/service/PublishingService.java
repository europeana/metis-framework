package eu.europeana.indexing.service;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.edm.exceptions.MongoDBException;
import eu.europeana.corelib.edm.utils.MongoConstructor;
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
import eu.europeana.indexing.IndexingException;
import eu.europeana.indexing.service.dao.FullBeanDao;

public class PublishingService {

  private static final Logger LOGGER = LoggerFactory.getLogger(PublishingService.class);

  private final FullBeanDao fullBeanDao;
  private static IBindingFactory rdfBindingFactory;
  private static final String UTF8 = StandardCharsets.UTF_8.name();
  private final SolrServer solrServer;

  static {
    try {
      rdfBindingFactory = BindingDirectory.getFactory(RDF.class);
    } catch (JiBXException e) {
      LOGGER.warn("Error creating the JibX factory.", e);
    }
  }

  public PublishingService(FullBeanDao fullBeanDao, SolrServer solrServer) {
    this.fullBeanDao = fullBeanDao;
    this.solrServer = solrServer;
  }

  public void process(String record) throws IndexingException {
    LOGGER.info("Processing record...");

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Record to process: " + record);
    }

    final RDF rdf = toRDF(record);
    FullBeanImpl fBean = null;
    if (rdf != null) {
      try {
        fBean = new MongoConstructor().constructFullBean(rdf);
      } catch (InstantiationException | IllegalAccessException | IOException e) {
        throw new IndexingException("Could not construct FullBean using MongoConstructor.", e);
      }
    }

    if (fBean != null) {
      processFullBean(fBean);
      LOGGER.info("Successfully processed record.");
    } else {
      // This should not happen.
      throw new IndexingException(
          "Could not construct FullBean using MongoConstructor: null was returned.", null);
    }
  }

  private void processFullBean(FullBeanImpl fBean) throws IndexingException {
    fBean.setEuropeanaCollectionName(new String[100]); // To prevent potential null pointer
                                                       // exceptions

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
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException
        | MongoDBException e) {
      throw new IndexingException("Could not save/update EDM classes of FullBean to Mongo.", e);
    }

    if (fBean.getAbout() == null) {
      fullBeanDao.save(fBean);
    } else {
      fullBeanDao.updateFullBean(fBean);
    }
  }

  private static RDF toRDF(String xml) throws IndexingException {
    try {
      IUnmarshallingContext context = getRdfBindingFactory().createUnmarshallingContext();
      return (RDF) context.unmarshalDocument(IOUtils.toInputStream(xml), UTF8);
    } catch (JiBXException e) {
      throw new IndexingException("Could not convert record to RDF.", e);
    }
  }

  private static IBindingFactory getRdfBindingFactory() throws IndexingException {
    if (rdfBindingFactory == null) {
      try {
        rdfBindingFactory = BindingDirectory.getFactory(RDF.class);
      } catch (JiBXException e) {
        LOGGER.warn("Error creating the JibX factory.", e);
        throw new IndexingException("Error creating the JibX factory.", e);
      }
    }
    return rdfBindingFactory;
  }
  
  private void saveEdmClasses(FullBeanImpl fullBean) throws NoSuchMethodException,
      IllegalAccessException, InvocationTargetException, MongoDBException {

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
