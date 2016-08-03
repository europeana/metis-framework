package eu.europeana.redirects.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.europeana.redirects.model.RedirectRequest;
import eu.europeana.redirects.model.RedirectResponse;

/**
 * Created by ymamakis on 8/2/16.
 */
public class RestClientTest {

    public static void main(String[] args){
        RedirectsClient client = new RedirectsClient();
        RedirectRequest request = new RedirectRequest();
        request.setFieldName("title");
        request.setFieldValue("PLANS ET ELEVATIONS DES DIFFER: MAISONS. TOM I.");
        request.setEuropeanaId("/12345/123456");
        try {
            RedirectResponse resp = client.redirectSingle(request);
            System.out.println(resp.getNewId());
            System.out.print(resp.getOldId());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
