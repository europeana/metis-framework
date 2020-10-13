package eu.europeana.enrichment.rest.client;

import static org.junit.jupiter.api.Assertions.assertThrows;

import eu.europeana.enrichment.rest.client.dereference.DereferencerBuilder;
import eu.europeana.enrichment.rest.client.enrichment.EnricherBuilder;
import org.junit.jupiter.api.Test;

class EnrichmentWorkerBuilderTest {

  @Test
  void testIllegalArgumentException() {
    assertThrows(IllegalStateException.class, () -> new EnrichmentWorkerBuilder().build());
    assertThrows(IllegalStateException.class,
            () -> new EnrichmentWorkerBuilder()
                .setDereferencer(
                    new DereferencerBuilder().setDereferenceUrl("").build())
                .setEnricher(new EnricherBuilder().setEnrichmentUrl("").build())
                .build());
  }

}
