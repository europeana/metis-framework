package eu.europeana.enrichment.rest.client.enrichment;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class EnricherProviderTest {

  @Test
  void testIllegalArgumentException() {
    assertThrows(IllegalStateException.class, () -> new EnricherProvider().build());
    assertThrows(IllegalStateException.class,
        () -> new EnricherProvider()
            .setEnrichmentUrl("")
            .build());
  }

}
