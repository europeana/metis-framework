package eu.europeana.metis.mongo.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link CustomObjectMapper} Only verifies that the module that contains ObjectIdSerializable is added.
 *
 * @author Jorge Ortiz
 * @since 31-01-2022
 */
class CustomObjectMapperTest {

  @Test
  void registerModule() {
    final CustomObjectMapper customObjectMapper = new CustomObjectMapper();
    Set<Object> modules = customObjectMapper.getRegisteredModuleIds();
    assertEquals(1, modules.size());
  }
}