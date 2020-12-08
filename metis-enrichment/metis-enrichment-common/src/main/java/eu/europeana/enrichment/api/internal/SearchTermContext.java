package eu.europeana.enrichment.api.internal;

import eu.europeana.enrichment.utils.EntityType;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class SearchTermContext extends AbstractSearchTerm {

  private final Set<FieldType> fieldTypes;

  public SearchTermContext(String textValue, String language, Set<FieldType> fieldTypes) {
    super(textValue, language);
    this.fieldTypes = fieldTypes;
  }

  @Override
  public List<EntityType> getCandidateTypes() {
    return fieldTypes.stream().map(FieldType::getEntityType).collect(Collectors.toList());
  }

  public Set<FieldType> getFieldTypes() {
    return fieldTypes;
  }

  @Override
  public boolean equals(Object other) {
    if(other == this){
      return true;
    }

    if(!(other instanceof SearchTermContext)){
      return false;
    }

    SearchTermContext o = (SearchTermContext) other;

    boolean hasSameTextValues = Objects.equals(o.getTextValue(), this.getTextValue());
    boolean hasSameLanguage = Objects.equals(o.getLanguage(), this.getLanguage());
    boolean hasSameFieldType = Objects.equals(o.getCandidateTypes(), this.getCandidateTypes());

    return hasSameTextValues && hasSameLanguage && hasSameFieldType;
  }

}
