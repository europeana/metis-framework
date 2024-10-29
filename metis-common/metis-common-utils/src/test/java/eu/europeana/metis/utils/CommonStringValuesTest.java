package eu.europeana.metis.utils;

import static eu.europeana.metis.utils.CommonStringValues.BATCH_OF_DATASETS_RETURNED;
import static eu.europeana.metis.utils.CommonStringValues.CRLF_PATTERN;
import static eu.europeana.metis.utils.CommonStringValues.DATE_FORMAT;
import static eu.europeana.metis.utils.CommonStringValues.DATE_FORMAT_FOR_REQUEST_PARAM;
import static eu.europeana.metis.utils.CommonStringValues.DATE_FORMAT_FOR_SCHEDULING;
import static eu.europeana.metis.utils.CommonStringValues.DATE_FORMAT_Z;
import static eu.europeana.metis.utils.CommonStringValues.EUROPEANA_ID_CREATOR_INITIALIZATION_FAILED;
import static eu.europeana.metis.utils.CommonStringValues.NEXT_PAGE_CANNOT_BE_NEGATIVE;
import static eu.europeana.metis.utils.CommonStringValues.PAGE_COUNT_CANNOT_BE_ZERO_OR_NEGATIVE;
import static eu.europeana.metis.utils.CommonStringValues.PLUGIN_EXECUTION_NOT_ALLOWED;
import static eu.europeana.metis.utils.CommonStringValues.REPLACEABLE_CRLF_CHARACTERS_REGEX;
import static eu.europeana.metis.utils.CommonStringValues.S_DATA_PROVIDERS_S_DATA_SETS_S_TEMPLATE;
import static eu.europeana.metis.utils.CommonStringValues.UNAUTHORIZED;
import static eu.europeana.metis.utils.CommonStringValues.WRONG_ACCESS_TOKEN;
import static eu.europeana.metis.utils.CommonStringValues.sanitizeCRLF;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class CommonStringValuesTest {

  @Test
  void testFieldsAreUsed() {
    assertNotNull(WRONG_ACCESS_TOKEN);
    assertNotNull(BATCH_OF_DATASETS_RETURNED);
    assertNotNull(NEXT_PAGE_CANNOT_BE_NEGATIVE);
    assertNotNull(PAGE_COUNT_CANNOT_BE_ZERO_OR_NEGATIVE);
    assertNotNull(PLUGIN_EXECUTION_NOT_ALLOWED);
    assertNotNull(UNAUTHORIZED);
    assertNotNull(EUROPEANA_ID_CREATOR_INITIALIZATION_FAILED);
    assertNotNull(DATE_FORMAT);
    assertNotNull(DATE_FORMAT_Z);
    assertNotNull(DATE_FORMAT_FOR_SCHEDULING);
    assertNotNull(DATE_FORMAT_FOR_REQUEST_PARAM);
    assertNotNull(S_DATA_PROVIDERS_S_DATA_SETS_S_TEMPLATE);
    assertNotNull(REPLACEABLE_CRLF_CHARACTERS_REGEX);
    assertNotNull(CRLF_PATTERN);
  }

  @Test
  void testPattern() {
    Pattern expectedPattern = Pattern.compile("[\r\n\t]");
    assertEquals(expectedPattern.pattern(), CRLF_PATTERN.pattern());
  }

  @Test
  void testSanitizeCRLF_NullInput() {
    assertNull(sanitizeCRLF(null));
  }

  @Test
  void testSanitizeStringForLogging_EmptyString() {
    String input = "";
    assertEquals("", sanitizeCRLF(input));
  }

  @Test
  void testSanitizeCRLF_NoSpecialCharacters() {
    String input = "This is a test.";
    assertEquals("This is a test.", sanitizeCRLF(input));
  }

  @Test
  void testSanitizeCRLF_WithCRLFCharacters() {
    String input = "This is a test.\nThis is a new line.\rThis is a carriage return.\tThis is a tab.";
    String expected = "This is a test.This is a new line.This is a carriage return.This is a tab.";
    assertEquals(expected, sanitizeCRLF(input));
  }

  @Test
  void testSanitizeCRLF_MixedInput() {
    String input = "\r\n\tThis string has special characters at the start.\r\n";
    String expected = "This string has special characters at the start.";
    assertEquals(expected, sanitizeCRLF(input));
  }

  @Test
  void testSanitizeCRLF_NoCRLFCharacters() {
    String input = "Regular string without CRLF.";
    assertEquals("Regular string without CRLF.", sanitizeCRLF(input));
  }
}

