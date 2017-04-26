package eu.europeana.metis.linkchecking.client;

import eu.europeana.metis.linkchecking.EdmFieldName;
import eu.europeana.metis.linkchecking.LinkcheckRequest;
import eu.europeana.metis.linkchecking.LinkcheckStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ymamakis on 11/4/16.
 */
public class LicheckingClientTest {
    public static void main(String[] args){
        LinkcheckingClient client = new LinkcheckingClient("http://metis-linkchecking-test.cfapps.io");
        LinkcheckRequest request = new LinkcheckRequest();
        request.setFieldName(EdmFieldName.EDM_ISHOWNBY);
        String url = "http://www.europeana.eu";
        List<String> urls = new ArrayList<>();
        urls.add(url);
        request.setUrls(urls);
        List<LinkcheckRequest> requests = new ArrayList<>();
        requests.add(request);
        List<LinkcheckStatus> status = client.getLinkCheckingReport(requests);
        System.out.println(status.size());
    }
}
