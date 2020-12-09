package eu.europeana.metis.core.execution;

import eu.europeana.metis.core.workflow.plugins.ExecutablePluginType;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class SemaphoresPerPluginManager {

  private final Map<ExecutablePluginType, Semaphore> unmodifiableMaxThreadsPerPlugin;

  public SemaphoresPerPluginManager(int maxConcurrentThreadsPerPlugin) {
    Map<ExecutablePluginType, Semaphore> maxThreadsPerPlugin = new EnumMap<>(
        ExecutablePluginType.class);
    for (ExecutablePluginType executablePluginType : ExecutablePluginType.values()) {
      maxThreadsPerPlugin
          .put(executablePluginType, new Semaphore(maxConcurrentThreadsPerPlugin, true));
    }
    this.unmodifiableMaxThreadsPerPlugin = Collections.unmodifiableMap(maxThreadsPerPlugin);
  }

  public Map<ExecutablePluginType, Semaphore> getUnmodifiableMaxThreadsPerPlugin() {
    return unmodifiableMaxThreadsPerPlugin;
  }
}
