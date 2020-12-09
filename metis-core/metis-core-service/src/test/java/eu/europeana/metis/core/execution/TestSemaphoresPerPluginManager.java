package eu.europeana.metis.core.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;

import eu.europeana.metis.core.workflow.plugins.ExecutablePluginType;
import java.util.Map;
import java.util.concurrent.Semaphore;
import org.junit.jupiter.api.Test;

class TestSemaphoresPerPluginManager {

  @Test
  void initiate() throws InterruptedException {
    final SemaphoresPerPluginManager semaphoresPerPluginManager = new SemaphoresPerPluginManager(2);
    final Map<ExecutablePluginType, Semaphore> unmodifiableMaxThreadsPerPlugin = semaphoresPerPluginManager
        .getUnmodifiableMaxThreadsPerPlugin();

    assertEquals(ExecutablePluginType.values().length, unmodifiableMaxThreadsPerPlugin.size());
    for (Semaphore semaphore : unmodifiableMaxThreadsPerPlugin.values()) {
      semaphore.acquire();
      semaphore.release();
    }
  }

}