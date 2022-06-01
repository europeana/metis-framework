package eu.europeana.enrichment.utils;

import eu.europeana.enrichment.api.external.model.EnrichmentQuery;
import eu.europeana.entitymanagement.definitions.model.Agent;
import eu.europeana.entitymanagement.definitions.model.Entity;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EntityResolverUtilsTest {

    @Test
    public void testCheckIfEntityAlreadyExists(){
        List<Entity> entityList = new ArrayList<>();
        Entity agent = new Agent();
        agent.setAbout("http://data.europeana.eu/agent/456");
        entityList.add(agent);

        Assertions.assertFalse(EntityResolverUtils.checkIfEntityAlreadyExists(
                "http://data.europeana.eu/agent/123", entityList));

        Assertions.assertTrue(EntityResolverUtils.checkIfEntityAlreadyExists(
                "http://data.europeana.eu/agent/456", entityList));
    }

    @Test
    public void testIsTextOrUriSearch() {
        Assertions.assertTrue(EntityResolverUtils.isTextOrUriSearch
                (new EnrichmentQuery("paris", "en", "Agent", false)));

        Assertions.assertTrue(EntityResolverUtils.isTextOrUriSearch
                (new EnrichmentQuery("https://viaf_test_uri", "place", true)));

        Assertions.assertFalse(EntityResolverUtils.isTextOrUriSearch
                (new EnrichmentQuery("http://data.europeana.eu/place/456" , null, true)));
    }
}
