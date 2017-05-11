package eu.europeana.metis;

import org.apache.commons.lang.StringUtils;

/**
 * REST Endpoints
 * Created by ymamakis on 7/29/16.
 */
public final class RestEndpoints {

    /* METIS-CORE Endopoints*/

    public final static String DATASET ="/dataset";
    public final static String DATASET_RETRIEVE = "/dataset/{name}";
    public final static String DATASET_BYPROVIDER = "/datasets/{dataProviderId}";
    public final static String ORGANIZATION = "/organization/{apikey}";
    public final static String ORGANIZATIONS = "/organizations";
    public final static String ORGANIZATIONS_ISOCODE = "/organizations/country";
    public final static String ORGANIZATIONS_ROLES = "/organizations/roles";
    public final static String ORGANIZATION_ID ="/organization/{id}";
    public final static String ORGANIZATION_ID_DATASETS="/organization/{id}/datasets";
    public final static String ORGANIZATION_OPTED_IN="/organization/{id}/optin";
    public final static String ORGANIZATION_SUGGEST = "/organizations/suggest/{suggestTerm}";
    public final static String CRM_ORGANIZATION_ID = "/crm/organization/{orgId}";
    public final static String CRM_ORGANIZATIONS="/crm/organizations";
    public final static String USER="/user";
    public final static String USERBYMAIL="/user/{email}";
    public final static String USERBYID ="/contact/{id}";
    public final static String ORGANIZATIONS_BYDATASET="/organizations/dataset/{id}";

    /* METIS-DEREFERENCE Endpoints*/
    public final static String DEREFERENCE="/dereference";
    public final static String VOCABULARY="/vocabulary";
    public final static String VOCABULARY_BYNAME="/vocabulary/{name}";
    public final static String VOCABULARIES = "/vocabularies";
    public final static String ENTITY="/entity";
    public final static String ENTITY_DELETE="/entity/{uri}";
    public final static String CACHE_EMPTY="/cache";

    /* METIS ENRICHMENT Endpoint */
    public final static String ENRICHMENT_DELETE = "/delete";
    public final static String ENRICHMENT_BYURI="/getByUri";
    public final static String ENRICHMENT_ENRICH="/enrich";

    /* METIS IDENTIFIER ITEMIZATION Endpoint */
    public final static String IDENTIFIER_GENERATE ="/identifier/generate/{collectionId}";
    public final static String IDENTIFIER_NORMALIZE_SINGLE = "/identifier/normalize/single";
    public final static String IDENTIFIER_NORMALIZE_BATCH = "/identifier/normalize/batch";
    public final static String ITEMIZE_URL = "/itemize/url";
    public final static String ITEMIZE_RECORDS = "/itemize/records";
    public final static String ITEMIZE_FILE = "/itemize/file";

    /*METIS REDIRECTS Endpoint*/
    public final static String REDIRECT_SINGLE="/redirect/single";
    public final static String REDIRECT_BATCH="/redirect/batch";

    /* METIS PANDORA Endpoint */
    public final static String MAPPING = "/mapping";
    public final static String MAPPING_BYID="/mapping/{mappingId}";
    public final static String MAPPING_DATASETNAME="/mapping/dataset/{name}";
    public final static String MAPPINGS_BYORGANIZATIONID="/mappings/organization/{orgId}";
    public final static String MAPPINGS_NAMES_BYORGANIZATIONID="/mappings/names/organization/{orgId}";
    public final static String MAPPING_TEMPLATES= "/mapping/templates";
    public final static String MAPPING_STATISTICS_BYNAME="/mapping/statistics/{name}";
    public final static String MAPPING_SCHEMATRON="/mapping/schematron";
    public final static String MAPPING_NAMESPACES="/mapping/namespaces";
    public final static String MAPPING_STATISTICS_ELEMENT="/mapping/statistics/{datasetId}/element";
    public final static String MAPPING_STATISTICS_ATTRIBUTE="/mapping/statistics/{datasetId}/attribute";
    public final static String STATISTICS_CALCULATE="/statistics/calculate/{datasetId}";
    public final static String STATISTICS_APPEND="/statistics/append/{datasetId}";
    public final static String VALIDATE_ATTRIBUTE="/mapping/validation/{mappingId}/attribute";
    public final static String VALIDATE_ELEMENT="/mapping/validation/{mappingId}/element";
    public final static String VALIDATE_CREATE_ATTTRIBUTE_FLAG = "/mapping/validation/{mappingId}/attribute/create/{value}/{flagType}";
    public final static String VALIDATE_CREATE_ELEMENT_FLAG="/mapping/validation/{mappingId}/element/create/{value}/{flagType}";
    public final static String VALIDATE_MAPPING="/mapping/validation/validate";
    public final static String XSD_UPLOAD = "/xsd/upload";
    public final static String XSD_URL = "/xsd/url";
    public final static String XSL_GENERATE="/xsl/generate";
    public final static String XSL_MAPPINGID="/xsl/{mappingId}";
    public final static String VALIDATE_DELETE_ATTRIBUTE_FLAG = "/mapping/validation/{mappingId}/attribute/{value}";
    public final static String VALIDATE_DELETE_ELEMENT_FLAG ="/mapping/validation/{mappingId}/element/{value}";

    /* METIS SCHEMA VALIDATION ENDPOINT */
    public final static String SCHEMA_VALIDATE = "/schema/validate/{schema}/{version}";
    public final static String SCHEMA_BATCH_VALIDATE = "/schema/validate/batch/{schema}/{version}";
    public final static String SCHEMA_RECORDS_BATCH_VALIDATE = "/schema/validate/batch/records/{schema}/{version}";
    public final static String SCHEMAS_DOWNLOAD_BY_NAME = "/schemas/download/schema/{name}/{version}";
    public final static String SCHEMAS_MANAGE_BY_NAME = "/schemas/schema/{name}/{version}";
    public final static String SCHEMAS_UPDATE_BY_NAME = "/schemas/schema/update/{name}/{version}";
    public final static String SCHEMAS_ALL = "/schemas/all";

    /* METIS PREVIEW SERVICE ENDPOINT*/
    public final static String PREVIEW_UPLOAD = "/upload";

    /* METIS LINKCHECK SERVICE ENDPOINT*/
    public final static String LINKCHECK = "/linkcheck";

    /* METIS ORCHESTRATION ENDPOINT*/
    public final static String ORCHESTRATION_TRIGGER_OPERATION="/orchestration/{datasetId}/{operation}";
    public final static String ORCHESTRATION_SCHEDULE="/orchestration/schedule/{datasetId}/{operation}/{millis}";
    public final static String ORCHESTRATION_SCHEDULED="/orchestration/scheduled";
    public final static String ORCHESTRATION_ACTIVE = "/orchestration/active";
    public final static String ORCHESTRATION_BYID = "/orchestration/execution/{executionId}";
    public final static String ORCHESTRATION_RANGE="/orchestration/range";
    public final static String ORCHESTRATION_EXECUTIONS="/orchestration";
    public final static String ORCHESTRATION_DATASET="/orchestration/{datasetId}";
    public final static String ORCHESTRATION_RANGE_DATASET="/orchestration/range/{datasetId}";
    public final static String ORCHESTRATION_OPERATIONS="/orchestration/operations";
    public final static String ORCHESTRATION_FAILED="/orchestration/failed/{executionId}";

    public static String resolve(String endpoint, String... params){
        if(params==null || params.length==0){
            return endpoint;
        }
        String[] test = StringUtils.split(endpoint,"{");
        String fin = "";
        int i=0;
        for (String en:test){
            if(i==0){
                fin=en;
            } else {
                fin += StringUtils.replace(en,StringUtils.substringBefore(en,"}")+"}", params[i-1]);
            }
            i++;
        }
        return fin;
    }
}

