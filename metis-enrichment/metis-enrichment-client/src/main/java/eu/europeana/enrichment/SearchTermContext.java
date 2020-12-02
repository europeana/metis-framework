package eu.europeana.enrichment;

import eu.europeana.enrichment.utils.EntityType;
import eu.europeana.enrichment.utils.FieldType;
import eu.europeana.metis.schema.jibx.LanguageCodes;
import java.util.List;
import java.util.Objects;

public class SearchTermContext extends AbstractSearchTerm {

  private final FieldType fieldType;

  public SearchTermContext(String textValue, LanguageCodes language, FieldType fieldType) {
    super(textValue, language);
    this.fieldType = fieldType;
  }

  @Override
  public List<EntityType> getFieldType() {
    return List.of(fieldType.getEntityType());
  }

  @Override
  public boolean equals(SearchTerm searchTerm) {
    if(searchTerm == this){
      return true;
    }

    if(!(searchTerm instanceof SearchTermContext)){
      return false;
    }

    SearchTermContext other = (SearchTermContext) searchTerm;

    boolean hasSameTextValues = Objects.equals(other.getTextValue(), this.getTextValue());
    boolean hasSameLanguage = Objects.equals(other.getLanguage(), this.getLanguage());
    boolean hasSameFieldType = Objects.equals(other.getFieldType(), this.getFieldType());

    return hasSameTextValues && hasSameLanguage && hasSameFieldType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.getTextValue(), this.getLanguage(), fieldType);
  }
}
