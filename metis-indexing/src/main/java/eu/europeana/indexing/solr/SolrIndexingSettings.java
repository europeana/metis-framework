package eu.europeana.indexing.solr;

import static eu.europeana.indexing.utils.IndexingSettingsUtils.nonNullFieldName;

import eu.europeana.indexing.IndexingProperties;
import eu.europeana.indexing.exception.SetupRelatedIndexingException;
import eu.europeana.metis.common.DatabaseProperties;
import eu.europeana.metis.common.SettingsHolder;
import eu.europeana.metis.solr.connection.SolrProperties;

/**
 * The type Solr indexing settings.
 */
public class SolrIndexingSettings implements SettingsHolder {

  private final SolrProperties<SetupRelatedIndexingException> solrProperties;
  private IndexingProperties indexingProperties;
  /**
   * Instantiates a new Solr indexing settings.
   *
   * @param properties the solr connection properties
   * @throws SetupRelatedIndexingException the setup related indexing exception
   */
  public SolrIndexingSettings(SolrProperties<SetupRelatedIndexingException> properties) throws SetupRelatedIndexingException {
    this.solrProperties = nonNullFieldName(properties, "properties");
  }

  /**
   * Gets solr properties.
   *
   * @return the solr properties
   */
  @Override
  public DatabaseProperties getDatabaseProperties() {
    return solrProperties;
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
