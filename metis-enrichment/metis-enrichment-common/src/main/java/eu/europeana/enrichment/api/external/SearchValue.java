package eu.europeana.enrichment.api.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.europeana.enrichment.utils.EntityType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The model class that contains the {@code value} to be used to find a match, the (optional) {@code
 * language} that the value is in and the (optional) {@code entityTypes} which correspond to the
 * type of the entity to search for.
 * <p>This is a JAXB class that follows (un)marshalling principles. Therefore the {@code
 * entityTypes} field is a {@link List}(and not a {@link Set}) but when the value is set or
 * unmarshalled the duplicates are removed.
 * </p>
 *
 * @author Joana Sousa (joana.sousa@europeana.eu)
 * @since 2020-11-02
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SearchValue {

  private String value;
  private String language;

  /**
   * Internally duplicates are removed when value is set or unmarshalled.
   */
  @XmlElement(name = "entityType")
  @JsonProperty("entityType")
  private List<EntityType> entityTypes;

  public SearchValue() {
    // Required for XML (un)marshalling.
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
    this.entityTypes = List.copyOf(Set.of(entityTypes));
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
    return entityTypes == null ? null : Collections.unmodifiableList(entityTypes);
  }

  /**
   * Sets the entity types and removes duplicates.
   *
   * @param entityTypes the entity types
   */
  public void setEntityTypes(List<EntityType> entityTypes) {
    this.entityTypes = entityTypes == null ? null : new ArrayList<>(new HashSet<>(entityTypes));
  }

  /**
   * This method is <b>REQUIRED</b> so that after unmarshalling the list contents of {@code entityTypes}
   * are cleaned to remove any duplicates.
   *
   * @param unmarshaller the unmarshaller
   * @param parent the parent
   */
  public void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
    //Remove duplicates from list after unmarshal
    entityTypes = new ArrayList<>(new HashSet<>(entityTypes));
  }
}
