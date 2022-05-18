package eu.europeana.enrichment.rest.enrichment;

import eu.europeana.enrichment.api.external.model.EntityClientRequest;
import eu.europeana.enrichment.utils.EntityResolverUtils;
import eu.europeana.entitymanagement.definitions.model.Agent;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.Place;
import eu.europeana.entitymanagement.definitions.model.TimeSpan;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

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
    public void testIsParentEntityRequiredOther() {
        Entity timespan = new TimeSpan();
        Assertions.assertFalse(EntityResolverUtils.isParentEntityRequired(timespan,
                new EntityClientRequest("paris", "en", "timespan", false)));

        Assertions.assertFalse(EntityResolverUtils.isParentEntityRequired(timespan,
                new EntityClientRequest("https://viaf_test_uri", "timespan", true)));

        Assertions.assertFalse(EntityResolverUtils.isParentEntityRequired(timespan,
                new EntityClientRequest("http://data.europeana.eu/timespan/456" , null, true)));
    }

    @Test
    public void testIsParentEntityRequiredPlace() {
        Entity place = new Place();
        Assertions.assertTrue(EntityResolverUtils.isParentEntityRequired(place,
                new EntityClientRequest("paris", "en", "Place", false)));

        Assertions.assertTrue(EntityResolverUtils.isParentEntityRequired(place,
                new EntityClientRequest("https://viaf_test_uri", "Place", true)));

        Assertions.assertFalse(EntityResolverUtils.isParentEntityRequired(place,
                new EntityClientRequest("http://data.europeana.eu/place/456" , null, true)));
    }

    @Test
    public void testIsParentEntityRequiredAgent() {
        Entity agent = new Agent();
        Assertions.assertTrue(EntityResolverUtils.isParentEntityRequired(agent,
                new EntityClientRequest("paris", "en", "Agent", false)));

        Assertions.assertTrue(EntityResolverUtils.isParentEntityRequired(agent,
                new EntityClientRequest("https://viaf_test_uri", "Agent", true)));

        Assertions.assertFalse(EntityResolverUtils.isParentEntityRequired(agent,
                new EntityClientRequest("http://data.europeana.eu/agent/456" , null, true)));
    }

    @Test
    public void testIsTextOrUriSearch() {
        Assertions.assertTrue(EntityResolverUtils.isTextOrUriSearch
                (new EntityClientRequest("paris", "en", "Agent", false)));

        Assertions.assertTrue(EntityResolverUtils.isTextOrUriSearch
                (new EntityClientRequest("https://viaf_test_uri", "place", true)));

        Assertions.assertFalse(EntityResolverUtils.isTextOrUriSearch
                (new EntityClientRequest("http://data.europeana.eu/place/456" , null, true)));
    }
}
