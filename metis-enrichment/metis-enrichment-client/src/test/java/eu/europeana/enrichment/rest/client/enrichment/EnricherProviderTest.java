package eu.europeana.enrichment.rest.client.enrichment;

import static org.junit.jupiter.api.Assertions.assertThrows;

import eu.europeana.enrichment.rest.client.exceptions.EnrichmentException;
import org.junit.jupiter.api.Test;

public class EnricherProviderTest {

  @Test
  void testIllegalArgumentException() {
    assertThrows(EnrichmentException.class, () -> new EnricherProvider().create());
    assertThrows(EnrichmentException.class,
        () -> {
      EnricherProvider provider = new EnricherProvider();
            provider.setEnrichmentPropertiesValues("", "", "");
            provider.create();
    });
  }
}
