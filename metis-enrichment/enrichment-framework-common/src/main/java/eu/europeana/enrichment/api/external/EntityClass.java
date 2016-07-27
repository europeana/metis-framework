package eu.europeana.enrichment.api.external;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Enumeration that holds the different vocabularies supported for enrichment
 * 
 * @author Yorgos.Mamakis@ europeana.eu
 * 
 */
@XmlRootElement
@JsonSerialize(include = Inclusion.ALWAYS)
public enum EntityClass {

	CONCEPT, TIMESPAN, AGENT, PLACE;
}
