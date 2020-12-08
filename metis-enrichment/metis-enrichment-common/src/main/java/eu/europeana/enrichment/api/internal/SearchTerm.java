package eu.europeana.enrichment.api.internal;

import eu.europeana.enrichment.utils.EntityType;
import eu.europeana.metis.schema.jibx.LanguageCodes;
import java.util.List;

public interface SearchTerm {

  List<EntityType> getCandidateTypes();

  @Override
  boolean equals(Object other);

  @Override
  int hashCode();

  String getTextValue();

  String getLanguage();

}
