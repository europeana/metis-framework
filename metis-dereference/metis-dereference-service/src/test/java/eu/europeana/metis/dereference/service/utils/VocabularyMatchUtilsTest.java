package eu.europeana.metis.dereference.service.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.Test;
import org.mockito.Mockito;
import eu.europeana.metis.dereference.Vocabulary;

public class VocabularyMatchUtilsTest {

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
    final List<Vocabulary> result =
        VocabularyMatchUtils.findVocabulariesForUrl(resourceId, vocabularyProviderMock);

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
    final List<Vocabulary> result =
        VocabularyMatchUtils.findVocabulariesForUrl(resourceId, vocabularyProvider);

    // Verify that the provider was called exactly once with the right value.
    assertTrue(result.isEmpty());
  }

  @Test
  public void findVocabularyForTypeTest() {

    // Create vocabularies that should match (with and without rules)
    final String correctTypeIdentifier = "#myCorrectType";
    final String wrongTypeIdentifier = "#myWrongType";
    final Vocabulary correctVocabulary1 = new Vocabulary();
    correctVocabulary1.setId("c1");
    correctVocabulary1
        .setTypeRules(new HashSet<>(Arrays.asList(correctTypeIdentifier, wrongTypeIdentifier)));
    final Vocabulary correctVocabulary2 = new Vocabulary();
    correctVocabulary2.setId("c2");

    // Create vocabulary that should not match
    final Vocabulary wrongVocabulary1 = new Vocabulary();
    wrongVocabulary1.setId("w1");
    wrongVocabulary1.setTypeRules(new HashSet<>(Arrays.asList(wrongTypeIdentifier)));

    // Create the input
    final String incomingDataXml = "abcd" + correctTypeIdentifier + "wxyz";
    final String resourceId = "http://dummy.com/123456";

    // Match with first correct vocabulary
    final Vocabulary result1 = VocabularyMatchUtils.findVocabularyForType(
        Arrays.asList(correctVocabulary1, wrongVocabulary1), incomingDataXml, resourceId);
    assertEquals(correctVocabulary1.getId(), result1.getId());

    // Match with second correct vocabulary
    final Vocabulary result2 = VocabularyMatchUtils.findVocabularyForType(
        Arrays.asList(wrongVocabulary1, correctVocabulary2), incomingDataXml, resourceId);
    assertEquals(correctVocabulary2.getId(), result2.getId());

    // Match without matching vocabularies
    final Vocabulary result3 = VocabularyMatchUtils
        .findVocabularyForType(Arrays.asList(wrongVocabulary1), incomingDataXml, resourceId);
    assertNull(result3);
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
