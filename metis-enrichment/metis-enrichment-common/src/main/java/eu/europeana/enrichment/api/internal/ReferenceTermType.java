package eu.europeana.enrichment.api.internal;

import eu.europeana.enrichment.utils.EntityType;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;


public class ReferenceTermType extends AbstractReferenceTerm{

  private final Set<EntityType> entityTypes;

  public ReferenceTermType(URL reference, Set<EntityType> entityTypes) {
    super(reference);
    this.entityTypes = Set.copyOf(entityTypes);
  }

  @Override
  public List<EntityType> getCandidateTypes() {
    return new ArrayList<>(entityTypes);
  }

  @Override
  public boolean equals(Object other) {

    if(!super.equals(other)){
      return false;
    }

    ReferenceTermType o = (ReferenceTermType) other;

    return Objects.equals(o.getCandidateTypes(), this.getCandidateTypes());
  }

  @Override
  public int hashCode(){
    return Objects.hash(this.getReference(), entityTypes);
  }

}
