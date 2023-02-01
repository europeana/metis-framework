package eu.europeana.enrichment.rest.client.dereference;

import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.internal.ReferenceTerm;
import eu.europeana.enrichment.rest.client.report.Report;
import eu.europeana.metis.schema.jibx.AboutType;

import java.util.*;

/**
 * Dereferenced Entity
 */
public class DereferencedEntities {

  private final Map<ReferenceTerm, List<EnrichmentBase>> referenceTermListMap;
  private final Set<Report> reports;
  Class<? extends AboutType> classType;

  /**
   * Constructor with an enrichment base a report of messages
   *
   * @param referenceTermListMap enrichment base list mapped to a referenceterm
   * @param reports report messages
   */
  public DereferencedEntities(Map<ReferenceTerm, List<EnrichmentBase>> referenceTermListMap, Set<Report> reports) {
    this(referenceTermListMap, reports, null);
  }

  /**
   * Constructor with an enrichment base a report of messages
   *
   * @param referenceTermListMap enrichment base list mapped to a referenceterm
   * @param reports report messages
   */
  public DereferencedEntities(Map<ReferenceTerm, List<EnrichmentBase>> referenceTermListMap, Set<Report> reports,
                              Class<? extends AboutType> classType) {
    this.referenceTermListMap = new HashMap<>(referenceTermListMap);
    this.reports = new HashSet<>(reports);
    this.classType = classType;
  }

  public Map<ReferenceTerm, List<EnrichmentBase>> getReferenceTermListMap() {
    return referenceTermListMap;
  }

  public Set<Report> getReportMessages() {
    return reports;
  }

  public Class<? extends AboutType> getClassType() {
    return classType;
  }
}
