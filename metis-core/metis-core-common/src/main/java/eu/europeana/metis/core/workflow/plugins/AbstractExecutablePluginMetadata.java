package eu.europeana.metis.core.workflow.plugins;

/**
 * Abstract super class for all plugin metadata that can be executed by Metis.
 */
public abstract class AbstractExecutablePluginMetadata extends AbstractMetisPluginMetadata {

  private boolean enabled;

  public AbstractExecutablePluginMetadata() {
  }

  @Override
  public final PluginType getPluginType() {
    return getExecutablePluginType().toPluginType();
  }

  public abstract ExecutablePluginType getExecutablePluginType();

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  /**
   * For the current plugin, setup the source/previous revision information.
   * <p>
   * The source revision information that this plugin will be based on, is coming from the {@code
   * previousPlugin} plugin metadata. The {@code previousPlugin} can be a RevisionLess plugin, in
   * which case the revision information for the current plugin will take the source revision
   * information that the {@code previousPlugin} was based on.
   * </p>
   *
   * @param previousPlugin the source/previous plugin that is used to base the current plugin on
   */
  public void setPreviousRevisionInformation(AbstractExecutablePlugin<?> previousPlugin) {
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
