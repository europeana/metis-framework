package eu.europeana.metis.dereference;

import eu.europeana.enrichment.api.external.DereferenceResultStatus;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import java.util.ArrayList;
import java.util.List;

/**
 * DereferenceResult contains the final result and status of dereferencing process
 */
public class DereferenceResult {

  private final List<EnrichmentBase> enrichmentBases;
  private final DereferenceResultStatus dereferenceStatus;

  /**
   * Constructor to return an enrichment base collection and status of dereference process
   *
   * @param enrichmentBases collection of enrichment base
   * @param dereferenceStatus status of dereference
   */
  public DereferenceResult(List<EnrichmentBase> enrichmentBases, DereferenceResultStatus dereferenceStatus) {
    this.enrichmentBases = new ArrayList<>(enrichmentBases);
    this.dereferenceStatus = dereferenceStatus;
  }

  public List<EnrichmentBase> getEnrichmentBasesAsList() {
    return enrichmentBases;
  }

  public DereferenceResultStatus getDereferenceStatus() {
    return dereferenceStatus;
  }
}
