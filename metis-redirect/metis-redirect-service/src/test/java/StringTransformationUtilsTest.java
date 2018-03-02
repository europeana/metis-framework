import eu.europeana.redirects.service.StringTransformationUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by ymamakis on 1/15/16.
 */
public class StringTransformationUtilsTest {
    @Test
    public void testTransformations(){
        Assert.assertEquals("test", StringTransformationUtils.applyTransformations("test",null));
        Assert.assertEquals("toast", StringTransformationUtils.applyTransformations("test","replace(e,oa)"));
        Assert.assertEquals("es",StringTransformationUtils.applyTransformations("test","substringBetween(t,t)"));
        Assert.assertEquals("tes",StringTransformationUtils.applyTransformations("test","substringBeforeLast(t)"));
        Assert.assertEquals("te",StringTransformationUtils.applyTransformations("test","substringBeforeFirst(s)"));
        Assert.assertEquals("t",StringTransformationUtils.applyTransformations("test","substringAfterFirst(s)"));
        Assert.assertEquals("st",StringTransformationUtils.applyTransformations("test","substringAfterLast(e)"));
        Assert.assertEquals("toatest",StringTransformationUtils.applyTransformations("test","concatBefore(toa)"));
        Assert.assertEquals("testtoa",StringTransformationUtils.applyTransformations("test","concatAfter(toa)"));
        Assert.assertEquals("ast", StringTransformationUtils.applyTransformations("test","replace(e,oa).substringAfterFirst(o)"));
    }
}
