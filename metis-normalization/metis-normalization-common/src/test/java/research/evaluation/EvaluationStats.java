package research.evaluation;

public class EvaluationStats {

  private int total = 0;
  private final NormalizationStats normalizationMethodStats;

  private final NormalizationStats targetCodesMatchesStats;
  private final NormalizationStats validateMatchesCorrectStats;
  private final NormalizationStats validateMatchesIncorrectStats;
  private final NormalizationStats validateMatchesPartiallyCorrectStats;

  public EvaluationStats() {
    normalizationMethodStats = new NormalizationStats();
    targetCodesMatchesStats = new NormalizationStats();
    validateMatchesCorrectStats = new NormalizationStats();
    validateMatchesIncorrectStats = new NormalizationStats();
    validateMatchesPartiallyCorrectStats = new NormalizationStats();
  }


  public NormalizationStats getNormalizationMethodStats() {
    return normalizationMethodStats;
  }

  public NormalizationStats getValidateMatchesCorrectStats() {
    return validateMatchesCorrectStats;
  }

  public NormalizationStats getValidateMatchesIncorrectStats() {
    return validateMatchesIncorrectStats;
  }

  public NormalizationStats getTargetCodesMatchesStats() {
    return targetCodesMatchesStats;
  }

  public NormalizationStats getValidateMatchesPartiallyCorrectStats() {
    return validateMatchesPartiallyCorrectStats;
  }


  public void addAlreadyNormalized(int cnt) {
    getNormalizationMethodStats().addAlreadyNormalized(cnt);
    getTargetCodesMatchesStats().addAlreadyNormalized(cnt);
  }


  public void addCodeMatch(int cnt) {
    getNormalizationMethodStats().addNormalizedFromCode(cnt);
  }


  @Override
  public String toString() {
    return "EvaluationStats [count=" + total + "\n, normalizationMethodStats="
        + normalizationMethodStats + "\n, targetCodesMatchesStats="
        + targetCodesMatchesStats + "\n, validateMatchesCorrectStats=" + validateMatchesCorrectStats
        + "\n, validateMatchesIncorrectStats=" + validateMatchesIncorrectStats
        + ",\n validateMatchesPartiallyCorrectStats=" + validateMatchesPartiallyCorrectStats + "]";
  }

  public String toCsv() {
    return "normalizationMethodStats=\n" + normalizationMethodStats.toCsvString()
        + "\n, targetCodesMatchesStats=\n"
        + targetCodesMatchesStats.toCsvString() + "\nvalidateMatchesCorrectStats=\n"
        + validateMatchesCorrectStats.toCsvString()
        + "\nvalidateMatchesIncorrectStats=\n" + validateMatchesIncorrectStats.toCsvString()
        + "\nvalidateMatchesPartiallyCorrectStats=\n" + validateMatchesPartiallyCorrectStats
        .toCsvString();
  }

  public int getTotalValues() {
    return total;
  }

  public float getRecall() {
    return -1;
  }

  public float getPrecision() {
    return -1;
  }


  public void addCount(Integer cnt) {
    total += cnt;
  }
}
