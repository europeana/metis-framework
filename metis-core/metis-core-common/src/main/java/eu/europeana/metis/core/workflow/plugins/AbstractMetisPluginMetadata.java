package eu.europeana.metis.core.workflow.plugins;

import java.util.List;
import java.util.Map;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-06-01
 */
public interface AbstractMetisPluginMetadata {
  Map<String, List<String>> getParameters();
  void setParameters(Map<String, List<String>> parameters);
}
