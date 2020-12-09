package eu.europeana.metis.core.execution;

/**
 * These are settings that are all related to the actual execution of workflows, and used mostly by
 * the classes {@link WorkflowExecutor} and {@link QueueConsumer}.
 */
interface WorkflowExecutionSettings {

  int getDpsMonitorCheckIntervalInSecs();

  int getPeriodOfNoProcessedRecordsChangeInMinutes();

  String getEcloudBaseUrl();

  String getEcloudProvider();

  String getMetisCoreBaseUrl();
}
