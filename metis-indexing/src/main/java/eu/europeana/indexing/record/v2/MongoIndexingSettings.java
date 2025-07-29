package eu.europeana.indexing.record.v2;

import static eu.europeana.indexing.utils.IndexingSettingsUtils.nonNullFieldName;

import eu.europeana.indexing.IndexingProperties;
import eu.europeana.indexing.common.exception.SetupRelatedIndexingException;
import eu.europeana.metis.common.DatabaseProperties;
import eu.europeana.metis.common.SettingsHolder;
import eu.europeana.metis.mongo.connection.MongoProperties;

/**
 * The type Mongo indexing settings.
 */
public class MongoIndexingSettings implements SettingsHolder {

  private String mongoDatabaseName;
  private String mongoTombstoneDatabaseName;
  private String recordRedirectDatabaseName;
  private final MongoProperties<SetupRelatedIndexingException> mongoProperties;
  private IndexingProperties indexingProperties;

  /**
   * Instantiates a new Mongo indexing settings.
   *
   * @param properties the mongo connection properties
   * @throws SetupRelatedIndexingException the setup related indexing exception
   */
  public MongoIndexingSettings(MongoProperties<SetupRelatedIndexingException> properties) throws SetupRelatedIndexingException {
    this.mongoProperties = nonNullFieldName(properties, "properties");
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
   * Gets mongo database name.
   *
   * @return the mongo tombstone database name
   */
  public String getMongoTombstoneDatabaseName() {
    return mongoTombstoneDatabaseName;
  }

  /**
   * Sets mongo tombstone database name.
   *
   * @param mongoTombstoneDatabaseName the mongo tombstone database name
   */
  public void setMongoTombstoneDatabaseName(String mongoTombstoneDatabaseName) {
    this.mongoTombstoneDatabaseName = mongoTombstoneDatabaseName;
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
  @Override
  public DatabaseProperties getDatabaseProperties() {
    return mongoProperties;
  }

  /**
   * Gets indexing properties.
   *
   * @return the indexing properties
   */
  public IndexingProperties getIndexingProperties() {
    return indexingProperties;
  }

  /**
   * Sets indexing properties.
   *
   * @param indexingProperties the indexing properties
   */
  public void setIndexingProperties(IndexingProperties indexingProperties) {
    this.indexingProperties = indexingProperties;
  }
}
