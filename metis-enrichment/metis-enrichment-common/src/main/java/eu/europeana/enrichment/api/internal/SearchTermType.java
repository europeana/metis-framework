package eu.europeana.enrichment.api.internal;

import eu.europeana.enrichment.utils.EntityType;
import eu.europeana.metis.schema.jibx.LanguageCodes;
import java.util.List;
import java.util.Objects;

public class SearchTermType extends AbstractSearchTerm{

  private List<EntityType> entityTypes;

  public SearchTermType(String textValue, LanguageCodes language, List<EntityType> entityTypes) {
    super(textValue, language);
    this.entityTypes = entityTypes;
  }

  @Override
  public List<EntityType> getCandidateTypes() {
    return entityTypes;
  }

  @Override
  public boolean equals(SearchTerm searchTerm) {
    if(searchTerm == this){
      return true;
    }

    if(!(searchTerm instanceof SearchTermType)){
      return false;
    }

    SearchTermType other = (SearchTermType) searchTerm;


    boolean hasSameTextValues = Objects.equals(other.getTextValue(), this.getTextValue());
    boolean hasSameLanguage = Objects.equals(other.getLanguage(), this.getLanguage());
    boolean hasSameFieldType = Objects.equals(other.getCandidateTypes(), this.getCandidateTypes());

    return hasSameTextValues && hasSameLanguage && hasSameFieldType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.getTextValue(), this.getLanguage(), entityTypes);
  }
}
