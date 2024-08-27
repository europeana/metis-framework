package eu.europeana.metis.debias.detect.model;

/**
 * The type Tag.
 */
public class Tag {
  private int start;
  private int end;
  private int length;
  private String uri;
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
   * Gets end.
   *
   * @return the end
   */
  public int getEnd() {
    return end;
  }

  /**
   * Sets end.
   *
   * @param end the end
   */
  public void setEnd(int end) {
    this.end = end;
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
  public String getUri() {
    return uri;
  }

  /**
   * Sets vocabulary.
   *
   * @param uri the vocabulary
   */
  public void setUri(String uri) {
    this.uri = uri;
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
