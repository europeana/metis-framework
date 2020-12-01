package eu.europeana.enrichment;

import eu.europeana.enrichment.utils.EntityType;
import eu.europeana.enrichment.utils.FieldType;
import eu.europeana.metis.schema.jibx.LanguageCodes;
import java.util.List;

public class SearchTermContext extends SearchTerm{

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
    return false;
  }

  @Override
  public int hashCode() {
    return 0;
  }
}
