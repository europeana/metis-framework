package eu.europeana.enrichment.rest.client;

import static eu.europeana.metis.RestEndpoints.ENRICHMENT_BYURI;
import static eu.europeana.metis.RestEndpoints.ENRICHMENT_ENRICH;
import java.util.List;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import eu.europeana.enrichment.api.exceptions.UnknownException;
import eu.europeana.enrichment.api.external.InputValueList;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.enrichment.utils.InputValue;

/**
 * REST API wrapper class abstracting the REST calls and providing a clean POJO
 * implementation
 *
 * @author Yorgos.Mamakis@ europeana.eu
 */
public class EnrichmentClient {
    private final String path;
  
	public EnrichmentClient(String path) {
		this.path = path;
	}

	/**
	 * Enrich REST call invocation
	 *
	 * @param values
	 *            The values to be enriched
	 * @return The enrichments generated for the input values
	 */
	public EnrichmentResultList enrich(List<InputValue> values) {
		RestTemplate template = new RestTemplate();

		InputValueList inList = new InputValueList();
	    inList.setInputValueList(values);
	        
        try {
        	return template.postForObject(path + ENRICHMENT_ENRICH, inList, EnrichmentResultList.class);
        } catch (Exception e){
            throw new UnknownException(e.getMessage());
        }
    }

    public EnrichmentBase getByUri(String uri) {    	   
		RestTemplate template = new RestTemplate();

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(path + ENRICHMENT_BYURI).queryParam("uri", uri);

		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", MediaType.APPLICATION_XML_VALUE);
		final HttpEntity<Void> request = new HttpEntity<>(headers);

		final ResponseEntity<EnrichmentBase> response = template.exchange(builder.build(true).toUri(), HttpMethod.GET,
				request, EnrichmentBase.class);

		return response.getBody();
    }
}
