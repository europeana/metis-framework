package eu.europeana.enrichment.profile;

import eu.europeana.enrichment.api.internal.*;
import eu.europeana.enrichment.utils.EntityType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class AopProfileTest {

    private static EntityResolver remoteEntityResolver;
    private static Set<SearchTerm> textSearchTerms = new HashSet<>();
    private static Set<ReferenceTerm> uriTotest = new HashSet<>();
    private static Set<ReferenceTerm> idToTest = new HashSet<>();


    @BeforeAll
    static void setup() throws MalformedURLException {
        ApplicationContext context = new AnnotationConfigApplicationContext(AspectJConfig.class);
        remoteEntityResolver = context.getBean(EntityResolver.class);

        textSearchTerms.add(new SearchTermImpl("belgium", "en", Set.of(EntityType.PLACE)));
        textSearchTerms.add(new SearchTermImpl("Paris", "en", Set.of(EntityType.PLACE)));
        textSearchTerms.add(new SearchTermImpl("test", "en", Set.of(EntityType.AGENT)));
        textSearchTerms.add(new SearchTermImpl("Vermeer", "it", Set.of(EntityType.AGENT)));

        uriTotest.add(new ReferenceTermImpl(new URL("http://dbpedia.org/resource/Johannes_Vermeer"), Set.of(EntityType.AGENT)));
        uriTotest.add(new ReferenceTermImpl(new URL("http://dbpedia.org/resource/Johannes_Vermeer"), Set.of(EntityType.AGENT)));

        idToTest.add(new ReferenceTermImpl(new URL("http://data.europeana.eu/agent/base/123_test"), Set.of(EntityType.AGENT)));
        idToTest.add(new ReferenceTermImpl(new URL("http://data.europeana.eu/agent/base/test"), Set.of(EntityType.AGENT)));
    }

    @Test
    void testProfiling(){
        remoteEntityResolver.resolveByText(textSearchTerms);
        remoteEntityResolver.resolveByUri(uriTotest);
        remoteEntityResolver.resolveById(idToTest);
    }

}
