package eu.europeana.enrichment.rest.client.enrichment;


import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.internal.*;
import eu.europeana.enrichment.rest.client.ConnectionProvider;
import eu.europeana.enrichment.utils.EntityType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

// TODO - This is a Integration Test. Once we have measured performance of the new implementation,
//  proper junit test will be added.
@Disabled
public class EntityClientResolverITTest {

    private ConnectionProvider connectionProvider;
    private EntityResolver entityResolver;

    @BeforeEach
    void setup() {
        connectionProvider = new ConnectionProvider();
        entityResolver = Mockito.spy(new EntityClientResolver(connectionProvider.createRestTemplate(), 2));
    }

    @Test
    public void testResolveByText() {
        Set<SearchTerm> setToTest = new HashSet<>();
        setToTest.add(new SearchTermImpl("belgium", "en", Set.of(EntityType.PLACE)));
        setToTest.add(new SearchTermImpl("Paris", "en"));
        setToTest.add(new SearchTermImpl("Johannes Vermeer", "en", Set.of(EntityType.AGENT)));
        setToTest.add(new SearchTermImpl("Aria", null, Set.of(EntityType.PLACE)));
        setToTest.add(new SearchTermImpl("invalidValue", "en", Set.of(EntityType.AGENT)));

        Map<SearchTerm, List<EnrichmentBase>> results = entityResolver.resolveByText(setToTest);
        Assertions.assertNotNull(results);
        Assertions.assertEquals(setToTest.size(), results.size());
    }

    @Test
    public void testResolveById() throws MalformedURLException {
        Set<ReferenceTerm> idTotest = new HashSet<>();
        idTotest.add(new ReferenceTermImpl(new URL("http://data.europeana.eu/agent/75")));
        idTotest.add(new ReferenceTermImpl(new URL("http://data.europeana.eu/organization/1482250000004477289")));
        idTotest.add(new ReferenceTermImpl(new URL("http://data.europeana.eu/organization/1482250000004514646")));
        idTotest.add(new ReferenceTermImpl(new URL("http://data.europeana.eu/timespan/base/10")));
        idTotest.add(new ReferenceTermImpl(new URL("http://data.europeana.eu/place/base/16436")));
        idTotest.add(new ReferenceTermImpl(new URL("http://data.europeana.eu/concept/base/1106")));

        Map<ReferenceTerm, EnrichmentBase> results = entityResolver.resolveById(idTotest);
        Assertions.assertNotNull(results);
        Assertions.assertEquals(idTotest.size(), results.size());
    }

    @Test
    public void testResolveByUri() throws MalformedURLException {
        Set<ReferenceTerm> uriTotest = new HashSet<>();
        uriTotest.add(new ReferenceTermImpl(new URL("http://dbpedia.org/resource/Johannes_Vermeer"), Set.of(EntityType.AGENT)));
        uriTotest.add(new ReferenceTermImpl(new URL("http://dbpedia.org/resource/Johannes_Vermeer")));
        entityResolver.resolveByUri(uriTotest);

        Map<ReferenceTerm, List<EnrichmentBase>> results = entityResolver.resolveByUri(uriTotest);
        Assertions.assertNotNull(results);
        Assertions.assertEquals(uriTotest.size(), results.size());
    }
}