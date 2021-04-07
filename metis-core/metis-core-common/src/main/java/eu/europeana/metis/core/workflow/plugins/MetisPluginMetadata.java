package eu.europeana.metis.core.workflow.plugins;

import java.util.Date;

/**
 * This interface represents plugin metadata
 */
public interface MetisPluginMetadata {

  PluginType getPluginType();

  String getRevisionNamePreviousPlugin();

  Date getRevisionTimestampPreviousPlugin();

}
