package research.evaluation;

public class NormalizationStats {
  //stats counting values and counting number of records

// iso 2 letter code match
  //any iso code match (including locales)

  private int okCnt = 0;
  private int normalizedFromCodeCnt = 0;
  private int normalizedCnt = 0;
  private int normalizedAmbiguousCnt = 0;
  private int normalizedWordCnt = 0;
  private int normalizedWordAllCnt = 0;
  private int noMatchCnt = 0;

  public void addAlreadyNormalized(int count) {
    this.okCnt += count;
  }

  public void addNormalizedFromCode(int count) {
    this.normalizedFromCodeCnt += count;
  }

  public void addNormalizedFromExactLabelMatch(int count) {
    this.normalizedCnt += count;
  }

  public void addNormalizedFromExactAmbigouosLabelMatch(int count) {
    this.normalizedAmbiguousCnt += count;
  }

  public void addNormalizedFromAllWordsLabelMatch(int count) {
    this.normalizedWordAllCnt += count;
  }

  public void addNormalizedFromWordsLabelMatch(int count) {
    this.normalizedWordCnt += count;
  }

  public void addNoMatch(Integer cnt) {
    this.noMatchCnt += cnt;
  }

  @Override
  public String toString() {
    return "NormalizationStats [okCnt=" + okCnt + ", normalizedFromCodeCnt=" + normalizedFromCodeCnt
        + ", normalizedCnt=" + normalizedCnt + ", normalizedAmbiguousCnt=" + normalizedAmbiguousCnt
        + ", normalizedWordCnt=" + normalizedWordCnt + ", normalizedWordAllCnt="
        + normalizedWordAllCnt
        + ", noMatchCnt=" + noMatchCnt + "]";
  }

  public String toCsvString() {
    String ret = "okCnt, normalizedFromCodeCnt,normalizedCnt,normalizedAmbiguousCnt,normalizedWordCnt,normalizedWordAllCnt,noMatchCnt\n";
    return ret + okCnt + "," + normalizedFromCodeCnt
        + "," + normalizedCnt + "," + normalizedAmbiguousCnt
        + "," + normalizedWordCnt + "," + normalizedWordAllCnt
        + "," + noMatchCnt;
  }


}
