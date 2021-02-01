package eu.europeana.enrichment.api.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Wrapper class for ReferenceValue class.
 * <p>Required for proper (un)marshalling xml</p>
 *
 * @author Joana Sousa (joana.sousa@europeana.eu)
 *
 */
@JsonSerialize
@JsonRootName(value = EnrichmentReference.API_NAME)
@XmlRootElement(name = EnrichmentReference.API_NAME)
@XmlAccessorType(XmlAccessType.FIELD)
@ApiModel(value = EnrichmentReference.API_NAME)
public class EnrichmentReference {

  static final String API_NAME = "inputReferences";

  @XmlElement(name = "references")
  @JsonProperty("references")
  private List<ReferenceValue> referenceValues;

  public EnrichmentReference() {
    // Required for XML mapping.
  }

  /**
   * Constructor with all searchValues.
   *
   * @param referenceValues the search queries that this value represents
   */
  public EnrichmentReference(ReferenceValue... referenceValues) {
    this.referenceValues = List.copyOf(Set.of(referenceValues));
  }

  public List<ReferenceValue> getReferenceValues() {
    return referenceValues == null ? null : Collections.unmodifiableList(referenceValues);
  }

  public void setReferenceValues(List<ReferenceValue> searchValues) {
    this.referenceValues = searchValues == null ? null : new ArrayList<>(searchValues);
  }
}
