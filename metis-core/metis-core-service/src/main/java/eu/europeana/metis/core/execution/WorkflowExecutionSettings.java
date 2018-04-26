package eu.europeana.metis.core.execution;

interface WorkflowExecutionSettings {

  int getMonitorCheckIntervalInSecs();

  int getMaxConcurrentThreads();

  int getPollingTimeoutForCleaningCompletionServiceInSecs();

  String getEcloudBaseUrl();

  String getEcloudProvider();
}
