package eu.europeana.enrichment.utils;

import javax.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Enumeration that holds the different vocabularies supported for enrichment
 * 
 * @author Yorgos.Mamakis@ europeana.eu
 * 
 */
@XmlRootElement
@JsonInclude(Include.ALWAYS)
public enum EntityClass {
	CONCEPT, TIMESPAN, AGENT, PLACE, ORGANIZATION
}
