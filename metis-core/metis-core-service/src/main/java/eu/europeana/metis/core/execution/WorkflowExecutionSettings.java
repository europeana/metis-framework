package eu.europeana.metis.core.execution;

interface WorkflowExecutionSettings {

  int getDpsMonitorCheckIntervalInSecs();

  int getMaxConcurrentThreads();

  int getPollingTimeoutForCleaningCompletionServiceInSecs();

  int getPeriodOfNoProcessedRecordsChangeInMinutes();

  String getEcloudBaseUrl();

  String getEcloudProvider();
}
