package eu.europeana.metis.debias.detect.model;

/**
 * The type Tag.
 */
public class Tag {
  private int start;
  private int length;
  private String vocabulary;
  private float score;

  /**
   * Gets start.
   *
   * @return the start
   */
  public int getStart() {
    return start;
  }

  /**
   * Sets start.
   *
   * @param start the start
   */
  public void setStart(int start) {
    this.start = start;
  }

  /**
   * Gets length.
   *
   * @return the length
   */
  public int getLength() {
    return length;
  }

  /**
   * Sets length.
   *
   * @param length the length
   */
  public void setLength(int length) {
    this.length = length;
  }

  /**
   * Gets vocabulary.
   *
   * @return the vocabulary
   */
  public String getVocabulary() {
    return vocabulary;
  }

  /**
   * Sets vocabulary.
   *
   * @param vocabulary the vocabulary
   */
  public void setVocabulary(String vocabulary) {
    this.vocabulary = vocabulary;
  }

  /**
   * Gets score.
   *
   * @return the score
   */
  public float getScore() {
    return score;
  }

  /**
   * Sets score.
   *
   * @param score the score
   */
  public void setScore(float score) {
    this.score = score;
  }
}
