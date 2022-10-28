package eu.europeana.enrichment.rest.client.report;

import eu.europeana.enrichment.rest.client.EnrichmentWorker.Mode;

public class ReportMessage {

  private int status;
  private Mode mode;
  private Type messageType;
  private String message;

  private ReportMessage(ReportMessageBuilder reportMessageBuilder) {
    status = reportMessageBuilder.status;
    mode = reportMessageBuilder.mode;
    messageType = reportMessageBuilder.messageType;
    message = reportMessageBuilder.message;
  }

  /**
   * {@code ReportMessage} builder static inner class.
   */
  public static final class ReportMessageBuilder {

    private int status;
    private Mode mode;
    private Type messageType;
    private String message;

    public ReportMessageBuilder() {
      // for builder
    }

    /**
     * Sets the {@code status} and returns a reference to this Builder enabling method chaining.
     *
     * @param val the {@code status} to set
     * @return a reference to this Builder
     */
    public ReportMessageBuilder withStatus(int val) {
      status = val;
      return this;
    }

    /**
     * Sets the {@code mode} and returns a reference to this Builder enabling method chaining.
     *
     * @param val the {@code mode} to set
     * @return a reference to this Builder
     */
    public ReportMessageBuilder withMode(Mode val) {
      mode = val;
      return this;
    }

    /**
     * Sets the {@code messageType} and returns a reference to this Builder enabling method chaining.
     *
     * @param val the {@code messageType} to set
     * @return a reference to this Builder
     */
    public ReportMessageBuilder withMessageType(Type val) {
      messageType = val;
      return this;
    }

    /**
     * Sets the {@code message} and returns a reference to this Builder enabling method chaining.
     *
     * @param val the {@code message} to set
     * @return a reference to this Builder
     */
    public ReportMessageBuilder withMessage(String val) {
      message = val;
      return this;
    }

    /**
     * Returns a {@code ErrorMessage} built from the parameters previously set.
     *
     * @return a {@code ErrorMessage} built with parameters of this {@code ErrorMessage.Builder}
     */
    public ReportMessage build() {
      return new ReportMessage(this);
    }
  }

  @Override
  public String toString() {
    return "[" + status
        + "," + mode.name()
        + "," + messageType.name()
        + "," + message + "]\n";
  }
}
