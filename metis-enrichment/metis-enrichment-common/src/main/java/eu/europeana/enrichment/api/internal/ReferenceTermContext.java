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
    this.fieldTypes = Set.copyOf(fieldTypes);
  }

  @Override
  public List<EntityType> getCandidateTypes() {
    return fieldTypes.stream().map(FieldType::getEntityType).collect(Collectors.toList());
  }

  @Override
  public boolean equals(Object other) {

    if(!super.equals(other)){
      return false;
    }

    if(getClass() != other.getClass()){
      return false;
    }

    ReferenceTermContext o = (ReferenceTermContext) other;

    return Objects.equals(o.getCandidateTypes(), this.getCandidateTypes());
  }

  @Override
  public int hashCode(){
    return Objects.hash(this.getReference(), fieldTypes);
  }
}
