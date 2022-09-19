package eu.europeana.normalization.dates.extraction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.normalization.dates.edtf.EdtfParser;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import java.text.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link DcmiPeriod} class
 */
class DcmiPeriodTest {

  private DcmiPeriod dcmiPeriod;
  private InstantEdtfDate start;
  private InstantEdtfDate end;

  @BeforeEach
  void setup() throws ParseException {
    start = (InstantEdtfDate) new EdtfParser().parse("1986-07-09");
    end = (InstantEdtfDate) new EdtfParser().parse("1986-07-12");
  }

  @Test
  void getStart() {
    dcmiPeriod = new DcmiPeriod(start, end);

    assertEquals(start, dcmiPeriod.getStart());
  }

  @Test
  void getEnd() {
    dcmiPeriod = new DcmiPeriod(start, end);

    assertEquals(end, dcmiPeriod.getEnd());
  }

  @Test
  void getName() {
    dcmiPeriod = new DcmiPeriod(start, end, "Rijksmuseum International Arts Festival, 1986");

    assertEquals("Rijksmuseum International Arts Festival, 1986", dcmiPeriod.getName());
  }

  @Test
  void isClosed() {
    dcmiPeriod = new DcmiPeriod(start, end);

    assertTrue(dcmiPeriod.isClosed());
  }

  @Test
  void isNotClosed() {
    dcmiPeriod = new DcmiPeriod(start, null);

    assertFalse(dcmiPeriod.isClosed());
  }

  @Test
  void hasStart() {
    dcmiPeriod = new DcmiPeriod(start, end);

    assertTrue(dcmiPeriod.hasStart());
  }

  @Test
  void doesNotHasStart() {
    dcmiPeriod = new DcmiPeriod(null, end);

    assertFalse(dcmiPeriod.hasStart());
  }

  @Test
  void hasEnd() {
    dcmiPeriod = new DcmiPeriod(start, end);

    assertTrue(dcmiPeriod.hasEnd());
  }

  @Test
  void doesNotHasEnd() {
    dcmiPeriod = new DcmiPeriod(start, null);

    assertFalse(dcmiPeriod.hasEnd());
  }

  @Test
  void hasName() {
    dcmiPeriod = new DcmiPeriod(start, end, "Rijksmuseum International Arts Festival, 1986");

    assertTrue(dcmiPeriod.hasName());
  }

  @Test
  void doesNotHasName() {
    dcmiPeriod = new DcmiPeriod(start, end);

    assertFalse(dcmiPeriod.hasName());
  }

  @Test
  void testToString() {
    dcmiPeriod = new DcmiPeriod(start, end, "Rijksmuseum International Arts Festival, 1986");
    assertEquals("DCMIPeriod{start=1986-07-09, end=1986-07-12, name='Rijksmuseum International Arts Festival, 1986'}",
        dcmiPeriod.toString());
  }

  @Test
  void errorInitialization() {
    assertThrows(IllegalStateException.class, () -> dcmiPeriod = new DcmiPeriod(null, null));
  }
}
