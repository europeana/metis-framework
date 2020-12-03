package eu.europeana.enrichment.api.internal;

import eu.europeana.enrichment.utils.EntityType;
import eu.europeana.metis.schema.jibx.LanguageCodes;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class SearchTermContext extends AbstractSearchTerm {

  private final Set<FieldType> fieldTypes;

  public SearchTermContext(String textValue, LanguageCodes language, Set<FieldType> fieldTypes) {
    super(textValue, language);
    this.fieldTypes = fieldTypes;
  }

  @Override
  public List<EntityType> getCandidateTypes() {
    return fieldTypes.stream().map(FieldType::getEntityType).collect(Collectors.toList());
  }

  @Override
  public boolean equals(SearchTerm searchTerm) {
    if(searchTerm == this){
      return true;
    }

//    if(!(searchTerm instanceof SearchTermContext)){
//      return false;
//    }

    SearchTermContext other = (SearchTermContext) searchTerm;

    boolean hasSameTextValues = Objects.equals(other.getTextValue(), this.getTextValue());
    boolean hasSameLanguage = Objects.equals(other.getLanguage(), this.getLanguage());
    boolean hasSameFieldType = Objects.equals(other.getCandidateTypes(), this.getCandidateTypes());

    return hasSameTextValues && hasSameLanguage && hasSameFieldType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.getTextValue(), this.getLanguage(), fieldTypes);
  }
}
