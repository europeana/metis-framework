package eu.europeana.patternanalysis.view;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.ArrayList;
import java.util.List;

/**
 * Class containing the problem occurrence report.
 * <p>It also contains {@link #affectedRecordIds} which indicate other records are part of this problem with this record and
 * problem. It can be null if the problem is only related to the current record.</p>
 */
public class ProblemOccurrence {

  private final String messageReport;
  @JsonInclude(Include.NON_NULL)
  private final List<String> affectedRecordIds;

  /**
   * Constructor with required parameters.
   *
   * @param message the problem message
   * @param affectedRecordIds the affected record ids. Can be null if the problem spans only to the current record.
   */
  public ProblemOccurrence(String message, List<String> affectedRecordIds) {
    this.messageReport = message;
    this.affectedRecordIds = affectedRecordIds == null ? new ArrayList<>() : new ArrayList<>(affectedRecordIds);
  }

  /**
   * Constructor with required parameters.
   *
   * @param messageReport the message report
   */
  public ProblemOccurrence(String messageReport) {
    this.messageReport = messageReport;
    this.affectedRecordIds = new ArrayList<>();
  }

  public String getMessageReport() {
    return messageReport;
  }

  public List<String> getAffectedRecordIds() {
    return new ArrayList<>(affectedRecordIds);
  }
}
