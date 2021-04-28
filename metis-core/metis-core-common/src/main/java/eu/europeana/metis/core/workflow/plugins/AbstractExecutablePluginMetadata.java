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

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

}
