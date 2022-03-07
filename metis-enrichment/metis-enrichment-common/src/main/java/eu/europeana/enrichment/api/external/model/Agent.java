package eu.europeana.enrichment.api.external.model;

import eu.europeana.entitymanagement.definitions.model.Entity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Model class that holds an Agent(for example a person)
 */
@XmlRootElement(namespace = "http://www.europeana.eu/schemas/edm/", name = "Agent")
@XmlAccessorType(XmlAccessType.FIELD)
public class Agent extends AgentBase {

    public Agent() {}

    public Agent(eu.europeana.entitymanagement.definitions.model.Agent entity) {
        super(entity);
    }

    public Agent(eu.europeana.entitymanagement.definitions.model.Organization entity) {
        super(entity);
    }

}
