package eu.europeana.enrichment.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Enumeration that holds the different vocabularies supported for enrichment
 */
@XmlRootElement
@JsonInclude
public enum EntityType {
  CONCEPT, TIMESPAN, AGENT, PLACE, ORGANIZATION
}
