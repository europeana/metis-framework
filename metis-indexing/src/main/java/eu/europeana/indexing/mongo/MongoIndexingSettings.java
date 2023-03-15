package eu.europeana.indexing.mongo;

import static eu.europeana.indexing.utils.IndexingSettingsUtils.nonNullFieldName;

import eu.europeana.indexing.exception.SetupRelatedIndexingException;
import eu.europeana.metis.common.SettingsHolder;
import eu.europeana.metis.mongo.connection.MongoProperties;

/**
 * The type Mongo indexing settings.
 */
public class MongoIndexingSettings implements SettingsHolder {

  private String mongoDatabaseName;
  private String recordRedirectDatabaseName;
  private final MongoProperties<SetupRelatedIndexingException> mongoProperties;

  /**
   * Instantiates a new Mongo indexing settings.
   *
   * @param properties the mongo connection properties
   * @throws SetupRelatedIndexingException the setup related indexing exception
   */
  public MongoIndexingSettings(MongoProperties<SetupRelatedIndexingException> properties) throws SetupRelatedIndexingException {
    this.mongoProperties = nonNullFieldName(properties,"properties");
  }

  /**
   * Gets mongo database name.
   *
   * @return the mongo database name
   */
  public String getMongoDatabaseName() {
    return mongoDatabaseName;
  }

  /**
   * Sets mongo database name.
   *
   * @param mongoDatabaseName the mongo database name
   */
  public void setMongoDatabaseName(String mongoDatabaseName) {
    this.mongoDatabaseName = mongoDatabaseName;
  }

  /**
   * Gets record redirect database name.
   *
   * @return the record redirect database name
   */
  public String getRecordRedirectDatabaseName() {
    return recordRedirectDatabaseName;
  }

  /**
   * Sets record redirect database name.
   *
   * @param recordRedirectDatabaseName the record redirect database name
   */
  public void setRecordRedirectDatabaseName(String recordRedirectDatabaseName) {
    this.recordRedirectDatabaseName = recordRedirectDatabaseName;
  }

  /**
   * Gets mongo properties.
   *
   * @return the mongo properties
   */
  public MongoProperties<SetupRelatedIndexingException> getMongoProperties() {
    return mongoProperties;
  }
}
