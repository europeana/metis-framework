package eu.europeana.metis.dereference.service.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class IncomingRecordToEdmConverterTest {


  @Test
  public void testIsEmptyXml() {

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
