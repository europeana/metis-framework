package eu.europeana.enrichment.api.external.model;

import static eu.europeana.enrichment.utils.EntityValuesConverter.convertListToLabel;
import static eu.europeana.enrichment.utils.EntityValuesConverter.convertListToLabelResource;
import static eu.europeana.enrichment.utils.EntityValuesConverter.convertListToPart;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * TimeSpan model class.
 */
@XmlRootElement(namespace = "http://www.europeana.eu/schemas/edm/", name = "TimeSpan")
@XmlAccessorType(XmlAccessType.FIELD)
public class TimeSpan extends EnrichmentBase {

  @XmlElement(name = "isPartOf", namespace = "http://purl.org/dc/terms/")
  private List<LabelResource> isPartOf;
  @XmlElement(name = "hasPart", namespace = "http://purl.org/dc/terms/")
  private List<LabelResource> hasPartsList = new ArrayList<>();
  @XmlElement(name = "sameAs", namespace = "http://www.w3.org/2002/07/owl#")
  private List<Part> sameAs = new ArrayList<>();
  @XmlElement(name = "begin", namespace = "http://www.europeana.eu/schemas/edm/")
  private Label begin;
  @XmlElement(name = "end", namespace = "http://www.europeana.eu/schemas/edm/")
  private Label end;
  @XmlElement(name = "hiddenLabel", namespace = "http://www.w3.org/2004/02/skos/core#")
  private List<Label> hiddenLabel = new ArrayList<>();
  @XmlElement(name = "isNextInSequence", namespace = "http://www.europeana.eu/schemas/edm/")
  private Part isNextInSequence;

  public TimeSpan() {
  }

  // Used for creating XML entity from EM model class
  public TimeSpan(eu.europeana.entitymanagement.definitions.model.TimeSpan timeSpan) {
    super(timeSpan);
    init(timeSpan);
  }

  public List<LabelResource> getIsPartOf() {
    return unmodifiableListAcceptingNull(isPartOf);
  }

  public void setIsPartOf(List<LabelResource> isPartOf) {
    this.isPartOf = cloneListAcceptingNull(isPartOf);
  }

  public List<LabelResource> getHasPartsList() {
    return unmodifiableListAcceptingNull(hasPartsList);
  }

  public void setHasPartsList(List<LabelResource> hasPartsList) {
    this.hasPartsList = cloneListAcceptingNull(hasPartsList);
  }

  public List<Part> getSameAs() {
    return unmodifiableListAcceptingNull(sameAs);
  }

  public void setSameAs(List<Part> sameAs) {
    this.sameAs = cloneListAcceptingNull(sameAs);
  }

  public Label getBegin() {
    return begin;
  }

  public void setBegin(Label begin) {
    this.begin = begin;
  }

  public Label getEnd() {
    return end;
  }

  public void setEnd(Label end) {
    this.end = end;
  }

  public List<Label> getHiddenLabel() {
    return unmodifiableListAcceptingNull(hiddenLabel);
  }

  public void setHiddenLabel(List<Label> hiddenLabel) {
    this.hiddenLabel = cloneListAcceptingNull(hiddenLabel);
  }

  public Part getIsNextInSequence() {
    return isNextInSequence;
  }

  public void setIsNextInSequence(Part isNextInSequence) {
    this.isNextInSequence = isNextInSequence;
  }

  private void init(eu.europeana.entitymanagement.definitions.model.TimeSpan timeSpan) {
    this.isPartOf = convertListToLabelResource(timeSpan.getIsPartOfArray());
    this.hasPartsList = convertListToLabelResource(timeSpan.getHasPart());
    this.sameAs = convertListToPart(timeSpan.getSameReferenceLinks());
    if (timeSpan.getBeginString() != null) {
      this.begin = new Label(timeSpan.getBeginString());
    }
    if (timeSpan.getEndString() != null) {
      this.end = new Label(timeSpan.getEndString());
    }
    this.hiddenLabel = convertListToLabel(timeSpan.getHiddenLabel());
    if (timeSpan.getIsNextInSequence() != null) {
      this.isNextInSequence = new Part(timeSpan.getIsNextInSequence().get(0));
    }
  }
}
