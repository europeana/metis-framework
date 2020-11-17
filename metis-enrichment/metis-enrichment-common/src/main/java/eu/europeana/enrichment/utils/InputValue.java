package eu.europeana.enrichment.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Arrays;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Enrichment input class wrapper. It defines the basics needed for enrichment as the value to be
 * enriched, the Controlled vocabulary to be used and the field (optional) from which the value
 * originated
 *
 * @author Yorgos.Mamakis@ europeana.eu
 * @deprecated
 */
@XmlRootElement
@JsonInclude
@Deprecated(forRemoval = true)
public class InputValue {

  private String rdfFieldName;

  private String value;

  private String language;

  private List<EntityType> entityTypes;

  @Deprecated
  public InputValue() {
  }

  /**
   * Constructor with all possible fields provided for enrichment.
   *
   * @param rdfFieldName the rdf field name
   * @param value the value to be enriched
   * @param language the language to use for enrichment of the value
   * @param entityTypes the vocabularies that this value represents
   */
  @Deprecated
  public InputValue(String rdfFieldName, String value, String language, EntityType... entityTypes) {
    this.rdfFieldName = rdfFieldName;
    this.value = value;
    this.language = language;
    this.entityTypes = Arrays.asList(entityTypes);
  }

  @Deprecated
  public String getRdfFieldName() {
    return rdfFieldName;
  }

  @Deprecated
  public void setRdfFieldName(String rdfFieldName) {
    this.rdfFieldName = rdfFieldName;
  }

  @Deprecated
  public String getValue() {
    return value;
  }

  @Deprecated
  public void setValue(String value) {
    this.value = value;
  }

  @Deprecated
  public List<EntityType> getEntityTypes() {
    return entityTypes;
  }

  @Deprecated
  public void setEntityTypes(List<EntityType> entityTypes) {
    this.entityTypes = entityTypes;
  }

  @Deprecated
  public String getLanguage() {
    return language;
  }

  @Deprecated
  public void setLanguage(String language) {
    this.language = language;
  }
}
