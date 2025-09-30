package eu.europeana.patternanalysis.view;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Set;

/**
 * Class containing the problem pattern analysis for a record.
 */
public record ProblemPatternAnalysis(
    String rdfAbout,
    List<ProblemPattern> problemPatterns,
    Set<String> titles) {

  /**
   * Constructor with required parameters.
   *
   * @param rdfAbout the rdf about
   * @param problemPatterns the problem patterns
   * @param titles the record titles
   */
  public ProblemPatternAnalysis {
    requireNonNull(rdfAbout);
    problemPatterns = (problemPatterns == null) ? List.of() : List.copyOf(problemPatterns);
    titles = (titles == null) ? Set.of() : Set.copyOf(titles);
  }
}
