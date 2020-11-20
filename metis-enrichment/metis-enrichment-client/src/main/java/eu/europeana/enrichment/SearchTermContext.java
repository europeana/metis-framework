package eu.europeana.enrichment;

import eu.europeana.enrichment.utils.EntityType;
import java.util.List;

public class SearchTermContext extends SearchTerm{

  @Override
  public List<EntityType> getCandidateTypes() {
    return null;
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
