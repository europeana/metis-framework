package eu.europeana.enrichment.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.enrichment.api.external.impl.ClientEntityResolver;
import eu.europeana.entitymanagement.definitions.model.Agent;
import eu.europeana.entitymanagement.definitions.model.Entity;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class EntityResolverUtilsTest {

  @Test
  void testCheckIfEntityAlreadyExists() {
    List<Entity> entityList = new ArrayList<>();
    Entity agent = new Agent();
    agent.setAbout("http://data.europeana.eu/agent/456");
    entityList.add(agent);

    assertFalse(ClientEntityResolver.doesEntityExist(
        "http://data.europeana.eu/agent/123", entityList));

    assertTrue(ClientEntityResolver.doesEntityExist(
        "http://data.europeana.eu/agent/456", entityList));
  }
}
