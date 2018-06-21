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
import eu.europeana.indexing.fullbean.RdfToFullBeanConverter;
import eu.europeana.indexing.mongo.FullBeanDao;
import eu.europeana.indexing.mongo.property.AgentUpdater;
import eu.europeana.indexing.mongo.property.AggregationUpdater;
import eu.europeana.indexing.mongo.property.ConceptUpdater;
import eu.europeana.indexing.mongo.property.EuropeanaAggregationUpdater;
import eu.europeana.indexing.mongo.property.LicenseUpdater;
import eu.europeana.indexing.mongo.property.PlaceUpdater;
import eu.europeana.indexing.mongo.property.PropertyMongoUpdater;
import eu.europeana.indexing.mongo.property.ProvidedChoUpdater;
import eu.europeana.indexing.mongo.property.ProxyUpdater;
import eu.europeana.indexing.mongo.property.ServiceUpdater;
import eu.europeana.indexing.mongo.property.TimespanUpdater;
import eu.europeana.indexing.solr.SolrDocumentPopulator;

/**
 * Publisher for Full Beans (instances of {@link FullBeanImpl}) that makes them accessible and
 * searchable for external agents.
 * 
 * @author jochen
 *
 */
class FullBeanPublisher {

  private final Supplier<RdfToFullBeanConverter> fullBeanConverterSupplier;

  private final FullBeanDao fullBeanDao;
  private final SolrClient solrServer;

  /**
   * Constructor.
   * 
   * @param fullBeanDao DAO object for saving and updating Full Beans.
   * @param solrServer The searchable persistence.
   */
  FullBeanPublisher(FullBeanDao fullBeanDao, SolrClient solrServer) {
    this(fullBeanDao, solrServer, RdfToFullBeanConverter::new);
  }

  /**
   * Constructor for testing purposes.
   * 
   * @param fullBeanDao DAO object for saving and updating Full Beans.
   * @param solrServer The searchable persistence.
   * @param fullBeanConverterSupplier Supplies an instance of {@link RdfToFullBeanConverter} used to
   *        parse strings to instances of {@link FullBeanImpl}. Will be called once during every
   *        publish.
   */
  FullBeanPublisher(FullBeanDao fullBeanDao, SolrClient solrServer,
      Supplier<RdfToFullBeanConverter> fullBeanConverterSupplier) {
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
    final RdfToFullBeanConverter fullBeanConverter = fullBeanConverterSupplier.get();
    final FullBeanImpl fullBean = fullBeanConverter.convertRdfToFullBean(rdf);

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

    // Publish properties/dependencies.
    saveOrUpdateProperties(fullBean);

    // Publish private properties as well as the bean itself.
    final boolean alreadyPersisted = fullBeanDao.getPersistedAbout(fullBean) != null;
    if (alreadyPersisted) {
      fullBeanDao.updateFullBean(fullBean);
    } else {
      fullBeanDao.save(fullBean);
    }
  }

  private <T extends AbstractEdmEntity> void saveOrUpdate(Supplier<List<T>> getter,
      Consumer<List<T>> setter, Class<T> clazz, PropertyMongoUpdater<T> updater) {
    setter.accept(fullBeanDao.update(getter.get(), clazz, updater));
  }

  private void saveOrUpdateProperties(FullBeanImpl bean) {

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
      Consumer<List<? extends WebResource>> setter) {
    setter.accept(fullBeanDao.updateWebResources(getter.get()));
  }
}
