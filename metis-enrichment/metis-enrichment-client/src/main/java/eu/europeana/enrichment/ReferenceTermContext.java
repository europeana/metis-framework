package eu.europeana.enrichment;

import eu.europeana.enrichment.utils.EntityType;
import java.net.URL;
import java.util.List;

public class ReferenceTermContext extends ReferenceTerm{

  private FieldType candidateTypes;

  ReferenceTermContext(URL reference) {
    super(reference);
  }

  @Override
  public List<EntityType> getCandidateTypes() {
    return candidateTypes.getCandidateTypes();
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
