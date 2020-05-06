package eu.europeana.metis.dereference;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.metis.dereference.IncomingRecordToEdmConverter;
import org.junit.jupiter.api.Test;

class IncomingRecordToEdmConverterTest {


  @Test
  void testIsEmptyXml() {

    assertTrue(IncomingRecordToEdmConverter.isEmptyXml(""));
    assertTrue(IncomingRecordToEdmConverter.isEmptyXml("<??>"));
    assertTrue(
        IncomingRecordToEdmConverter.isEmptyXml("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
    assertTrue(
        IncomingRecordToEdmConverter.isEmptyXml("  <?xml version=\"1.0\" encoding=\"UTF-8\"?>  "));
    assertTrue(IncomingRecordToEdmConverter
        .isEmptyXml("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!---->"));
    assertTrue(IncomingRecordToEdmConverter
        .isEmptyXml("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!-- COMMENT -->"));
    assertTrue(IncomingRecordToEdmConverter
        .isEmptyXml("  <?xml version=\"1.0\" encoding=\"UTF-8\"?> \n <!-- COMMENT -->  "));

    assertFalse(IncomingRecordToEdmConverter.isEmptyXml("A"));
    assertFalse(IncomingRecordToEdmConverter.isEmptyXml(
        "  <?xml version=\"1.0\" encoding=\"UTF-8\"?> \n <tag/> \n <!-- COMMENT --> \n <tag/>  "));
    assertFalse(IncomingRecordToEdmConverter
        .isEmptyXml("<?xml version=\"1.0\" encoding=\"UTF-8\"?><tag/><!-- COMMENT --><tag/>"));

  }
}
