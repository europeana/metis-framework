package eu.europeana.metis.core.workflow.plugins;

/**
 * This enum lists the various states the result data of an executable plugin can be in when it
 * comes to presence in eCloud. This does not attempt to say anything about the quality of the data
 * (e.g. if it is complete or if there are errors). Furthermore, data here denotes not just the
 * result of the plugin, but also all associated logs and error reports, etc.
 */
public enum DataStatus {

  /**
   * The result data of this executable plugin is not yet present because the executable plugin has
   * not (started to) run yet.
   */
  NOT_YET_GENERATED,

  /**
   * The result data of this executable plugin is available and can be used.
   */
  VALID,

  /**
   * The result data of this executable plugin is available, but it has been deprecated and should
   * no longer be used for further processing (e.g. superseded by a non-executable plugin or an
   * indexing removed from mongo/solr).
   */
  DEPRECATED,

  /**
   * The result data of this executable plugin is no longer available. It has been processed but the
   * data has subsequently been removed and is not available for further processing.
   */
  DELETED

}
