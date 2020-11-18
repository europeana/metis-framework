package eu.europeana.enrichment;

import eu.europeana.enrichment.api.external.ReferenceValue;
import eu.europeana.enrichment.api.external.SearchValue;
import eu.europeana.enrichment.internal.model.Entity;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RemoteEntityResolver implements EntityResolver{

  private URL enrichmentServiceUrl;

  @Override
  public Map<SearchValue, List<Entity>> resolveByText(Set<SearchValue> searchValues) {
    return null;
  }

  @Override
  public Map<ReferenceValue, Entity> resolveById(Set<ReferenceValue> referenceValues) {
    return null;
  }

  @Override
  public Map<ReferenceValue, List<Entity>> resolveByUri(Set<ReferenceValue> referenceValues) {
    return null;
  }
}
