package eu.europeana.metis.core.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.metis.core.workflow.plugins.ExecutablePluginType;
import org.junit.jupiter.api.Test;

class TestSemaphoresPerPluginManager {

  @Test
  void initiateAndCheckAllSemaphores() {
    final SemaphoresPerPluginManager semaphoresPerPluginManager = new SemaphoresPerPluginManager(2);

    int successAcquiresCounter = 0;
    for (ExecutablePluginType executablePluginType : ExecutablePluginType.values()) {
      if (semaphoresPerPluginManager.tryAcquireForExecutablePluginType(executablePluginType)) {
        semaphoresPerPluginManager.releaseForPluginType(executablePluginType);
        successAcquiresCounter++;
      }
    }

    assertEquals(ExecutablePluginType.values().length, successAcquiresCounter);
  }

  @Test
  void checkFullSemaphore() {
    final SemaphoresPerPluginManager semaphoresPerPluginManager = new SemaphoresPerPluginManager(1);

    boolean acquired = semaphoresPerPluginManager
        .tryAcquireForExecutablePluginType(ExecutablePluginType.ENRICHMENT);
    assertTrue(acquired);
    // Same plugin should not be allowed
    acquired = semaphoresPerPluginManager
        .tryAcquireForExecutablePluginType(ExecutablePluginType.ENRICHMENT);
    assertFalse(acquired);
    // Different plugin should be allowed
    acquired = semaphoresPerPluginManager
        .tryAcquireForExecutablePluginType(ExecutablePluginType.NORMALIZATION);
    assertTrue(acquired);

    // Release previously failed and re-acquire
    semaphoresPerPluginManager.releaseForPluginType(ExecutablePluginType.ENRICHMENT);
    acquired = semaphoresPerPluginManager
        .tryAcquireForExecutablePluginType(ExecutablePluginType.ENRICHMENT);
    assertTrue(acquired);
    semaphoresPerPluginManager.releaseForPluginType(ExecutablePluginType.ENRICHMENT);
    semaphoresPerPluginManager.releaseForPluginType(ExecutablePluginType.NORMALIZATION);
  }

}