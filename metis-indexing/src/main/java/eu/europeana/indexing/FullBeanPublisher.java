package eu.europeana.indexing;

import com.mongodb.MongoClientException;
import com.mongodb.MongoConfigurationException;
import com.mongodb.MongoIncompatibleDriverException;
import com.mongodb.MongoInternalException;
import com.mongodb.MongoInterruptedException;
import com.mongodb.MongoSecurityException;
import com.mongodb.MongoSocketException;
import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.definitions.edm.beans.IdBean;
import eu.europeana.corelib.mongo.server.EdmMongoServer;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.indexing.exception.IndexerRelatedIndexingException;
import eu.europeana.indexing.exception.IndexingException;
import eu.europeana.indexing.exception.RecordRelatedIndexingException;
import eu.europeana.indexing.exception.SetupRelatedIndexingException;
import eu.europeana.indexing.fullbean.RdfToFullBeanConverter;
import eu.europeana.indexing.mongo.FullBeanUpdater;
import eu.europeana.indexing.solr.SolrDocumentPopulator;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.metis.utils.ExternalRequestUtil;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Date;
import java.util.function.Supplier;
import org.apache.logging.log4j.util.TriConsumer;
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

  private static final String MONGO_SERVER_PUBLISH_ERROR = "Could not publish to Mongo server.";

  private static final String SOLR_SERVER_PUBLISH_ERROR = "Could not publish to Solr server.";

  private static final TriConsumer<FullBeanImpl, FullBeanImpl, Date> EMPTY_PREPROCESSOR = (created, updated, recordDate) -> {
  };
  private static final int PUBLISH_MAX_RETRIES = 30;
  private static final int PERIOD_BETWEEN_RETRIES_IN_MILLIS = 1000;

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

  private static void setUpdateAndCreateTime(IdBean current, FullBean updated, Date recordDate) {
    final Date updatedDate = recordDate == null ? new Date() : recordDate;
    final Date createdDate = current == null ? updatedDate : current.getTimestampCreated();
    updated.setTimestampCreated(createdDate);
    updated.setTimestampUpdated(updatedDate);
  }

  /**
   * Publishes an RDF.
   *
   * @param rdf RDF to publish.
   * @param recordDate The date that would represent the created/updated date of a record
   * @throws IndexingException which can be one of:
   * <ul>
   * <li>{@link IndexerRelatedIndexingException} In case an error occurred during publication.</li>
   * <li>{@link SetupRelatedIndexingException} in case an error occurred during indexing setup</li>
   * <li>{@link RecordRelatedIndexingException} in case an error occurred related to record
   * contents</li>
   * </ul>
   */
  public void publish(RdfWrapper rdf, Date recordDate) throws IndexingException {

    // Convert RDF to Full Bean.
    final RdfToFullBeanConverter fullBeanConverter = fullBeanConverterSupplier.get();
    final FullBeanImpl fullBean = fullBeanConverter.convertRdfToFullBean(rdf);

    // Provide the preprocessor: this will set the created and updated timestamps as needed.
    final TriConsumer<FullBeanImpl, FullBeanImpl, Date> fullBeanPreprocessor =
        preserveUpdateAndCreateTimesFromRdf ? EMPTY_PREPROCESSOR
            : (FullBeanPublisher::setUpdateAndCreateTime);

    // Publish to Mongo
    final FullBeanImpl savedFullBean;
    try {
      savedFullBean = new FullBeanUpdater(fullBeanPreprocessor).update(fullBean, recordDate, mongoClient);
    } catch (MongoIncompatibleDriverException | MongoConfigurationException | MongoSecurityException e) {
      throw new SetupRelatedIndexingException(MONGO_SERVER_PUBLISH_ERROR, e);
    } catch (MongoSocketException | MongoClientException | MongoInternalException | MongoInterruptedException e) {
      throw new IndexerRelatedIndexingException(MONGO_SERVER_PUBLISH_ERROR, e);
    } catch (RuntimeException e) {
      throw new RecordRelatedIndexingException(MONGO_SERVER_PUBLISH_ERROR, e);
    }

    // Publish to Solr
    try {
      ExternalRequestUtil.retryableExternalRequest(() -> {
            publishToSolr(rdf, savedFullBean);
            return null;
          }, Collections.singletonMap(UnknownHostException.class, ""), PUBLISH_MAX_RETRIES,
          PERIOD_BETWEEN_RETRIES_IN_MILLIS);
    } catch (IndexingException e) {
      throw e;
    } catch (Exception e) {
      throw new RecordRelatedIndexingException(SOLR_SERVER_PUBLISH_ERROR, e);
    }
  }

  private void publishToSolr(RdfWrapper rdf, FullBeanImpl fullBean) throws IndexingException {

    // Create Solr document.
    final SolrDocumentPopulator documentPopulator = new SolrDocumentPopulator();
    final SolrInputDocument document = new SolrInputDocument();
    documentPopulator.populateWithProperties(document, fullBean);
    documentPopulator.populateWithFacets(document, rdf);

    // Save Solr document.
    try {
      solrServer.add(document);
    } catch (IOException e) {
      throw new IndexerRelatedIndexingException(SOLR_SERVER_PUBLISH_ERROR, e);
    } catch (SolrServerException | RuntimeException e) {
      throw new RecordRelatedIndexingException(SOLR_SERVER_PUBLISH_ERROR, e);
    }
  }
}
