package eu.europeana.enrichment.api.external.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Model class that holds an Agent(for example a person)
 */
@XmlRootElement(namespace = "http://www.europeana.eu/schemas/edm/", name = "Agent")
@XmlAccessorType(XmlAccessType.FIELD)
public class Agent extends AbstractAgent {

}
