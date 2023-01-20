package eu.europeana.enrichment.internal.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.morphia.annotations.Entity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.SerializationUtils;

/**
 * Abstract class representing an enrichment entity
 *
 * @author Simon Tzanakis
 * @since 2020-08-31
 */
@Entity
public abstract class AbstractEnrichmentEntity implements eu.europeana.enrichment.internal.model.Entity {

  private String about;
  private Map<String, List<String>> prefLabel;
  private Map<String, List<String>> altLabel;
  private Map<String, List<String>> hiddenLabel;
  private Map<String, List<String>> note;
  private List<String> owlSameAs;
  private String isPartOf;
  private String foafDepiction;

  public String getAbout() {
    return about;
  }

  public void setAbout(String about) {
    this.about = about;
  }

  @Override
  public Map<String, List<String>> getPrefLabel() {
    return SerializationUtils.clone(new HashMap<>(this.prefLabel));
  }

  @Override
  public Map<String, List<String>> getHiddenLabel() {
    return SerializationUtils.clone(new HashMap<>(this.hiddenLabel));
  }

  @Override
  public Map<String, List<String>> getAltLabel() {
    return SerializationUtils.clone(new HashMap<>(this.altLabel));
  }

  @Override
  public Map<String, List<String>> getNote() {
    return SerializationUtils.clone(new HashMap<>(this.note));
  }

  @Override
  public void setPrefLabel(Map<String, List<String>> prefLabel) {
    this.prefLabel = SerializationUtils.clone(new HashMap<>(prefLabel));
  }

  @Override
  public void setAltLabel(Map<String, List<String>> altLabel) {
    this.altLabel = SerializationUtils.clone(new HashMap<>(altLabel));
  }

  @Override
  public void setHiddenLabel(Map<String, List<String>> hiddenLabel) {
    this.hiddenLabel = SerializationUtils.clone(new HashMap<>(hiddenLabel));
  }

  @Override
  public void setNote(Map<String, List<String>> note) {
    this.note = SerializationUtils.clone(new HashMap<>(note));
  }

  @Override
  public List<String> getOwlSameAs() {
    return this.owlSameAs == null ? null : new ArrayList<>(this.owlSameAs);
  }

  @Override
  public void setOwlSameAs(List<String> owlSameAs) {
    this.owlSameAs = owlSameAs == null ? new ArrayList<>() : new ArrayList<>(owlSameAs);
  }

  @Override
  public String getIsPartOf() {
    return this.isPartOf;
  }

  @Override
  public void setIsPartOf(String isPartOf) {
    this.isPartOf = isPartOf;
  }

  @Override
  public String getFoafDepiction() {
    return foafDepiction;
  }

  @Override
  public void setFoafDepiction(String foafDepiction) {
    this.foafDepiction = foafDepiction;
  }

  @JsonIgnore
  @Override
  public String getEntityIdentifier() {
    String[] splitArray = this.getAbout().split("/");
    return splitArray[splitArray.length - 1];
  }
}
