package eu.europeana.enrichment.rest.client.report;

import eu.europeana.enrichment.rest.client.EnrichmentWorker.Mode;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;

/**
 * Report class used to inform processed results
 */
public final class Report {

  private static final int MAX_COMPARE_STACK_TRACE = 50;
  private HttpStatus status;
  private Mode mode;
  private Type messageType;
  private String value = "";
  private String message = "";
  private String stackTrace = "";

  private Report() {
    //for builder
  }

  private Report(Report val) {
    this.status = val.status;
    this.mode = val.mode;
    this.messageType = val.messageType;
    this.value = val.value;
    this.message = val.message;
    this.stackTrace = val.stackTrace;
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
      Report that = (Report) o;
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

  @Override
  public String toString() {
    return "[" + status + "," + mode.name() + "," + messageType.name() + "," + value + "," + message + "," + stackTrace + "]\n";
  }

  /**
   * Create a report message for enrichment with ok status and ignore type
   *
   * @return a reference to this Builder
   */
  public static Report buildEnrichmentIgnore() {
    return new Report().withMode(Mode.ENRICHMENT).withStatus(HttpStatus.OK)
                       .withMessageType(Type.IGNORE);
  }

  /**
   * Create a report message for enrichment with warn type
   *
   * @return a reference to this Builder
   */
  public static Report buildEnrichmentWarn() {
    return new Report().withMode(Mode.ENRICHMENT)
                       .withMessageType(Type.WARN);
  }

  /**
   * Create a report message for enrichment with error status and error type
   *
   * @return a reference to this Builder
   */
  public static Report buildEnrichmentError() {
    return new Report().withMode(Mode.ENRICHMENT)
                       .withMessageType(Type.ERROR);
  }

  /**
   * Create a report message for dereference with ok status and ignore type
   *
   * @return a reference to this Builder
   */
  public static Report buildDereferenceIgnore() {
    return new Report().withMode(Mode.DEREFERENCE).withStatus(HttpStatus.OK)
                       .withMessageType(Type.IGNORE);
  }

  /**
   * Create a report message for dereference with warn type
   *
   * @return a reference to this Builder
   */
  public static Report buildDereferenceWarn() {
    return new Report().withMode(Mode.DEREFERENCE)
                       .withMessageType(Type.WARN);
  }

  /**
   * Sets the {@code status} and returns a reference to this Builder enabling method chaining.
   *
   * @param val the {@code status} to set
   * @return a reference to this Builder
   */
  public Report withStatus(HttpStatus val) {
    status = val;
    return this;
  }

  /**
   * Sets the {@code mode} and returns a reference to this Builder enabling method chaining.
   *
   * @param val the {@code mode} to set
   * @return a reference to this Builder
   */
  public Report withMode(Mode val) {
    mode = val;
    return this;
  }

  /**
   * Sets the {@code messageType} and returns a reference to this Builder enabling method chaining.
   *
   * @param val the {@code messageType} to set
   * @return a reference to this Builder
   */
  public Report withMessageType(Type val) {
    messageType = val;
    return this;
  }

  /**
   * Sets the {@code value} and returns a reference to this Builder enabling method chaining.
   *
   * @param val the {@code value} to set
   * @return a reference to this Builder
   */
  public Report withValue(String val) {
    value = val;
    return this;
  }

  /**
   * Sets the {@code message} and returns a reference to this Builder enabling method chaining.
   *
   * @param val the {@code message} to set
   * @return a reference to this Builder
   */
  public Report withMessage(String val) {
    message = val;
    return this;
  }

  /**
   * Sets the {@code Throwable} and returns a reference to this Builder enabling method chaining.
   *
   * @param val the {@code Throwable} to set
   * @return a reference to this Builder
   */
  public Report withException(Throwable val) {
    message = ExceptionUtils.getMessage(val);
    stackTrace = ExceptionUtils.getStackTrace(val);
    return this;
  }

  /**
   * Returns a {@code ErrorMessage} built from the parameters previously set.
   *
   * @return a {@code ErrorMessage} built with parameters of this {@code ErrorMessage.Builder}
   */
  public Report build() {
    return new Report(this);
  }
}
