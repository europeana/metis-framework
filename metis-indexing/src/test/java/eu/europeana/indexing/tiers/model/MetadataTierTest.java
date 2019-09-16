package eu.europeana.indexing.tiers.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import java.util.Arrays;
import java.util.Comparator;
import org.junit.jupiter.api.Test;

public class MetadataTierTest {

  @Test
  void verifyOrder() {
    final MetadataTier[] tiersOrderedByLevel = Arrays.stream(MetadataTier.values())
        .sorted(Comparator.comparingInt(MetadataTier::getLevel)).toArray(MetadataTier[]::new);
    final MetadataTier[] expectedOrder =
        new MetadataTier[] {MetadataTier.T0, MetadataTier.TA, MetadataTier.TB, MetadataTier.TC};
    assertArrayEquals(expectedOrder, tiersOrderedByLevel);
  }
}
