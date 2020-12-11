package eu.europeana.metis.core.execution;

import eu.europeana.metis.core.workflow.plugins.ExecutablePluginType;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Manages a map of {@link ExecutablePluginType} keys and {@link Semaphore} values.
 * <p>Each executable plugin type contains it's own semaphore so that access to those plugin
 * types is controlled.</p>
 */
public class SemaphoresPerPluginManager {

  private final int acquireTimeoutSecs;
  private final Map<ExecutablePluginType, Semaphore> unmodifiableMaxThreadsPerPlugin;

  /**
   * Constructor that initializes the map with provided number of permissions on the semaphores.
   *
   * @param acquireTimeoutSecs the timeout for acquiring a permission from a semaphore
   * @param permissionsPerSemaphore the permissions for each semaphore
   */
  public SemaphoresPerPluginManager(int acquireTimeoutSecs, int permissionsPerSemaphore) {
    this.acquireTimeoutSecs = acquireTimeoutSecs;
    Map<ExecutablePluginType, Semaphore> maxThreadsPerPlugin = new EnumMap<>(
        ExecutablePluginType.class);
    for (ExecutablePluginType executablePluginType : ExecutablePluginType.values()) {
      maxThreadsPerPlugin.put(executablePluginType, new Semaphore(permissionsPerSemaphore, true));
    }
    this.unmodifiableMaxThreadsPerPlugin = Collections.unmodifiableMap(maxThreadsPerPlugin);
  }

  /**
   * Try acquisition of a semaphore for a provided {@link ExecutablePluginType}.
   *
   * @param executablePluginType the provided executable plugin type
   * @return true if acquisition was successful, false otherwise
   * @throws InterruptedException if we were interrupted during acquisition
   */
  public boolean tryAcquireForExecutablePluginType(ExecutablePluginType executablePluginType)
      throws InterruptedException {
    return unmodifiableMaxThreadsPerPlugin.get(executablePluginType)
        .tryAcquire(acquireTimeoutSecs, TimeUnit.SECONDS);
  }

  /**
   * Release a permission for a semaphore by {@link ExecutablePluginType}.
   *
   * @param executablePluginType the executable plugin type to release the permission from
   */
  public void releaseForPluginType(ExecutablePluginType executablePluginType) {
    unmodifiableMaxThreadsPerPlugin.get(executablePluginType).release();
  }
}
