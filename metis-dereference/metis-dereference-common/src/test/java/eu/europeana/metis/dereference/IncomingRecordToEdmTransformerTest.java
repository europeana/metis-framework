package eu.europeana.metis.dereference;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class IncomingRecordToEdmTransformerTest {

  @Test
  void testIsEmptyXml() {
    assertTrue(IncomingRecordToEdmTransformer.isEmptyXml("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
    assertTrue(IncomingRecordToEdmTransformer.isEmptyXml("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\t"));
    assertFalse(IncomingRecordToEdmTransformer.isEmptyXml("  <?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
    assertFalse(IncomingRecordToEdmTransformer.isEmptyXml("<?xml version=\"1.0\" encoding=\"UTF-8\"?><root/>"));
    assertFalse(IncomingRecordToEdmTransformer.isEmptyXml("<?xml version=\"1.0\" encoding=\"UTF-8\"?><root></root>"));
  }
}

