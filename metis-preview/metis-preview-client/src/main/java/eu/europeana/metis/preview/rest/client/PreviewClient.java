package eu.europeana.metis.preview.rest.client;

import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.preview.service.ExtendedValidationResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;

/**
 * A REST client for the preview service
 * Created by ymamakis on 9/5/16.
 */
public class PreviewClient {

    private String previewUrl;
    private RestTemplate restTemplate = new RestTemplate();

    /**
     * Default constructor for the preview REST client
     * @param previewUrl
     */
    public PreviewClient(String previewUrl){
        this.previewUrl = previewUrl;
    }

    /**
     * Persist and preview records
     * @param recordFile The records to persist
     * @param collectionId The collection id (can be null)
     * @param edmExternal Whether the records are in EDM-External
     * @return The result with the preview URL
     */
    public ExtendedValidationResult previewRecords(File recordFile, String collectionId, boolean edmExternal){
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("file", new FileSystemResource(recordFile));
        if(StringUtils.isNotEmpty(collectionId)) {
            parts.add("collectionId", collectionId);
        }
        parts.add("edmExternal", edmExternal);
        return restTemplate.postForObject(previewUrl+ RestEndpoints.PREVIEW_UPLOAD,
                parts,ExtendedValidationResult.class);
    }
}
