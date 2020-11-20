package eu.europeana.enrichment;

import eu.europeana.enrichment.utils.EntityType;
import eu.europeana.metis.schema.jibx.LanguageCodes;
import java.util.List;

public abstract class SearchTerm {

  private String textValue;
  private LanguageCodes language;

  public abstract List<EntityType> getCandidateTypes();

  public abstract boolean equals(SearchTerm searchTerm);

  public abstract int hashCode();
}
