package eu.europeana.indexing;

import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.mongo.server.EdmMongoServer;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.indexing.exception.IndexingException;
import eu.europeana.indexing.fullbean.RdfToFullBeanConverter;
import eu.europeana.indexing.mongo.property.FullBeanUpdater;
import eu.europeana.indexing.solr.SolrDocumentPopulator;
import eu.europeana.metis.utils.ExternalRequestUtil;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Date;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;

/**
 * Publisher for Full Beans (instances of {@link FullBeanImpl}) that makes them accessible and
 * searchable for external agents.
 *
 * @author jochen
 */
class FullBeanPublisher {

  private static final BiConsumer<FullBeanImpl, FullBeanImpl> EMPTY_PREPROCESSOR = (created, updated) -> {
  };

  private final Supplier<RdfToFullBeanConverter> fullBeanConverterSupplier;

  private final EdmMongoServer mongoClient;
  private final SolrClient solrServer;
  private final boolean preserveUpdateAndCreateTimesFromRdf;

  /**
   * Constructor.
   *
   * @param mongoClient The Mongo persistence.
   * @param solrServer The searchable persistence.
   * @param preserveUpdateAndCreateTimesFromRdf This determines whether this publisher will use the
   * updated and created times from the incoming RDFs, or whether it computes its own.
   */
  FullBeanPublisher(EdmMongoServer mongoClient, SolrClient solrServer,
      boolean preserveUpdateAndCreateTimesFromRdf) {
    this(mongoClient, solrServer, preserveUpdateAndCreateTimesFromRdf, RdfToFullBeanConverter::new);
  }

  /**
   * Constructor for testing purposes.
   *
   * @param mongoClient The Mongo persistence.
   * @param solrServer The searchable persistence.
   * @param preserveUpdateAndCreateTimesFromRdf This determines whether this publisher will use the
   * updated and created times from the incoming RDFs, or whether it computes its own.
   * @param fullBeanConverterSupplier Supplies an instance of {@link RdfToFullBeanConverter} used to
   * parse strings to instances of {@link FullBeanImpl}. Will be called once during every publish.
   */
  FullBeanPublisher(EdmMongoServer mongoClient, SolrClient solrServer,
      boolean preserveUpdateAndCreateTimesFromRdf,
      Supplier<RdfToFullBeanConverter> fullBeanConverterSupplier) {
    this.mongoClient = mongoClient;
    this.solrServer = solrServer;
    this.fullBeanConverterSupplier = fullBeanConverterSupplier;
    this.preserveUpdateAndCreateTimesFromRdf = preserveUpdateAndCreateTimesFromRdf;
  }

  private static void setUpdateAndCreateTime(FullBeanImpl current, FullBeanImpl updated) {
    final Date currentDate = new Date();
    updated.setTimestampCreated(current != null ? current.getTimestampCreated() : currentDate);
    updated.setTimestampUpdated(currentDate);
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

    // Provide the preprocessor: this will set the created and updated timestamps as needed.
    final BiConsumer<FullBeanImpl, FullBeanImpl> fullBeanPreprocessor =
        preserveUpdateAndCreateTimesFromRdf ? EMPTY_PREPROCESSOR
            : (FullBeanPublisher::setUpdateAndCreateTime);

    // Publish to Mongo
    final FullBeanImpl savedFullBean;
    try {
      savedFullBean = new FullBeanUpdater(fullBeanPreprocessor).update(fullBean, mongoClient);
    } catch (RuntimeException e) {
      throw new IndexingException("Could not publish to Mongo server.", e);
    }

    // Publish to Solr
    try {
      ExternalRequestUtil.retryableExternalRequest(() -> {
        publishToSolr(rdf, savedFullBean);
        return null;
      }, Collections.singletonMap(UnknownHostException.class, ""), 30, 1000);
    } catch (Exception e) {
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
