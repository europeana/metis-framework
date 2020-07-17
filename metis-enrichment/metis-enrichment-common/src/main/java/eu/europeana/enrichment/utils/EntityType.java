package eu.europeana.enrichment.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.corelib.solr.entity.ConceptImpl;
import eu.europeana.corelib.solr.entity.OrganizationImpl;
import eu.europeana.corelib.solr.entity.PlaceImpl;
import eu.europeana.corelib.solr.entity.TimespanImpl;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Enumeration that holds the different vocabularies supported for enrichment
 *
 * @author Yorgos.Mamakis@ europeana.eu
 */
@XmlRootElement
@JsonInclude
public enum EntityType {
  CONCEPT(ConceptImpl.class),
  TIMESPAN(TimespanImpl.class),
  AGENT(AgentImpl.class),
  PLACE(PlaceImpl.class),
  ORGANIZATION(OrganizationImpl.class);

  private final Class<?> entityClassImpl;

  EntityType(Class<?> entityClassImpl) {
    this.entityClassImpl = entityClassImpl;
  }

  public static EntityType entityTypeFromEnumName(String entityTypeName) {
    for (EntityType entityType : EntityType.values()) {
      if (entityType.name().equalsIgnoreCase(entityTypeName)) {
        return entityType;
      }
    }
    return null;
  }

  public static EntityType entityTypeFromClassImpl(String entityClassImpl) {
    for (EntityType entityType : EntityType.values()) {
      if (entityType.getEntityClassImpl().getSimpleName().equalsIgnoreCase(entityClassImpl)) {
        return entityType;
      }
    }
    return null;
  }

  public Class<?> getEntityClassImpl() {
    return entityClassImpl;
  }
}
