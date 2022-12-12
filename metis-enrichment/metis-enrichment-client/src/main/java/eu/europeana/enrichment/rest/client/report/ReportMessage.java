package eu.europeana.enrichment.rest.client.report;

import eu.europeana.enrichment.rest.client.EnrichmentWorker.Mode;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;

/**
 * ReportMessage class used to inform processed results
 */
public final class ReportMessage {

  private final HttpStatus status;
  private final Mode mode;
  private final Type messageType;
  private final String value;
  private final String message;
  private final String stackTrace;
  private static final int MAX_COMPARE_STACK_TRACE = 50;

  private ReportMessage(ReportMessageBuilder reportMessageBuilder) {
    status = reportMessageBuilder.status;
    mode = reportMessageBuilder.mode;
    messageType = reportMessageBuilder.messageType;
    value = reportMessageBuilder.value;
    message = reportMessageBuilder.message;
    stackTrace = reportMessageBuilder.stackTrace;
  }

  public HttpStatus getStatus() {
    return status;
  }

  public Mode getMode() {
    return mode;
  }

  public Type getMessageType() {
    return messageType;
  }

  public String getValue() {
    return value;
  }

  public String getMessage() {
    return message;
  }

  public String getStackTrace() {
    return stackTrace;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o != null) {
      if (this.getClass() != o.getClass()) {
        return false;
      }
      ReportMessage that = (ReportMessage) o;
      return status == that.status && mode == that.mode && messageType == that.messageType && Objects.equals(value, that.value)
          && Objects.equals(message, that.message) && Objects.equals(
          StringUtils.substring(stackTrace, 1, MAX_COMPARE_STACK_TRACE),
          StringUtils.substring(that.stackTrace, 1, MAX_COMPARE_STACK_TRACE));
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(status, mode, messageType, value, message, StringUtils.substring(stackTrace, 1, MAX_COMPARE_STACK_TRACE));
  }

  /**
   * {@code ReportMessage} builder static inner class.
   */
  public static final class ReportMessageBuilder {

    private HttpStatus status;
    private Mode mode;
    private Type messageType;
    private String value = "";
    private String message = "";
    private String stackTrace = "";

    public ReportMessageBuilder() {
      // for builder
    }

    public static ReportMessageBuilder buildEnrichmentIgnore() {
      return new ReportMessageBuilder().withMode(Mode.ENRICHMENT).withStatus(HttpStatus.OK)
                                       .withMessageType(Type.IGNORE);
    }

    public static ReportMessageBuilder buildEnrichmentWarn() {
      return new ReportMessageBuilder().withMode(Mode.ENRICHMENT)
                                       .withMessageType(Type.WARN);
    }

    public static ReportMessageBuilder buildEnrichmentError() {
      return new ReportMessageBuilder().withMode(Mode.ENRICHMENT).withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                                       .withMessageType(Type.ERROR);
    }

    public static ReportMessageBuilder buildDereferenceIgnore() {
      return new ReportMessageBuilder().withMode(Mode.DEREFERENCE).withStatus(HttpStatus.OK)
                                       .withMessageType(Type.IGNORE);
    }

    public static ReportMessageBuilder buildDereferenceWarn() {
      return new ReportMessageBuilder().withMode(Mode.DEREFERENCE)
                                       .withMessageType(Type.WARN);
    }

    /**
     * Sets the {@code status} and returns a reference to this Builder enabling method chaining.
     *
     * @param val the {@code status} to set
     * @return a reference to this Builder
     */
    public ReportMessageBuilder withStatus(HttpStatus val) {
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
     *
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
     * Sets the {@code Throwable} and returns a reference to this Builder enabling method chaining.
     *
     * @param val the {@code Throwable} to set
     * @return a reference to this Builder
     */
    public ReportMessageBuilder withException(Throwable e) {
      message = ExceptionUtils.getMessage(e);
      stackTrace = ExceptionUtils.getStackTrace(e);
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
    return "[" + status + "," + mode.name() + "," + messageType.name() + "," + value + "," + message + "," + stackTrace + "]\n";
  }
}
