package eu.europeana.enrichment;

import eu.europeana.enrichment.internal.model.Entity;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RemoteEntityResolver implements EntityResolver{

  private URL enrichmentServiceUrl;

  @Override
  public Map<SearchTerm, List<Entity>> resolveByText(Set<SearchTerm> searchTermSet) {
    return null;
  }

  @Override
  public Map<ReferenceTerm, Entity> resolveById(Set<ReferenceTerm> referenceTermSet) {
    return null;
  }

  @Override
  public Map<ReferenceTerm, List<Entity>> resolveByUri(Set<ReferenceTerm> referenceTermSet) {
    return null;
  }
}
