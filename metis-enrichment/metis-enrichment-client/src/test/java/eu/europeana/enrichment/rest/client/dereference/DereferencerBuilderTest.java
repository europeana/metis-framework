package eu.europeana.enrichment.rest.client.dereference;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class DereferencerBuilderTest {

  @Test
  void testIllegalArgumentException() {
    assertThrows(IllegalStateException.class, () -> new DereferencerBuilder().build());
    assertThrows(IllegalStateException.class,
        () -> new DereferencerBuilder()
            .setDereferenceUrl("")
            .setEnrichmentUrl("")
            .build());
  }

}
