package eu.europeana.normalization.common.language.nal;

import java.util.ArrayList;
import java.util.List;

class PreprocessingWordMatch {

  private String unmatchedRemainingValue;
  private List<String> matchedLabels;

  public PreprocessingWordMatch() {
    matchedLabels = new ArrayList<>();
  }

  public String getUnmatchedRemainingValue() {
    return unmatchedRemainingValue;
  }

  public void setUnmatchedRemainingValue(String unmatchedRemainingValue) {
    this.unmatchedRemainingValue = unmatchedRemainingValue;
  }

  public List<String> getMatchedLabels() {
    return matchedLabels;
  }

  public void setMatchedLabels(List<String> matchedLabels) {
    this.matchedLabels = matchedLabels;
  }


}
