package eu.europeana.normalization.normalizers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import eu.europeana.normalization.languages.LanguageMatch;
import eu.europeana.normalization.languages.LanguageMatch.Type;
import eu.europeana.normalization.languages.LanguageMatcher;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class DcLanguageNormalizerTest {

  @Test
  void testNormalizeValue() {

    // String constants.
    final String matchString1 = "match1";
    final String matchString2 = "match2";

    // Mock the macher.
    final LanguageMatcher matcher = mock(LanguageMatcher.class);
    final LanguageMatch match1 = new LanguageMatch("input1", matchString1, Type.CODE_MATCH);
    final LanguageMatch match2 = new LanguageMatch("input2", matchString1, Type.CODE_MATCH);
    final LanguageMatch match3 = new LanguageMatch("input3", matchString2, Type.CODE_MATCH);
    doReturn(Arrays.asList(match1, match2, match3)).when(matcher).match(anyString());

    // Create normalizer
    final float minimumConfidence = 0.5f;
    final DcLanguageNormalizer normalizer =
        spy(new DcLanguageNormalizer(matcher, minimumConfidence));

    // Check with valid confidence
    final float validConfidence = minimumConfidence + 0.1F;
    doReturn(validConfidence).when(normalizer).determineConfidence(any());
    final List<NormalizedValueWithConfidence> result = normalizer.normalizeValue("input0");
    assertEquals(2, result.size());
    final Set<String> values = result.stream()
        .map(NormalizedValueWithConfidence::getNormalizedValue).collect(Collectors.toSet());
    assertTrue(values.contains(matchString1));
    assertTrue(values.contains(matchString2));
    final Set<Float> confidences = result.stream().map(NormalizedValueWithConfidence::getConfidence)
        .collect(Collectors.toSet());
    assertEquals(1, confidences.size());
    assertEquals(validConfidence, confidences.toArray()[0]);

    // Check with no confidence or a low confidence
    doReturn(null).when(normalizer).determineConfidence(any());
    assertTrue(normalizer.normalizeValue("input1").isEmpty());
    doReturn(minimumConfidence - 0.1F).when(normalizer).determineConfidence(any());
    assertTrue(normalizer.normalizeValue("input2").isEmpty());
  }

  @Test
  void testDetermineConfidence() {

    // Create different matches
    final LanguageMatch exactMatch = new LanguageMatch("code1", "code1", Type.CODE_MATCH);
    final LanguageMatch codeMatch = new LanguageMatch("input2", "code2", Type.CODE_MATCH);
    final LanguageMatch labelMatch = new LanguageMatch("input3", "code3", Type.LABEL_MATCH);
    final LanguageMatch noMatch = new LanguageMatch("input4", null, Type.NO_MATCH);

    // Create normalizer
    final DcLanguageNormalizer normalizer = new DcLanguageNormalizer(null, 1.0f);

    // Test single codes or labels
    assertEquals(Float.valueOf(DcLanguageNormalizer.CONFIDENCE_SINGLE_CODE_EQUALS),
        normalizer.determineConfidence(Collections.singletonList(exactMatch)));
    assertEquals(Float.valueOf(DcLanguageNormalizer.CONFIDENCE_SINGLE_CODE_KNOWN),
        normalizer.determineConfidence(Collections.singletonList(codeMatch)));
    assertEquals(Float.valueOf(DcLanguageNormalizer.CONFIDENCE_LABELS_OR_CODES_MATCHES),
        normalizer.determineConfidence(Collections.singletonList(labelMatch)));

    // Test multiple codes and labels
    assertEquals(Float.valueOf(DcLanguageNormalizer.CONFIDENCE_LABELS_OR_CODES_MATCHES),
        normalizer.determineConfidence(Arrays.asList(labelMatch, labelMatch)));
    assertEquals(Float.valueOf(DcLanguageNormalizer.CONFIDENCE_LABELS_OR_CODES_MATCHES),
        normalizer.determineConfidence(Arrays.asList(exactMatch, codeMatch)));
    assertEquals(Float.valueOf(DcLanguageNormalizer.CONFIDENCE_LABELS_AND_CODES_MATCHES),
        normalizer.determineConfidence(Arrays.asList(exactMatch, labelMatch)));

    // Test no match
    assertNull(normalizer.determineConfidence(Collections.singletonList(noMatch)));
    assertNull(normalizer.determineConfidence(Arrays.asList(exactMatch, noMatch)));
    assertNull(normalizer.determineConfidence(Arrays.asList(noMatch, exactMatch, codeMatch)));
    assertNull(normalizer.determineConfidence(Arrays.asList(exactMatch, noMatch, labelMatch)));

    // Test empty match
    assertNull(normalizer.determineConfidence(Collections.emptyList()));
  }
}
