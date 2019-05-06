package eu.europeana.metis.core.workflow.plugins;

/**
 * <p>This enum lists the various states the result data of an executable plugin can be in when it
 * comes to presence in eCloud. This does not attempt to say anything about the quality of the data
 * (e.g. if it is complete or if there are errors). Furthermore, data here denotes not just the
 * result of the plugin, but also all associated logs and error reports, etc.</p>
 * <p>This enum is meant to be backwards compatible: if an executable plugin doesn't have a
 * status set, it should be assumed that the status is {@link DataStatus#DEFAULT_VALUE}.</p>
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
   * The result data of this executable plugin is available, but it has been deprecated (by a
   * non-executable plugin).
   */
  DEPRECATED;

  // TODO Add 'DELETED': the result data is no longer available.

  /**
   * This constant captures the default value. If an executable pluign doesn't have a status set, it
   * should be assumed that the status is equal to this value. It is currently equal to {@link
   * DataStatus#VALID}.
   */
  public static final DataStatus DEFAULT_VALUE = DataStatus.VALID;

}
