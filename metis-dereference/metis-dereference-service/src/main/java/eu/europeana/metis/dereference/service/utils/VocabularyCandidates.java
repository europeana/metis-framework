package eu.europeana.metis.dereference.service.utils;

import static eu.europeana.metis.utils.CommonStringValues.CRLF_PATTERN;

import eu.europeana.metis.dereference.Vocabulary;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * This class provides functionality related to finding the appropriate vocabulary for a given dereference request. It represents
 * the list of vocabulary candidates for a given resource before the resource has been resolved, and provides further
 * functionality for when that happens.
 * </p>
 * In practice, there should be at most one candidate vocabulary (and if there are more, it will be noted in the logs). But this
 * class is fully functional for multiple vocabulary candidates.
 *
 * @author jochen
 */
public final class VocabularyCandidates {

  private static final Logger LOGGER = LoggerFactory.getLogger(VocabularyCandidates.class);

  private final List<Vocabulary> vocabularies;

  /**
   * Constructor.
   *
   * @param vocabularies The candidates to be in this list.
   */
  VocabularyCandidates(List<Vocabulary> vocabularies) {
    this.vocabularies = new ArrayList<>(vocabularies);
  }

  /**
   * Return the vocabularies that match the given resource identifier. A vocabulary matches the given resource Id if and only if
   * the resource ID starts with one of the vocabulary URIs.
   *
   * @param resourceId The resource identifier (URI) that we receive and that we are to match.
   * @param searchInPersistence A function that searches the persistence for vocabularies of which the URL contains a given
   * string. This function will be called once for the host name of the resource to allow persistence to greatly narrow down the
   * list of candidate vocabularies before they are loaded in memory and processed.
   * @return The list of vocabularies that match the given URI. May be empty list if none are found. This method does not return
   * null.
   * @throws URISyntaxException In case the resource ID could not be read as URI.
   */
  public static VocabularyCandidates findVocabulariesForUrl(String resourceId,
      Function<String, List<Vocabulary>> searchInPersistence) throws URISyntaxException {

    // Initial search on the host name (already filtering the great majority of vocabularies).
    final String searchString = new URI(resourceId).getHost();
    final List<Vocabulary> searchedVocabularies = searchInPersistence.apply(searchString);

    // Narrow it down further: precisely match the URI.
    final List<Vocabulary> candidates;
    if (searchedVocabularies == null) {
      candidates = Collections.emptyList();
    } else {
      candidates = searchedVocabularies.stream()
                                       .filter(vocabulary -> vocabularyMatchesUri(resourceId, vocabulary))
                                       .toList();
    }

    // Log and done.
    if (candidates.isEmpty() && (LOGGER.isInfoEnabled())) {
      LOGGER.info("No vocabularies found for uri {}", CRLF_PATTERN.matcher(resourceId).replaceAll(""));
    }
    if (candidates.size() > 1 && LOGGER.isWarnEnabled()) {
      LOGGER.warn("Multiple vocabularies found for uri {}: {}", CRLF_PATTERN.matcher(resourceId).replaceAll(""),
          candidates.stream().map(Vocabulary::getName).collect(Collectors.joining(", ")));
    }
    return new VocabularyCandidates(candidates);
  }

  private static boolean vocabularyMatchesUri(String resourceId, Vocabulary vocabulary) {
    return vocabulary.getUris().stream().anyMatch(resourceId::startsWith);
  }

  /**
   * Collects the suffixes of all the vocabulary candidates. This method is useful for resolving the resource. Note that all the
   * vocabulary candidates must at this point represent the same vocabulary, so the number of suffixes should be very limited (1,
   * if all vocabularies are configured the same way).
   *
   * @return The collection of suffixes. Is not null.
   */
  public Set<String> getVocabulariesSuffixes() {
    return vocabularies.stream()
                       .map(vocabulary -> vocabulary.getSuffix() == null ? "" : vocabulary.getSuffix())
                       .collect(Collectors.toSet());
  }

  /**
   * Returns whether there are any candidates that matched the provided resource.
   *
   * @return Whether there are any candidates.
   */
  public boolean isEmpty() {
    return vocabularies.isEmpty();
  }

  /**
   * Provides an immutable (read-only) view of the vocabularies in this collection.
   *
   * @return The candidates.
   */
  public List<Vocabulary> getVocabularies() {
    return Collections.unmodifiableList(vocabularies);
  }
}
