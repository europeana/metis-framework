package eu.europeana.enrichment.api.internal;

import eu.europeana.enrichment.utils.EntityType;
import java.net.URL;
import java.util.List;

public interface ReferenceTerm {

  List<EntityType> getCandidateTypes();

  boolean equals(ReferenceTerm referenceTerm);

  int hashCode();

  URL getReference();
}
