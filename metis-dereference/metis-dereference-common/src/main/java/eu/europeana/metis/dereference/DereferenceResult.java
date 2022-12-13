package eu.europeana.metis.dereference;

import eu.europeana.enrichment.api.external.DereferenceResultStatus;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DereferenceResult {

  private Collection<EnrichmentBase> enrichmentBases;
  private DereferenceResultStatus dereferenceStatus;

  /**
   * Constructor to return an enrichment base collection and status of dereference process
   *
   * @param enrichmentBases collection of enrichment base
   * @param dereferenceStatus status of dereference
   */
  public DereferenceResult(Collection<EnrichmentBase> enrichmentBases, DereferenceResultStatus dereferenceStatus) {
    this.enrichmentBases = new ArrayList<>(enrichmentBases);
    this.dereferenceStatus = dereferenceStatus;
  }

  public List<EnrichmentBase> getEnrichmentBasesAsList() {
    return (List) this.enrichmentBases;
  }

  public DereferenceResultStatus getDereferenceStatus() {
    return dereferenceStatus;
  }
}
