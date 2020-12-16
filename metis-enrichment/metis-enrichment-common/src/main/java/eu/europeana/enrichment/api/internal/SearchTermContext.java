package eu.europeana.enrichment.api.internal;

import eu.europeana.enrichment.utils.EntityType;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class SearchTermContext extends AbstractSearchTerm {

  private final Set<FieldType> fieldTypes;

  public SearchTermContext(String textValue, String language, Set<FieldType> fieldTypes) {
    super(textValue, language);
    this.fieldTypes = Set.copyOf(fieldTypes);
  }

  @Override
  public List<EntityType> getCandidateTypes() {
    return fieldTypes.stream().map(FieldType::getEntityType).collect(Collectors.toList());
  }

  public Set<FieldType> getFieldTypes() {
    return new HashSet<>(fieldTypes);
  }

  @Override
  public boolean equals(Object other) {

    if(!super.equals(other)){
      return false;
    }

    SearchTermContext o = (SearchTermContext) other;

    return Objects.equals(o.getCandidateTypes(), this.getCandidateTypes());
  }

  @Override
  public int hashCode(){
    return Objects.hash(getTextValue(), getLanguage(), fieldTypes);
  }

}
