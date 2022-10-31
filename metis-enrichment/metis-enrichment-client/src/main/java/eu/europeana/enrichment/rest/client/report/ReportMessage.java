package eu.europeana.enrichment.rest.client.report;

import eu.europeana.enrichment.rest.client.EnrichmentWorker.Mode;

public class ReportMessage {

  private int status;
  private Mode mode;
  private Type messageType;
  private String value;
  private String message;
  private String stackTrace;

  private ReportMessage(ReportMessageBuilder reportMessageBuilder) {
    status = reportMessageBuilder.status;
    mode = reportMessageBuilder.mode;
    messageType = reportMessageBuilder.messageType;
    value = reportMessageBuilder.value;
    message = reportMessageBuilder.message;
    stackTrace = reportMessageBuilder.stackTrace;
  }

  /**
   * {@code ReportMessage} builder static inner class.
   */
  public static final class ReportMessageBuilder {

    private int status;
    private Mode mode;
    private Type messageType;
    private String value;
    private String message;
    private String stackTrace;

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
     * Sets the {@code value} and returns a reference to this Builder enabling method chaining.
     * @param val the {@code value} to set
     * @return a reference to this Builder
     */
    public ReportMessageBuilder withValue(String val) {
      value = val;
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
     * Sets the {@code stackTrace} and returns a reference to this Builder enabling method chaining.
     * @param val the {@code stackTrace} to set
     * @return a reference to this Builder
     */
    public ReportMessageBuilder withStackTrace(String val) {
      stackTrace = val;
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
        + "," + value
        + "," + message
        + "," + stackTrace
        + "]\n";
  }
}
