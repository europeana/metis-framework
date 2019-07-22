package eu.europeana.metis.core.workflow.plugins;

/**
 * This abstract class is the base implementation of {@link ExecutablePluginMetadata} and all
 * executable plugins should inherit from it.
 */
public abstract class AbstractExecutablePluginMetadata extends AbstractMetisPluginMetadata
    implements ExecutablePluginMetadata {

  private boolean enabled;

  public AbstractExecutablePluginMetadata() {
  }

  @Override
  public final PluginType getPluginType() {
    return getExecutablePluginType().toPluginType();
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public void setPreviousRevisionInformation(ExecutablePlugin<?> previousPlugin) {
    if (previousPlugin.getPluginMetadata().getExecutablePluginType().isRevisionLess()) {
      //If previous plugin is revisionLess use the previous plugin of that instead
      // TODO JV this will work only if there are not two revisionless plugins in a row.
      this.setRevisionNamePreviousPlugin(
          previousPlugin.getPluginMetadata().getRevisionNamePreviousPlugin());
      this.setRevisionTimestampPreviousPlugin(
          previousPlugin.getPluginMetadata().getRevisionTimestampPreviousPlugin());
    } else {
      this.setRevisionNamePreviousPlugin(previousPlugin.getPluginType().name());
      this.setRevisionTimestampPreviousPlugin(previousPlugin.getStartedDate());
    }
  }
}
