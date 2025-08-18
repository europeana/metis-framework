package eu.europeana.indexing;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import eu.europeana.indexing.common.contract.RecordPersistence;
import eu.europeana.indexing.common.contract.SearchPersistence;
import eu.europeana.indexing.common.contract.TombstonePersistence;
import eu.europeana.indexing.exception.SetupRelatedIndexingException;
import eu.europeana.metis.mongo.connection.MongoProperties;
import eu.europeana.metis.solr.connection.SolrProperties;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.jupiter.api.Test;

class PersistenceFactoryTest {

  private final PersistenceFactory persistenceFactory = new PersistenceFactory();

  @Test
  void getSolrIndexer() throws SetupRelatedIndexingException, URISyntaxException {
    SolrProperties<SetupRelatedIndexingException> solrProperties = new SolrProperties<>(SetupRelatedIndexingException::new);
    solrProperties.addSolrHost(new URI("http://localhost:8983"));
    assertInstanceOf(SearchPersistence.class, persistenceFactory.createIndexerForSearch(solrProperties));
  }

  @Test
  void getMongoIndexerForTombstoneRecords() throws SetupRelatedIndexingException {
    MongoProperties<SetupRelatedIndexingException> mongoProperties = new MongoProperties<>(SetupRelatedIndexingException::new);
    mongoProperties.setMongoHosts(new String[]{"localhost"},new int[]{27001});
    assertInstanceOf(TombstonePersistence.class, persistenceFactory.createIndexerForTombstones(mongoProperties, "tombstoneRecordDB"));
  }

  @Test
  void getMongoIndexerForLiveRecords() throws SetupRelatedIndexingException {
    MongoProperties<SetupRelatedIndexingException> mongoProperties = new MongoProperties<>(SetupRelatedIndexingException::new);
    mongoProperties.setMongoHosts(new String[]{"localhost"},new int[]{27001});
    assertInstanceOf(RecordPersistence.class, persistenceFactory.createIndexerForPersistence(mongoProperties, "recordDB"));
  }
}
