package eu.europeana.enrichment;

import eu.europeana.enrichment.utils.EntityType;
import eu.europeana.enrichment.utils.FieldType;
import java.net.URL;
import java.util.List;

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
    return false;
  }

  @Override
  public int hashCode() {
    return 0;
  }
}
