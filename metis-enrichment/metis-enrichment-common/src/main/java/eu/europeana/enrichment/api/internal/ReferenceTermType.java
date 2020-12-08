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
  public boolean equals(Object other) {
    if(other == this){
      return true;
    }

    if(!(other instanceof ReferenceTermType)){
      return false;
    }

    ReferenceTermType o = (ReferenceTermType) other;

    boolean hasSameReference = Objects.equals(o.getReference(), this.getReference());
    boolean hasSameFieldType = Objects.equals(o.getCandidateTypes(), this.getCandidateTypes());

    return hasSameReference && hasSameFieldType;
  }

}
