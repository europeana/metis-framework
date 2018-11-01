package eu.europeana.metis.data.checker.common.model;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.util.ClientUtils;

public class DatasetProperties {

  private static final String DATASET_NAME_SEPARATOR = "_";

  private static final String DEFAULT_DATASET_NAME = "Unnamed dataset";
  private static final String DEFAULT_EDM_COUNTRY = "Netherlands";
  private static final String DEFAULT_EDM_LANGUAGE = "nl";

  private final String datasetId;
  private final String datasetName;
  private final String edmCountry;
  private final String edmLanguage;

  /**
   * Constructor.
   *
   * @param datasetId The dataset ID.
   * @param datasetName The dataset name that the user specified. Can be blank or null, in which
   * case a default value ({@value #DEFAULT_DATASET_NAME}) will be used. Also, in all cases, this
   * constructor will prefix this name by the dataset ID, in order to create a unique name.
   * @param edmCountry The country value that the user specified. Can be null, in which case a
   * default value ({@value #DEFAULT_EDM_COUNTRY}) will be used.
   * @param edmLanguage The language value that the user specified. Can be null, in which case a
   * default value ({@value #DEFAULT_EDM_LANGUAGE}) will be used.
   */
  public DatasetProperties(String datasetId, String datasetName, String edmCountry,
      String edmLanguage) {
    if (datasetId == null) {
      throw new IllegalArgumentException();
    }
    this.datasetId = datasetId;
    final String nonBlankDatasetName =
        StringUtils.isBlank(datasetName) ? DEFAULT_DATASET_NAME : datasetName;
    this.datasetName = datasetId + DATASET_NAME_SEPARATOR + nonBlankDatasetName;
    this.edmCountry = edmCountry != null ? edmCountry : DEFAULT_EDM_COUNTRY;
    this.edmLanguage = edmLanguage != null ? edmLanguage : DEFAULT_EDM_LANGUAGE;
  }

  /**
   * @return The Solr query value (properly escaped for use in a Solr query) that can be used to
   * match against the dataset name in order to retrieve all records in this dataset.
   */
  public String getDatasetNameSolrQueryValue() {
    return ClientUtils.escapeQueryChars(this.datasetId + DATASET_NAME_SEPARATOR) + "*";
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
