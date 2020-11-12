package eu.europeana.enrichment.api.external;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import eu.europeana.enrichment.utils.EntityType;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.bind.annotation.XmlRootElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Enrichment input class wrapper. It defines the basics needed for enrichment as the value to be
 * enriched, the Controlled vocabulary to be used and the field (optional) from which the value
 * originated
 *
 * @author Joana Sousa (joana.sousa@europeana.eu)
 * @since 2020-11-02
 */
@XmlRootElement
@JsonInclude
public class SearchValue {

  private String value;

  private String language;

  private Set<EntityType> entityTypes;

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
    this.entityTypes = Set.of(entityTypes);
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

  public Set<EntityType> getEntityTypes() {
    return new HashSet<>(entityTypes);
  }

  public void setEntityTypes(Set<EntityType> entityTypes) {
    this.entityTypes = new HashSet<>(entityTypes);
  }
}
