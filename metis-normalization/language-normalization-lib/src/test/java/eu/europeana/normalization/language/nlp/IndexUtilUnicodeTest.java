package eu.europeana.normalization.language.nlp;

import org.junit.Assert;
import org.junit.Test;

import eu.europeana.normalization.language.nlp.IndexUtilUnicode;

/**
 * Tests unicode character normalization.
 * 
 * @author Nuno Freire (nfreire@gmail.com)
 * @since 11/05/2016
 */
public class IndexUtilUnicodeTest {
    /**
     * Tests unicode normalization.
     * 
     * @throws Exception
     */
    @Test
    public void testSimpleNGrams() throws Exception {
        String input = "Hello World!?#$^*!@#*(\0x309A";
        String encode = IndexUtilUnicode.encode(input, true);
        Assert.assertFalse(encode.contains(" "));
    }
}
