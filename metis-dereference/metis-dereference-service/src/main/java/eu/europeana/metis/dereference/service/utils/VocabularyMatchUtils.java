package eu.europeana.metis.dereference.service.utils;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europeana.metis.dereference.Vocabulary;
import eu.europeana.metis.dereference.service.MongoDereferenceService;

/**
 * This class contains some utility functionality related to finding the appropriate vocabulary for
 * a given dereference request.
 * 
 * @author jochen
 *
 */
public final class VocabularyMatchUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(MongoDereferenceService.class);

  private VocabularyMatchUtils() {}

  /**
   * Return the vocabularies that match the given resource identifier.
   * 
   * TODO MET-655 We should change this so that it doesn't just look at the uri field of the
   * vocabulary, but also at the filter of the vocabulary. So first get all vocabularies matching
   * the uri, but then in this method filter them on the vocabulary's url rules (if there are any).
   * We also need to normalize the url to the extent that we can (remove '/./' and '//'). Finally,
   * we should be more generic, not assuming that the uri consists of the three parts given. A unit
   * test for this method may be in order.
   * 
   * @param resource The resource identifier (URI) that we receive and that we are to match.
   * @param retrieveFromPersistence A function that retrieves the list of vocabularies from
   *        persistence given a specific URI.
   * @return The list of vocabularies that match the given URI. Or the empty list if none are found.
   *         This method does not return null.
   */
  public static List<Vocabulary> findVocabulariesForResource(String resource,
      Function<String, List<Vocabulary>> retrieveFromPersistence) {

    final String[] splitName = resource.split("/");
    if (splitName.length <= 3) {
      LOGGER.info("Invalid uri {}", resource);
      return Collections.emptyList();
    }

    final String vocabularyUri = splitName[0] + "/" + splitName[1] + "/" + splitName[2] + "/";
    final List<Vocabulary> vocabularies = retrieveFromPersistence.apply(vocabularyUri);

    if (vocabularies == null || vocabularies.isEmpty()) {
      LOGGER.info("No vocabularies found for uri {}", resource);
      return Collections.emptyList();
    }

    return vocabularies;
  }

  /**
   * Once the entity has been retrieved decide on the actual vocabulary that you want
   *
   * @param vocabularies The vocabularies to choose from
   * @param incomingDataXml The actual retrieved entity
   * @param resource The resource identifier (URI) of the record to check for rules
   * @return The corresponding vocabulary, or null if no such vocabulary is found.
   */
  public static Vocabulary findByEntity(List<Vocabulary> vocabularies, String incomingDataXml,
      String resource) {
    return vocabularies.stream()
        .filter(vocabulary -> vocabularyMatches(vocabulary, incomingDataXml, resource)).findAny()
        .orElse(null);
  }

  // TODO MET-655: It requires the whole uri to be present in the rule (including the
  // record-specific part). It should rather test that a certain part of it is included as a rule
  // (the path?). A unit test for this may also be in order.
  private static boolean vocabularyMatches(Vocabulary vocabulary, String incomingDataXml,
      String resource) {

    // Check the rules
    final Set<String> vocabularyRules = vocabulary.getRules();
    if (vocabularyRules != null && !vocabularyRules.isEmpty() && !vocabularyRules.contains(resource)) {
      return false;
    }

    // Check the type rules (more expensive operation: only do when needed).
    final Set<String> typeRules = vocabulary.getTypeRules();
    return typeRules == null || typeRules.isEmpty()
        || typeRules.stream().anyMatch(typeRule -> StringUtils.contains(incomingDataXml, typeRule));
  }
}
