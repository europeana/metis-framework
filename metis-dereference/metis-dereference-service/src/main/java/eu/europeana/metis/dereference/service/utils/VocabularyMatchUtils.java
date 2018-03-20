package eu.europeana.metis.dereference.service.utils;

import java.net.URI;
import java.net.URISyntaxException;
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
 * This class contains some utility functionality related to finding the appropriate vocabulary for
 * a given dereference request.
 * 
 * @author jochen
 *
 */
public final class VocabularyMatchUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(VocabularyMatchUtils.class);

  private VocabularyMatchUtils() {}

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
   * {@link #findVocabularyForType(List, String, String)}, which will require the resource to have
   * been resolved.
   * </p>
   * 
   * @param resourceId The resource identifier (URI) that we receive and that we are to match.
   * @param searchInPersistence A function that searches the persistence for vocabularies of which
   *        the URL contains a given string. This function will be called once for the host name of
   *        the resource to allow persistence to greatly narrow down the list of candidate
   *        vocabularies before they are loaded in memory and processed.
   * @return The list of vocabularies that match the given URI. Or the empty list if none are found.
   *         This method does not return null.
   * @throws URISyntaxException In case the resource ID could not be read as URI.
   */
  public static List<Vocabulary> findVocabulariesForUrl(String resourceId,
      Function<String, List<Vocabulary>> searchInPersistence) throws URISyntaxException {

    // Initial search on the host name (already filtering the great majority of vocabularies).
    final String searchString = new URI(resourceId).getHost();
    final List<Vocabulary> searchedVocabularies = searchInPersistence.apply(searchString);

    // Narrow it down further: precisely match the URI and URI rules.
    final List<Vocabulary> matchingVocabularies;
    if (searchedVocabularies == null) {
      matchingVocabularies = Collections.emptyList();
    } else {
      matchingVocabularies = searchedVocabularies.stream()
          .filter(vocabulary -> vocabularyMatchesUri(resourceId, vocabulary))
          .collect(Collectors.toList());
    }

    // Log and done.
    if (matchingVocabularies.isEmpty()) {
      LOGGER.info("No vocabularies found for uri {}", resourceId);
    }
    return matchingVocabularies;
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
   * @param vocabularyCandidates The candidate vocabularies to choose from
   * @param incomingDataXml The actual retrieved entity of which to map the type
   * @param resourceId The resource identifier (URI) of the retrieved entity
   * @return The corresponding vocabulary, or null if no such vocabulary is found.
   */
  public static Vocabulary findVocabularyForType(List<Vocabulary> vocabularyCandidates,
      String incomingDataXml, String resourceId) {
    final Vocabulary result = vocabularyCandidates.stream()
        .filter(vocabulary -> vocabularyMatchesType(vocabulary, incomingDataXml)).findAny()
        .orElse(null);
    if (result == null) {
      LOGGER.info("No vocabularies found for uri {}", resourceId);
    }
    return result;
  }

  private static boolean vocabularyMatchesType(Vocabulary vocabulary, String incomingDataXml) {
    final Set<String> typeRules = vocabulary.getTypeRules();
    return typeRules == null || typeRules.isEmpty()
        || typeRules.stream().anyMatch(typeRule -> StringUtils.contains(incomingDataXml, typeRule));
  }
}
