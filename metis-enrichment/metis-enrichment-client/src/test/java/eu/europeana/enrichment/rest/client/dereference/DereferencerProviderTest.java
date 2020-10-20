package eu.europeana.enrichment.rest.client.dereference;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class DereferencerProviderTest {

  @Test
  void testIllegalArgumentException() {
    assertThrows(IllegalStateException.class, () -> new DereferencerProvider().build());
    assertThrows(IllegalStateException.class,
        () -> new DereferencerProvider()
            .setDereferenceUrl("")
            .setEnrichmentUrl("")
            .build());
  }

}
