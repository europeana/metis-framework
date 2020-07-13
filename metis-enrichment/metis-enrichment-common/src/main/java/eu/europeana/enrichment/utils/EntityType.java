package eu.europeana.enrichment.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Enumeration that holds the different vocabularies supported for enrichment
 *
 * @author Yorgos.Mamakis@ europeana.eu
 */
@XmlRootElement
@JsonInclude
public enum EntityType {
  CONCEPT, TIMESPAN, AGENT, PLACE, ORGANIZATION;
}
