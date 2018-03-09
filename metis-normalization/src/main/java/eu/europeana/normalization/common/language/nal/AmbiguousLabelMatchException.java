package eu.europeana.normalization.common.language.nal;

import java.util.List;

public class AmbiguousLabelMatchException extends Exception {

  /** Required for instances of {@link java.io.Serializable}. **/
  private static final long serialVersionUID = -764115641641109988L;

  private final List<String> ambigouosMatches;

  public AmbiguousLabelMatchException(List<String> ambigouosMatches) {
    super();
    this.ambigouosMatches = ambigouosMatches;
  }

  public List<String> getAmbigouosMatches() {
    return ambigouosMatches;
  }
}
