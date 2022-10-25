package eu.europeana.enrichment.rest.client.report;

import eu.europeana.enrichment.rest.client.EnrichmentWorker.Mode;

public class ErrorMessage {
  private String status;
  private Mode mode;
  private Type messageType;
  private String message;

  private ErrorMessage(Builder builder) {
    status = builder.status;
    mode = builder.mode;
    messageType = builder.messageType;
    message = builder.message;
  }

  /**
   * {@code ErrorMessage} builder static inner class.
   */
  public static final class Builder {

    private String status;
    private Mode mode;
    private Type messageType;
    private String message;

    public Builder() {
    }

    /**
     * Sets the {@code status} and returns a reference to this Builder enabling method chaining.
     *
     * @param val the {@code status} to set
     * @return a reference to this Builder
     */
    public Builder withStatus(String val) {
      status = val;
      return this;
    }

    /**
     * Sets the {@code mode} and returns a reference to this Builder enabling method chaining.
     *
     * @param val the {@code mode} to set
     * @return a reference to this Builder
     */
    public Builder withMode(Mode val) {
      mode = val;
      return this;
    }

    /**
     * Sets the {@code messageType} and returns a reference to this Builder enabling method chaining.
     *
     * @param val the {@code messageType} to set
     * @return a reference to this Builder
     */
    public Builder withMessageType(Type val) {
      messageType = val;
      return this;
    }

    /**
     * Sets the {@code message} and returns a reference to this Builder enabling method chaining.
     *
     * @param val the {@code message} to set
     * @return a reference to this Builder
     */
    public Builder withMessage(String val) {
      message = val;
      return this;
    }

    /**
     * Returns a {@code ErrorMessage} built from the parameters previously set.
     *
     * @return a {@code ErrorMessage} built with parameters of this {@code ErrorMessage.Builder}
     */
    public ErrorMessage build() {
      return new ErrorMessage(this);
    }
  }
}
