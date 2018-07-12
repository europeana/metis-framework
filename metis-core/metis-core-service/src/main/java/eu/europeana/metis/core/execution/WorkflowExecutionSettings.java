package eu.europeana.metis.core.execution;

interface WorkflowExecutionSettings {

  int getDpsMonitorCheckIntervalInSecs();

  int getMaxConcurrentThreads();

  int getPollingTimeoutForCleaningCompletionServiceInSecs();

  String getEcloudBaseUrl();

  String getEcloudProvider();
}
