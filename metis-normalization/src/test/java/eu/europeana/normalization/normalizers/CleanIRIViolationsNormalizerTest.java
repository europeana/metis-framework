package eu.europeana.normalization.normalizers;

import org.apache.jena.iri.Violation;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;


public class CleanIRIViolationsNormalizerTest {

  private final CleanIRIViolationsNormalizer normalizer = new CleanIRIViolationsNormalizer();
  private static final String WHITESPACE_VIOLATION = "http://example.com/query?q=random word";
  private static final String UNWISE_CHARACTER_VIOLATION = "http://example.com/query?q=random<word";
  private static final String ILLEGAL_CHARACTER_VIOLATION = "ht$tp://example.com/query?q=random_word";
  private static final String NOT_XML_SCHEMA_WHITESPACE_VIOLATION = "http://example.com/query?q=random\u0009word";
  private static final String DOUBLE_WHITESPACE_VIOLATION = "http://example.com/query?q=random  word";
  private static final String CONTROL_CHARACTER_VIOLATION = "http://example.com/query?q=random\u0085word";
  private static final String COMPATIBILITY_CHARACTER_VIOLATION = "http://example.com/query?q=randomໜword";


  @Test
  void whitespaceViolationCleanupTest() {
    List<NormalizedValueWithConfidence> result = normalizer.normalizeValue(WHITESPACE_VIOLATION);
    Iterator<Violation> violations = normalizer.getViolations();
    assertTrue(StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(violations, Spliterator.SORTED), false)
        .anyMatch(violation -> violation.codeName().equals("WHITESPACE")));
    assertEquals("http://example.com/query?q=random%20word", result.get(0).getNormalizedValue());
    assertEquals(1, result.get(0).getConfidence());
  }

  @Test
  void unwiseCharacterViolationCleanupTest() {
    List<NormalizedValueWithConfidence> result = normalizer
        .normalizeValue(UNWISE_CHARACTER_VIOLATION);
    Iterator<Violation> violations = normalizer.getViolations();
    assertTrue(StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(violations, Spliterator.SIZED), false)
        .anyMatch(violation -> violation.codeName().equals("UNWISE_CHARACTER")));
    assertEquals("http://example.com/query?q=random%3Cword", result.get(0).getNormalizedValue());
    assertEquals(1, result.get(0).getConfidence());
  }

  @Test
  void illegalCharacterViolationReturnEmptyListTest() {
    List<NormalizedValueWithConfidence> result = normalizer
        .normalizeValue(ILLEGAL_CHARACTER_VIOLATION);
    assertEquals(Collections.emptyList(), result);
  }

  @Test
  void notXMLSchemaWhitespaceViolationCleanupTest() {
    List<NormalizedValueWithConfidence> result = normalizer
        .normalizeValue(NOT_XML_SCHEMA_WHITESPACE_VIOLATION);
    Iterator<Violation> violations = normalizer.getViolations();
    assertTrue(StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(violations, Spliterator.SIZED), false)
        .anyMatch(violation -> violation.codeName().equals("NOT_XML_SCHEMA_WHITESPACE")));
    assertEquals("http://example.com/query?q=random%09word", result.get(0).getNormalizedValue());
    assertEquals(1, result.get(0).getConfidence());
  }

  @Test
  void doubleWhitespaceViolationCleanupTest() {
    List<NormalizedValueWithConfidence> result = normalizer
        .normalizeValue(DOUBLE_WHITESPACE_VIOLATION);
    Iterator<Violation> violations = normalizer.getViolations();
    assertTrue(StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(violations, Spliterator.SIZED), false)
        .anyMatch(violation -> violation.codeName().equals("DOUBLE_WHITESPACE")));
    assertEquals("http://example.com/query?q=random%20%20word", result.get(0).getNormalizedValue());
    assertEquals(1, result.get(0).getConfidence());
  }

  @Test
  void controlCharacterViolationCleanupTest() {
    List<NormalizedValueWithConfidence> result = normalizer
        .normalizeValue(CONTROL_CHARACTER_VIOLATION);
    Iterator<Violation> violations = normalizer.getViolations();
    assertTrue(StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(violations, Spliterator.SIZED), false)
        .anyMatch(violation -> violation.codeName().equals("CONTROL_CHARACTER")));
    assertEquals("http://example.com/query?q=random%C2%85word", result.get(0).getNormalizedValue());
    assertEquals(1, result.get(0).getConfidence());
  }

  @Test
  void compatibilityCharacterViolationCleanupTest() {
    List<NormalizedValueWithConfidence> result = normalizer
        .normalizeValue(COMPATIBILITY_CHARACTER_VIOLATION);
    Iterator<Violation> violations = normalizer.getViolations();
    assertTrue(StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(violations, Spliterator.SIZED), false)
        .anyMatch(violation -> violation.codeName().equals("COMPATIBILITY_CHARACTER")));
    assertEquals("http://example.com/query?q=random%E0%BB%9Cword",
        result.get(0).getNormalizedValue());
    assertEquals(1, result.get(0).getConfidence());
  }
}
