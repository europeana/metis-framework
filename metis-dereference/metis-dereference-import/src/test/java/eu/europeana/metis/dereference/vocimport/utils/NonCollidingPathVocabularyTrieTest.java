package eu.europeana.metis.dereference.vocimport.utils;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import eu.europeana.metis.dereference.vocimport.exception.VocabularyImportException;
import eu.europeana.metis.dereference.vocimport.model.Vocabulary;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

public class NonCollidingPathVocabularyTrieTest {

  private static Vocabulary createVocabulary(String... paths) {
    return Vocabulary.builder().setReadableMetadataLocation("").setPaths(Arrays.asList(paths))
            .build();
  }

  private static Vocabulary createVocabularyWithEmptyPath() {
    final Vocabulary vocabulary = spy(Vocabulary.builder().setReadableMetadataLocation("").build());
    when(vocabulary.getPaths()).thenReturn(Collections.singletonList(""));
    return vocabulary;
  }

  @Test
  void testInsert() throws VocabularyImportException {

    // Create trie with three paths
    final NonCollidingPathVocabularyTrie trie = new NonCollidingPathVocabularyTrie();
    trie.insert(createVocabulary("abcdef", "123456"));
    trie.insert(createVocabulary("123abc"));
    trie.insert(createVocabulary("abc123"));

    // Add a vocabulary without any paths
    trie.insert(Vocabulary.builder().setReadableMetadataLocation("").build());

    // Add vocabularies with conflicting paths
    assertThrows(VocabularyImportException.class,
            () -> trie.insert(createVocabularyWithEmptyPath()));
    assertThrows(VocabularyImportException.class, () -> trie.insert(createVocabulary("a")));
    assertThrows(VocabularyImportException.class, () -> trie.insert(createVocabulary("abcde")));
    assertThrows(VocabularyImportException.class, () -> trie.insert(createVocabulary("abcdef")));
    assertThrows(VocabularyImportException.class, () -> trie.insert(createVocabulary("abcdefg")));
    assertThrows(VocabularyImportException.class,
            () -> trie.insert(createVocabulary("abcdefghijk")));
    assertThrows(VocabularyImportException.class, () -> trie.insert(createVocabulary("12345")));
    assertThrows(VocabularyImportException.class, () -> trie.insert(createVocabulary("1234567")));

    // Add vocabularies with conflicting paths differing by case
    assertThrows(VocabularyImportException.class, () -> trie.insert(createVocabulary("A")));
    assertThrows(VocabularyImportException.class, () -> trie.insert(createVocabulary("AbCdE")));
    assertThrows(VocabularyImportException.class, () -> trie.insert(createVocabulary("aBcdef")));
    assertThrows(VocabularyImportException.class, () -> trie.insert(createVocabulary("abcDefg")));
    assertThrows(VocabularyImportException.class,
            () -> trie.insert(createVocabulary("abcdeFghijk")));
  }

  @Test
  void testInsertIntoTrieWithEmptyPath() throws VocabularyImportException {
    final NonCollidingPathVocabularyTrie trie = new NonCollidingPathVocabularyTrie();
    trie.insert(createVocabularyWithEmptyPath());
    assertThrows(VocabularyImportException.class, () -> trie.insert(createVocabulary("a")));
    assertThrows(VocabularyImportException.class, () -> trie.insert(createVocabulary("abcde")));
    assertThrows(VocabularyImportException.class, () -> trie.insert(createVocabulary("abcdef")));
    assertThrows(VocabularyImportException.class, () -> trie.insert(createVocabulary("abcdefg")));
  }
}
