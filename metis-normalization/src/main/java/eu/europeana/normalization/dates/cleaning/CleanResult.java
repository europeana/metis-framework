package eu.europeana.normalization.dates.cleaning;

/**
 * Class containing the operation that was used to clean a value and the cleaned value itself
 */
public class CleanResult {

  private final CleanOperation cleanOperation;
  private final String cleanedValue;

  /**
   * All arguments constructor.
   *
   * @param cleanOperation the clean operation id
   * @param cleanedValue the cleaned value
   */
  public CleanResult(CleanOperation cleanOperation, String cleanedValue) {
    this.cleanOperation = cleanOperation;
    this.cleanedValue = cleanedValue;
  }

  public CleanOperation getCleanOperation() {
    return cleanOperation;
  }

  public String getCleanedValue() {
    return cleanedValue;
  }
}
