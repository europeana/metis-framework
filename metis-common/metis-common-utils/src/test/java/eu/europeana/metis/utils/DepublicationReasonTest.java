package eu.europeana.metis.utils;

import static eu.europeana.metis.utils.DepublicationReason.BROKEN_MEDIA_LINKS;
import static eu.europeana.metis.utils.DepublicationReason.GDPR;
import static eu.europeana.metis.utils.DepublicationReason.GENERIC;
import static eu.europeana.metis.utils.DepublicationReason.PERMISSION_ISSUES;
import static eu.europeana.metis.utils.DepublicationReason.REMOVED_DATA_AT_SOURCE;
import static eu.europeana.metis.utils.DepublicationReason.SENSITIVE_CONTENT;
import static eu.europeana.metis.utils.DepublicationReason.UNKNOWN;
import static eu.europeana.metis.utils.DepublicationReason.values;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class DepublicationReasonTest {

  @Test
  void testValues() {
    Arrays.stream(values()).forEach(depublicationReason -> {
      assertNotNull(depublicationReason.getTitle());
      assertNotNull(depublicationReason.getUrl());
    });
  }

  @Test
  void testToStringMethod() {
    assertEquals("Broken media links", BROKEN_MEDIA_LINKS.toString());
    assertEquals("GDPR", GDPR.toString());
    assertEquals("Permission issues", PERMISSION_ISSUES.toString());
    assertEquals("Sensitive content", SENSITIVE_CONTENT.toString());
    assertEquals("Removed data at source", REMOVED_DATA_AT_SOURCE.toString());
    assertEquals("Generic", GENERIC.toString());
    assertEquals("Unknown", UNKNOWN.toString());
  }

  @Test
  void testEnumValuePresence() {
    List<DepublicationReason> depublicationReasons = asList(values());
    assertEquals(7, depublicationReasons.size());

    assertTrue(depublicationReasons.contains(BROKEN_MEDIA_LINKS));
    assertTrue(depublicationReasons.contains(GDPR));
    assertTrue(depublicationReasons.contains(PERMISSION_ISSUES));
    assertTrue(depublicationReasons.contains(SENSITIVE_CONTENT));
    assertTrue(depublicationReasons.contains(REMOVED_DATA_AT_SOURCE));
    assertTrue(depublicationReasons.contains(GENERIC));
    assertTrue(depublicationReasons.contains(UNKNOWN));
  }
}