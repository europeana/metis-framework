package eu.europeana.metis.dereference;

import eu.europeana.enrichment.api.external.DereferenceResultStatus;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DereferenceResult {
  private Collection<EnrichmentBase> enrichmentBases;
  private DereferenceResultStatus dereferenceStatus;

  public DereferenceResult(Collection<EnrichmentBase> enrichmentBases, DereferenceResultStatus dereferenceStatus) {
    this.enrichmentBases = enrichmentBases;
    this.dereferenceStatus = dereferenceStatus;
  }

  public DereferenceResult(DereferenceResult dereferenceResult) {
    this.dereferenceStatus = dereferenceResult.getDereferenceStatus();
    this.enrichmentBases = dereferenceResult.getEnrichmentBasesAsCollection();
  }
  public Collection<EnrichmentBase> getEnrichmentBasesAsCollection() {
    return enrichmentBases;
  }
  public List<EnrichmentBase> getEnrichmentBasesAsList() {
    return new ArrayList<>(enrichmentBases);
  }

  public DereferenceResultStatus getDereferenceStatus() {
    return dereferenceStatus;
  }
}
