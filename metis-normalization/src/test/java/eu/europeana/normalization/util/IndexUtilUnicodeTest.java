package eu.europeana.normalization.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests unicode character normalization.
 *
 * @author Nuno Freire (nfreire@gmail.com)
 * @since 11/05/2016
 */
public class IndexUtilUnicodeTest {

  /**
   * Tests unicode normalization.
   */
  @Test
  public void testSimpleNGrams() throws Exception {
    String input = "Hello World!?#$^*!@#*(\0x309A";
    String encode = IndexUtilUnicode.encode(input, true);
    Assert.assertFalse(encode.contains(" "));
  }
}
