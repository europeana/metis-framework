/*
 * Copyright 2007-2013 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */
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
