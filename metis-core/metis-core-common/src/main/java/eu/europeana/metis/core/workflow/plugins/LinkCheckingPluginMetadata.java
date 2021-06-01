package eu.europeana.metis.core.workflow.plugins;

/**
 * Link Checking Plugin Metadata.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-05-16
 */
public class LinkCheckingPluginMetadata extends AbstractExecutablePluginMetadata {

  private static final ExecutablePluginType pluginType = ExecutablePluginType.LINK_CHECKING;

  private Boolean performSampling;
  private Integer sampleSize;

  public LinkCheckingPluginMetadata() {
    //Required for json serialization
  }

  @Override
  public ExecutablePluginType getExecutablePluginType() {
    return pluginType;
  }

  public void setPerformSampling(Boolean performSampling) {
    this.performSampling = performSampling;
  }

  public Integer getSampleSize() {
    return sampleSize;
  }

  public void setSampleSize(Integer sampleSize) {
    this.sampleSize = sampleSize;
  }

  public Boolean getPerformSampling() {
    return performSampling;
  }
}
