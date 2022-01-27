package eu.europeana.metis.mongo.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link CustomObjectMapper} Only verifies that the module that contains ObjectIdSerializable is added.
 */
class CustomObjectMapperTest {

  @Test
  void registerModule() {
    final CustomObjectMapper customObjectMapper = new CustomObjectMapper();

    customObjectMapper.findAndRegisterModules();
    Set<Object> modules = customObjectMapper.getRegisteredModuleIds();

    assertEquals(1, modules.size());
  }
}