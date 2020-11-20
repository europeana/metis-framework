package eu.europeana.enrichment;

import eu.europeana.enrichment.utils.EntityType;
import java.util.List;

public class ReferenceTermContext extends ReferenceTerm{

  @Override
  public List<EntityType> getCandidateTypes() {
    return null;
  }

  @Override
  public boolean equals(ReferenceTerm referenceTerm) {
    return false;
  }

  @Override
  public int hashCode() {
    return 0;
  }
}
