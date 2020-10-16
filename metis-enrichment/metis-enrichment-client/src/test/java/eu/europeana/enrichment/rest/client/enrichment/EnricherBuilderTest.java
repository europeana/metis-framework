package eu.europeana.enrichment.rest.client.enrichment;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class EnricherBuilderTest {

  @Test
  void testIllegalArgumentException() {
    assertThrows(IllegalStateException.class, () -> new EnricherBuilder().build());
    assertThrows(IllegalStateException.class,
        () -> new EnricherBuilder()
            .setEnrichmentUrl("")
            .build());
  }

}
