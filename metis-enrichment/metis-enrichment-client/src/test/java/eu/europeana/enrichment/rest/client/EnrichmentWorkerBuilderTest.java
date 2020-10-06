package eu.europeana.enrichment.rest.client;

import static org.junit.jupiter.api.Assertions.assertThrows;

import eu.europeana.enrichment.rest.client.enrichment.EnrichmentWorkerBuilder;
import org.junit.jupiter.api.Test;

class EnrichmentWorkerBuilderTest {

  @Test
  void testIllegalArgumentException() {
    assertThrows(IllegalStateException.class, () -> new EnrichmentWorkerBuilder().build());
    assertThrows(IllegalStateException.class,
            () -> new EnrichmentWorkerBuilder().setDereferenceUrl("").setEnrichmentUrl("").build());
  }

}
