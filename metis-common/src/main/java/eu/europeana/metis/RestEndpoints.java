package eu.europeana.metis;

import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * REST Endpoints Created by ymamakis on 7/29/16.
 */
public final class RestEndpoints {
  /* METIS-CORE Endopoints*/

  //DATASETS
  public static final String DATASETS = "/datasets";
  public static final String DATASETS_DATASETID = "/datasets/{datasetId}";
  public static final String DATASETS_DATASETID_XSLT = "/datasets/{datasetId}/xslt";
  public static final String DATASETS_DATASETID_XSLT_TRANSFORM = "/datasets/{datasetId}/xslt/transform";
  public static final String DATASETS_DATASETID_XSLT_TRANSFORM_DEFAULT = "/datasets/{datasetId}/xslt/transform/default";
  public static final String DATASETS_XSLT_DEFAULT = "/datasets/xslt/default";
  public static final String DATASETS_XSLT_XSLTID = "/datasets/xslt/{xsltId}";
  public static final String DATASETS_DATASETNAME = "/datasets/dataset_name/{datasetName}";
  public static final String DATASETS_DATAPROVIDER = "/datasets/data_provider/{dataProvider}";
  public static final String DATASETS_PROVIDER = "/datasets/provider/{provider}";
  public static final String DATASETS_INTERMEDIATE_PROVIDER = "/datasets/intermediate_provider/{intermediateProvider}";
  public static final String DATASETS_ORGANIZATION_ID = "/datasets/organization_id/{organizationId}";
  public static final String DATASETS_ORGANIZATION_NAME = "/datasets/organization_name/{organizationName}";
  public static final String DATASETS_COUNTRIES = "/datasets/countries";
  public static final String DATASETS_LANGUAGES = "/datasets/languages";
  public static final String DATASETS_SEARCH = "/datasets/search";

  // DEPUBLISHED RECORDS
  public static final String DEPUBLISH_RECORDIDS_DATASETID = "/depublish/record_ids/{datasetId}";
  public static final String DEPUBLISH_EXECUTE_DATASETID = "/depublish/execute/{datasetId}";

  //AUTHENTICATION
  public static final String AUTHENTICATION_REGISTER = "/authentication/register";
  public static final String AUTHENTICATION_LOGIN = "/authentication/login";
  public static final String AUTHENTICATION_DELETE = "/authentication/delete";
  public static final String AUTHENTICATION_UPDATE = "/authentication/update";
  public static final String AUTHENTICATION_UPDATE_PASSD = "/authentication/update/password";
  public static final String AUTHENTICATION_UPDATE_ROLE_ADMIN = "/authentication/update/role/admin";
  public static final String AUTHENTICATION_USER_BY_TOKEN = "/authentication/user_by_access_token";
  public static final String AUTHENTICATION_USER_BY_USER_ID = "/authentication/user_by_user_id";
  public static final String AUTHENTICATION_USERS = "/authentication/users";

  //ORCHESTRATION
  public static final String ORCHESTRATOR_WORKFLOWS_DATASETID = "/orchestrator/workflows/{datasetId}";
  public static final String ORCHESTRATOR_WORKFLOWS_DATASETID_EXECUTE = "/orchestrator/workflows/{datasetId}/execute";
  public static final String ORCHESTRATOR_WORKFLOWS_DATASETID_EXECUTE_DIRECT = "/orchestrator/workflows/{datasetId}/execute/direct";
  public static final String ORCHESTRATOR_WORKFLOWS_SCHEDULE = "/orchestrator/workflows/schedule";
  public static final String ORCHESTRATOR_WORKFLOWS_SCHEDULE_DATASETID = "/orchestrator/workflows/schedule/{datasetId}";
  public static final String ORCHESTRATOR_WORKFLOWS_EXECUTIONS_EXECUTIONID = "/orchestrator/workflows/executions/{executionId}";
  public static final String ORCHESTRATOR_WORKFLOWS_EXECUTIONS_EXECUTIONID_PLUGINS_DATA_AVAILABILITY = "/orchestrator/workflows/executions/{executionId}/plugins/data-availability";
  public static final String ORCHESTRATOR_WORKFLOWS_EXECUTIONS_DATASET_DATASETID = "/orchestrator/workflows/executions/dataset/{datasetId}";
  public static final String ORCHESTRATOR_WORKFLOWS_EXECUTIONS_DATASET_DATASETID_ALLOWED_PLUGIN = "/orchestrator/workflows/executions/dataset/{datasetId}/allowed_plugin";
  public static final String ORCHESTRATOR_WORKFLOWS_EXECUTIONS_DATASET_DATASETID_HISTORY = "/orchestrator/workflows/executions/dataset/{datasetId}/history";
  public static final String ORCHESTRATOR_WORKFLOWS_EXECUTIONS_DATASET_DATASETID_INFORMATION = "/orchestrator/workflows/executions/dataset/{datasetId}/information";
  public static final String ORCHESTRATOR_WORKFLOWS_EXECUTIONS = "/orchestrator/workflows/executions";
  public static final String ORCHESTRATOR_WORKFLOWS_EXECUTIONS_OVERVIEW = "/orchestrator/workflows/executions/overview";
  public static final String ORCHESTRATOR_WORKFLOWS_EVOLUTION = "/orchestrator/workflows/evolution/{workflowExecutionId}/{pluginType}";
  public static final String ORCHESTRATOR_PROXIES_TOPOLOGY_TASK_LOGS = "/orchestrator/proxies/{topologyName}/task/{externalTaskId}/logs";
  public static final String ORCHESTRATOR_PROXIES_TOPOLOGY_TASK_REPORT = "/orchestrator/proxies/{topologyName}/task/{externalTaskId}/report";
  public static final String ORCHESTRATOR_PROXIES_TOPOLOGY_TASK_REPORT_EXISTS = "/orchestrator/proxies/{topologyName}/task/{externalTaskId}/report/exists";
  public static final String ORCHESTRATOR_PROXIES_TOPOLOGY_TASK_STATISTICS = "/orchestrator/proxies/{topologyName}/task/{externalTaskId}/statistics";
  public static final String ORCHESTRATOR_PROXIES_TOPOLOGY_TASK_NODE_STATISTICS = "/orchestrator/proxies/{topologyName}/task/{externalTaskId}/nodestatistics";
  public static final String ORCHESTRATOR_PROXIES_RECORDS = "/orchestrator/proxies/records";
  public static final String ORCHESTRATOR_PROXIES_RECORDS_BY_IDS = "/orchestrator/proxies/recordsbyids";

  /* METIS-DEREFERENCE Endpoints*/
  public static final String DEREFERENCE = "/dereference";
  public static final String VOCABULARIES = "/vocabularies";
  public static final String CACHE_EMPTY = "/cache";
  public static final String LOAD_VOCABULARIES = "/load_vocabularies";

  /* METIS ENRICHMENT Endpoint */
  public static final String ENRICH_ENTITY_ABOUT = "/enrich/entity_about";
  public static final String ENRICH_ENTITY_ABOUT_OR_OWLSAMEAS = "/enrich/entity_about_or_owl_same_as";
  public static final String ENRICH_INPUT_VALUE_LIST = "/enrich/input_value_list";

  /* METIS SCHEMA VALIDATION ENDPOINT */
  public static final String SCHEMA_VALIDATE = "/schema/validate/{schema}";
  public static final String SCHEMA_BATCH_VALIDATE = "/schema/validate/batch/{schema}";
  public static final String SCHEMAS_DOWNLOAD_BY_NAME = "/schemas/download/schema/{name}/{version}";
  public static final String SCHEMAS_MANAGE_BY_NAME = "/schemas/schema/{name}/{version}";
  public static final String SCHEMAS_UPDATE_BY_NAME = "/schemas/schema/update/{name}/{version}";
  public static final String SCHEMAS_ALL = "/schemas/all";

  /* METIS PREVIEW SERVICE ENDPOINT*/
  public static final String DATA_CHECKER_UPLOAD = "/upload";

  private RestEndpoints() {
  }

  /**
   * Resolves an endpoint with parameters wrapped around "{" and "}" by providing the endpoint and
   * all the required parameters.
   *
   * @param endpoint the endpoint to resolve
   * @param params all the parameters specified
   * @return the resolved endpoint
   */
  public static String resolve(String endpoint, List<String> params) {
    if (params == null || params.isEmpty()) {
      return endpoint;
    }
    String[] test = StringUtils.split(endpoint, "{");
    StringBuilder fin = new StringBuilder();
    int i = 0;
    for (String en : test) {
      if (i == 0) {
        fin = new StringBuilder(en);
      } else {
        fin.append(
            StringUtils.replace(en, StringUtils.substringBefore(en, "}") + "}", params.get(i - 1)));
      }
      i++;
    }
    return fin.toString();
  }
}

