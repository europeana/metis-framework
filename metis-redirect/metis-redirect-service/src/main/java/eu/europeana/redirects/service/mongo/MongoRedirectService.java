package eu.europeana.redirects.service.mongo;

import eu.europeana.corelib.tools.lookuptable.CollectionMongoServer;
import eu.europeana.corelib.tools.lookuptable.EuropeanaId;
import eu.europeana.corelib.tools.lookuptable.EuropeanaIdMongoServer;
import eu.europeana.corelib.utils.EuropeanaUriUtils;
import eu.europeana.redirects.model.RedirectRequest;
import eu.europeana.redirects.model.RedirectRequestList;
import eu.europeana.redirects.model.RedirectResponse;
import eu.europeana.redirects.model.RedirectResponseList;
import eu.europeana.redirects.params.ControlledParams;
import eu.europeana.redirects.service.RedirectService;
import eu.europeana.redirects.service.StringTransformationUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Mongo Implementation of the Europeana Redirect Service
 * <p/>
 * Created by ymamakis on 1/13/16.
 */
public class MongoRedirectService implements RedirectService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoRedirectService.class);
    private static final String EUROPEANA_ID = "europeana_id";

    @Autowired
    private EuropeanaIdMongoServer mongoServer;
    @Autowired
    private CloudSolrServer productionSolrServer;
    @Autowired
    private CollectionMongoServer collectionMongoServer;

    /**
     * Handle a request for a redirect
     *
     * @param request The object that describes what Europeana Identifier should be redirected and how
     * @return A response specifying whether the redirect was generated or not consisting of the new identifier and
     * the old identifier. The old identifier can be null if the redirect was not generated
     */
    public RedirectResponse createRedirect(RedirectRequest request) {
        String newId = request.getEuropeanaId();

        RedirectResponse response = new RedirectResponse();
        response.setNewId(newId);
        String finalId;


        //FIRST CHECK IF THERE IS A COLLECTION NAME CHANGE
        CollectionMongoServer collecitonMongoServer = collectionMongoServer;
        String oldCollectionId = collecitonMongoServer.findOldCollectionId(request.getCollection());
        if (oldCollectionId != null) {
            finalId = EuropeanaUriUtils.createEuropeanaId(oldCollectionId, StringUtils.substringAfterLast(newId, "/"));
            if (request.getParameters() != null) {
                finalId = StringTransformationUtils.applyTransformations(finalId,
                        request.getParameters().get(ControlledParams.REDIRECT_USE_CUSTOM_FUNCTIONS.toString()));
            }

        } else if (request.getFieldName() != null) {
            //IF THERE ARE NONE GET THE CUSTOM FIELD POTENTIALLY APPLYING ANY TRANSFORMATION GET THE ID AND SAVE
            String finalFieldValue = request.getFieldValue();
            if (request.getParameters() != null) {
                finalFieldValue = StringTransformationUtils.applyTransformations(finalFieldValue,
                        request.getParameters().get(ControlledParams.REDIRECT_USE_CUSTOM_FUNCTIONS.toString()));
            }
            finalId = checkIdExists(request.getFieldName(), finalFieldValue);
        } else {
            //IF THERE ARE NONE APPLY THE TRANSFORMATION ON THE ID AND SAVE
            finalId = StringTransformationUtils.applyTransformations(newId,
                    request.getParameters().get(ControlledParams.REDIRECT_USE_CUSTOM_FUNCTIONS.toString()));
        }
        if (finalId != null && checkIdExists(EUROPEANA_ID, finalId) != null) {
            response.setOldId(generateEuropeanaId(newId, finalId));
        }
        return response;
    }


    /**
     * Check if a unique id exists in Solr based on a field and a value
     *
     * @param field The field to query
     * @param value The value of a field
     * @return The Europeana Id field value if the Solr response had exactly 1 response or null other wise
     */
    private String checkIdExists(String field, String value) {
        ModifiableSolrParams params = new ModifiableSolrParams();
        params.set("q", ClientUtils.escapeQueryChars(field + ":" + value));
        params.set("rows", 1);
        params.set("fl", EUROPEANA_ID);
        try {
            QueryResponse resp = productionSolrServer.query(params);
            SolrDocumentList list = resp.getResults();
            if (list.getNumFound() == 1) {
                return list.get(0).getFieldValue(EUROPEANA_ID).toString();
            }
        } catch (SolrServerException e) {
            LOGGER.error("Solr exception when querying" , e);
        }
        return null;
    }

    /**
     * Generate and save a Europeana Id
     *
     * @param oldId The old Europeana identifier
     * @param newId The new Europeana identifier on which the redirect will be generated
     * @return The old identifier or null if the redirect was not generated (already existing EuropeanaId)
     */
    private String generateEuropeanaId(String newId, String oldId) {
        EuropeanaId id = mongoServer.retrieveEuropeanaIdFromOld(Collections.singletonList(oldId));
        if (id == null || !StringUtils.equals(id.getNewId(), newId)) {
            EuropeanaId europeanaId = new EuropeanaId();
            europeanaId.setOldId(oldId);
            europeanaId.setNewId(newId);
            europeanaId.setTimestamp(new Date().getTime());
            mongoServer.saveEuropeanaId(europeanaId);
            return oldId;
        }
        return null;
    }

    /**
     * Batch operation for redirect requests
     *
     * @param requests The object that contains a list of objects to be redirected
     * @return A list of redirect generation responses
     */
    public RedirectResponseList createRedirects(RedirectRequestList requests) {
        RedirectResponseList list = new RedirectResponseList();
        List<RedirectResponse> responseList = new ArrayList<>(requests.getRequestList().size());
        for (RedirectRequest req : requests.getRequestList()) {
            responseList.add(createRedirect(req));
        }
        list.setResponseList(responseList);
        return list;
    }
}
