package eu.europeana.metis.core.workflow;


import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * The execution model
 * Created by ymamakis on 11/9/16.
 */
@Entity
public class Execution {
    /**
     * The id of the executiob
     */
    @Id
    private ObjectId id;

    /**
     * When the execution was started in Europeana Cloud
     */
    @Indexed
    private Date startedAt;

    /**
     * When the execution finished in Europeana Cloud
     */
    @Indexed
    private Date finishedAt;

    /**
     * When the execution stastics were updated
     */
    @Indexed
    private Date updatedAt;

    /**
     * The name of the implementing workflow
     */
    @Indexed
    private String workflow;

    /**
     * The dataset identifier
     */
    @Indexed
    private String datasetId;

    /**
     * When the execution of the workflow is scheduled
     */
    @Indexed
    private Date scheduledAt;

    /**
     * If the workflow is active
     */
    @Indexed
    private boolean active;

    /**
     * If the execution is cancelled
     */
    @Indexed
    private boolean cancelled;

    /**
     * The METIS statistics URL
     */
    private String statisticsUrl;

    /**
     * The Europeana Cloud statistics URL
     */
    private String cloudStatisticsUrl;

    /**
     * The execution parameters
     */
    private Map<String,List<String>> executionParameters;

    /**
     * The statistics for this execution
     */
    @Embedded
    private ExecutionStatistics statistics;

    @Indexed
    private String operatorEmail;

    public String getOperatorEmail() {
        return operatorEmail;
    }

    public void setOperatorEmail(String operatorEmail) {
        this.operatorEmail = operatorEmail;
    }


    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public Date getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Date startedAt) {
        this.startedAt = startedAt;
    }

    public Date getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Date finishedAt) {
        this.finishedAt = finishedAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getWorkflow() {
        return workflow;
    }

    public void setWorkflow(String workflow) {
        this.workflow = workflow;
    }

    public String getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }

    public Date getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(Date scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public ExecutionStatistics getStatistics() {
        return statistics;
    }

    public void setStatistics(ExecutionStatistics statistics) {
        this.statistics = statistics;
    }

    public String getStatisticsUrl() {
        return statisticsUrl;
    }

    public void setStatisticsUrl(String statisticsUrl) {
        this.statisticsUrl = statisticsUrl;
    }

    public Map<String, List<String>> getExecutionParameters() {
        return executionParameters;
    }

    public void setExecutionParameters(Map<String, List<String>> executionParameters) {
        this.executionParameters = executionParameters;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public String getCloudStatisticsUrl() {
        return cloudStatisticsUrl;
    }

    public void setCloudStatisticsUrl(String cloudStatisticsUrl) {
        this.cloudStatisticsUrl = cloudStatisticsUrl;
    }
}
