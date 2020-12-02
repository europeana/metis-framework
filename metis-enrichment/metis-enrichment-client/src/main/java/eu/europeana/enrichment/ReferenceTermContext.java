package eu.europeana.enrichment;

import eu.europeana.enrichment.utils.EntityType;
import eu.europeana.enrichment.utils.FieldType;
import java.net.URL;
import java.util.List;
import java.util.Objects;

public class ReferenceTermContext extends ReferenceTerm{

  private final FieldType fieldType;

  ReferenceTermContext(URL reference, FieldType fieldType) {
    super(reference);
    this.fieldType = fieldType;
  }

  @Override
  public List<EntityType> getFieldType() {
    return List.of(fieldType.getEntityType());
  }

  @Override
  public boolean equals(ReferenceTerm referenceTerm) {

    if(referenceTerm == this){
      return true;
    }

    if(!(referenceTerm instanceof ReferenceTermContext)){
      return false;
    }

    ReferenceTermContext other = (ReferenceTermContext) referenceTerm;

    boolean hasSameReference = Objects.equals(other.getReference(), this.getReference());
    boolean hasSameFieldType = Objects.equals(other.getFieldType(), this.getFieldType());

    return hasSameReference && hasSameFieldType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.getReference(), fieldType);
  }
}
