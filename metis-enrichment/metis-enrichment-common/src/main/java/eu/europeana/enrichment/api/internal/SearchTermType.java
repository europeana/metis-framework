package eu.europeana.enrichment.api.internal;

import eu.europeana.enrichment.utils.EntityType;
import java.util.List;
import java.util.Objects;

public class SearchTermType extends AbstractSearchTerm{

  private List<EntityType> entityTypes;

  public SearchTermType(String textValue, String language, List<EntityType> entityTypes) {
    super(textValue, language);
    this.entityTypes = entityTypes;
  }

  @Override
  public List<EntityType> getCandidateTypes() {
    return entityTypes;
  }

  @Override
  public boolean equals(Object other) {
    if(other == this){
      return true;
    }

    if(!(other instanceof SearchTermType)){
      return false;
    }

    SearchTermType o = (SearchTermType) other;


    boolean hasSameTextValues = Objects.equals(o.getTextValue(), this.getTextValue());
    boolean hasSameLanguage = Objects.equals(o.getLanguage(), this.getLanguage());
    boolean hasSameFieldType = Objects.equals(o.getCandidateTypes(), this.getCandidateTypes());

    return hasSameTextValues && hasSameLanguage && hasSameFieldType;
  }

}
