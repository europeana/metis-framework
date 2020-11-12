package eu.europeana.enrichment.api.external;

import com.fasterxml.jackson.annotation.JsonInclude;
import eu.europeana.enrichment.utils.EntityType;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonInclude
public class ReferenceValue {

  private String reference;

  private Set<EntityType> entityTypes;

  public ReferenceValue(){
  }

  /**
   * Constructor with all possible fields provided for enrichment search.
   *
   * @param reference the id to be enriched
   * @param entityTypes the vocabularies that this value represents
   */
  public ReferenceValue(String reference, Set<EntityType> entityTypes) {
    this.reference = reference;
    this.entityTypes = entityTypes;
  }

  public String getReference() {
    return reference;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }

  public Set<EntityType> getEntityTypes() {
    return new HashSet<>(entityTypes);
  }

  public void setEntityTypes(Set<EntityType> entityTypes) {
    this.entityTypes = new HashSet<>(entityTypes);
  }
}
