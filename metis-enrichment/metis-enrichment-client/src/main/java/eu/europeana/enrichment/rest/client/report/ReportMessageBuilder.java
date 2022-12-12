package eu.europeana.enrichment.rest.client.report;

import eu.europeana.enrichment.rest.client.EnrichmentWorker.Mode;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;

/**
 * {@code ReportMessage} builder static class.
 */
public final class ReportMessageBuilder {

  private HttpStatus status;
  private Mode mode;
  private Type messageType;
  private String value = "";
  private String message = "";
  private String stackTrace = "";

  public ReportMessageBuilder() {
    // for builder
  }

  /**
   * Create a report message for enrichment with ok status and ignore type
   *
   * @return a reference to this Builder
   */
  public static ReportMessageBuilder buildEnrichmentIgnore() {
    return new ReportMessageBuilder().withMode(Mode.ENRICHMENT).withStatus(HttpStatus.OK)
                                     .withMessageType(Type.IGNORE);
  }

  /**
   * Create a report message for enrichment with warn type
   *
   * @return a reference to this Builder
   */
  public static ReportMessageBuilder buildEnrichmentWarn() {
    return new ReportMessageBuilder().withMode(Mode.ENRICHMENT)
                                     .withMessageType(Type.WARN);
  }

  /**
   * Create a report message for enrichment with error status and error type
   *
   * @return a reference to this Builder
   */
  public static ReportMessageBuilder buildEnrichmentError() {
    return new ReportMessageBuilder().withMode(Mode.ENRICHMENT).withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                                     .withMessageType(Type.ERROR);
  }

  /**
   * Create a report message for dereference with ok status and ignore type
   *
   * @return a reference to this Builder
   */
  public static ReportMessageBuilder buildDereferenceIgnore() {
    return new ReportMessageBuilder().withMode(Mode.DEREFERENCE).withStatus(HttpStatus.OK)
                                     .withMessageType(Type.IGNORE);
  }

  /**
   * Create a report message for dereference with warn type
   *
   * @return a reference to this Builder
   */
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
  public ReportMessageBuilder withException(Throwable val) {
    message = ExceptionUtils.getMessage(val);
    stackTrace = ExceptionUtils.getStackTrace(val);
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
}
