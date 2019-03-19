package eu.europeana.metis.core.workflow.plugins;

/**
 * Class that contains basic parameters required for each {@link eu.europeana.cloud.service.dps.DpsTask}
 * that is sent to ECloud.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2019-03-19
 */
public class EcloudBasePluginParameters {

  private final String ecloudBaseUrl;
  private final String ecloudProvider;
  private final String ecloudDataset;
  private final String previousExternalTaskId;

  /**
   * Constructor with all parameters.
   *
   * @param ecloudBaseUrl the base url endpoint to ecloud api
   * @param ecloudProvider the ecloud provider to use
   * @param ecloudDataset the ecloud dataset to use
   * @param previousExternalTaskId the task identifier from the previous plugin execution. It is used
   * to calculate faster the total records for the current execution on the ecloud side. Can be null
   * if there is no previous task, like for example a harvesting plugin.
   */
  public EcloudBasePluginParameters(String ecloudBaseUrl, String ecloudProvider, String ecloudDataset,
      String previousExternalTaskId) {
    this.ecloudBaseUrl = ecloudBaseUrl;
    this.ecloudProvider = ecloudProvider;
    this.ecloudDataset = ecloudDataset;
    this.previousExternalTaskId = previousExternalTaskId;
  }

  public String getEcloudBaseUrl() {
    return ecloudBaseUrl;
  }

  public String getEcloudProvider() {
    return ecloudProvider;
  }

  public String getEcloudDataset() {
    return ecloudDataset;
  }

  public String getPreviousExternalTaskId() {
    return previousExternalTaskId;
  }
}
