import eu.europeana.metis.RestEndpoints;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
/**
 * Created by ymamakis on 7/29/16.
 */
public class TestRestEndpointResolve {

    @Test
    public void testResolve(){
        Assert.assertTrue(StringUtils.equals(RestEndpoints.SCHEMAS_ALL,RestEndpoints.resolve(RestEndpoints.SCHEMAS_ALL)));
        Assert.assertTrue(StringUtils.equals("/organizations/crm/1",RestEndpoints.resolve(RestEndpoints.ORGANIZATIONS_CRM_ORGANIZATION_ID,"1")));
        Assert.assertTrue(StringUtils.equals("/mapping/validation/1/attribute/2",RestEndpoints.resolve(RestEndpoints.VALIDATE_DELETE_ATTRIBUTE_FLAG,"1","2")));
    }
}
