
package eu.europeana.enrichment.api.external.model;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = "http://www.europeana.eu/schemas/edm/", name = "Timespan")
@XmlAccessorType(XmlAccessType.FIELD)

public class Timespan extends EnrichmentBase {
  @XmlElement(name = "isPartOf", namespace = "http://purl.org/dc/terms/")
  private List<Part> isPartOfList = new ArrayList<>();
  @XmlElement(name = "hasPart", namespace = "http://purl.org/dc/terms/")
  private List<Part> hasPartsList = new ArrayList<>();
  @XmlElement(name = "sameAs", namespace = "http://www.w3.org/2002/07/owl#")
  private List<Part> sameAs = new ArrayList<>();
  @XmlElement(name = "begin", namespace = "http://www.europeana.eu/schemas/edm/")
  private List<Label> beginList = new ArrayList<>();
  @XmlElement(name = "end", namespace = "http://www.europeana.eu/schemas/edm/")
  private List<Label> endList = new ArrayList<>();
  @XmlElement(name = "hiddenLabel", namespace = "http://www.w3.org/2004/02/skos/core#")
  private List<Label> hiddenLabel = new ArrayList<>();

  public List<Part> getIsPartOfList() {
    return unmodifiableListAcceptingNull(isPartOfList);
  }

  public void setIsPartOfList(List<Part> isPartOfList) {
    this.isPartOfList = cloneListAcceptingNull(isPartOfList);
  }

  public List<Part> getHasPartsList() {
    return unmodifiableListAcceptingNull(hasPartsList);
  }

  public void setHasPartsList(List<Part> hasPartsList) {
    this.hasPartsList = cloneListAcceptingNull(hasPartsList);
  }

  public List<Part> getSameAs() {
    return unmodifiableListAcceptingNull(sameAs);
  }

  public void setSameAs(List<Part> sameAs) {
    this.sameAs = cloneListAcceptingNull(sameAs);
  }

  public List<Label> getBeginList() {
    return unmodifiableListAcceptingNull(beginList);
  }

  public void setBeginList(List<Label> beginList) {
    this.beginList = cloneListAcceptingNull(beginList);
  }

  public List<Label> getEndList() {
    return unmodifiableListAcceptingNull(endList);
  }

  public void setEndList(List<Label> endList) {
    this.endList = cloneListAcceptingNull(endList);
  }

  public List<Label> getHiddenLabel() {
    return unmodifiableListAcceptingNull(hiddenLabel);
  }

  public void setHiddenLabel(List<Label> hiddenLabel) {
    this.hiddenLabel = cloneListAcceptingNull(hiddenLabel);
  }
}
