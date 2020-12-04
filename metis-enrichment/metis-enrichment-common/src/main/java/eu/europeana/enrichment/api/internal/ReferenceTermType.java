package eu.europeana.enrichment.api.internal;

import eu.europeana.enrichment.utils.EntityType;
import java.net.URL;
import java.util.List;
import java.util.Objects;


public class ReferenceTermType extends AbstractReferenceTerm{

  private List<EntityType> entityTypes;

  public ReferenceTermType(URL reference, List<EntityType> entityTypes) {
    super(reference);
    this.entityTypes = entityTypes;
  }

  @Override
  public List<EntityType> getCandidateTypes() {
    return entityTypes;
  }

  @Override
  public boolean equals(ReferenceTerm referenceTerm) {
    if(referenceTerm == this){
      return true;
    }

    if(!(referenceTerm instanceof ReferenceTermType)){
      return false;
    }

    ReferenceTermType other = (ReferenceTermType) referenceTerm;

    boolean hasSameReference = Objects.equals(other.getReference(), this.getReference());
    boolean hasSameFieldType = Objects.equals(other.getCandidateTypes(), this.getCandidateTypes());

    return hasSameReference && hasSameFieldType;
  }

  @Override
  public int hashCode(){
    return Objects.hash(this.getReference(), entityTypes);
  }
}
