package eu.europeana.enrichment.rest.client;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.dereference.Vocabulary;

/**
 * A REST wrapper client to be used for dereferencing
 * <p>
 * Created by ymamakis on 2/15/16.
 */
public class DereferenceClient {
	
    private final String hostUrl;
    private final RestTemplate restTemplate = new RestTemplate();

    public DereferenceClient(String hostUrl) {
        this.hostUrl = hostUrl;
    }

    /**
     * Create a vocabulary
     *
     * @param voc The vocabulary to persist
     */
    public void createVocabulary(Vocabulary voc) {
        restTemplate.postForObject(hostUrl + RestEndpoints.VOCABULARY, voc, Void.class);
    }

    /**
     * Update a vocabulary
     *
     * @param voc The vocabulary to update
     */
    public void updateVocabulary(Vocabulary voc) {
        try {
            restTemplate.put(new URI(hostUrl + RestEndpoints.VOCABULARY), voc);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * Delete a vocabulary
     *
     * @param name The vocabulary to delete
     */
    public void deleteVocabulary(String name) {
        restTemplate.delete(hostUrl + RestEndpoints.resolve(RestEndpoints.VOCABULARY_BYNAME, name));
    }

    /**
     * Retrieve the vocabulary by namedereference
     *
     * @param name The name of the vocabulary to retrieve
     * @return The retrieved vocabulary
     */
	public Vocabulary getVocabularyByName(String name) {
		return restTemplate.getForObject(hostUrl + RestEndpoints.resolve(RestEndpoints.VOCABULARY_BYNAME, name),
				Vocabulary.class);
	}

    /**
     * Retrieve all the vocabularies
     *
     * @return The list of all vocabularies
     */
	public List<Vocabulary> getAllVocabularies() {
		@SuppressWarnings("unchecked")
		final List<Vocabulary> result = restTemplate.getForObject(hostUrl + RestEndpoints.VOCABULARIES, List.class);
		return result;
    }

    /**
     * Delete an entity by URL
     *
     * @param uri The url of the entity
     */
	public void deleteEntity(String uri) {
		try {
			String encodedUri = URLEncoder.encode(uri, StandardCharsets.UTF_8.name());
			restTemplate.delete(hostUrl + RestEndpoints.resolve(RestEndpoints.ENTITY_DELETE, encodedUri));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

    /**
     * Update Entity by URL
     *
     * @param uri The url of the Entity
     * @param xml The xml to update the entity with
     */
    public void updateEntity(String uri, String xml) {
        Map<String, String> params = new HashMap<>();
        params.put("uri", uri);
        params.put("xml", xml);
        restTemplate.postForEntity(hostUrl + RestEndpoints.ENTITY, params, null);
    }

    /**
     * Dereference an entity
     *
     * @param resourceId the resource ID (URI) to dereference
     * @return A string of the referenced response
     */
	public EnrichmentResultList dereference(String resourceId) {
		final String resourceString;
		try {
		    resourceString = URLEncoder.encode(resourceId, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
		
		if (!resourceString.startsWith("http") && !resourceString.startsWith("https"))
			return null;
		
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_XML));
		HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

		final String dereferenceUrl = hostUrl + RestEndpoints.DEREFERENCE + "?uri=" + resourceString;
		return restTemplate.exchange(dereferenceUrl, HttpMethod.GET, entity, EnrichmentResultList.class).getBody();
	}
}
