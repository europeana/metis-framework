package eu.europeana.enrichment.rest.client.report;

import eu.europeana.enrichment.rest.client.EnrichmentWorker.Mode;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;

/**
 * ReportMessage class used to inform processed results
 */
public final class ReportMessage {

  private static final int MAX_COMPARE_STACK_TRACE = 50;
  private final HttpStatus status;
  private final Mode mode;
  private final Type messageType;
  private final String value;
  private final String message;
  private final String stackTrace;

  ReportMessage(ReportMessageBuilder reportMessageBuilder) {
    status = reportMessageBuilder.getStatus();
    mode = reportMessageBuilder.getMode();
    messageType = reportMessageBuilder.getMessageType();
    value = reportMessageBuilder.getValue();
    message = reportMessageBuilder.getMessage();
    stackTrace = reportMessageBuilder.getStackTrace();
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

  @Override
  public String toString() {
    return "[" + status + "," + mode.name() + "," + messageType.name() + "," + value + "," + message + "," + stackTrace + "]\n";
  }
}
