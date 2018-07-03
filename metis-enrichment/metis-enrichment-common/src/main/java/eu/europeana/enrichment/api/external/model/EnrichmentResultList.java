package eu.europeana.enrichment.api.external.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(namespace = "http://www.europeana.eu/schemas/metis", name = "results")
@XmlAccessorType(XmlAccessType.FIELD)
public class EnrichmentResultList {

  @XmlElements(value={
      @XmlElement(name="Concept", namespace = "http://www.europeana.eu/schemas/edm/", type=Concept.class),
      @XmlElement(name="Agent", namespace = "http://www.europeana.eu/schemas/edm/", type=Agent.class),
      @XmlElement(name="Place", namespace = "http://www.europeana.eu/schemas/edm/", type=Place.class),
      @XmlElement(name="Timespan", namespace = "http://www.europeana.eu/schemas/edm/", type=Timespan.class)
      })
  private final List<EnrichmentBase> result = new ArrayList<>();

  public EnrichmentResultList() {}

  public EnrichmentResultList(Collection<EnrichmentBase> result) {
    this.result.addAll(result);
  }

  public List<EnrichmentBase> getResult() {
    return result;
  }
}
