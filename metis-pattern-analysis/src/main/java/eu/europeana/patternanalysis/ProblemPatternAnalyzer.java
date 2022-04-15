package eu.europeana.patternanalysis;

import static java.lang.String.format;
import static java.util.function.Predicate.not;

import eu.europeana.metis.schema.convert.RdfConversionUtils;
import eu.europeana.metis.schema.convert.SerializationException;
import eu.europeana.metis.schema.jibx.Description;
import eu.europeana.metis.schema.jibx.EuropeanaType;
import eu.europeana.metis.schema.jibx.EuropeanaType.Choice;
import eu.europeana.metis.schema.jibx.ProvidedCHOType;
import eu.europeana.metis.schema.jibx.ProxyType;
import eu.europeana.metis.schema.jibx.RDF;
import eu.europeana.metis.schema.jibx.Title;
import eu.europeana.patternanalysis.view.ProblemOccurrence;
import eu.europeana.patternanalysis.view.ProblemPattern;
import eu.europeana.patternanalysis.view.ProblemPatternDescription;
import eu.europeana.patternanalysis.view.RecordAnalysis;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Class that contains functionality to analyze a record and retrieve all problem patterns.
 */
public class ProblemPatternAnalyzer {

  public static final int MIN_TITLE_LENGTH = 2;

  /**
   * Analyzes a record for problem patterns.
   *
   * @param rdfString the rdf record as a string
   * @return a list of problem patterns
   * @throws SerializationException if the record could not be converted to {@link RDF}
   */
  public List<ProblemPattern> analyzeRecord(String rdfString) throws SerializationException {
    return analyzeRecord(new RdfConversionUtils().convertStringToRdf(rdfString));
  }

  /**
   * Analyzes a record for problem patterns.
   *
   * @param rdf the rdf record
   * @return a list of problem patterns
   */
  public List<ProblemPattern> analyzeRecord(RDF rdf) {
    final List<ProxyType> providerProxies = getProviderProxies(rdf);
    final List<String> titles = providerProxies.stream().map(EuropeanaType::getChoiceList).flatMap(Collection::stream)
                                               .filter(Choice::ifTitle).map(Choice::getTitle)
                                               .map(Title::getString)
                                               .filter(StringUtils::isNotBlank)
                                               .map(String::trim)
                                               .collect(Collectors.toList());
    final List<String> descriptions = providerProxies.stream().map(EuropeanaType::getChoiceList).flatMap(Collection::stream)
                                                     .filter(Choice::ifDescription).map(Choice::getDescription)
                                                     .map(Description::getString)
                                                     .filter(StringUtils::isNotBlank)
                                                     .map(String::trim)
                                                     .collect(Collectors.toList());
    final String rdfAbout = rdf.getProvidedCHOList().stream().filter(Objects::nonNull).findFirst()
                               .map(ProvidedCHOType::getAbout).orElse(null);
    return computeProblemPatterns(rdfAbout, titles, descriptions);
  }

  private ArrayList<ProblemPattern> computeProblemPatterns(String rdfAbout, List<String> titles, List<String> descriptions) {
    final ArrayList<ProblemPattern> problemPatterns = new ArrayList<>();

    constructProblemPattern(rdfAbout, ProblemPatternDescription.P2, checkP2(titles, descriptions)).ifPresent(
        problemPatterns::add);
    constructProblemPattern(rdfAbout, ProblemPatternDescription.P6, checkP6(titles)).ifPresent(problemPatterns::add);
    return problemPatterns;
  }

  private List<ProxyType> getProviderProxies(RDF rdf) {
    return rdf.getProxyList().stream().filter(proxyType -> proxyType.getEuropeanaProxy() != null)
              .filter(not(proxyType -> proxyType.getEuropeanaProxy().isEuropeanaProxy())).collect(Collectors.toList());
  }

  private List<ProblemOccurrence> checkP2(List<String> titles, List<String> descriptions) {
    final Set<String> uniqueTitles = titles.stream().map(String::toLowerCase).collect(Collectors.toSet());
    final Set<String> uniqueDescriptions = descriptions.stream().map(String::toLowerCase).collect(Collectors.toSet());
    final HashSet<String> equalTitlesAndDescriptions = new HashSet<>(uniqueTitles);
    equalTitlesAndDescriptions.retainAll(uniqueDescriptions);

    return equalTitlesAndDescriptions.stream().map(
        value -> new ProblemOccurrence(format("Equal(lower cased) title and description: %s", value))
    ).collect(Collectors.toList());
  }

  private List<ProblemOccurrence> checkP6(List<String> titles) {
    return titles.stream().filter(title -> title.length() <= MIN_TITLE_LENGTH)
                 .map(title -> new ProblemOccurrence(format("Non meaningful title: %s", title))).collect(Collectors.toList());
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
