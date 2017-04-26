package eu.europeana.normalization.client;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.europeana.normalization.model.NormalizedBatchResult;

/**
 * A Client to the REST API of the language normalization service
 * 
 * @author Nuno Freire (nfreire@gmail.com)
 * @since 16/05/2016
 */
public class NormalizationClient {
    private Client client = ClientBuilder.newBuilder().build();
    private Config config;
    private ObjectMapper jsonMapper=new ObjectMapper();
 
    public NormalizationClient() {
    	config = new Config();
	}
    
    public NormalizationClient(String normalizationServiceUrl) {
		config=new Config(normalizationServiceUrl);
	}

	public NormalizedBatchResult normalize(List<String> edmXmlRecords) throws Exception {
        WebTarget target = client.target(config.getNormalizationServiceUrl()).path(
                "normalization/normalizeEdmInternal");

        
        String json = jsonMapper.writeValueAsString(edmXmlRecords);
        
//        Form form = new Form();
//		form.param("record", json);
//        target., form)
//        String requestResult =
//        target.request(MediaType.APPLICATION_XML)
//            .post(Entity.entity(form,MediaType.APPLICATION_FORM_URLENCODED_TYPE),
//                String.class);
        
//		Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.entity(form,MediaType.APPLICATION_FORM_URLENCODED_TYPE));
        Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.entity(json,MediaType.APPLICATION_JSON));
        if (response.getStatus() == 200) {
        	String normalizedEdmReport = response.readEntity(String.class);
        	
        	NormalizedBatchResult report = jsonMapper.readValue(normalizedEdmReport, NormalizedBatchResult.class);
        	
            return report;
        } else
            throw handleInvalidResponse(target, "POST", json, response);
    }

    private Exception handleInvalidResponse(WebTarget trg, String method, String message,
            Response response) {
        return new RuntimeException(method + " " + trg.getUri() + "\n " +
                                    (message == null ? "" : message) + "\nHTTPstatus: " +
                                    response.getStatus() + "\n" + response.readEntity(String.class));
    }



}
