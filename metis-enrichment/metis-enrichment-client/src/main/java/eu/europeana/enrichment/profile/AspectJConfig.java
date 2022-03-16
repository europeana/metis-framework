package eu.europeana.enrichment.profile;

import eu.europeana.enrichment.api.internal.EntityResolver;
import eu.europeana.enrichment.rest.client.ConnectionProvider;
import eu.europeana.enrichment.rest.client.enrichment.RemoteEntityResolver;
import org.springframework.context.annotation.*;

import java.net.MalformedURLException;
import java.net.URL;

@EnableAspectJAutoProxy
public class AspectJConfig {

    // TODO move this to a property file. Will be done in EA-2890
    private static final String enrichmentUrl = "http://localhost:8080";

    public int getBatchSize() {
        return ConnectionProvider.DEFAULT_BATCH_SIZE_ENRICHMENT;
    }

    @Bean
    SimpleProfiler simpleProfiler() {
        return new SimpleProfiler();
    }

    @Bean
    EntityResolver remoteEntityResolver() throws MalformedURLException {
        return new RemoteEntityResolver(new URL(enrichmentUrl), getBatchSize(), new ConnectionProvider().createRestTemplate());
    }
}