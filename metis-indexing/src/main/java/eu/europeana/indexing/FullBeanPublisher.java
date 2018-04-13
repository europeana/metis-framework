package eu.europeana.indexing;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import eu.europeana.corelib.definitions.edm.entity.AbstractEdmEntity;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.edm.exceptions.MongoUpdateException;
import eu.europeana.corelib.edm.utils.construct.AgentUpdater;
import eu.europeana.corelib.edm.utils.construct.AggregationUpdater;
import eu.europeana.corelib.edm.utils.construct.ConceptUpdater;
import eu.europeana.corelib.edm.utils.construct.EuropeanaAggregationUpdater;
import eu.europeana.corelib.edm.utils.construct.LicenseUpdater;
import eu.europeana.corelib.edm.utils.construct.PlaceUpdater;
import eu.europeana.corelib.edm.utils.construct.ProvidedChoUpdater;
import eu.europeana.corelib.edm.utils.construct.ProxyUpdater;
import eu.europeana.corelib.edm.utils.construct.ServiceUpdater;
import eu.europeana.corelib.edm.utils.construct.TimespanUpdater;
import eu.europeana.corelib.edm.utils.construct.Updater;
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
import eu.europeana.indexing.exception.IndexingException;
import eu.europeana.indexing.solr.SolrDocumentPopulator;

/**
 * Publisher for Full Beans (instances of {@link FullBeanImpl}) that makes them accessible and
 * searchable for external agents.
 * 
 * @author jochen
 *
 */
class FullBeanPublisher {

  private final Supplier<FullBeanConverter> fullBeanConverterSupplier;

  private final FullBeanDao fullBeanDao;
  private final SolrClient solrServer;

  /**
   * Constructor.
   * 
   * @param fullBeanDao DAO object for saving and updating Full Beans.
   * @param solrServer The searchable persistence.
   */
  FullBeanPublisher(FullBeanDao fullBeanDao, SolrClient solrServer) {
    this(fullBeanDao, solrServer, FullBeanConverter::new);
  }

  /**
   * Constructor for testing purposes.
   * 
   * @param fullBeanDao DAO object for saving and updating Full Beans.
   * @param solrServer The searchable persistence.
   * @param fullBeanConverterSupplier Supplies an instance of {@link FullBeanConverter} used to
   *        parse strings to instances of {@link FullBeanImpl}. Will be called once during every
   *        publish.
   */
  FullBeanPublisher(FullBeanDao fullBeanDao, SolrClient solrServer,
      Supplier<FullBeanConverter> fullBeanConverterSupplier) {
    this.fullBeanDao = fullBeanDao;
    this.solrServer = solrServer;
    this.fullBeanConverterSupplier = fullBeanConverterSupplier;
  }

  /**
   * Publishes an RDF.
   * 
   * @param rdf RDF to publish.
   * @throws IndexingException In case an error occurred during publication.
   */
  public void publish(RDF rdf) throws IndexingException {

    // Convert RDF to Full Bean.
    final FullBeanConverter fullBeanConverter = fullBeanConverterSupplier.get();
    final FullBeanImpl fullBean = fullBeanConverter.convertFromRdf(rdf);

    // Create Solr document.
    final SolrDocumentPopulator documentPopulator = new SolrDocumentPopulator();
    final SolrInputDocument document = new SolrInputDocument();
    documentPopulator.populateWithProperties(document, fullBean);
    documentPopulator.populateWithCrfFields(document, rdf);

    // Save Solr document.
    try {
      solrServer.add(document);
    } catch (IOException | SolrServerException e) {
      throw new IndexingException("Could not add Solr input document to Solr server.", e);
    }

    // Save properties/dependencies to Mongo.
    try {
      saveEdmClasses(fullBean);
    } catch (MongoUpdateException e) {
      throw new IndexingException("Could not save/update EDM classes of FullBean to Mongo.", e);
    }

    // Save object itself to Mongo.
    if (fullBean.getAbout() == null) {
      fullBeanDao.save(fullBean);
    } else {
      fullBeanDao.updateFullBean(fullBean);
    }
  }

  private <T extends AbstractEdmEntity> void saveOrUpdateSharedProperty(Supplier<List<T>> getter,
      Consumer<List<T>> setter, Class<T> clazz, Updater<T> updater) throws MongoUpdateException {
    setter.accept(fullBeanDao.update(getter.get(), clazz, updater, true));
  }

  private void saveEdmClasses(FullBeanImpl fullBean) throws MongoUpdateException {

    // Save or update shared properties.
    saveOrUpdateSharedProperty(fullBean::getAgents, fullBean::setAgents, AgentImpl.class,
        new AgentUpdater());
    saveOrUpdateSharedProperty(fullBean::getPlaces, fullBean::setPlaces, PlaceImpl.class,
        new PlaceUpdater());
    saveOrUpdateSharedProperty(fullBean::getConcepts, fullBean::setConcepts, ConceptImpl.class,
        new ConceptUpdater());
    saveOrUpdateSharedProperty(fullBean::getTimespans, fullBean::setTimespans, TimespanImpl.class,
        new TimespanUpdater());
    saveOrUpdateSharedProperty(fullBean::getLicenses, fullBean::setLicenses, LicenseImpl.class,
        new LicenseUpdater());
    saveOrUpdateSharedProperty(fullBean::getServices, fullBean::setServices, ServiceImpl.class,
        new ServiceUpdater());

    // Save or update private properties.
    final boolean isFirstSave = fullBean.getAbout() == null;
    if (isFirstSave) {
      savePrivateProperties(fullBean);
    } else {
      updatePrivateProperties(fullBean);
    }
  }

  private void savePrivateProperties(FullBeanImpl fullBean) {
    fullBeanDao.save(fullBean.getProvidedCHOs());
    fullBeanDao.save(fullBean.getEuropeanaAggregation());
    fullBeanDao.save(fullBean.getProxies());
    fullBeanDao.save(fullBean.getAggregations());

    if (fullBean.getEuropeanaAggregation().getWebResources() != null) {
      fullBeanDao.save(fullBean.getEuropeanaAggregation().getWebResources());
    }

    fullBean.getAggregations().stream().map(AggregationImpl::getWebResources)
        .filter(Objects::nonNull).forEach(fullBeanDao::save);
  }

  private void updatePrivateProperties(FullBeanImpl fullBean) throws MongoUpdateException {
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
