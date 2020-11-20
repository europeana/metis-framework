package eu.europeana.enrichment;

import eu.europeana.enrichment.internal.model.Entity;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface EntityResolver {

  Map<SearchTerm, List<Entity>> resolveByText(Set<SearchTerm> searchTermSet);

  Map<ReferenceTerm, Entity> resolveById(Set<ReferenceTerm> referenceTermSet);

  Map<ReferenceTerm, List<Entity>> resolveByUri(Set<ReferenceTerm> referenceTermSet);

}
