package eu.europeana.enrichment.api.internal;

import eu.europeana.enrichment.utils.EntityType;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ReferenceTermContext extends AbstractReferenceTerm {

  private final Set<FieldType> fieldTypes;

  public ReferenceTermContext(URL reference, Set<FieldType> fieldTypes) {
    super(reference);
    this.fieldTypes = fieldTypes;
  }

  @Override
  public List<EntityType> getCandidateTypes() {
    return fieldTypes.stream().map(FieldType::getEntityType).collect(Collectors.toList());
  }

  @Override
  public boolean equals(Object other) {

    if(other == this){
      return true;
    }

    if(!(other instanceof ReferenceTermContext)){
      return false;
    }

    ReferenceTermContext o = (ReferenceTermContext) other;

    boolean hasSameReference = Objects.equals(o.getReference(), this.getReference());
    boolean hasSameFieldType = Objects.equals(o.getCandidateTypes(), this.getCandidateTypes());

    return hasSameReference && hasSameFieldType;
  }
}
