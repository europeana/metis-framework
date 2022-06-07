package eu.europeana.enrichment.rest.enrichment;

import eu.europeana.enrichment.api.external.impl.ClientEntityResolver;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.internal.EntityResolver;
import eu.europeana.enrichment.api.internal.ReferenceTerm;
import eu.europeana.enrichment.api.internal.ReferenceTermImpl;
import eu.europeana.enrichment.api.internal.SearchTerm;
import eu.europeana.enrichment.api.internal.SearchTermImpl;
import eu.europeana.enrichment.utils.EntityType;
import eu.europeana.entity.client.web.EntityClientApiImpl;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

// TODO - This is a Integration Test. Once we have measured performance of the new implementation,
//  proper junit test will be added.
// This is not a test we usually have in metis. It has to either be implemented with Wiremock or kept disable for the time being.
@Disabled
class ClientEntityResolverIT {

    private EntityResolver entityResolver;

    @BeforeEach
    void setup() {
        entityResolver = Mockito.spy(new ClientEntityResolver(new EntityClientApiImpl(), 2));
    }

    @Test
    void testResolveByText() {
        Set<SearchTerm> setToTest = new HashSet<>();
        setToTest.add(new SearchTermImpl("belgium", "en", Set.of(EntityType.PLACE)));
        setToTest.add(new SearchTermImpl("Paris", "en"));
        setToTest.add(new SearchTermImpl("Johannes Vermeer", "en", Set.of(EntityType.AGENT)));
        setToTest.add(new SearchTermImpl("Aria", null, Set.of(EntityType.PLACE)));
        setToTest.add(new SearchTermImpl("invalidValue", "en", Set.of(EntityType.AGENT)));

        Map<SearchTerm, List<EnrichmentBase>> results = entityResolver.resolveByText(setToTest);
        Assertions.assertNotNull(results);
        // For invalidValue no entity will be found
        Assertions.assertEquals(setToTest.size()-1, results.size());
    }

    @Test
    void testResolveById() throws MalformedURLException {
        Set<ReferenceTerm> idTotest = new HashSet<>();
        idTotest.add(new ReferenceTermImpl(new URL("http://data.europeana.eu/agent/75")));
        idTotest.add(new ReferenceTermImpl(new URL("http://data.europeana.eu/organization/1482250000004477289")));
        idTotest.add(new ReferenceTermImpl(new URL("http://data.europeana.eu/organization/1454482250000004514646554646")));
        idTotest.add(new ReferenceTermImpl(new URL("http://data.europeana.eu/timespan/base/10")));
        idTotest.add(new ReferenceTermImpl(new URL("http://data.europeana.eu/place/base/16436")));
        idTotest.add(new ReferenceTermImpl(new URL("http://data.europeana.eu/concept/base/1106")));

        Map<ReferenceTerm, EnrichmentBase> results = entityResolver.resolveById(idTotest);
        Assertions.assertNotNull(results);
        Assertions.assertEquals(idTotest.size()-1, results.size());
    }

    @Test
    void testResolveByUri() throws MalformedURLException {
        Set<ReferenceTerm> uriTotest = new HashSet<>();
        uriTotest.add(new ReferenceTermImpl(new URL("http://viaf.org/viaf/invalid"), Set.of(EntityType.AGENT)));
        uriTotest.add(new ReferenceTermImpl(new URL("http://www.idref.fr/092255841/id")));

        Map<ReferenceTerm, List<EnrichmentBase>> results = entityResolver.resolveByUri(uriTotest);
        Assertions.assertNotNull(results);
        // For invalidValue no entity will be found
        Assertions.assertEquals(uriTotest.size() - 1, results.size());
    }

    @Test
    void testParentEntityForResolveByID() throws MalformedURLException {
        Set<ReferenceTerm> idTotest = new HashSet<>();
        idTotest.add(new ReferenceTermImpl(new URL("http://data.europeana.eu/place/176923")));
        Map<ReferenceTerm, EnrichmentBase> results = entityResolver.resolveById(idTotest);
        Assertions.assertNotNull(results);
        Assertions.assertEquals(idTotest.size(), results.size());
    }
}