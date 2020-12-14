package eu.europeana.enrichment.api.internal;

import eu.europeana.enrichment.utils.EntityType;
import java.util.List;

public interface SearchTerm {

  List<EntityType> getCandidateTypes();

  String getTextValue();

  String getLanguage();

}
