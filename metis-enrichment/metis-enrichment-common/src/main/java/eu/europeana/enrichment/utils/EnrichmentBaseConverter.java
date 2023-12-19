package eu.europeana.enrichment.utils;

import eu.europeana.enrichment.api.external.model.Agent;
import eu.europeana.enrichment.api.external.model.Concept;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.Organization;
import eu.europeana.enrichment.api.external.model.Place;
import eu.europeana.enrichment.api.external.model.TimeSpan;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import java.io.StringReader;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * This class contains conversion tools for the {@link EnrichmentBase} class.
 */
public final class EnrichmentBaseConverter {

  private EnrichmentBaseConverter() {
    // This class should not be instantiated.
  }

  /**
   * Parse the XML string to convert it to an instance of (one of the subclasses of) {@link EnrichmentBase}.
   *
   * @param entityXml The XML string.
   * @return The entity.
   * @throws JAXBException In case there was an issue parsing the XML.
   */
  public static EnrichmentBase convertToEnrichmentBase(String entityXml) throws JAXBException {
    final StringReader reader = new StringReader(entityXml);
    final JAXBContext context = JAXBContext.newInstance(EnrichmentBase.class);
    return (EnrichmentBase) context.createUnmarshaller().unmarshal(reader);
  }

  /**
   * Converts a list of {@link Entity} class to {@link EnrichmentBase}
   *
   * @param entities the entities
   * @return the enrichment bases
   */
  public static List<EnrichmentBase> convertEntitiesToEnrichmentBase(List<Entity> entities) {
    return entities.stream().map(EnrichmentBaseConverter::convertEntitiesToEnrichmentBase).toList();
  }

  /**
   * Converts an {@link Entity} class to {@link EnrichmentBase}
   *
   * @param entity the entity
   * @return the enrichment base
   */
  public static EnrichmentBase convertEntitiesToEnrichmentBase(Entity entity) {
    final EnrichmentBase enrichmentBase;
    switch (EntityTypes.valueOf(entity.getType())) {
      case Agent:
        enrichmentBase = new Agent((eu.europeana.entitymanagement.definitions.model.Agent) entity);
        break;
      case Place:
        enrichmentBase = new Place((eu.europeana.entitymanagement.definitions.model.Place) entity);
        break;
      case Concept:
        enrichmentBase = new Concept((eu.europeana.entitymanagement.definitions.model.Concept) entity);
        break;
      case TimeSpan:
        enrichmentBase = new TimeSpan((eu.europeana.entitymanagement.definitions.model.TimeSpan) entity);
        break;
      case Organization:
        enrichmentBase = new Organization((eu.europeana.entitymanagement.definitions.model.Organization) entity);
        break;
      default:
        enrichmentBase = null;
        break;
    }
    return enrichmentBase;
  }
}
