package eu.europeana.metis.core.dataset;

import eu.europeana.metis.core.common.HarvestType;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-06-01
 */
public class FolderHarvestingMetadata implements HarvestingMetadata {
  private HarvestType harvestType = HarvestType.FOLDER_HARVEST;
  private String recordXPath;

  @Override
  public HarvestType getHarvestType() {
    return harvestType;
  }

  public String getRecordXPath() {
    return recordXPath;
  }

  public void setRecordXPath(String recordXPath) {
    this.recordXPath = recordXPath;
  }
}
