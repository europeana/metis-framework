package eu.europeana.metis.core.rest.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Class using {@link ConfigurationProperties} loading.
 */
@ConfigurationProperties(prefix = "metis-core")
public class MetisCoreConfigurationProperties {

    private int maxConcurrentThreads;
    private int dpsMonitorCheckIntervalInSeconds;
    private int dpsConnectTimeoutInMilliseconds;
    private int dpsReadTimeoutInMilliseconds;
    private int failsafeMarginOfInactivityInSeconds;
    private int periodicFailsafeCheckInMilliseconds;
    private int periodicSchedulerCheckInMilliseconds;
    private int pollingTimeoutForCleaningCompletionServiceInMilliseconds;
    private int periodOfNoProcessedRecordsChangeInMinutes;
    private int threadLimitThrottlingLevelWeak;
    private int threadLimitThrottlingLevelMedium;
    private int threadLimitThrottlingLevelStrong;

    private String baseUrl;
    private int maxServedExecutionListLength;
    private int maxDepublishRecordIdsPerDataset;

    private int linkCheckingDefaultSamplingSize;
    private int solrCommitPeriodInMinutes;

    private String authenticationBaseUrl;
    private String[] allowedCorsHosts;


    public int getMaxConcurrentThreads() {
        return maxConcurrentThreads;
    }

    public void setMaxConcurrentThreads(int maxConcurrentThreads) {
        this.maxConcurrentThreads = maxConcurrentThreads;
    }

    public int getDpsMonitorCheckIntervalInSeconds() {
        return dpsMonitorCheckIntervalInSeconds;
    }

    public void setDpsMonitorCheckIntervalInSeconds(int dpsMonitorCheckIntervalInSeconds) {
        this.dpsMonitorCheckIntervalInSeconds = dpsMonitorCheckIntervalInSeconds;
    }

    public int getDpsConnectTimeoutInMilliseconds() {
        return dpsConnectTimeoutInMilliseconds;
    }

    public void setDpsConnectTimeoutInMilliseconds(int dpsConnectTimeoutInMilliseconds) {
        this.dpsConnectTimeoutInMilliseconds = dpsConnectTimeoutInMilliseconds;
    }

    public int getDpsReadTimeoutInMilliseconds() {
        return dpsReadTimeoutInMilliseconds;
    }

    public void setDpsReadTimeoutInMilliseconds(int dpsReadTimeoutInMilliseconds) {
        this.dpsReadTimeoutInMilliseconds = dpsReadTimeoutInMilliseconds;
    }

    public int getFailsafeMarginOfInactivityInSeconds() {
        return failsafeMarginOfInactivityInSeconds;
    }

    public void setFailsafeMarginOfInactivityInSeconds(int failsafeMarginOfInactivityInSeconds) {
        this.failsafeMarginOfInactivityInSeconds = failsafeMarginOfInactivityInSeconds;
    }

    public int getPeriodicFailsafeCheckInMilliseconds() {
        return periodicFailsafeCheckInMilliseconds;
    }

    public void setPeriodicFailsafeCheckInMilliseconds(int periodicFailsafeCheckInMilliseconds) {
        this.periodicFailsafeCheckInMilliseconds = periodicFailsafeCheckInMilliseconds;
    }

    public int getPeriodicSchedulerCheckInMilliseconds() {
        return periodicSchedulerCheckInMilliseconds;
    }

    public void setPeriodicSchedulerCheckInMilliseconds(int periodicSchedulerCheckInMilliseconds) {
        this.periodicSchedulerCheckInMilliseconds = periodicSchedulerCheckInMilliseconds;
    }

    public int getPollingTimeoutForCleaningCompletionServiceInMilliseconds() {
        return pollingTimeoutForCleaningCompletionServiceInMilliseconds;
    }

    public void setPollingTimeoutForCleaningCompletionServiceInMilliseconds(
        int pollingTimeoutForCleaningCompletionServiceInMilliseconds) {
        this.pollingTimeoutForCleaningCompletionServiceInMilliseconds = pollingTimeoutForCleaningCompletionServiceInMilliseconds;
    }

    public int getPeriodOfNoProcessedRecordsChangeInMinutes() {
        return periodOfNoProcessedRecordsChangeInMinutes;
    }

    public void setPeriodOfNoProcessedRecordsChangeInMinutes(int periodOfNoProcessedRecordsChangeInMinutes) {
        this.periodOfNoProcessedRecordsChangeInMinutes = periodOfNoProcessedRecordsChangeInMinutes;
    }

    public int getThreadLimitThrottlingLevelWeak() {
        return threadLimitThrottlingLevelWeak;
    }

    public void setThreadLimitThrottlingLevelWeak(int threadLimitThrottlingLevelWeak) {
        this.threadLimitThrottlingLevelWeak = threadLimitThrottlingLevelWeak;
    }

    public int getThreadLimitThrottlingLevelMedium() {
        return threadLimitThrottlingLevelMedium;
    }

    public void setThreadLimitThrottlingLevelMedium(int threadLimitThrottlingLevelMedium) {
        this.threadLimitThrottlingLevelMedium = threadLimitThrottlingLevelMedium;
    }

    public int getThreadLimitThrottlingLevelStrong() {
        return threadLimitThrottlingLevelStrong;
    }

    public void setThreadLimitThrottlingLevelStrong(int threadLimitThrottlingLevelStrong) {
        this.threadLimitThrottlingLevelStrong = threadLimitThrottlingLevelStrong;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public int getMaxServedExecutionListLength() {
        return maxServedExecutionListLength;
    }

    public void setMaxServedExecutionListLength(int maxServedExecutionListLength) {
        this.maxServedExecutionListLength = maxServedExecutionListLength;
    }

    public int getMaxDepublishRecordIdsPerDataset() {
        return maxDepublishRecordIdsPerDataset;
    }

    public void setMaxDepublishRecordIdsPerDataset(int maxDepublishRecordIdsPerDataset) {
        this.maxDepublishRecordIdsPerDataset = maxDepublishRecordIdsPerDataset;
    }

    public int getLinkCheckingDefaultSamplingSize() {
        return linkCheckingDefaultSamplingSize;
    }

    public void setLinkCheckingDefaultSamplingSize(int linkCheckingDefaultSamplingSize) {
        this.linkCheckingDefaultSamplingSize = linkCheckingDefaultSamplingSize;
    }

    public int getSolrCommitPeriodInMinutes() {
        return solrCommitPeriodInMinutes;
    }

    public void setSolrCommitPeriodInMinutes(int solrCommitPeriodInMinutes) {
        this.solrCommitPeriodInMinutes = solrCommitPeriodInMinutes;
    }

    public String getAuthenticationBaseUrl() {
        return authenticationBaseUrl;
    }

    public void setAuthenticationBaseUrl(String authenticationBaseUrl) {
        this.authenticationBaseUrl = authenticationBaseUrl;
    }

    public String[] getAllowedCorsHosts() {
      return allowedCorsHosts == null ? null : allowedCorsHosts.clone();
    }

    public void setAllowedCorsHosts(String[] allowedCorsHosts) {
      this.allowedCorsHosts = allowedCorsHosts == null ? null : allowedCorsHosts.clone();
    }
}
