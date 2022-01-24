package eu.europeana.indexing.utils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.metis.schema.jibx.Rights1;
import java.util.Arrays;
import java.util.Comparator;
import org.junit.jupiter.api.Test;

class LicenseTypeTest {

  @Test
  void checkValues() {
    final LicenseType[] tiersOrderedByLevel = Arrays.stream(LicenseType.values())
                                                    .sorted(Comparator.comparingInt(LicenseType::getOrder))
                                                    .toArray(LicenseType[]::new);
    assertEquals(3, tiersOrderedByLevel.length);

    final LicenseType[] expectedOrder =
        new LicenseType[]{LicenseType.CLOSED, LicenseType.RESTRICTED, LicenseType.OPEN};
    assertArrayEquals(expectedOrder, tiersOrderedByLevel);

    assertEquals(MediaTier.T2, LicenseType.CLOSED.getMediaTier());
    assertEquals(MediaTier.T3, LicenseType.RESTRICTED.getMediaTier());
    assertEquals(MediaTier.T4, LicenseType.OPEN.getMediaTier());

    //Find max of two
    assertEquals(LicenseType.OPEN, LicenseType.getLicenseBinaryOperator().apply(LicenseType.CLOSED, LicenseType.OPEN));
  }

  @Test
  void getLicenseTypeTest() {
    final Rights1 rights1 = new Rights1();
    assertNull(LicenseType.getLicenseType(null));
    assertNull(LicenseType.getLicenseType(rights1));
    rights1.setResource("http://creativecommons.org/licenses/by/");
    assertEquals(LicenseType.OPEN, LicenseType.getLicenseType(rights1));
    rights1.setResource("http://creativecommons.org/licenses/by-nc/");
    assertEquals(LicenseType.RESTRICTED, LicenseType.getLicenseType(rights1));
    rights1.setResource("http://any-url");
    assertEquals(LicenseType.CLOSED, LicenseType.getLicenseType(rights1));
  }
}