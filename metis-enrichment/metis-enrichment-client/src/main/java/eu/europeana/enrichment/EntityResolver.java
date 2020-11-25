package eu.europeana.enrichment;

import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface EntityResolver {

  Map<SearchTerm, List<EnrichmentBase>> resolveByText(Set<SearchTerm> searchTermSet);

  Map<ReferenceTerm, EnrichmentBase> resolveById(Set<ReferenceTerm> referenceTermSet);

  Map<ReferenceTerm, List<EnrichmentBase>> resolveByUri(Set<ReferenceTerm> referenceTermSet);

}
