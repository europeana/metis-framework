package eu.europeana.metis.dereference.service.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.Test;
import org.mockito.Mockito;
import eu.europeana.metis.dereference.Vocabulary;

public class VocabularyCandidatesTest {

  @Test
  public void findVocabulariesForUrlTest() throws URISyntaxException {

    // The host name. Note: this is the only one we need to try as no other host names are
    // returned by the function.
    final String hostName = "dummy.com";

    // Create vocabularies that should match (with and without rules)
    final String correctPath = "complex/path1/";
    final String correctVocUri = "http://" + hostName + "/" + correctPath;
    final String correctUriRule = "voc1";
    final String wrongUriRule = "voc2";
    final Vocabulary correctVocabulary1 = new Vocabulary();
    correctVocabulary1.setId("c1");
    correctVocabulary1.setUri(correctVocUri);
    correctVocabulary1.setRules(new HashSet<>(Arrays.asList(correctUriRule, wrongUriRule)));
    final Vocabulary correctVocabulary2 = new Vocabulary();
    correctVocabulary2.setId("c2");
    correctVocabulary2.setUri(correctVocUri);

    // Create vocabularies with the wrong path but the right rules
    final String wrongPath = "complex/path2/";
    final String wrongVocUri = "http://" + hostName + "/" + wrongPath;
    final Vocabulary wrongVocabulary1 = new Vocabulary();
    wrongVocabulary1.setId("w1");
    wrongVocabulary1.setUri(wrongVocUri);

    // Create vocabulary with the right path but the wrong rules
    final Vocabulary wrongVocabulary2 = new Vocabulary();
    wrongVocabulary2.setId("w2");
    wrongVocabulary2.setUri(correctVocUri);
    wrongVocabulary2.setRules(new HashSet<>(Arrays.asList("voc2")));

    // Create mock of vocabulary provider
    final Function<String, List<Vocabulary>> vocabularyProvider = new VocabularyProvider(
        Arrays.asList(correctVocabulary1, correctVocabulary2, wrongVocabulary1, wrongVocabulary2));
    final Function<String, List<Vocabulary>> vocabularyProviderMock =
        Mockito.spy(vocabularyProvider);

    // Match and obtain list of matching vocabularies
    final String resourceId = correctVocUri + correctUriRule + "/123456";
    final List<Vocabulary> result = VocabularyCandidates
        .findVocabulariesForUrl(resourceId, vocabularyProviderMock).getCandidates();

    // Verify that the provider was called exactly once with the right value.
    Mockito.verify(vocabularyProviderMock).apply(Mockito.anyString());
    Mockito.verify(vocabularyProviderMock).apply(hostName);

    // Verify that the right vocabularies are in the result.
    final Set<String> resultingIds =
        result.stream().map(Vocabulary::getId).collect(Collectors.toSet());
    final Set<String> expectedIds =
        new HashSet<>(Arrays.asList(correctVocabulary1.getId(), correctVocabulary2.getId()));
    assertEquals(expectedIds.size(), result.size());
    assertEquals(expectedIds, resultingIds);

  }

  @Test
  public void findVocabulariesForUrlWithoutVocabularies() throws URISyntaxException {

    // Create mock of vocabulary provider (cannot use lambda as class would be final)
    final Function<String, List<Vocabulary>> vocabularyProvider = new VocabularyProvider(null);
    final String resourceId = "http://dummy.com/123456";
    final VocabularyCandidates result =
        VocabularyCandidates.findVocabulariesForUrl(resourceId, vocabularyProvider);

    // Verify that the provider was called exactly once with the right value.
    assertTrue(result.isEmpty());
  }

  @Test
  public void processTypeRulesTest() {

    // Create vocabularies that should match (with and without rules)
    final String correctTypeIdentifier = "#myCorrectType";
    final String wrongTypeIdentifier = "#myWrongType";
    final Vocabulary vocWithRightType = new Vocabulary();
    vocWithRightType.setId("v1");
    vocWithRightType
        .setTypeRules(new HashSet<>(Arrays.asList(correctTypeIdentifier, wrongTypeIdentifier)));
    final Vocabulary vocWithNoType = new Vocabulary();
    vocWithNoType.setId("v2");

    // Create vocabulary that should not match
    final Vocabulary vocWithWrongType = new Vocabulary();
    vocWithWrongType.setId("v3");
    vocWithWrongType.setTypeRules(new HashSet<>(Arrays.asList(wrongTypeIdentifier)));

    // Create the input
    final String incomingDataXml = "abcd" + correctTypeIdentifier + "wxyz";
    final String resourceId = "http://dummy.com/123456";

    // Match with vocabulary with correct type
    final VocabularyCandidates candidates1 =
        new VocabularyCandidates(resourceId, Arrays.asList(vocWithRightType, vocWithWrongType));
    final Vocabulary result1 = candidates1.findVocabularyForType(incomingDataXml);
    assertEquals(vocWithRightType.getId(), result1.getId());
    assertNull(candidates1.findVocabularyWithoutTypeRules());

    // Match with vocabulary without type
    final VocabularyCandidates candidates2 =
        new VocabularyCandidates(resourceId, Arrays.asList(vocWithWrongType, vocWithNoType));
    final Vocabulary result2 = candidates2.findVocabularyForType(incomingDataXml);
    assertEquals(vocWithNoType.getId(), result2.getId());
    assertEquals(vocWithNoType.getId(), candidates2.findVocabularyWithoutTypeRules().getId());

    // Match without matching vocabularies
    final VocabularyCandidates candidates3 =
        new VocabularyCandidates(resourceId, Arrays.asList(vocWithWrongType));
    final Vocabulary result3 = candidates3.findVocabularyForType(incomingDataXml);
    assertNull(result3);
    assertNull(candidates3.findVocabularyWithoutTypeRules());
  }

  @Test
  public void testIsEmpty() {
    final Vocabulary vocabulary = new Vocabulary();
    vocabulary.setId("vId");
    assertTrue(new VocabularyCandidates("r1", Collections.emptyList()).isEmpty());
    assertFalse(new VocabularyCandidates("r2", Arrays.asList(vocabulary)).isEmpty());
  }

  @Test
  public void testGetSuffixes() {

    // Creat vocabularies with suffixes
    final String suffixA = "sa";
    final String suffixB = "sb";
    final Vocabulary vocabulary1 = new Vocabulary();
    vocabulary1.setId("v1");
    vocabulary1.setSuffix(suffixA);
    final Vocabulary vocabulary2 = new Vocabulary();
    vocabulary2.setId("v2");
    vocabulary2.setSuffix(suffixB);
    final Vocabulary vocabulary3 = new Vocabulary();
    vocabulary3.setId("v3");
    vocabulary3.setSuffix(suffixA);

    // Try with all vocabularies
    final Set<String> suffixes1 =
        new VocabularyCandidates("r1", Arrays.asList(vocabulary1, vocabulary2, vocabulary3))
            .getCandidateSuffixes();
    assertEquals(2, suffixes1.size());
    assertTrue(suffixes1.contains(suffixA));
    assertTrue(suffixes1.contains(suffixB));

    // Try with one vocabulary
    final Set<String> suffixes2 =
        new VocabularyCandidates("r2", Arrays.asList(vocabulary1)).getCandidateSuffixes();
    assertEquals(1, suffixes2.size());
    assertTrue(suffixes2.contains(suffixA));

    // Try with no vocabularies
    final Set<String> suffixes3 =
        new VocabularyCandidates("r3", Collections.emptyList()).getCandidateSuffixes();
    assertTrue(suffixes3.isEmpty());
  }

  private static class VocabularyProvider implements Function<String, List<Vocabulary>> {

    private final List<Vocabulary> result;

    public VocabularyProvider(List<Vocabulary> result) {
      this.result = result;
    }

    @Override
    public List<Vocabulary> apply(String t) {
      return result;
    }
  }
}
