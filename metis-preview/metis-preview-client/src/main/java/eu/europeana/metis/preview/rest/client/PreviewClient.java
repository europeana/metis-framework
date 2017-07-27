package eu.europeana.metis.preview.rest.client;

import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.preview.common.model.ExtendedValidationResult;
import java.io.File;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * A REST client for the preview service
 * Created by ymamakis on 9/5/16.
 */
public class PreviewClient {

    private String previewUrl;
    private RestTemplate restTemplate = new RestTemplate();

    /**
     * Default constructor for the preview REST client
     * @param previewUrl the url where the REST api of the preview service can be found
     */
    public PreviewClient(String previewUrl){
        this.previewUrl = previewUrl;
    }

    /**
     * Persist and preview records
     * @param recordFile A zip file with the records to persist
     * @param collectionId The collection id (can be null)
     * @param edmExternal Whether the records are in EDM-External
     * @param crosswalk The path of the crosswalk (null or EDM_external2internal_v2_repox.xsl)
     * @param requestIndividualRecordsIds return the ids of each created record
     * @return The result with the preview URL and the record ids
     */
    public ExtendedValidationResult previewRecords(File recordFile, String collectionId, boolean edmExternal, String crosswalk, boolean requestIndividualRecordsIds){
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("file", new FileSystemResource(recordFile));
        if(StringUtils.isNotEmpty(collectionId)) {
            parts.add("collectionId", collectionId);
        }
        parts.add("edmExternal", edmExternal);
        if(StringUtils.isNotEmpty(crosswalk)) {
            parts.add("crosswalk", crosswalk);
        }
        parts.add("individualRecords",requestIndividualRecordsIds);
        return restTemplate.postForObject(previewUrl+ RestEndpoints.PREVIEW_UPLOAD,
                parts,ExtendedValidationResult.class);
    }
}
