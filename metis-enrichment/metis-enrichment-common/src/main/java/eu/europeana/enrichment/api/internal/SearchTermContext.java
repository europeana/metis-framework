package eu.europeana.enrichment.api.internal;

import eu.europeana.enrichment.utils.EntityType;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is an implementation of {@link SearchTerm} that provides context in the sense that it is
 * aware of the field type(s) in which the search term was found.
 */
public class SearchTermContext extends AbstractSearchTerm {

  private final Set<FieldType> fieldTypes;

  public SearchTermContext(String textValue, String language, Set<FieldType> fieldTypes) {
    super(textValue, language);
    this.fieldTypes = Set.copyOf(fieldTypes);
  }

  @Override
  public Set<EntityType> getCandidateTypes() {
    return fieldTypes.stream().map(FieldType::getEntityType).collect(Collectors.toSet());
  }

  public Set<FieldType> getFieldTypes() {
    return Collections.unmodifiableSet(fieldTypes);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final SearchTermContext that = (SearchTermContext) o;
    return Objects.equals(getTextValue(), that.getTextValue()) &&
            Objects.equals(getLanguage(), that.getLanguage()) &&
            Objects.equals(getFieldTypes(), that.getFieldTypes());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getTextValue(), getLanguage(), getFieldTypes());
  }
}
