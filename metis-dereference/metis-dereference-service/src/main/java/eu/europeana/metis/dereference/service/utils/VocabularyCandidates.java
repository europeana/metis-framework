package eu.europeana.metis.dereference.service.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europeana.metis.dereference.Vocabulary;

/**
 * <p>
 * This class provides functionality related to finding the appropriate vocabulary for a given
 * dereference request. It represents the list of vocabulary candidates for a given resource before
 * the resource has been resolved, and provides further functionality for when that happens.
 * </p>
 * 
 * @author jochen
 *
 */
public final class VocabularyCandidates {

  private static final Logger LOGGER = LoggerFactory.getLogger(VocabularyCandidates.class);

  private final String resourceId;
  private final List<Vocabulary> candidates;

  /**
   * Constructor.
   * 
   * @param resourceId The resource ID for this candidate list.
   * @param candidates The candidates to be in this list.
   */
  VocabularyCandidates(String resourceId, List<Vocabulary> candidates) {
    this.resourceId = resourceId;
    this.candidates = new ArrayList<>(candidates);
  }

  /**
   * <p>
   * Return the vocabularies that match the given resource identifier. This method looks at the
   * vocabulary's URL as well as the vocabulary rules (if present), but not at the type rules.
   * </p>
   * <p>
   * A vocabulary matches the given resource Id if and only if:
   * <ol>
   * <li>The resource ID starts with the vocabulary URI, in case the vocabulary doesn't have rules,
   * or</li>
   * <li>The resource ID starts with the vocabulary URI immediately followed by one of the rules, in
   * case the vocabulary does have rules.</li>
   * </ol>
   * </p>
   * <p>
   * Hence, all vocabulary objects that are returned by this method represent the same vocabulary. A
   * further selection on the type mappings (or in case of duplicates) can be achieved by the method
   * {@link #findVocabularyForType(String)}, which will require the resource to have been resolved.
   * </p>
   * 
   * @param resourceId The resource identifier (URI) that we receive and that we are to match.
   * @param searchInPersistence A function that searches the persistence for vocabularies of which
   *        the URL contains a given string. This function will be called once for the host name of
   *        the resource to allow persistence to greatly narrow down the list of candidate
   *        vocabularies before they are loaded in memory and processed.
   * @return The list of vocabularies that match the given URI. May be empty list if none are found.
   *         This method does not return null.
   * @throws URISyntaxException In case the resource ID could not be read as URI.
   */
  public static VocabularyCandidates findVocabulariesForUrl(String resourceId,
      Function<String, List<Vocabulary>> searchInPersistence) throws URISyntaxException {

    // Initial search on the host name (already filtering the great majority of vocabularies).
    final String searchString = new URI(resourceId).getHost();
    final List<Vocabulary> searchedVocabularies = searchInPersistence.apply(searchString);

    // Narrow it down further: precisely match the URI and URI rules.
    final List<Vocabulary> candidates;
    if (searchedVocabularies == null) {
      candidates = Collections.emptyList();
    } else {
      candidates = searchedVocabularies.stream()
          .filter(vocabulary -> vocabularyMatchesUri(resourceId, vocabulary))
          .collect(Collectors.toList());
    }

    // Log and done.
    if (candidates.isEmpty()) {
      LOGGER.info("No vocabularies found for uri {}", resourceId);
    }
    return new VocabularyCandidates(resourceId, candidates);
  }

  private static boolean vocabularyMatchesUri(String resourceId, Vocabulary vocabulary) {

    // If there are no URI rules, the resource ID must start with the vocabulary URI.
    if (vocabulary.getRules() == null || vocabulary.getRules().isEmpty()) {
      return resourceId.startsWith(vocabulary.getUri());
    }

    // If there are rules, the resource ID must start with the vocabulary URI followed immediately
    // by the given rule.
    return vocabulary.getRules().stream()
        .anyMatch(rule -> resourceId.startsWith(vocabulary.getUri() + rule));
  }

  /**
   * <p>
   * Return a vocabulary <b>without type rules</b> that matches the given resource identifier. This
   * method looks at the vocabulary's URL as well as the vocabulary rules (if present). The same
   * matching rules apply as detailed in {@link #findVocabulariesForUrl(String, Function)}. If
   * multiple candidate vocabularies would match, this method gives no guarantee about which one is
   * returned.
   * </p>
   * <p>
   * This method is useful for determining the vocabulary of a given resource without resolving the
   * resource. If this method returns null, the resource will need to be resolved in order to obtain
   * the resource's vocabulary (by calling {@link #findVocabularyForType(String)} after resolving
   * the resource).
   * </p>
   * 
   * @return The corresponding vocabulary, or null if no such vocabulary is found (or if there are
   *         no candidates).
   */
  public Vocabulary findVocabularyWithoutTypeRules() {
    return candidates.stream().filter(vocabulary -> vocabularyMatchesType(vocabulary, "")).findAny()
        .orElse(null);
  }

  /**
   * <p>
   * Returns any vocabulary that match the given incoming data. This method looks at the
   * vocabulary's type rules (if present), but not at the URL and URL rules. If multiple candidate
   * vocabularies would match, this method gives no guarantee about which one is returned.
   * </p>
   * <p>
   * A vocabulary matches the given incoming data XML as follows:
   * <ol>
   * <li>If the vocabulary doesn't have type rules, it always matches the data XML ,</li>
   * <li>If the vocabulary does have type rules, it matches the data XML if and only if the XML (as
   * a string) contains one of the type rules as a substring. <b>Please note:</b> no further
   * requirements are imposed on the structure or context of the type rule occurrence in the data
   * XML.</li>
   * </ol>
   * </p>
   * 
   * @param incomingDataXml The actual retrieved entity of which to map the type
   * @return The corresponding vocabulary, or null if no such vocabulary is found (or if there are
   *         no candidates).
   */
  public Vocabulary findVocabularyForType(String incomingDataXml) {
    final Vocabulary result =
        candidates.stream().filter(vocabulary -> vocabularyMatchesType(vocabulary, incomingDataXml))
            .findAny().orElse(null);
    if (result == null) {
      LOGGER.info("No vocabularies found for resource {}", resourceId);
    }
    return result;
  }

  private static boolean vocabularyMatchesType(Vocabulary vocabulary, String incomingDataXml) {
    final Set<String> typeRules = vocabulary.getTypeRules();
    return typeRules == null || typeRules.isEmpty()
        || typeRules.stream().anyMatch(typeRule -> StringUtils.contains(incomingDataXml, typeRule));
  }

  /**
   * Collects the suffixes of all the candidates. This method is useful for resolving the resource.
   * Note that all the vocabulary candidates must at this point represent the same vocabulary, so
   * the number of suffixes should be very limited (1, if all vocabularies are configured the same
   * way).
   * 
   * @return The collection of suffixes. Is not null.
   */
  public Set<String> getCandidateSuffixes() {
    return candidates.stream()
        .map(vocabulary -> vocabulary.getSuffix() == null ? "" : vocabulary.getSuffix())
        .collect(Collectors.toSet());
  }

  /**
   * Returns whether there are any candidates that matched the provided resource.
   * 
   * @return Whether there are any candidates.
   */
  public boolean isEmpty() {
    return candidates.isEmpty();
  }

  /**
   * Provides an immutable (read-only) view of the vocabularies in this collection.
   * 
   * @return The candidates.
   */
  List<Vocabulary> getCandidates() {
    return Collections.unmodifiableList(candidates);
  }
}
