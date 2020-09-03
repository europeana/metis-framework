package eu.europeana.metis.dereference.service.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.metis.dereference.Vocabulary;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class VocabularyCandidatesTest {

  @Test
  void findVocabulariesForUrlTest() throws URISyntaxException {

    // The host name. Note: this is the only one we need to try as no other host names are
    // returned by the function.
    final String hostName = "dummy.com";

    // Create vocabularies that should match
    final String baseUri = "http://" + hostName + "/complex/path1/";
    final String correctUri = baseUri + "voc1";
    final String wrongUri = baseUri + "voc2";
    final Vocabulary correctVocabulary1 = new Vocabulary();
    correctVocabulary1.setId(new ObjectId());
    correctVocabulary1.setUris(new HashSet<>(Arrays.asList(correctUri, wrongUri)));
    final Vocabulary correctVocabulary2 = new Vocabulary();
    correctVocabulary2.setId(new ObjectId());
    correctVocabulary2.setUris(Collections.singleton(correctUri));

    // Create vocabulary with the wrong path
    final Vocabulary wrongVocabulary = new Vocabulary();
    wrongVocabulary.setId(new ObjectId());
    wrongVocabulary.setUris(Collections.singleton(wrongUri));

    // Create mock of vocabulary provider
    final Function<String, List<Vocabulary>> vocabularyProvider = new VocabularyProvider(
        Arrays.asList(correctVocabulary1, correctVocabulary2, wrongVocabulary));
    final Function<String, List<Vocabulary>> vocabularyProviderMock =
        Mockito.spy(vocabularyProvider);

    // Match and obtain list of matching vocabularies
    final String resourceId = correctUri + "/123456";
    final List<Vocabulary> result = VocabularyCandidates
        .findVocabulariesForUrl(resourceId, vocabularyProviderMock).getCandidates();

    // Verify that the provider was called exactly once with the right value.
    Mockito.verify(vocabularyProviderMock).apply(Mockito.anyString());
    Mockito.verify(vocabularyProviderMock).apply(hostName);

    // Verify that the right vocabularies are in the result.
    final Set<ObjectId> resultingIds =
        result.stream().map(Vocabulary::getId).collect(Collectors.toSet());
    final Set<ObjectId> expectedIds =
        new HashSet<>(Arrays.asList(correctVocabulary1.getId(), correctVocabulary2.getId()));
    assertEquals(expectedIds.size(), result.size());
    assertEquals(expectedIds, resultingIds);

  }

  @Test
  void findVocabulariesForUrlWithoutVocabularies() throws URISyntaxException {

    // Create mock of vocabulary provider (cannot use lambda as class would be final)
    final Function<String, List<Vocabulary>> vocabularyProvider = new VocabularyProvider(null);
    final String resourceId = "http://dummy.com/123456";
    final VocabularyCandidates result =
        VocabularyCandidates.findVocabulariesForUrl(resourceId, vocabularyProvider);

    // Verify that the provider was called exactly once with the right value.
    assertTrue(result.isEmpty());
  }

  @Test
  void testIsEmpty() {
    final Vocabulary vocabulary = new Vocabulary();
    final ObjectId vId = new ObjectId();
    vocabulary.setId(vId);

    assertTrue(new VocabularyCandidates(Collections.emptyList()).isEmpty());
    assertFalse(new VocabularyCandidates(Collections.singletonList(vocabulary)).isEmpty());
  }

  @Test
  void testGetSuffixes() {

    // Creat vocabularies with suffixes
    final String suffixA = "sa";
    final String suffixB = "sb";
    final Vocabulary vocabulary1 = new Vocabulary();
    vocabulary1.setId(new ObjectId());
    vocabulary1.setSuffix(suffixA);
    final Vocabulary vocabulary2 = new Vocabulary();
    vocabulary2.setId(new ObjectId());
    vocabulary2.setSuffix(suffixB);
    final Vocabulary vocabulary3 = new Vocabulary();
    vocabulary3.setId(new ObjectId());
    vocabulary3.setSuffix(suffixA);

    // Try with all vocabularies
    final Set<String> suffixes1 = new VocabularyCandidates(
            Arrays.asList(vocabulary1, vocabulary2, vocabulary3)).getCandidateSuffixes();
    assertEquals(2, suffixes1.size());
    assertTrue(suffixes1.contains(suffixA));
    assertTrue(suffixes1.contains(suffixB));

    // Try with one vocabulary
    final Set<String> suffixes2 =
        new VocabularyCandidates(Collections.singletonList(vocabulary1)).getCandidateSuffixes();
    assertEquals(1, suffixes2.size());
    assertTrue(suffixes2.contains(suffixA));

    // Try with no vocabularies
    final Set<String> suffixes3 =
        new VocabularyCandidates(Collections.emptyList()).getCandidateSuffixes();
    assertTrue(suffixes3.isEmpty());
  }

  private static class VocabularyProvider implements Function<String, List<Vocabulary>> {

    private final List<Vocabulary> result;

    VocabularyProvider(List<Vocabulary> result) {
      this.result = result;
    }

    @Override
    public List<Vocabulary> apply(String t) {
      return result;
    }
  }
}
