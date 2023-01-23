package eu.europeana.metis.utils;

import static eu.europeana.metis.utils.RestEndpoints.AUTHENTICATION_DELETE;
import static eu.europeana.metis.utils.RestEndpoints.AUTHENTICATION_LOGIN;
import static eu.europeana.metis.utils.RestEndpoints.AUTHENTICATION_REGISTER;
import static eu.europeana.metis.utils.RestEndpoints.AUTHENTICATION_UPDATE;
import static eu.europeana.metis.utils.RestEndpoints.AUTHENTICATION_UPDATE_PASSD;
import static eu.europeana.metis.utils.RestEndpoints.AUTHENTICATION_UPDATE_ROLE_ADMIN;
import static eu.europeana.metis.utils.RestEndpoints.AUTHENTICATION_USERS;
import static eu.europeana.metis.utils.RestEndpoints.AUTHENTICATION_USER_BY_TOKEN;
import static eu.europeana.metis.utils.RestEndpoints.AUTHENTICATION_USER_BY_USER_ID;
import static eu.europeana.metis.utils.RestEndpoints.CACHE_EMPTY;
import static eu.europeana.metis.utils.RestEndpoints.DATASETS;
import static eu.europeana.metis.utils.RestEndpoints.DATASETS_COUNTRIES;
import static eu.europeana.metis.utils.RestEndpoints.DATASETS_DATAPROVIDER;
import static eu.europeana.metis.utils.RestEndpoints.DATASETS_DATASETID;
import static eu.europeana.metis.utils.RestEndpoints.DATASETS_DATASETID_XSLT;
import static eu.europeana.metis.utils.RestEndpoints.DATASETS_DATASETID_XSLT_TRANSFORM;
import static eu.europeana.metis.utils.RestEndpoints.DATASETS_DATASETID_XSLT_TRANSFORM_DEFAULT;
import static eu.europeana.metis.utils.RestEndpoints.DATASETS_DATASETNAME;
import static eu.europeana.metis.utils.RestEndpoints.DATASETS_INTERMEDIATE_PROVIDER;
import static eu.europeana.metis.utils.RestEndpoints.DATASETS_LANGUAGES;
import static eu.europeana.metis.utils.RestEndpoints.DATASETS_ORGANIZATION_ID;
import static eu.europeana.metis.utils.RestEndpoints.DATASETS_ORGANIZATION_NAME;
import static eu.europeana.metis.utils.RestEndpoints.DATASETS_PROVIDER;
import static eu.europeana.metis.utils.RestEndpoints.DATASETS_SEARCH;
import static eu.europeana.metis.utils.RestEndpoints.DATASETS_XSLT_DEFAULT;
import static eu.europeana.metis.utils.RestEndpoints.DATASETS_XSLT_XSLTID;
import static eu.europeana.metis.utils.RestEndpoints.DEPUBLISH_EXECUTE_DATASETID;
import static eu.europeana.metis.utils.RestEndpoints.DEPUBLISH_RECORDIDS_DATASETID;
import static eu.europeana.metis.utils.RestEndpoints.DEREFERENCE;
import static eu.europeana.metis.utils.RestEndpoints.LOAD_VOCABULARIES;
import static eu.europeana.metis.utils.RestEndpoints.ORCHESTRATOR_PROXIES_RECORDS;
import static eu.europeana.metis.utils.RestEndpoints.ORCHESTRATOR_PROXIES_RECORDS_BY_IDS;
import static eu.europeana.metis.utils.RestEndpoints.ORCHESTRATOR_PROXIES_TOPOLOGY_TASK_LOGS;
import static eu.europeana.metis.utils.RestEndpoints.ORCHESTRATOR_PROXIES_TOPOLOGY_TASK_NODE_STATISTICS;
import static eu.europeana.metis.utils.RestEndpoints.ORCHESTRATOR_PROXIES_TOPOLOGY_TASK_REPORT;
import static eu.europeana.metis.utils.RestEndpoints.ORCHESTRATOR_PROXIES_TOPOLOGY_TASK_REPORT_EXISTS;
import static eu.europeana.metis.utils.RestEndpoints.ORCHESTRATOR_PROXIES_TOPOLOGY_TASK_STATISTICS;
import static eu.europeana.metis.utils.RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID;
import static eu.europeana.metis.utils.RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID_EXECUTE;
import static eu.europeana.metis.utils.RestEndpoints.ORCHESTRATOR_WORKFLOWS_EVOLUTION;
import static eu.europeana.metis.utils.RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS;
import static eu.europeana.metis.utils.RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS_DATASET_DATASETID;
import static eu.europeana.metis.utils.RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS_DATASET_DATASETID_ALLOWED_INCREMENTAL;
import static eu.europeana.metis.utils.RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS_DATASET_DATASETID_ALLOWED_PLUGIN;
import static eu.europeana.metis.utils.RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS_DATASET_DATASETID_HISTORY;
import static eu.europeana.metis.utils.RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS_DATASET_DATASETID_INFORMATION;
import static eu.europeana.metis.utils.RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS_EXECUTIONID;
import static eu.europeana.metis.utils.RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS_EXECUTIONID_PLUGINS_DATA_AVAILABILITY;
import static eu.europeana.metis.utils.RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS_OVERVIEW;
import static eu.europeana.metis.utils.RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE;
import static eu.europeana.metis.utils.RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE_DATASETID;
import static eu.europeana.metis.utils.RestEndpoints.REPOSITORY_HTTP_ENDPOINT_ZIP;
import static eu.europeana.metis.utils.RestEndpoints.REPOSITORY_OAI_ENDPOINT;
import static eu.europeana.metis.utils.RestEndpoints.REPOSITORY_RECORDS;
import static eu.europeana.metis.utils.RestEndpoints.REPOSITORY_RECORDS_RECORD_ID;
import static eu.europeana.metis.utils.RestEndpoints.REPOSITORY_RECORDS_RECORD_ID_HEADER;
import static eu.europeana.metis.utils.RestEndpoints.VOCABULARIES;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Unit test for {@link RestEndpoints}
 *
 * @author Jorge Ortiz
 * @since 31-01-2022
 */
class RestEndpointsTest {

  private static Stream<Arguments> provideEndpointsAndParameters() {
    return Stream.of(
        Arguments.of(DATASETS, List.of(""), "/datasets"),
        Arguments.of(DATASETS_DATASETID, List.of("datasetId"), "/datasets/datasetId"),
        Arguments.of(DATASETS_DATASETID_XSLT, List.of("datasetId"), "/datasets/datasetId/xslt"),
        Arguments.of(DATASETS_DATASETID_XSLT_TRANSFORM, List.of("datasetId"), "/datasets/datasetId/xslt/transform"),
        Arguments.of(DATASETS_DATASETID_XSLT_TRANSFORM_DEFAULT, List.of("datasetId"),
            "/datasets/datasetId/xslt/transform/default"),
        Arguments.of(DATASETS_XSLT_DEFAULT, List.of(""), "/datasets/xslt/default"),
        Arguments.of(DATASETS_XSLT_XSLTID, List.of("xsltId"), "/datasets/xslt/xsltId"),
        Arguments.of(DATASETS_DATASETNAME, List.of("datasetName"), "/datasets/dataset_name/datasetName"),
        Arguments.of(DATASETS_DATAPROVIDER, List.of("dataProvider"), "/datasets/data_provider/dataProvider"),
        Arguments.of(DATASETS_PROVIDER, List.of("provider"), "/datasets/provider/provider"),
        Arguments.of(DATASETS_INTERMEDIATE_PROVIDER, List.of("intermediateProvider"),
            "/datasets/intermediate_provider/intermediateProvider"),
        Arguments.of(DATASETS_ORGANIZATION_ID, List.of("organizationId"), "/datasets/organization_id/organizationId"),
        Arguments.of(DATASETS_ORGANIZATION_NAME, List.of("organizationName"), "/datasets/organization_name/organizationName"),
        Arguments.of(DATASETS_COUNTRIES, List.of(""), "/datasets/countries"),
        Arguments.of(DATASETS_LANGUAGES, List.of(""), "/datasets/languages"),
        Arguments.of(DATASETS_SEARCH, List.of(""), "/datasets/search"),

        Arguments.of(DEPUBLISH_RECORDIDS_DATASETID, List.of("datasetId"), "/depublish/record_ids/datasetId"),
        Arguments.of(DEPUBLISH_EXECUTE_DATASETID, List.of("datasetId"), "/depublish/execute/datasetId"),

        Arguments.of(AUTHENTICATION_REGISTER, List.of(""), "/authentication/register"),
        Arguments.of(AUTHENTICATION_LOGIN, List.of(""), "/authentication/login"),
        Arguments.of(AUTHENTICATION_DELETE, List.of(""), "/authentication/delete"),
        Arguments.of(AUTHENTICATION_UPDATE, List.of(""), "/authentication/update"),
        Arguments.of(AUTHENTICATION_UPDATE_PASSD, List.of(""), "/authentication/update/password"),
        Arguments.of(AUTHENTICATION_UPDATE_ROLE_ADMIN, List.of(""), "/authentication/update/role/admin"),
        Arguments.of(AUTHENTICATION_USER_BY_TOKEN, List.of(""), "/authentication/user_by_access_token"),
        Arguments.of(AUTHENTICATION_USER_BY_USER_ID, List.of(""), "/authentication/user_by_user_id"),
        Arguments.of(AUTHENTICATION_USERS, List.of(""), "/authentication/users"),

        Arguments.of(ORCHESTRATOR_WORKFLOWS_DATASETID, List.of("datasetId"), "/orchestrator/workflows/datasetId"),
        Arguments.of(ORCHESTRATOR_WORKFLOWS_DATASETID_EXECUTE, List.of("datasetId"), "/orchestrator/workflows/datasetId/execute"),
        Arguments.of(ORCHESTRATOR_WORKFLOWS_SCHEDULE, List.of(""), "/orchestrator/workflows/schedule"),
        Arguments.of(ORCHESTRATOR_WORKFLOWS_SCHEDULE_DATASETID, List.of("datasetId"), "/orchestrator/workflows/schedule/datasetId"),
        Arguments.of(ORCHESTRATOR_WORKFLOWS_EXECUTIONS_EXECUTIONID, List.of("executionId"), "/orchestrator/workflows/executions/executionId"),
        Arguments.of(ORCHESTRATOR_WORKFLOWS_EXECUTIONS_EXECUTIONID_PLUGINS_DATA_AVAILABILITY, List.of("executionId"), "/orchestrator/workflows/executions/executionId/plugins/data-availability"),
        Arguments.of(ORCHESTRATOR_WORKFLOWS_EXECUTIONS_DATASET_DATASETID, List.of("datasetId"), "/orchestrator/workflows/executions/dataset/datasetId"),
        Arguments.of(ORCHESTRATOR_WORKFLOWS_EXECUTIONS_DATASET_DATASETID_ALLOWED_INCREMENTAL, List.of("datasetId"), "/orchestrator/workflows/executions/dataset/datasetId/allowed_incremental"),
        Arguments.of(ORCHESTRATOR_WORKFLOWS_EXECUTIONS_DATASET_DATASETID_ALLOWED_PLUGIN, List.of("datasetId"), "/orchestrator/workflows/executions/dataset/datasetId/allowed_plugin"),
        Arguments.of(ORCHESTRATOR_WORKFLOWS_EXECUTIONS_DATASET_DATASETID_HISTORY, List.of("datasetId"), "/orchestrator/workflows/executions/dataset/datasetId/history"),
        Arguments.of(ORCHESTRATOR_WORKFLOWS_EXECUTIONS_DATASET_DATASETID_INFORMATION, List.of("datasetId"), "/orchestrator/workflows/executions/dataset/datasetId/information"),
        Arguments.of(ORCHESTRATOR_WORKFLOWS_EXECUTIONS, List.of(""), "/orchestrator/workflows/executions"),
        Arguments.of(ORCHESTRATOR_WORKFLOWS_EXECUTIONS_OVERVIEW, List.of(""), "/orchestrator/workflows/executions/overview"),
        Arguments.of(ORCHESTRATOR_WORKFLOWS_EVOLUTION, List.of("workflowExecutionId","pluginType"), "/orchestrator/workflows/evolution/workflowExecutionId/pluginType"),
        Arguments.of(ORCHESTRATOR_PROXIES_TOPOLOGY_TASK_LOGS, List.of("topologyName","externalTaskId"), "/orchestrator/proxies/topologyName/task/externalTaskId/logs"),
        Arguments.of(ORCHESTRATOR_PROXIES_TOPOLOGY_TASK_REPORT, List.of("topologyName","externalTaskId"), "/orchestrator/proxies/topologyName/task/externalTaskId/report"),
        Arguments.of(ORCHESTRATOR_PROXIES_TOPOLOGY_TASK_REPORT_EXISTS, List.of("topologyName","externalTaskId"), "/orchestrator/proxies/topologyName/task/externalTaskId/report/exists"),
        Arguments.of(ORCHESTRATOR_PROXIES_TOPOLOGY_TASK_STATISTICS, List.of("topologyName","externalTaskId"), "/orchestrator/proxies/topologyName/task/externalTaskId/statistics"),
        Arguments.of(ORCHESTRATOR_PROXIES_TOPOLOGY_TASK_NODE_STATISTICS, List.of("topologyName","externalTaskId"), "/orchestrator/proxies/topologyName/task/externalTaskId/nodestatistics"),
        Arguments.of(ORCHESTRATOR_PROXIES_RECORDS, List.of(""), "/orchestrator/proxies/records"),
        Arguments.of(ORCHESTRATOR_PROXIES_RECORDS_BY_IDS, List.of(""), "/orchestrator/proxies/recordsbyids"),

        Arguments.of(DEREFERENCE, List.of(""), "/dereference"),
        Arguments.of(VOCABULARIES, List.of(""), "/vocabularies"),
        Arguments.of(CACHE_EMPTY, List.of(""), "/cache"),
        Arguments.of(LOAD_VOCABULARIES, List.of(""), "/load_vocabularies"),

        Arguments.of(REPOSITORY_RECORDS, List.of(""), "/repository/records"),
        Arguments.of(REPOSITORY_RECORDS_RECORD_ID, List.of("recordId"), "/repository/records/recordId"),
        Arguments.of(REPOSITORY_RECORDS_RECORD_ID_HEADER, List.of("recordId"), "/repository/records/recordId/header"),
        Arguments.of(REPOSITORY_HTTP_ENDPOINT_ZIP, List.of("dataset"), "/repository/zip/dataset.zip"),
        Arguments.of(REPOSITORY_OAI_ENDPOINT, List.of(""), "/repository/oai"),
        Arguments.of(REPOSITORY_RECORDS_RECORD_ID, List.of("recordId"), "/repository/records/recordId"),
        Arguments.of(REPOSITORY_RECORDS_RECORD_ID, List.of("recordId"), "/repository/records/recordId"),
        Arguments.of(REPOSITORY_RECORDS_RECORD_ID, List.of("recordId"), "/repository/records/recordId")
    );
  }

  @ParameterizedTest
  @MethodSource("provideEndpointsAndParameters")
  void resolve(String endpoint, List<String> parameters, String expectedEndpoint) {
    final String actualEndpoint = RestEndpoints.resolve(endpoint, parameters);

    assertEquals(expectedEndpoint, actualEndpoint);
  }
}