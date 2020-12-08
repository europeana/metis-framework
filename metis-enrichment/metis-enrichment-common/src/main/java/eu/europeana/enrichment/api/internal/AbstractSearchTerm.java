package eu.europeana.enrichment.api.internal;

import eu.europeana.enrichment.utils.EntityType;
import eu.europeana.metis.schema.jibx.LanguageCodes;
import java.util.List;
import java.util.Objects;

public abstract class AbstractSearchTerm implements SearchTerm {

  private String textValue;
  private LanguageCodes language;

  public AbstractSearchTerm(String textValue, String language){
    this.textValue = textValue;
    this.language = LanguageCodes.convert(language);
  }

  public abstract List<EntityType> getCandidateTypes();

  @Override
  public abstract boolean equals(Object other);

  @Override
  public int hashCode(){
    return Objects.hash(textValue, language);
  }

  public String getTextValue(){
    return textValue;
  }

  public String getLanguage(){
    return language != null ? language.toString() : null;
  }
}
