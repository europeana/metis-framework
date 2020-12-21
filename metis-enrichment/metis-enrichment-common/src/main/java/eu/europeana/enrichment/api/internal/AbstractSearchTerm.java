package eu.europeana.enrichment.api.internal;

import eu.europeana.metis.schema.jibx.LanguageCodes;
import java.util.Objects;

public abstract class AbstractSearchTerm implements SearchTerm {

  private final String textValue;
  private final LanguageCodes language;

  public AbstractSearchTerm(String textValue, String language){
    this.textValue = textValue;
    this.language = LanguageCodes.convert(language);
  }

  @Override
  public boolean equals(Object other){
    if(other == this){
      return true;
    }

    if(other == null || getClass() != other.getClass()){
      return false;
    }

    AbstractSearchTerm o = (AbstractSearchTerm) other;

    boolean hasSameTextValues = Objects.equals(o.getTextValue(), this.getTextValue());
    boolean hasSameLanguage = Objects.equals(o.getLanguage(), this.getLanguage());

    return hasSameTextValues && hasSameLanguage;
  }

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
