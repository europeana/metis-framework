package eu.europeana.enrichment;

import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.service.dao.EnrichmentDao;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PersistentEntityResolver implements  EntityResolver{

  EnrichmentDao enrichmentDao;


  @Override
  public Map<SearchTerm, List<EnrichmentBase>> resolveByText(Set<SearchTerm> searchTermSet) {
    return null;
  }

  @Override
  public Map<ReferenceTerm, EnrichmentBase> resolveById(Set<ReferenceTerm> referenceTermSet) {
    return null;
  }

  @Override
  public Map<ReferenceTerm, List<EnrichmentBase>> resolveByUri(Set<ReferenceTerm> referenceTermSet) {
    return null;
  }
}
