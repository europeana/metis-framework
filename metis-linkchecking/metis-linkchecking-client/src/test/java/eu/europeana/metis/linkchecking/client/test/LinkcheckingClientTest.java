package eu.europeana.metis.linkchecking.client.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.linkchecking.EdmFieldName;
import eu.europeana.metis.linkchecking.LinkcheckRequest;
import eu.europeana.metis.linkchecking.LinkcheckStatus;
import eu.europeana.metis.linkchecking.client.LinkcheckingClient;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Created by ymamakis on 11/7/16.
 */
public class LinkcheckingClientTest {

    MockRestServiceServer server;

    @Test
    public void testSuccess() throws JsonProcessingException {
        RestTemplate template = new RestTemplate();
        LinkcheckingClient client = new LinkcheckingClient(template);

        server = MockRestServiceServer.createServer(template);
        List<LinkcheckStatus> response = generateResponse(true);

        server.expect(requestTo(RestEndpoints.LINKCHECK)).andRespond(withSuccess().contentType(MediaType.APPLICATION_JSON).body(new ObjectMapper().writeValueAsBytes(response)));
        List<LinkcheckRequest> links = new ArrayList<>();
        LinkcheckRequest request = new LinkcheckRequest();
        request.setFieldName(EdmFieldName.EDM_ISHOWNBY);
        List<String> urls = new ArrayList<>();
        urls.add("http://www.google.com");
        request.setUrls(urls);
        links.add(request);
        List<LinkcheckStatus> statuses = client.getLinkCheckingReport(links);
        Assert.assertNotNull(statuses);
        Assert.assertEquals(1,statuses.size());
    }


    private List<LinkcheckStatus> generateResponse(boolean succeeded) {
        LinkcheckStatus status = new LinkcheckStatus();
        status.setEdmFieldName(EdmFieldName.EDM_ISHOWNBY);
        status.setFailed(succeeded?0:1);
        status.setSucceeded(succeeded?1:0);
        List<LinkcheckStatus> statuses = new ArrayList<>();
        statuses.add(status);
        return statuses;
    }
}
