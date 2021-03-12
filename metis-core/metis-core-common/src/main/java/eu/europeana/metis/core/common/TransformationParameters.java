package eu.europeana.metis.core.common;

import eu.europeana.metis.core.dataset.Dataset;
import java.util.Locale;

/**
 * This class is to be used to create the transformation parameters based on a provided {@link
 * Dataset}, so that there is a centralized location of how those parameters should be created.
 */
public class TransformationParameters {

  private final String datasetName;
  private final String edmCountry;
  private final String edmLanguage;

  /**
   * Constructor that initializes all final fields.
   *
   * @param dataset the provided dataset
   */
  public TransformationParameters(Dataset dataset) {
    //DatasetName in Transformation should be a concatenation datasetId_datasetName
    datasetName = dataset.getDatasetId() + "_" + dataset.getDatasetName();
    edmCountry = dataset.getCountry().getName();
    edmLanguage = dataset.getLanguage().name().toLowerCase(Locale.US);
  }

  public String getDatasetName() {
    return datasetName;
  }

  public String getEdmCountry() {
    return edmCountry;
  }

  public String getEdmLanguage() {
    return edmLanguage;
  }
}
