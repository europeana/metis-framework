package eu.europeana.indexing.tiers.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Arrays;
import java.util.Comparator;
import org.junit.jupiter.api.Test;

class MetadataTierTest {

  @Test
  void checkValues() {
    final MetadataTier[] tiersOrderedByLevel = Arrays.stream(MetadataTier.values())
                                                     .sorted(Comparator.comparingInt(MetadataTier::getLevel))
                                                     .toArray(MetadataTier[]::new);
    assertEquals(4, tiersOrderedByLevel.length);
    final MetadataTier[] expectedOrder =
        new MetadataTier[]{MetadataTier.T0, MetadataTier.TA, MetadataTier.TB, MetadataTier.TC};
    assertArrayEquals(expectedOrder, tiersOrderedByLevel);

    //Check comparators
    assertSame(MetadataTier.TA, Tier.max(MetadataTier.T0, MetadataTier.TA));
    assertSame(MetadataTier.TA, Tier.max(MetadataTier.TA, MetadataTier.T0));
    assertSame(MetadataTier.T0, Tier.max(MetadataTier.T0, MetadataTier.T0));

    assertEquals("0", MetadataTier.T0.toString());
    assertEquals("A", MetadataTier.TA.toString());
    assertEquals("B", MetadataTier.TB.toString());
    assertEquals("C", MetadataTier.TC.toString());
  }
}
