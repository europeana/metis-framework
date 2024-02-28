package eu.europeana.enrichment.api.external.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Model class that holds an Agent(for example a person)
 */
@XmlRootElement(namespace = "http://www.europeana.eu/schemas/edm/", name = "Agent")
@XmlAccessorType(XmlAccessType.FIELD)
public class Agent extends AgentBase {

  public Agent() {
  }

  public Agent(eu.europeana.entitymanagement.definitions.model.Agent entity) {
    super(entity);
  }

  public Agent(eu.europeana.entitymanagement.definitions.model.Organization entity) {
    super(entity);
  }

}
