package eu.europeana.enrichment.api.internal;

import eu.europeana.enrichment.utils.EntityType;
import java.net.URL;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is an implementation of {@link ReferenceTerm} that provides context in the sense that it is
 * aware of the field type(s) in which the reference term was found.
 */
public class ReferenceTermContext extends AbstractReferenceTerm {

  private final Set<FieldType> fieldTypes;

  public ReferenceTermContext(URL reference, Set<FieldType> fieldTypes) {
    super(reference);
    this.fieldTypes = Set.copyOf(fieldTypes);
  }

  @Override
  public Set<EntityType> getCandidateTypes() {
    return fieldTypes.stream().map(FieldType::getEntityType).collect(Collectors.toSet());
  }

  public Set<FieldType> getFieldTypes() {
    return Collections.unmodifiableSet(fieldTypes);
  }
}
