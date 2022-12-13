package eu.europeana.enrichment.rest.client.dereference;

import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.rest.client.report.ReportMessage;
import java.util.List;
import java.util.Set;

/**
 * Dereferenced Entity
 */
public class DereferencedEntity {

  private final List<EnrichmentBase> enrichmentBaseList;
  private final Set<ReportMessage> reportMessages;

  /**
   * Constructor with an enrichment base an report of messages
   *
   * @param enrichmentBaseList enrichment base list
   * @param reportMessages report messages
   */
  public DereferencedEntity(List<EnrichmentBase> enrichmentBaseList, Set<ReportMessage> reportMessages) {
    this.enrichmentBaseList = enrichmentBaseList;
    this.reportMessages = reportMessages;
  }

  public List<EnrichmentBase> getEnrichmentBaseList() {
    return enrichmentBaseList;
  }

  public Set<ReportMessage> getReportMessages() {
    return reportMessages;
  }
}
