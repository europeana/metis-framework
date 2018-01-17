
package eu.europeana.metis.common.model;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Timespan", namespace = "http://www.europeana.eu/schemas/edm/")
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
  private List<Label> hiddenLabel = new  ArrayList<>();

  public List<Part> getIsPartOfList() {
    return isPartOfList;
  }
  public void setIsPartOfList(List<Part> isPartOfList) {
    this.isPartOfList = isPartOfList;
  }

  public List<Part> getHasPartsList() {
    return hasPartsList;
  }
  public void setHasPartsList(List<Part> hasPartsList) {
    this.hasPartsList = hasPartsList;
  }

  public List<Part> getSameAs() {
    return sameAs;
  }
  public void setSameAs(List<Part> sameAs) {
    this.sameAs = sameAs;
  }

  public List<Label> getBeginList() {
    return beginList;
  }

  public void setBeginList(List<Label> beginList) {
    this.beginList = beginList;
  }


  public List<Label> getEndList() {
    return endList;
  }

  public void setEndList(List<Label> endList) {
    this.endList = endList;
  }

  public List<Label> getHiddenLabel() {
    return hiddenLabel;
  }

  public void setHiddenLabel(List<Label> hiddenLabel) {
    this.hiddenLabel = hiddenLabel;
  }
}
