package eu.europeana.metis.data.checker.service.persistence;

import java.io.IOException;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.mongo.server.EdmMongoServer;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.corelib.solr.entity.AggregationImpl;
import eu.europeana.corelib.solr.entity.ConceptImpl;
import eu.europeana.corelib.solr.entity.EuropeanaAggregationImpl;
import eu.europeana.corelib.solr.entity.EventImpl;
import eu.europeana.corelib.solr.entity.LicenseImpl;
import eu.europeana.corelib.solr.entity.PhysicalThingImpl;
import eu.europeana.corelib.solr.entity.PlaceImpl;
import eu.europeana.corelib.solr.entity.ProvidedCHOImpl;
import eu.europeana.corelib.solr.entity.ProxyImpl;
import eu.europeana.corelib.solr.entity.ServiceImpl;
import eu.europeana.corelib.solr.entity.TimespanImpl;
import eu.europeana.corelib.solr.entity.WebResourceImpl;
import eu.europeana.indexing.AbstractConnectionProvider;
import eu.europeana.indexing.Indexer;
import eu.europeana.indexing.IndexerFactory;
import eu.europeana.indexing.exception.IndexingException;

/**
 * Record persistence DAO Created by ymamakis on 9/2/16.
 */
@Service
public class RecordDao {

  private final SolrClient solrServer;
  private final EdmMongoServer mongoServer;
  private final Indexer indexer;

  /**
   * Constructor with required fields.
   *
   * @param connectionProvider
   * @throws IndexingException In case of problems setting up the indexer.
   */
  @Autowired
  public RecordDao(AbstractConnectionProvider connectionProvider) throws IndexingException {
    this(connectionProvider.getSolrClient(), connectionProvider.getMongoClient(),
        new IndexerFactory(connectionProvider.getMongoClient(), connectionProvider.getSolrClient())
            .getIndexer());
  }

  RecordDao(SolrClient solrServer, EdmMongoServer mongoServer, Indexer indexer) {
    this.solrServer = solrServer;
    this.mongoServer = mongoServer;
    this.indexer = indexer;
  }

  /**
   * Persist a record in mongo and solr
   *
   * @param rdf The record
   * @throws IndexingException In case indexing failed.
   */
  public void createRecord(RDF rdf) throws IndexingException {
    indexer.indexRdf(rdf, false);
  }

  /**
   * Commit all the changes to the databases.
   * 
   * @throws IndexingException In case of problems with committing the changes to the Sorl server.
   */
  public void commit() throws IndexingException {
    indexer.triggerFlushOfPendingChanges(true);
  }

  /**
   * Delete the records persisted over the last 24h
   * 
   * @throws SolrServerException In case a problem occurred while deleting the records from the Solr
   *         server.
   * @throws IOException In case a problem occurred while deleting the records from the Solr server.
   * @throws IndexingException In case a problem occurred while committing the changes to the Solr
   *         server.
   */
  public void deleteRecordIdsByTimestamp()
      throws SolrServerException, IOException, IndexingException {

    // Clear the Solr server.
    SolrQuery query = new SolrQuery();
    query.setQuery("*:*");
    solrServer.deleteByQuery(query.getQuery());
    commit();

    // Clear the Mongo server: full beans.
    clearCollectionFromMongo(FullBeanImpl.class);

    // Clear the Mongo server: full bean direct dependents.
    clearCollectionFromMongo(AgentImpl.class);
    clearCollectionFromMongo(AggregationImpl.class);
    clearCollectionFromMongo(ConceptImpl.class);
    clearCollectionFromMongo(EuropeanaAggregationImpl.class);
    clearCollectionFromMongo(EventImpl.class);
    clearCollectionFromMongo(LicenseImpl.class);
    clearCollectionFromMongo(PhysicalThingImpl.class);
    clearCollectionFromMongo(PlaceImpl.class);
    clearCollectionFromMongo(ProvidedCHOImpl.class);
    clearCollectionFromMongo(ProxyImpl.class);
    clearCollectionFromMongo(ServiceImpl.class);
    clearCollectionFromMongo(TimespanImpl.class);

    // Clear the Mongo server: full bean indirect dependents.
    clearCollectionFromMongo(WebResourceImpl.class);
  }

  private void clearCollectionFromMongo(Class<?> collectionType) {
    this.mongoServer.getDatastore()
        .delete(this.mongoServer.getDatastore().createQuery(collectionType));
  }
}
