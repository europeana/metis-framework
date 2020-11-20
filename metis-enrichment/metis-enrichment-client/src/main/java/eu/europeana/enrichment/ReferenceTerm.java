package eu.europeana.enrichment;

import eu.europeana.enrichment.utils.EntityType;
import java.net.URL;
import java.util.List;

public abstract class ReferenceTerm {

  URL reference;

  public abstract List<EntityType> getCandidateTypes();

  public abstract boolean equals(ReferenceTerm referenceTerm);

  public abstract int hashCode();

}
