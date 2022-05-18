package eu.europeana.patternanalysis;

import static java.lang.String.format;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.BooleanUtils.isFalse;
import static org.apache.commons.lang3.StringUtils.abbreviate;

import eu.europeana.metis.schema.convert.RdfConversionUtils;
import eu.europeana.metis.schema.convert.SerializationException;
import eu.europeana.metis.schema.jibx.EuropeanaType;
import eu.europeana.metis.schema.jibx.EuropeanaType.Choice;
import eu.europeana.metis.schema.jibx.LiteralType;
import eu.europeana.metis.schema.jibx.ProvidedCHOType;
import eu.europeana.metis.schema.jibx.ProxyType;
import eu.europeana.metis.schema.jibx.RDF;
import eu.europeana.metis.schema.jibx.ResourceOrLiteralType;
import eu.europeana.patternanalysis.view.ProblemOccurrence;
import eu.europeana.patternanalysis.view.ProblemPattern;
import eu.europeana.patternanalysis.view.ProblemPatternAnalysis;
import eu.europeana.patternanalysis.view.ProblemPatternDescription;
import eu.europeana.patternanalysis.view.RecordAnalysis;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.LongestCommonSubsequence;

/**
 * Class that contains functionality to analyze a record and retrieve all problem patterns.
 */
public class ProblemPatternAnalyzer {

  private static final int MIN_TITLE_LENGTH = 2;
  private static final int MAX_TITLE_LENGTH = 70;
  private static final int MIN_DESCRIPTION_LENGTH = 50;
  private static final int UNRECOGNIZABLE_CHARACTERS_THRESHOLD = 5;
  private static final double LCS_CALCULATION_THRESHOLD = 0.9;
  private static final int TITLE_DESCRIPTION_LENGTH_DISTANCE = 20;
  private static final int DEFAULT_MAX_CHARACTERS_ELEMENT_LENGTH_FOR_REPORT = 50;
  // Match anything that is not alphanumeric in all languages or literal spaces. We cannot just use \\w
  private static final String UNRECOGNIZABLE_CHARACTERS_REGEX = "[^\\p{IsAlphabetic}\\p{IsDigit} ]";
  private static final Pattern UNRECOGNIZABLE_CHARACTERS_PATTERN = Pattern.compile(UNRECOGNIZABLE_CHARACTERS_REGEX);

  /**
   * Analyzes a record for problem patterns.
   *
   * @param rdfString the rdf record as a string
   * @return a list of problem patterns
   * @throws SerializationException if the record could not be converted to {@link RDF}
   */
  public ProblemPatternAnalysis analyzeRecord(String rdfString) throws SerializationException {
    return analyzeRecord(new RdfConversionUtils().convertStringToRdf(rdfString));
  }

  /**
   * Analyzes a record for problem patterns.
   *
   * @param rdf the rdf record
   * @return a list of problem patterns
   */
  public ProblemPatternAnalysis analyzeRecord(RDF rdf) {
    final List<ProxyType> providerProxies = getProviderProxies(rdf);
    final List<Choice> choices = providerProxies.stream().map(EuropeanaType::getChoiceList)
                                                .filter(Objects::nonNull)
                                                .flatMap(Collection::stream)
                                                .collect(toList());

    final List<String> titles = getChoicesInStringList(choices, Choice::ifTitle, Choice::getTitle, LiteralType::getString);
    final List<String> descriptions = getChoicesInStringList(choices, Choice::ifDescription, Choice::getDescription,
        ResourceOrLiteralType::getString);
    final List<String> identifiers = getChoicesInStringList(choices, Choice::ifIdentifier, Choice::getIdentifier,
        LiteralType::getString);
    final String rdfAbout = rdf.getProvidedCHOList().stream().filter(Objects::nonNull).findFirst()
                               .map(ProvidedCHOType::getAbout).orElse(null);
    final ArrayList<ProblemPattern> problemPatterns = computeProblemPatterns(rdfAbout, titles, descriptions, identifiers);
    return new ProblemPatternAnalysis(rdfAbout, problemPatterns, Set.copyOf(titles));
  }

  private <T> List<String> getChoicesInStringList(List<Choice> choices, Predicate<Choice> choicePredicate,
      Function<Choice, T> choiceGetter, Function<T, String> getString) {
    return choices.stream().filter(Objects::nonNull).filter(choicePredicate).map(choiceGetter).map(getString).collect(toList());
  }

  private ArrayList<ProblemPattern> computeProblemPatterns(String rdfAbout, List<String> titles, List<String> descriptions,
      List<String> identifiers) {
    final ArrayList<ProblemPattern> problemPatterns = new ArrayList<>();

    constructProblemPattern(rdfAbout, ProblemPatternDescription.P2, checkP2(titles, descriptions)).ifPresent(
        problemPatterns::add);
    constructProblemPattern(rdfAbout, ProblemPatternDescription.P3, checkP3(titles, descriptions)).ifPresent(
        problemPatterns::add);
    constructProblemPattern(rdfAbout, ProblemPatternDescription.P5, checkP5(titles, identifiers)).ifPresent(problemPatterns::add);
    constructProblemPattern(rdfAbout, ProblemPatternDescription.P6, checkP6(titles)).ifPresent(problemPatterns::add);
    constructProblemPattern(rdfAbout, ProblemPatternDescription.P7, checkP7(descriptions)).ifPresent(problemPatterns::add);
    constructProblemPattern(rdfAbout, ProblemPatternDescription.P9, checkP9(descriptions)).ifPresent(problemPatterns::add);
    constructProblemPattern(rdfAbout, ProblemPatternDescription.P12, checkP12(titles)).ifPresent(problemPatterns::add);
    return problemPatterns;
  }

  private static boolean isProviderProxy(ProxyType proxy) {
    return proxy.getEuropeanaProxy() == null || isFalse(proxy.getEuropeanaProxy().isEuropeanaProxy());
  }

  private List<ProxyType> getProviderProxies(RDF rdf) {
    return Optional.ofNullable(rdf.getProxyList()).stream().flatMap(Collection::stream)
                   .filter(Objects::nonNull).filter(ProblemPatternAnalyzer::isProviderProxy)
                   .collect(Collectors.toList());
  }

  /**
   * Abbreviate(based on {@link StringUtils#abbreviate(String, int)}) an element up to a default max length {@link
   * #DEFAULT_MAX_CHARACTERS_ELEMENT_LENGTH_FOR_REPORT}.
   * <p>Is used locally and can be used publicly for global problem patterns like P1.</p>
   *
   * @param element the string element
   * @return the truncated string
   */
  public String abbreviateElement(String element) {
    return abbreviate(element, DEFAULT_MAX_CHARACTERS_ELEMENT_LENGTH_FOR_REPORT);
  }

  /**
   * Check whether there is a title - description pair for which the values are equal, ignoring letter (upper or lower) case.
   * <p>It will report a single occurrence for multiple same fields</p>
   *
   * @param titles the list of titles
   * @param descriptions the list of descriptions
   * @return the list of problem occurrences encountered
   */
  private List<ProblemOccurrence> checkP2(List<String> titles, List<String> descriptions) {
    final Set<String> uniqueTitles = titles.stream().map(String::toLowerCase).collect(toSet());
    final Set<String> uniqueDescriptions = descriptions.stream().map(String::toLowerCase).collect(toSet());
    final HashSet<String> equalTitlesAndDescriptions = new HashSet<>(uniqueTitles);
    equalTitlesAndDescriptions.retainAll(uniqueDescriptions);

    return equalTitlesAndDescriptions.stream().map(
        value -> new ProblemOccurrence(abbreviateElement(value))
    ).collect(toList());
  }

  private List<String> nearIdenticalDescriptions(String title, List<String> descriptions) {
    final LongestCommonSubsequence longestCommonSubsequence = new LongestCommonSubsequence();
    final Predicate<String> lcsPredicate = description ->
        ((double) longestCommonSubsequence.apply(title, description) / Math.min(title.length(), description.length()))
            >= LCS_CALCULATION_THRESHOLD;
    final Predicate<String> distancePredicate = description -> Math.abs(title.length() - description.length())
        <= TITLE_DESCRIPTION_LENGTH_DISTANCE;
    return descriptions.stream().filter(StringUtils::isNotBlank).filter(not(title::equalsIgnoreCase))
                       .filter(lcsPredicate.and(distancePredicate)).collect(toList());
  }

  /**
   * Check whether there is a title - description pair for which the values are too similar.
   * <p>
   * The solution is based on the LCS algorithm(<a href="https://en.wikipedia.org/wiki/Longest_common_subsequence_problem">Longest
   * Common Subsequence</a>).
   * <p>
   * The formula chosen is:
   * <p>
   * LCS (title, description) / minimum(length(title), length(desc)) >= 0.9 && |length(title)-length(desc)| <= 20
   * <p>
   * Blank values are filtered out. Titles and descriptions that are equal, ignoring letter (upper or lower) case are filtered
   * out. Same titles will be reported once and will not have a duplicate of it self with same near identical descriptions.
   *
   * @param titles the list of titles
   * @param descriptions the list of descriptions
   * @return the list of problem occurrences encountered
   */
  private List<ProblemOccurrence> checkP3(List<String> titles, List<String> descriptions) {
    final Map<String, List<String>> nearIdenticalTitleDescriptionsMap =
        titles.stream().filter(StringUtils::isNotBlank)
              .collect(toMap(title -> title, title -> nearIdenticalDescriptions(title, descriptions), (t1, t2) -> t1));

    return nearIdenticalTitleDescriptionsMap.entrySet().stream().flatMap(
        entry -> entry.getValue().stream().map(
            value -> new ProblemOccurrence(format("%s <--> %s", abbreviateElement(entry.getKey()), abbreviateElement(value)))
        )
    ).collect(toList());
  }

  /**
   * Check whether a title is not human-readable.
   * <p>
   * We check this by:
   *   <ul>
   *     <li>Whether there are more than 5 characters that are not valid.
   *     Non valid characters are considered characters that are not alphanumeric and are not simple "literal" spaces(tabs,
   *     new lines etc. are considered invalid characters).
   *     This is performed with regex unicode matching {@link #UNRECOGNIZABLE_CHARACTERS_REGEX} and should support all languages.
   *     For more information check <a href="https://www.regular-expressions.info/unicode.html#category">unicode regex</a></li>
   *     <li>The title does not fully contain an identifier</li>
   *   </ul>
   * </p>
   *
   * @param titles the list of titles
   * @param identifiers the list of identifiers
   * @return the list of problem occurrences encountered
   */
  private List<ProblemOccurrence> checkP5(List<String> titles, List<String> identifiers) {
    final Predicate<String> moreThanThresholdUnrecognizableCharacters = s ->
        UNRECOGNIZABLE_CHARACTERS_PATTERN.matcher(s).results().count() > UNRECOGNIZABLE_CHARACTERS_THRESHOLD;
    final Predicate<String> containsIdentifier = s -> identifiers.stream().anyMatch(s::contains);
    return titles.stream().filter(moreThanThresholdUnrecognizableCharacters.or(containsIdentifier))
                 .map(title -> new ProblemOccurrence(abbreviateElement(title))
                 ).collect(toList());
  }

  /**
   * Check whether the record has titles of {@link #MIN_TITLE_LENGTH} characters or fewer.
   *
   * @param titles the list of titles
   * @return the list of problem occurrences encountered
   */
  private List<ProblemOccurrence> checkP6(List<String> titles) {
    return titles.stream().filter(title -> title.length() <= MIN_TITLE_LENGTH)
                 .map(title -> new ProblemOccurrence(abbreviateElement(title)))
                 .collect(toList());
  }

  /**
   * Check whether the record is lacking a description (or only has empty descriptions).
   *
   * @param descriptions the list of descriptions
   * @return the list of problem occurrences encountered
   */
  private List<ProblemOccurrence> checkP7(List<String> descriptions) {
    if (CollectionUtils.isEmpty(descriptions) || descriptions.stream().allMatch(StringUtils::isBlank)) {
      return List.of(new ProblemOccurrence(abbreviateElement("Missing description fields")));
    }
    return Collections.emptyList();
  }

  /**
   * Check whether the record has descriptions of {@link #MIN_DESCRIPTION_LENGTH} characters or fewer.
   * <p>Blank values are filtered out</p>
   *
   * @param descriptions the list of descriptions
   * @return the list of problem occurrences encountered
   */
  private List<ProblemOccurrence> checkP9(List<String> descriptions) {
    return descriptions.stream().filter(StringUtils::isNotBlank)
                       .filter(description -> description.length() <= MIN_DESCRIPTION_LENGTH)
                       .map(description -> new ProblemOccurrence(abbreviateElement(description)))
                       .collect(toList());
  }

  /**
   * Check whether the record has titles of more than {@link #MAX_TITLE_LENGTH} characters.
   * <p>Unicode codes are converted to relevant characters(counted as one character) and the length of that is checked.</p>
   *
   * @param titles the list of titles
   * @return the list of problem occurrences encountered
   */
  private List<ProblemOccurrence> checkP12(List<String> titles) {
    return titles.stream().filter(title -> title.length() > MAX_TITLE_LENGTH)
                 .map(title -> new ProblemOccurrence(abbreviateElement(title)))
                 .collect(toList());
  }

  private Optional<ProblemPattern> constructProblemPattern(String recordId, ProblemPatternDescription problemPatternDescription,
      List<ProblemOccurrence> problemOccurrences) {
    if (CollectionUtils.isNotEmpty(problemOccurrences)) {
      return Optional.of(new ProblemPattern(
          problemPatternDescription, 1, List.of(new RecordAnalysis(recordId, problemOccurrences))));
    }
    return Optional.empty();
  }

}
