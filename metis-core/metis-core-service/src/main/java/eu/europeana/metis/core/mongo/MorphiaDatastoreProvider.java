package eu.europeana.metis.core.mongo;

import com.mongodb.MongoClient;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.dataset.DatasetIdSequence;
import eu.europeana.metis.core.dataset.DatasetXslt;
import eu.europeana.metis.core.workflow.ScheduledWorkflow;
import eu.europeana.metis.core.workflow.Workflow;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.EnrichmentPlugin;
import eu.europeana.metis.core.workflow.plugins.HTTPHarvestPlugin;
import eu.europeana.metis.core.workflow.plugins.OaipmhHarvestPlugin;
import eu.europeana.metis.core.workflow.plugins.TransformationPlugin;
import eu.europeana.metis.core.workflow.plugins.ValidationExternalPlugin;
import eu.europeana.metis.core.workflow.plugins.ValidationInternalPlugin;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

/**
 * Class to initialize the mongo collections and the {@link Datastore} connection.
 */
public class MorphiaDatastoreProvider {

  private final Datastore datastore;

  /**
   * Constructor to initialize the mongo mappings/collections and the {@link Datastore} connection.
   * @param mongoClient {@link MongoClient}
   * @param databaseName the database name
   */
  public MorphiaDatastoreProvider(MongoClient mongoClient, String databaseName) {
    Morphia morphia = new Morphia();
    morphia.map(Dataset.class);
    morphia.map(DatasetIdSequence.class);
    morphia.map(Workflow.class);
    morphia.map(WorkflowExecution.class);
    morphia.map(ScheduledWorkflow.class);
    morphia.map(AbstractMetisPlugin.class);
    morphia.map(OaipmhHarvestPlugin.class);
    morphia.map(HTTPHarvestPlugin.class);
    morphia.map(EnrichmentPlugin.class);
    morphia.map(ValidationInternalPlugin.class);
    morphia.map(TransformationPlugin.class);
    morphia.map(ValidationExternalPlugin.class);
    morphia.map(AbstractMetisPluginMetadata.class);
    morphia.map(DatasetXslt.class);
    datastore = morphia.createDatastore(mongoClient, databaseName);
    datastore.ensureIndexes();

    DatasetIdSequence datasetIdSequence = datastore.find(DatasetIdSequence.class).get();
    if (datasetIdSequence == null) {
      datastore.save(new DatasetIdSequence(0));
    }
  }

  /**
   * @return the {@link Datastore} connection to Mongo
   */
  public Datastore getDatastore() {
    return datastore;
  }
}
