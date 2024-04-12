package eu.europeana.metis.core.mongo;

import com.mongodb.client.MongoClient;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.mapping.DiscriminatorFunction;
import dev.morphia.mapping.Mapper;
import dev.morphia.mapping.MapperOptions;
import dev.morphia.mapping.NamingStrategy;
import eu.europeana.metis.core.dao.DatasetXsltDao;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.dataset.DatasetIdSequence;
import eu.europeana.metis.core.dataset.DatasetXslt;
import eu.europeana.metis.core.dataset.DepublishRecordId;
import eu.europeana.metis.core.workflow.ScheduledWorkflow;
import eu.europeana.metis.core.workflow.Workflow;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.plugins.AbstractExecutablePlugin;
import eu.europeana.metis.core.workflow.plugins.AbstractHarvestPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.AbstractIndexPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.DepublishPlugin;
import eu.europeana.metis.core.workflow.plugins.DepublishPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.EnrichmentPlugin;
import eu.europeana.metis.core.workflow.plugins.EnrichmentPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.HTTPHarvestPlugin;
import eu.europeana.metis.core.workflow.plugins.HTTPHarvestPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.IndexToPreviewPlugin;
import eu.europeana.metis.core.workflow.plugins.IndexToPreviewPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.IndexToPublishPlugin;
import eu.europeana.metis.core.workflow.plugins.IndexToPublishPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.LinkCheckingPlugin;
import eu.europeana.metis.core.workflow.plugins.LinkCheckingPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.MediaProcessPlugin;
import eu.europeana.metis.core.workflow.plugins.MediaProcessPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.NormalizationPlugin;
import eu.europeana.metis.core.workflow.plugins.NormalizationPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.OaipmhHarvestPlugin;
import eu.europeana.metis.core.workflow.plugins.OaipmhHarvestPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.ReindexToPreviewPlugin;
import eu.europeana.metis.core.workflow.plugins.ReindexToPreviewPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.ReindexToPublishPlugin;
import eu.europeana.metis.core.workflow.plugins.ReindexToPublishPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.TransformationPlugin;
import eu.europeana.metis.core.workflow.plugins.TransformationPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.ValidationExternalPlugin;
import eu.europeana.metis.core.workflow.plugins.ValidationExternalPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.ValidationInternalPlugin;
import eu.europeana.metis.core.workflow.plugins.ValidationInternalPluginMetadata;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Class to initialize the mongo collections and the {@link Datastore} connection. It also performs
 * data initialization tasks if needed.
 */
public class MorphiaDatastoreProviderImpl implements MorphiaDatastoreProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(MorphiaDatastoreProviderImpl.class);
  private Datastore datastore;

  /**
   * Constructor to initialize the mongo mappings/collections and the {@link Datastore} connection.
   * This also initializes the {@link DatasetIdSequence} that this database uses. This constructor
   * is meant to be used when the database is already available.
   *
   * @param mongoClient {@link MongoClient}
   * @param databaseName the database name
   */
  public MorphiaDatastoreProviderImpl(MongoClient mongoClient, String databaseName) {
    this(mongoClient, databaseName, false);
  }

  /**
   * Constructor to initialize the mongo mappings/collections and the {@link Datastore} connection.
   * This also initializes the {@link DatasetIdSequence} that this database uses. This constructor
   * is meant to be used mostly for when the creation of the database is required.
   *
   * @param mongoClient {@link MongoClient}
   * @param databaseName the database name
   * @param createIndexes flag that initiates the database/indices
   */
  public MorphiaDatastoreProviderImpl(MongoClient mongoClient, String databaseName,
      boolean createIndexes) {
    createDatastore(mongoClient, databaseName);
    if (createIndexes) {
      LOGGER.info("Initializing database indices");
      datastore.ensureIndexes();
    }
  }

  private void createDatastore(MongoClient mongoClient, String databaseName) {
    // Register the mappings and set up the data store.
    // TODO: 8/28/20 The mapper options should eventually be removed but requires an update of the affected fields on all documents in the database
    final MapperOptions mapperOptions = MapperOptions.builder().discriminatorKey("className")
        .discriminator(DiscriminatorFunction.className())
        .collectionNaming(NamingStrategy.identity()).build();
    datastore = Morphia.createDatastore(mongoClient, databaseName, mapperOptions);
    final Mapper mapper = datastore.getMapper();
    mapper.getEntityModel(Dataset.class);
    mapper.getEntityModel(DatasetIdSequence.class);
    mapper.getEntityModel(Workflow.class);
    mapper.getEntityModel(WorkflowExecution.class);
    mapper.getEntityModel(ScheduledWorkflow.class);
    mapper.getEntityModel(DatasetXslt.class);
    mapper.getEntityModel(DepublishRecordId.class);
    // Plugins
    mapper.getEntityModel(AbstractMetisPlugin.class);
    mapper.getEntityModel(AbstractExecutablePlugin.class);
    mapper.getEntityModel(DepublishPlugin.class);
    mapper.getEntityModel(EnrichmentPlugin.class);
    mapper.getEntityModel(HTTPHarvestPlugin.class);
    mapper.getEntityModel(IndexToPreviewPlugin.class);
    mapper.getEntityModel(IndexToPublishPlugin.class);
    mapper.getEntityModel(LinkCheckingPlugin.class);
    mapper.getEntityModel(MediaProcessPlugin.class);
    mapper.getEntityModel(NormalizationPlugin.class);
    mapper.getEntityModel(OaipmhHarvestPlugin.class);
    mapper.getEntityModel(ReindexToPreviewPlugin.class);
    mapper.getEntityModel(ReindexToPublishPlugin.class);
    mapper.getEntityModel(TransformationPlugin.class);
    mapper.getEntityModel(ValidationExternalPlugin.class);
    mapper.getEntityModel(ValidationInternalPlugin.class);

    // Plugins metadata
    mapper.getEntityModel(AbstractMetisPluginMetadata.class);
    mapper.getEntityModel(AbstractHarvestPluginMetadata.class);
    mapper.getEntityModel(AbstractIndexPluginMetadata.class);
    mapper.getEntityModel(DepublishPluginMetadata.class);
    mapper.getEntityModel(EnrichmentPluginMetadata.class);
    mapper.getEntityModel(HTTPHarvestPluginMetadata.class);
    mapper.getEntityModel(IndexToPreviewPluginMetadata.class);
    mapper.getEntityModel(IndexToPublishPluginMetadata.class);
    mapper.getEntityModel(LinkCheckingPluginMetadata.class);
    mapper.getEntityModel(MediaProcessPluginMetadata.class);
    mapper.getEntityModel(NormalizationPluginMetadata.class);
    mapper.getEntityModel(OaipmhHarvestPluginMetadata.class);
    mapper.getEntityModel(ReindexToPreviewPluginMetadata.class);
    mapper.getEntityModel(ReindexToPublishPluginMetadata.class);
    mapper.getEntityModel(TransformationPluginMetadata.class);
    mapper.getEntityModel(ValidationExternalPluginMetadata.class);
    mapper.getEntityModel(ValidationInternalPluginMetadata.class);

    // Initialize the DatasetIdSequence if required.
    if (datastore.find(DatasetIdSequence.class).count() == 0) {
      datastore.save(new DatasetIdSequence(0));
    }
    LOGGER.info("Datastore initialized");
  }

  /**
   * Constructor. In addition to the functionality of {@link #MorphiaDatastoreProviderImpl(MongoClient,
   * String)}, it also sets a default non-dataset specific {@link DatasetXslt} if none is present.
   *
   * @param mongoClient {@link MongoClient}
   * @param databaseName the database name
   * @param defaultTransformationSupplier The default non-dataset specific {@link DatasetXslt} to
   * set if none is available.
   * @throws IOException In case the default transformation could not be loaded.
   */
  public MorphiaDatastoreProviderImpl(MongoClient mongoClient, String databaseName,
      InputStreamProvider defaultTransformationSupplier) throws IOException {

    // Initialize this class.
    this(mongoClient, databaseName);

    // Initialize the default DatasetXslt if needed.
    final DatasetXsltDao datasetXsltDao = new DatasetXsltDao(this);
    if (datasetXsltDao.getLatestDefaultXslt() == null) {
      try (final InputStream inputStream = defaultTransformationSupplier.get()) {
        final String defaultTransformationAsString = IOUtils
            .toString(inputStream, StandardCharsets.UTF_8.name());
        final DatasetXslt defaultTransformation = new DatasetXslt(DatasetXslt.DEFAULT_DATASET_ID,
            defaultTransformationAsString);
        datasetXsltDao.create(defaultTransformation);
      }
    }
  }

  @Override
  public Datastore getDatastore() {
    return datastore;
  }

  /**
   * An interface similar to {@link java.util.function.Supplier}, but specifically for instances of
   * {@link InputStream} and that allows the throwing of an {@link IOException}.
   */
  public interface InputStreamProvider {

    /**
     * @return The input stream.
     * @throws IOException In case the stream could not be opened.
     */
    InputStream get() throws IOException;
  }
}
