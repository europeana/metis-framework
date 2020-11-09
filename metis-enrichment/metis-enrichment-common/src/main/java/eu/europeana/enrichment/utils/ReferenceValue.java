package eu.europeana.enrichment.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonInclude
public class ReferenceValue {

  private String reference;

  private List<EntityType> entityTypes;

  public ReferenceValue(){
  }

  /**
   * Constructor with all possible fields provided for enrichment search.
   *
   * @param reference the id to be enriched
   * @param entityTypes the vocabularies that this value represents
   */
  public ReferenceValue(String reference, List<EntityType> entityTypes) {
    this.reference = reference;
    this.entityTypes = entityTypes;
  }

  public String getReference() {
    return reference;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }

  public List<EntityType> getEntityTypes() {
    return entityTypes;
  }

  public void setEntityTypes(List<EntityType> entityTypes) {
    this.entityTypes = entityTypes;
  }
}
