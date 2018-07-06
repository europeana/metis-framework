package eu.europeana.metis.data.checker.common.model;

public class DatasetProperties {

  private static final String DEFAULT_DATASET_NAME_TEMPLATE = "Temporary dataset %s";
  private static final String DEFAULT_EDM_COUNTRY_TEMPLATE = "Country of dataset %s";
  private static final String DEFAULT_EDM_LANGUAGE_TEMPLATE = "Language of dataset %s";

  private final String datasetId;
  private final String datasetName;
  private final String edmCountry;
  private final String edmLanguage;

  public DatasetProperties(String datasetId, String datasetName, String edmCountry,
      String edmLanguage) {
    if (datasetId == null) {
      throw new IllegalArgumentException();
    }
    this.datasetId = datasetId;
    this.datasetName =
        datasetName != null ? datasetName : String.format(DEFAULT_DATASET_NAME_TEMPLATE, datasetId);
    this.edmCountry =
        edmCountry != null ? edmCountry : String.format(DEFAULT_EDM_COUNTRY_TEMPLATE, datasetId);
    this.edmLanguage =
        edmLanguage != null ? edmLanguage : String.format(DEFAULT_EDM_LANGUAGE_TEMPLATE, datasetId);
  }

  public String getDatasetId() {
    return datasetId;
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
