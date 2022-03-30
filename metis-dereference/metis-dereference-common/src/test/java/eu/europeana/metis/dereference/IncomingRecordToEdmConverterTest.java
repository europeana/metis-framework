package eu.europeana.metis.dereference;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class IncomingRecordToEdmConverterTest {

  @Test
  void testIsEmptyXml() {
    assertTrue(IncomingRecordToEdmConverter.isEmptyXml("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
    assertTrue(IncomingRecordToEdmConverter.isEmptyXml("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\t"));
    assertFalse(IncomingRecordToEdmConverter.isEmptyXml("  <?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
    assertFalse(IncomingRecordToEdmConverter.isEmptyXml("<?xml version=\"1.0\" encoding=\"UTF-8\"?><root/>"));
    assertFalse(IncomingRecordToEdmConverter.isEmptyXml("<?xml version=\"1.0\" encoding=\"UTF-8\"?><root></root>"));
  }
}

