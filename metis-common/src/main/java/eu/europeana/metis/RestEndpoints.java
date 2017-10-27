package eu.europeana.metis;

import org.apache.commons.lang3.StringUtils;

/**
 * REST Endpoints Created by ymamakis on 7/29/16.
 */
public final class RestEndpoints {

  private RestEndpoints() {
  }

    /* METIS-CORE Endopoints*/

  //ORGANIZATIONS
  public static final String ORGANIZATIONS = "/organizations";
  public static final String ORGANIZATIONS_ORGANIZATION_ID = "/organizations/{organizationId}";
  public static final String ORGANIZATIONS_COUNTRY_ISOCODE = "/organizations/country/{isoCode}";
  public static final String ORGANIZATIONS_ROLES = "/organizations/roles";
  public static final String ORGANIZATIONS_SUGGEST = "/organizations/suggest";
  public static final String ORGANIZATIONS_ORGANIZATION_ID_DATASETS = "/organizations/{organizationId}/datasets";
  public static final String ORGANIZATIONS_ORGANIZATION_ID_OPTINIIIF = "/organizations/{organizationId}/optInIIIF";
  public static final String ORGANIZATIONS_CRM_ORGANIZATION_ID = "/organizations/crm/{organizationId}";
  public static final String ORGANIZATIONS_CRM = "/organizations/crm";

  //DATASETS
  public static final String DATASETS = "/datasets";
  public static final String DATASETS_DATASETNAME = "/datasets/{datasetName}";
  public static final String DATASETS_DATASETNAME_UPDATENAME = "/datasets/{datasetName}/updateName";
  public static final String DATASETS_DATAPROVIDER = "/datasets/data_provider/{dataProvider}";

  //AUTHENTICATION
  public static final String AUTHENTICATION_REGISTER = "/authentication/register";
  public static final String AUTHENTICATION_LOGIN = "/authentication/login";
  //USERS
  public static final String USER = "/user";
  public static final String USERBYMAIL = "/user/{email}";

  //ORCHESTRATION
  public static final String ORCHESTRATOR_USERWORKFLOWS = "/orchestrator/user_workflows";
  public static final String ORCHESTRATOR_USERWORKFLOWS_OWNER = "/orchestrator/user_workflows/{workflowOwner}";
  public static final String ORCHESTRATOR_USERWORKFLOWS_DATASETNAME_EXECUTE = "/orchestrator/user_workflows/{datasetName}/execute";
  public static final String ORCHESTRATOR_USERWORKFLOWS_DATASETNAME_EXECUTE_DIRECT = "/orchestrator/user_workflows/{datasetName}/execute/direct";
  public static final String ORCHESTRATOR_USERWORKFLOWS_SCHEDULE = "/orchestrator/user_workflows/schedule";
  public static final String ORCHESTRATOR_USERWORKFLOWS_SCHEDULE_DATASETNAME = "/orchestrator/user_workflows/schedule/{datasetName}";
  public static final String ORCHESTRATOR_USERWORKFLOWS_EXECUTION_DATASETNAME = "/orchestrator/user_workflows/execution/{datasetName}";
  public static final String ORCHESTRATOR_USERWORKFLOWS_EXECUTIONS_DATASETNAME = "/orchestrator/user_workflows/executions/{datasetName}";
  public static final String ORCHESTRATOR_USERWORKFLOWS_EXECUTIONS = "/orchestrator/user_workflows/executions";

  /* METIS-DEREFERENCE Endpoints*/
  public static final String DEREFERENCE = "/dereference";
  public static final String VOCABULARY = "/vocabulary";
  public static final String VOCABULARY_BYNAME = "/vocabulary/{name}";
  public static final String VOCABULARIES = "/vocabularies";
  public static final String ENTITY = "/entity";
  public static final String ENTITY_DELETE = "/entity/{uri}";
  public static final String CACHE_EMPTY = "/cache";

  /* METIS ENRICHMENT Endpoint */
  public static final String ENRICHMENT_DELETE = "/delete";
  public static final String ENRICHMENT_BYURI = "/getByUri";
  public static final String ENRICHMENT_ENRICH = "/enrich";

  /* METIS IDENTIFIER ITEMIZATION Endpoint */
  public static final String IDENTIFIER_GENERATE = "/identifier/generate/{collectionId}";
  public static final String IDENTIFIER_NORMALIZE_SINGLE = "/identifier/normalize/single";
  public static final String IDENTIFIER_NORMALIZE_BATCH = "/identifier/normalize/batch";
  public static final String ITEMIZE_URL = "/itemize/url";
  public static final String ITEMIZE_RECORDS = "/itemize/records";
  public static final String ITEMIZE_FILE = "/itemize/file";

  /*METIS REDIRECTS Endpoint*/
  public static final String REDIRECT_SINGLE = "/redirect/single";
  public static final String REDIRECT_BATCH = "/redirect/batch";

  /* METIS SCHEMA VALIDATION ENDPOINT */
  public static final String SCHEMA_VALIDATE = "/schema/validate/{schema}/{version}";
  public static final String SCHEMA_BATCH_VALIDATE = "/schema/validate/batch/{schema}/{version}";
  public static final String SCHEMA_RECORDS_BATCH_VALIDATE = "/schema/validate/batch/records/{schema}/{version}";
  public static final String SCHEMAS_DOWNLOAD_BY_NAME = "/schemas/download/schema/{name}/{version}";
  public static final String SCHEMAS_MANAGE_BY_NAME = "/schemas/schema/{name}/{version}";
  public static final String SCHEMAS_UPDATE_BY_NAME = "/schemas/schema/update/{name}/{version}";
  public static final String SCHEMAS_ALL = "/schemas/all";

  /* METIS PREVIEW SERVICE ENDPOINT*/
  public static final String PREVIEW_UPLOAD = "/upload";

  /* METIS LINKCHECK SERVICE ENDPOINT*/
  public static final String LINKCHECK = "/linkcheck";


  public static String resolve(String endpoint, String... params) {
    if (params == null || params.length == 0) {
      return endpoint;
    }
    String[] test = StringUtils.split(endpoint, "{");
    String fin = "";
    int i = 0;
    for (String en : test) {
      if (i == 0) {
        fin = en;
      } else {
        fin += StringUtils.replace(en, StringUtils.substringBefore(en, "}") + "}", params[i - 1]);
      }
      i++;
    }
    return fin;
  }
}

