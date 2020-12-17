package eu.europeana.enrichment.api.internal;

import eu.europeana.enrichment.utils.EntityType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class SearchTermType extends AbstractSearchTerm{

  private final Set<EntityType> entityTypes;

  public SearchTermType(String textValue, String language, Set<EntityType> entityTypes) {
    super(textValue, language);
    this.entityTypes = Set.copyOf(entityTypes);
  }

  @Override
  public List<EntityType> getCandidateTypes() {
    return new ArrayList<>(entityTypes);
  }

  @Override
  public boolean equals(Object other) {

    if(!super.equals(other)){
      return false;
    }

    if(getClass() != other.getClass()){
      return false;
    }

    SearchTermType o = (SearchTermType) other;

    return Objects.equals(o.getCandidateTypes(), this.getCandidateTypes());
  }

  @Override
  public int hashCode(){
    return Objects.hash(getTextValue(), getLanguage(), entityTypes);
  }

}
