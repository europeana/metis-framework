package eu.europeana.normalization.language.client;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import javax.ws.rs.FormParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBElement;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * A Client to the REST API of the language normalization service
 * 
 * @author Nuno Freire (nfreire@gmail.com)
 * @since 16/05/2016
 */
public class NormalizationLanguageClient {
    private Client client = ClientBuilder.newBuilder().build();
    private Config config = new Config();

    public List<String> normalize(String value) throws Exception {
        WebTarget target = client.target(config.getNormalizationLanguageServiceUrl()).path(
                "normalize").queryParam("value", value);
        Response response = target.request().get();
        if (response.getStatus() == 200) {
            List<String> normalizedValues = response.readEntity(new GenericType<List<String>>() {
            });
            return normalizedValues;
        } else
            throw handleInvalidResponse(target, "GET", value, response);
    }

    private Exception handleInvalidResponse(WebTarget trg, String method, String message,
            Response response) {
        return new RuntimeException(method + " " + trg.getUri() + "\n " +
                                    (message == null ? "" : message) + "\nHTTPstatus: " +
                                    response.getStatus() + "\n" + response.readEntity(String.class));
    }

}
