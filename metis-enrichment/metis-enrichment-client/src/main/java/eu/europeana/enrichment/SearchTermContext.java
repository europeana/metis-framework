package eu.europeana.enrichment;

import eu.europeana.enrichment.utils.EntityType;
import eu.europeana.metis.schema.jibx.LanguageCodes;
import java.util.List;

public class SearchTermContext extends SearchTerm{

  private FieldType candidateTypes;

  public SearchTermContext(String textValue, LanguageCodes language) {
    super(textValue, language);
  }

  @Override
  public List<EntityType> getCandidateTypes() {
    return candidateTypes.getCandidateTypes();
  }

  @Override
  public boolean equals(SearchTerm searchTerm) {
    return false;
  }

  @Override
  public int hashCode() {
    return 0;
  }
}
