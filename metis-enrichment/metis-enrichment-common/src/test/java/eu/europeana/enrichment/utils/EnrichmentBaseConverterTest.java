package eu.europeana.enrichment.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.enrichment.api.external.model.Agent;
import eu.europeana.enrichment.api.external.model.Concept;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.Organization;
import eu.europeana.enrichment.api.external.model.Place;
import eu.europeana.entitymanagement.definitions.model.Entity;
import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class EnrichmentBaseConverterTest {

  public static String getResourceFileContent(String fileName) {
    try {
      return new String(EnrichmentBaseConverterTest.class.getClassLoader().getResourceAsStream(fileName).readAllBytes());
    } catch (IOException ioException) {
      return "";
    }
  }

  private static Stream<Arguments> provideEntitiesAndClasses() {
    return Stream.of(
        Arguments.of("entity-agent.xml", Agent.class),
        Arguments.of("entity-concept.xml", Concept.class),
        Arguments.of("entity-place.xml", Place.class),
        Arguments.of("entity-organization.xml", Organization.class)
    );
  }

  @ParameterizedTest
  @MethodSource("provideEntitiesAndClasses")
  void convertToEnrichmentBase(String entityFile, Class clazz) throws JAXBException {
    final String entityXml = getResourceFileContent(entityFile);
    EnrichmentBase enrichmentBase = EnrichmentBaseConverter.convertToEnrichmentBase(entityXml);
    assertNotNull(enrichmentBase);
    assertTrue(clazz.isInstance(enrichmentBase));
  }

  @Test
  void convertEntitiesToEnrichmentBase() {
    List<Entity> entities = new ArrayList<>();
    entities.add(new eu.europeana.entitymanagement.definitions.model.Agent());
    entities.add(new eu.europeana.entitymanagement.definitions.model.Place());
    entities.add(new eu.europeana.entitymanagement.definitions.model.Concept());
    entities.add(new eu.europeana.entitymanagement.definitions.model.TimeSpan());
    entities.add(new eu.europeana.entitymanagement.definitions.model.Organization());
    entities.add(new eu.europeana.entitymanagement.definitions.model.Aggregator());
    List<EnrichmentBase> result = EnrichmentBaseConverter.convertEntitiesToEnrichmentBase(entities);
    assertNotNull(result);
    assertEquals(entities.size(), result.size());
  }
}
