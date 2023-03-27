package eu.europeana.indexing.base;

import java.lang.invoke.MethodHandles;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.DynamicPropertyRegistry;

/**
 * The type Test container.
 */
public abstract class TestContainer {
  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /**
   * Log configuration.
   */
  abstract public void logConfiguration();

  /**
   * Dynamic properties.
   *
   * @param registry the registry
   */
  abstract public void dynamicProperties(DynamicPropertyRegistry registry);

  /**
   * Run scripts.
   *
   * @param scripts the scripts
   */
  abstract public void runScripts(List<String> scripts);
}
