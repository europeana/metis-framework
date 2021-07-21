package eu.europeana.metis.core.workflow.plugins;

/**
 * This abstract class is the base implementation of {@link ExecutablePluginMetadata} for harvest
 * tasks. All executable harvest plugins should inherit from it.
 */
public abstract class AbstractHarvestPluginMetadata extends AbstractExecutablePluginMetadata {

  //Default false. If false, it indicates that the ProvidedCHO rdf:about should be used to set the identifier for ECloud
  private boolean useDefaultIdentifiers;

  public AbstractHarvestPluginMetadata() {
    //Required for json serialization
  }

  public boolean isUseDefaultIdentifiers() {
    return useDefaultIdentifiers;
  }

  public void setUseDefaultIdentifiers(boolean useDefaultIdentifiers) {
    this.useDefaultIdentifiers = useDefaultIdentifiers;
  }

  public abstract boolean isIncrementalHarvest();
}
