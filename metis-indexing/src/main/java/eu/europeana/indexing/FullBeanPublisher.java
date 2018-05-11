package eu.europeana.indexing;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import eu.europeana.corelib.definitions.edm.entity.AbstractEdmEntity;
import eu.europeana.corelib.definitions.edm.entity.WebResource;
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
import eu.europeana.indexing.mongo.FullBeanDao;
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

    // Publish
    publishToMongo(fullBean);
    publishToSolr(rdf, fullBean);
  }

  private void publishToSolr(RDF rdf, FullBeanImpl fullBean) throws IndexingException {

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
  }

  private void publishToMongo(FullBeanImpl fullBean) throws IndexingException {
    try {

      // Publish properties/dependencies.
      saveOrUpdateProperties(fullBean);

      // Publish private properties as well as the bean itself.
      final boolean alreadyPersisted = fullBeanDao.getPersistedAbout(fullBean) != null;
      if (alreadyPersisted) {
        fullBeanDao.updateFullBean(fullBean);
      } else {
        fullBeanDao.save(fullBean);
      }
    } catch (MongoUpdateException e) {
      throw new IndexingException("Could not save/update EDM classes of FullBean to Mongo.", e);
    }
  }

  private <T extends AbstractEdmEntity> void saveOrUpdate(Supplier<List<? extends T>> getter,
      Consumer<List<? extends T>> setter, Class<T> clazz, Updater<T> updater)
      throws MongoUpdateException {
    setter.accept(fullBeanDao.update(getter.get(), clazz, updater));
  }

  private void saveOrUpdateProperties(FullBeanImpl bean) throws MongoUpdateException {

    // The shared properties
    saveOrUpdate(bean::getAgents, bean::setAgents, AgentImpl.class, new AgentUpdater());
    saveOrUpdate(bean::getPlaces, bean::setPlaces, PlaceImpl.class, new PlaceUpdater());
    saveOrUpdate(bean::getConcepts, bean::setConcepts, ConceptImpl.class, new ConceptUpdater());
    saveOrUpdate(bean::getTimespans, bean::setTimespans, TimespanImpl.class, new TimespanUpdater());
    saveOrUpdate(bean::getLicenses, bean::setLicenses, LicenseImpl.class, new LicenseUpdater());
    saveOrUpdate(bean::getServices, bean::setServices, ServiceImpl.class, new ServiceUpdater());

    // The aggregator web resources
    saveWebResources(bean.getEuropeanaAggregation()::getWebResources,
        bean.getEuropeanaAggregation()::setWebResources);
    for (AggregationImpl aggregation : bean.getAggregations()) {
      saveWebResources(aggregation::getWebResources, aggregation::setWebResources);
    }

    // The private properties (incluiding the aggregators)
    saveOrUpdate(bean::getProvidedCHOs, bean::setProvidedCHOs, ProvidedCHOImpl.class,
        new ProvidedChoUpdater());
    saveOrUpdate(bean::getAggregations, bean::setAggregations, AggregationImpl.class,
        new AggregationUpdater());
    bean.setEuropeanaAggregation(
        fullBeanDao.update((EuropeanaAggregationImpl) bean.getEuropeanaAggregation(),
            EuropeanaAggregationImpl.class, new EuropeanaAggregationUpdater()));
    saveOrUpdate(bean::getProxies, bean::setProxies, ProxyImpl.class, new ProxyUpdater());
  }

  private void saveWebResources(Supplier<List<? extends WebResource>> getter,
      Consumer<List<? extends WebResource>> setter) throws MongoUpdateException {
    setter.accept(fullBeanDao.updateWebResources(getter.get()));
  }
}
