package eu.europeana.metis.common.model;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "results", namespace = "http://www.europeana.eu/schemas/metis")
@XmlAccessorType(XmlAccessType.FIELD)
public class EnrichmentResultList {

  public List<EnrichmentBase> getResult() {
    return result;
  }

  public void setResult(List<EnrichmentBase> result) {
    this.result = result;
  }

  @XmlElements(value={
      @XmlElement(name="Concept", type=Concept.class, namespace = "http://www.europeana.eu/schemas/edm/"),
      @XmlElement(name="Agent", type=Agent.class, namespace = "http://www.europeana.eu/schemas/edm/"),
      @XmlElement(name="Place", type=Place.class, namespace = "http://www.europeana.eu/schemas/edm/"),
      @XmlElement(name="Timespan", type=Timespan.class, namespace = "http://www.europeana.eu/schemas/edm/")
      })
  private List<EnrichmentBase> result;

  public EnrichmentResultList(List<EnrichmentBase> result) {
    this.result = result;
  }

  public EnrichmentResultList() {
    result = new ArrayList<>();
  }
}