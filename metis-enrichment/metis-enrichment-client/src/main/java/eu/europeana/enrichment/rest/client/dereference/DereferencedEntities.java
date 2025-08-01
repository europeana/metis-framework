package eu.europeana.enrichment.rest.client.dereference;

import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.internal.ReferenceTerm;
import eu.europeana.enrichment.rest.client.report.Report;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Dereferenced Entity
 */
public class DereferencedEntities {

  private final Map<ReferenceTerm, List<EnrichmentBase>> referenceTermListMap;
  private final Set<Report> reports;

  /**
   * Constructor with an enrichment base a report of messages
   *
   * @param referenceTermListMap enrichment base list mapped to a referenceterm
   * @param reports report messages
   */
  public DereferencedEntities(Map<ReferenceTerm, List<EnrichmentBase>> referenceTermListMap, Set<Report> reports) {
    this.referenceTermListMap = new HashMap<>(referenceTermListMap);
    this.reports = new HashSet<>(reports);
  }

  /**
   * Creates an instance of this object without content.
   * @return An empty instance.
   */
  public static DereferencedEntities emptyInstance() {
    return new DereferencedEntities(new HashMap<>(), new HashSet<>());
  }

  /**
   * Merge the other entities into this one.
   * @param otherEntities the other entities to add.
   */
  public void addAll(DereferencedEntities otherEntities) {
    this.referenceTermListMap.putAll(otherEntities.getReferenceTermListMap());
    this.reports.addAll(otherEntities.getReportMessages());
  }

  public Map<ReferenceTerm, List<EnrichmentBase>> getReferenceTermListMap() {
    return Collections.unmodifiableMap(referenceTermListMap);
  }

  public Set<Report> getReportMessages() {
    return Collections.unmodifiableSet(reports);
  }
}
