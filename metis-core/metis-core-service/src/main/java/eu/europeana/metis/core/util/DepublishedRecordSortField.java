package eu.europeana.metis.core.util;

import eu.europeana.metis.core.dataset.DepublishedRecord;

/**
 * Defines the sorting fields known for depublished records.
 */
public enum DepublishedRecordSortField {

  /**
   * Sorting based on record ID.
   */
  RECORD_ID(DepublishedRecord.RECORD_ID_FIELD),

  /**
   * Sorting based on the depublication state.
   */
  DEPUBLICATION_STATE(DepublishedRecord.DEPUBLICATION_STATE_FIELD),

  /**
   * Sorting based on the depublication date.
   */
  DEPUBLICATION_DATE(DepublishedRecord.DEPUBLICATION_DATE_FIELD);

  private final String databaseField;

  DepublishedRecordSortField(String databaseField) {
    this.databaseField = databaseField;
  }

  /**
   * Get the corresponding field name in the database.
   *
   * @return The field name.
   */
  public String getDatabaseField() {
    return databaseField;
  }
}
