package eu.europeana.enrichment.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Arrays;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Enrichment input class wrapper. It defines the basics needed for enrichment as the value to be
 * enriched, the Controlled vocabulary to be used and the field (optional) from which the value
 * originated
 *
 * @author Joana Sousa
 */
@XmlRootElement
@JsonInclude
public class SearchValue {

  private String value;

  private String language;

  private List<EntityType> entityTypes;

  public SearchValue(){
  }

  /**
   * Constructor with all possible fields provided for enrichment search.
   *
   * @param value the value to be enriched
   * @param language the language to use for enrichment of the value
   * @param entityTypes the vocabularies that this value represents
   */
  public SearchValue(String value, String language, EntityType... entityTypes) {
    this.value = value;
    this.language = language;
    this.entityTypes = Arrays.asList(entityTypes);
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public List<EntityType> getEntityTypes() {
    return entityTypes;
  }

  public void setEntityTypes(List<EntityType> entityTypes) {
    this.entityTypes = entityTypes;
  }
}
