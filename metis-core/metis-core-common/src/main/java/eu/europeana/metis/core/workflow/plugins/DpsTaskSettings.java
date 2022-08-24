package eu.europeana.metis.core.workflow.plugins;

/**
 * Class that contains basic parameters required for each {@link eu.europeana.cloud.service.dps.DpsTask}
 * that is sent to ECloud.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2019-03-19
 */
public class DpsTaskSettings {

  private final String ecloudBaseUrl;
  private final String ecloudProvider;
  private final String ecloudDatasetId;
  private final String metisCoreBaseUrl;
  private final String previousExternalTaskId;
  private final ThrottlingValues throttlingValues;

  /**
   * Constructor with all parameters.
   *
   * @param ecloudBaseUrl the base url endpoint to ecloud api
   * @param ecloudProvider the ecloud provider to use
   * @param ecloudDatasetId the ecloud dataset to use
   * @param previousExternalTaskId the task identifier from the previous plugin execution. It is
   * used to calculate faster the total records for the current execution on the ecloud side. Can be
   * null if there is no previous task, like for example a harvesting plugin.
   * @param metisCoreBaseUrl the base url of metis core application
   */
  public DpsTaskSettings(String ecloudBaseUrl, String ecloudProvider,
                         String ecloudDatasetId, String previousExternalTaskId, String metisCoreBaseUrl, ThrottlingValues throttlingValues) {
    this.ecloudBaseUrl = ecloudBaseUrl;
    this.ecloudProvider = ecloudProvider;
    this.ecloudDatasetId = ecloudDatasetId;
    this.metisCoreBaseUrl = metisCoreBaseUrl;
    this.previousExternalTaskId = previousExternalTaskId;
    this.throttlingValues = throttlingValues;
  }

  public String getEcloudBaseUrl() {
    return ecloudBaseUrl;
  }

  public String getEcloudProvider() {
    return ecloudProvider;
  }

  public String getEcloudDatasetId() {
    return ecloudDatasetId;
  }

  public String getPreviousExternalTaskId() {
    return previousExternalTaskId;
  }

  public String getMetisCoreBaseUrl() {
    return metisCoreBaseUrl;
  }

  public ThrottlingValues getThrottlingValues(){
    return throttlingValues;
  }
}
