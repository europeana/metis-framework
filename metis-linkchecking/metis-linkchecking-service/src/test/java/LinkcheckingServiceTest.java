import eu.europeana.metis.linkchecking.EdmFieldName;
import eu.europeana.metis.linkchecking.LinkcheckRequest;
import eu.europeana.metis.linkchecking.LinkcheckStatus;
import eu.europeana.metis.linkchecking.service.LinkcheckingService;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ymamakis on 11/7/16.
 */

public class LinkcheckingServiceTest {

    private LinkcheckingService service = new LinkcheckingService();
    @Test
    public void testSuccess(){
        List<LinkcheckRequest> links = new ArrayList<>();
        LinkcheckRequest request = new LinkcheckRequest();
        request.setFieldName(EdmFieldName.EDM_ISHOWNBY);
        List<String> urls = new ArrayList<>();
        urls.add("http://www.google.com");
        request.setUrls(urls);
        links.add(request);
        List<LinkcheckStatus> statuses = service.generateLinkCheckingReport(links);
        Assert.assertNotNull(statuses);
        Assert.assertEquals(1,statuses.size());
        LinkcheckStatus status =statuses.get(0);
        Assert.assertEquals(status.getEdmFieldName(),EdmFieldName.EDM_ISHOWNBY);
        Assert.assertEquals(status.getSucceeded(),1);
        Assert.assertEquals(status.getFailed(),0);
    }

    @Test
    public void testError(){
        List<LinkcheckRequest> links = new ArrayList<>();
        LinkcheckRequest request = new LinkcheckRequest();
        request.setFieldName(EdmFieldName.EDM_ISHOWNBY);
        List<String> urls = new ArrayList<>();
        urls.add("http://www.shouldnotexisteverfortheloveofgod.com");
        request.setUrls(urls);
        links.add(request);
        List<LinkcheckStatus> statuses = service.generateLinkCheckingReport(links);
        Assert.assertNotNull(statuses);
        Assert.assertEquals(1,statuses.size());
        LinkcheckStatus status =statuses.get(0);
        Assert.assertEquals(status.getEdmFieldName(),EdmFieldName.EDM_ISHOWNBY);
        Assert.assertEquals(status.getSucceeded(),0);
        Assert.assertEquals(status.getFailed(),1);
    }
}
