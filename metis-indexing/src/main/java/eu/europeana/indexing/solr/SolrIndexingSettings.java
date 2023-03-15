package eu.europeana.indexing.solr;

import static eu.europeana.indexing.utils.IndexingSettingsUtils.nonNullFieldName;

import eu.europeana.indexing.exception.SetupRelatedIndexingException;
import eu.europeana.metis.common.SettingsHolder;
import eu.europeana.metis.solr.connection.SolrProperties;

/**
 * The type Solr indexing settings.
 */
public class SolrIndexingSettings implements SettingsHolder {
  private final SolrProperties<SetupRelatedIndexingException> solrProperties;

  /**
   * Instantiates a new Solr indexing settings.
   *
   * @param properties the solr connection properties
   * @throws SetupRelatedIndexingException the setup related indexing exception
   */
  public SolrIndexingSettings(SolrProperties<SetupRelatedIndexingException> properties) throws SetupRelatedIndexingException {
    this. solrProperties = nonNullFieldName(properties,"properties");
  }

  /**
   * Gets solr properties.
   *
   * @return the solr properties
   */
  public SolrProperties<SetupRelatedIndexingException> getSolrProperties() {
    return solrProperties;
  }
}
