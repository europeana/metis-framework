package eu.europeana.normalization.language.nal;

import java.util.ArrayList;
import java.util.List;

public class PreprocessingWordMatch {

  String unmatchedRemainingValue;
  List<String> matchedLabels;

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
