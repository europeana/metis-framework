package eu.europeana.indexing.base;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.DynamicPropertyRegistry;

public abstract class TestContainer {
  private static final Logger LOGGER = LoggerFactory.getLogger(TestContainer.class);
  abstract public void logConfiguration();
  abstract public void dynamicProperties(DynamicPropertyRegistry registry);
  abstract public void runScripts(List<String> scripts);
}
