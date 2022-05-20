package eu.europeana.patternanalysis.view;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElseGet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class containing the problem pattern analysis for a record.
 */
public class ProblemPatternAnalysis {

  private final String rdfAbout;
  private final List<ProblemPattern> problemPatterns;
  private final Set<String> titles;

  /**
   * Constructor with required parameters.
   *
   * @param rdfAbout the rdf about
   * @param problemPatterns the problem patterns
   * @param titles the record titles
   */
  public ProblemPatternAnalysis(String rdfAbout, List<ProblemPattern> problemPatterns, Set<String> titles) {
    this.rdfAbout = requireNonNull(rdfAbout);
    this.problemPatterns = requireNonNullElseGet(problemPatterns, ArrayList::new);
    this.titles = requireNonNullElseGet(titles, HashSet::new);
  }

  public String getRdfAbout() {
    return rdfAbout;
  }

  public List<ProblemPattern> getProblemPatterns() {
    return new ArrayList<>(problemPatterns);
  }

  public Set<String> getTitles() {
    return new HashSet<>(titles);
  }
}
