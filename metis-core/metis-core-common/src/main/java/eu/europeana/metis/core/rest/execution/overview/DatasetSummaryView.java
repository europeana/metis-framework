package eu.europeana.metis.core.rest.execution.overview;

import eu.europeana.metis.core.dataset.Dataset;

/**
 * This class represents the vital information on a dataset needed for the execution overview.
 */
public class DatasetSummaryView {

  private String id;
  private String datasetId;
  private String datasetName;

  DatasetSummaryView() {
  }

  DatasetSummaryView(Dataset dataset) {
    this.id = dataset.getId().toString();
    this.datasetId = dataset.getDatasetId();
    this.datasetName = dataset.getDatasetName();
  }

  public String getId() {
    return id;
  }

  public String getDatasetId() {
    return datasetId;
  }

  public String getDatasetName() {
    return datasetName;
  }
}
