package eu.europeana.identifier.service.test;

import eu.europeana.identifier.service.IdentifierService;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by ymamakis on 2/9/16.
 */

public class IdentifierServiceTest {
    @Test
    public void testGenerate(){
        Assert.assertTrue(StringUtils.equals("/12345/abc_def", new IdentifierService().generateIdentifier("12345a","abc?def")));
        Assert.assertFalse(StringUtils.equals("/12345/abc_def", new IdentifierService().generateIdentifier("12345a","abcgdef")));
    }
}
