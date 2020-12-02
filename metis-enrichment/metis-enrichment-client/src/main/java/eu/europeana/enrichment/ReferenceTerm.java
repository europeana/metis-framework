package eu.europeana.enrichment;

import eu.europeana.enrichment.utils.EntityType;
import java.net.URL;
import java.util.List;

public interface ReferenceTerm {

  List<EntityType> getFieldType();

  boolean equals(ReferenceTerm referenceTerm);

  int hashCode();

  URL getReference();
}
