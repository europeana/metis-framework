package eu.europeana.enrichment.rest.client.dereference;

import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.rest.client.report.Report;
import eu.europeana.metis.schema.jibx.AboutType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Dereferenced Entity
 */
public class DereferencedEntity {

  private final List<EnrichmentBase> enrichmentBaseList;
  private final Set<Report> reports;
  private final Class<? extends AboutType> classType;

  /**
   * Constructor with an enrichment base a report of messages
   *
   * @param enrichmentBaseList enrichment base list
   * @param reports report messages
   */
  public DereferencedEntity(List<EnrichmentBase> enrichmentBaseList, Set<Report> reports) {
    this(enrichmentBaseList, reports, null);
  }

  public DereferencedEntity(List<EnrichmentBase> enrichmentBaseList, Set<Report> reports, Class<? extends AboutType> classType){
    this.enrichmentBaseList = new ArrayList<>(enrichmentBaseList);
    this.reports = new HashSet<>(reports);
    this.classType = classType;
  }

  public List<EnrichmentBase> getEnrichmentBaseList() {
    return enrichmentBaseList;
  }

  public Set<Report> getReportMessages() {
    return reports;
  }

  public Class<? extends AboutType> getClassType(){
    return classType;
  }
}
