package eu.europeana.metis.dereference.vocimport.utils;

import eu.europeana.metis.dereference.vocimport.exception.VocabularyImportException;
import eu.europeana.metis.dereference.vocimport.model.Vocabulary;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * <p>This class implements a Trie with vocabularies categorized by path (so the same vocabulary
 * can be present multiple times if it has multiple paths). When adding a new vocabulary, it checks
 * whether there is already a path in the trie that collides with one of the paths of the the new
 * vocabulary (either because a new path is a substring of an existing one or vice versa).
 * </p><p>
 * A trie is a directed search tree specialized in  storing lexicographical data in which each node
 * represents a string that is defined by all parents and the node itself. So for instance:
 * <ul>
 * <li> The word 'bat' would be represented by the path "" (the empty root node) -> 'b' -> 'a' ->
 * 't'. </li>
 * <li> The word 'barn' would be represented by "" -> 'b' -> 'a' -> 'r' -> 'n'. </li>
 * </ul>
 * These two words would share the same prefix "" -> 'b' -> 'a', but would diverge in the 'a' node
 * (it has two children, one for 'r' and one for 't'). And each of the final nodes ('t' and 'n'
 * respectively) would be marked, indicating that they form a key (i.e. a word in the trie).
 * </p><p>
 * This would help us to detect overlapping paths when adding a new key.
 * <ul>
 * <li>If we were to try to add the key 'barnacle', we would walk along the path "" -> 'b' -> 'a'
 * -> 'r' -> 'n' and find a marker for a key, indicating that there is a conflict due to a substring
 * already being present. (Note that the other word 'bat' would not cause a conflict since it's key
 * marker is in a diverging branch.)</li>
 * <li>If we were to add the word 'bar', we would walk along the path "" -> 'b' -> 'a' -> 'r' and
 * instead of creating at least one new node, we would discover that all nodes exist and that the
 * last one even has a child ('n'), indicating that there is a conflict due to the new string being
 * a substring of an already added string.</li>
 * </ul>
 * </p><p>
 * If L is the (average/maximum) length of a word, and N is the number of words we intend to add,
 * using this datastructure to detect any conflicts would require O(L * N) time. Note that by
 * contrast, the naive approach (e.g. comparing all strings against each other) would require O(L *
 * N^2), making this a significant improvement (especially for larger N).
 * </p><p>
 * Implementation note: This Trie is implemented in a traditional way. It has a root node
 * representing the empty string. Since we don't allow collisions, we can only have vocabularies in
 * the leaf nodes (that don't have children). The exception is the root node, and then only when the
 * Trie is empty.</p>
 */
public class NonCollidingPathVocabularyTrie {

  private final TrieNode rootNode = new TrieNode();

  /**
   * This method inserts a new vocabulary. It will add it by all its paths.
   *
   * @param vocabulary The vocabulary to insert.
   * @throws VocabularyImportException In case one of the paths collides with one that's already
   * present (or, possibly, with another path of the new vocabulary).
   */
  public void insert(Vocabulary vocabulary) throws VocabularyImportException {
    for (String path : vocabulary.getPaths()) {
      insert(vocabulary, path);
    }
  }

  private void insert(Vocabulary vocabularyToAdd, String pathToAdd)
          throws VocabularyImportException {

    // Find/construct the node corresponding to the path to add.
    TrieNode currentNode = rootNode;
    for (char character : pathToAdd.toCharArray()) {

      // If we encounter a node with a vocabulary, we have a conflict (existing is substring of new).
      if (currentNode.vocabularyAndPath != null) {
        conflict(vocabularyToAdd, pathToAdd, currentNode.vocabularyAndPath);
      }

      // Recursion
      final Character normalizedChar = Character.toLowerCase(character);
      currentNode = currentNode.children.computeIfAbsent(normalizedChar, key -> new TrieNode());
    }

    // Check for conflicts (new is substring of existing), otherwise add.
    final Pair<Vocabulary, String> existingVocabulary = findAnyVocabulary(currentNode);
    if (existingVocabulary != null) {

      // If there is a vocabulary in this subtree, we have a conflict
      conflict(vocabularyToAdd, pathToAdd, existingVocabulary);
    } else {

      // We set the vocabulary and we are done
      currentNode.vocabularyAndPath = new ImmutablePair<>(vocabularyToAdd, pathToAdd);
    }
  }

  private Pair<Vocabulary, String> findAnyVocabulary(TrieNode node) {
    TrieNode currentNode = node;
    while (true) {
      if (currentNode.vocabularyAndPath != null || currentNode.children.isEmpty()) {
        return currentNode.vocabularyAndPath;
      } else {
        currentNode = currentNode.children.values().iterator().next();
      }
    }
  }

  private void conflict(Vocabulary conflictingVocabulary, String conflictingPath,
          Pair<Vocabulary, String> existingVocabularyAndPath) throws VocabularyImportException {
    final String message = String.format("Duplicate path '%s' detected in metadata at [%s]:"
                    + " metadata at [%s] contains path `%s` that collides with it.",
            conflictingPath, conflictingVocabulary.getReadableMetadataLocation(),
            existingVocabularyAndPath.getLeft().getReadableMetadataLocation(),
            existingVocabularyAndPath.getRight());
    throw new VocabularyImportException(message);
  }

  private static class TrieNode {

    final Map<Character, TrieNode> children = new HashMap<>();
    Pair<Vocabulary, String> vocabularyAndPath;
  }
}
