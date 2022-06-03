package eu.europeana.enrichment.utils;

import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.enrichment.api.external.impl.ClientEntityResolver;
import eu.europeana.enrichment.api.internal.ReferenceTermImpl;
import eu.europeana.enrichment.api.internal.SearchTermImpl;
import eu.europeana.entitymanagement.definitions.model.Agent;
import eu.europeana.entitymanagement.definitions.model.Entity;
import java.net.MalformedURLException;
import java.net.URL;
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

  @Test
  void isSearchTermOrReferenceThatIsNotAEuropeanaEntity() throws MalformedURLException {
    assertTrue(EntityResolverUtils.isSearchTermOrReferenceThatIsNotAEuropeanaEntity(
        new SearchTermImpl("paris", "en", singleton(EntityType.AGENT))));

    assertTrue(EntityResolverUtils.isSearchTermOrReferenceThatIsNotAEuropeanaEntity(
        new ReferenceTermImpl(new URL("https://viaf_test_uri"), singleton(EntityType.AGENT))));

    assertFalse(EntityResolverUtils.isSearchTermOrReferenceThatIsNotAEuropeanaEntity(
        new ReferenceTermImpl(new URL("http://data.europeana.eu/place/456"), singleton(EntityType.AGENT))));
  }
}
