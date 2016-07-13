import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.redirects.model.RedirectRequest;
import eu.europeana.redirects.model.RedirectRequestList;
import eu.europeana.redirects.model.RedirectResponse;
import eu.europeana.redirects.model.RedirectResponseList;
import eu.europeana.redirects.rest.RedirectResource;

import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Form;

/**
 * Created by ymamakis on 1/20/16.
 */

public class RedirectsRESTTest extends JerseyTest {
    RedirectServiceStub stub;
    @Override
    public Application configure(){
         stub= Mockito.mock(RedirectServiceStub.class);
       return new eu.europeana.redirects.rest.config.Application(stub);
    }



    //TODO: ignore for now
    @Ignore
    @Test
    public void testRedirectSingle()throws Exception{

        RedirectRequest req = new RedirectRequest();
        req.setEuropeanaId("test");
        Form form = new Form();
        form.param("record", new ObjectMapper().writeValueAsString(req));
        RedirectResponse resp1 = new RedirectResponse();
        resp1.setNewId(req.getEuropeanaId());
        resp1.setOldId(req.getEuropeanaId());
        Mockito.when(stub.createRedirect(Mockito.any(RedirectRequest.class))).thenReturn(resp1);
        RedirectResponse resp = target("redirect/single").request().post(Entity.form(form)).readEntity(RedirectResponse.class);
        Assert.assertTrue(StringUtils.equals(resp.getNewId(),req.getEuropeanaId()));
    }

}
