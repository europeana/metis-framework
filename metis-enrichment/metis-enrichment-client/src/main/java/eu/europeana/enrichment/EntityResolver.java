package eu.europeana.enrichment;

import eu.europeana.enrichment.api.external.ReferenceValue;
import eu.europeana.enrichment.api.external.SearchValue;
import eu.europeana.enrichment.internal.model.Entity;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface EntityResolver {

  Map<SearchValue, List<Entity>> resolveByText(Set<SearchValue> searchValues);

  Map<ReferenceValue, Entity> resolveById(Set<ReferenceValue> referenceValues);

  Map<ReferenceValue, List<Entity>> resolveByUri(Set<ReferenceValue> referenceValues);

}
