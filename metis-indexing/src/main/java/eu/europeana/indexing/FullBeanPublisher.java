package eu.europeana.indexing;

import java.io.IOException;
import java.util.function.Supplier;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.mongo.server.EdmMongoServer;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.indexing.exception.IndexingException;
import eu.europeana.indexing.fullbean.RdfToFullBeanConverter;
import eu.europeana.indexing.mongo.property.FullBeanUpdater;
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

  private final EdmMongoServer mongoClient;
  private final SolrClient solrServer;

  /**
   * Constructor.
   * 
   * @param mongoClient The Mongo persistence.
   * @param solrServer The searchable persistence.
   */
  FullBeanPublisher(EdmMongoServer mongoClient, SolrClient solrServer) {
    this(mongoClient, solrServer, RdfToFullBeanConverter::new);
  }

  /**
   * Constructor for testing purposes.
   * 
   * @param mongoClient The Mongo persistence.
   * @param solrServer The searchable persistence.
   * @param fullBeanConverterSupplier Supplies an instance of {@link RdfToFullBeanConverter} used to
   *        parse strings to instances of {@link FullBeanImpl}. Will be called once during every
   *        publish.
   */
  FullBeanPublisher(EdmMongoServer mongoClient, SolrClient solrServer,
      Supplier<RdfToFullBeanConverter> fullBeanConverterSupplier) {
    this.mongoClient = mongoClient;
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

    // Publish to Mongo
    final FullBeanImpl savedFullBean;
    try {
      savedFullBean = new FullBeanUpdater().update(fullBean, mongoClient);
    } catch (RuntimeException e) {
      throw new IndexingException("Could not publish to Mongo server.", e);
    }

    // Publish to Solr
    try {
      publishToSolr(rdf, savedFullBean);
    } catch (IOException | SolrServerException | RuntimeException e) {
      throw new IndexingException("Could not add Solr input document to Solr server.", e);
    }
  }

  private void publishToSolr(RDF rdf, FullBeanImpl fullBean)
      throws SolrServerException, IOException {

    // Create Solr document.
    final SolrDocumentPopulator documentPopulator = new SolrDocumentPopulator();
    final SolrInputDocument document = new SolrInputDocument();
    documentPopulator.populateWithProperties(document, fullBean);
    documentPopulator.populateWithCrfFields(document, rdf);

    // Save Solr document.
    solrServer.add(document);
  }
}
