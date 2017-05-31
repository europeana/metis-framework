package eu.europeana.metis.core.workflow.plugins;

import eu.europeana.metis.core.workflow.CloudStatistics;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mongodb.morphia.annotations.Indexed;

/**
 * Created by ymamakis on 11/15/16.
 */
public class VoidMetisPlugin implements AbstractMetisPlugin {
    @Indexed
    private String id;
    private PluginStatus pluginStatus = PluginStatus.INQUEUE;
    private final PluginType pluginType = PluginType.VOID;
    private Map<String, List<String>> parameters = new HashMap<>();

    @Indexed
    private Date startedDate;
    @Indexed
    private Date updatedDate;
    @Indexed
    private Date finishedDate;

    private long recordsProcessed;
    private long recordsFailed;
    private long recordsCreated;
    private long recordsUpdated;
    private long recordsDeleted;

    public VoidMetisPlugin() {
    }


    public VoidMetisPlugin(VoidMetisPluginInfo voidMetisPluginInfo)
    {
        if (voidMetisPluginInfo != null)
            this.parameters = voidMetisPluginInfo.getParameters();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public PluginStatus getPluginStatus() {
        return pluginStatus;
    }

    @Override
    public void setPluginStatus(PluginStatus pluginStatus) {
        this.pluginStatus = pluginStatus;
    }

    public PluginType getPluginType() {
        return pluginType;
    }

    @Override
    public Date getStartedDate() {
        return startedDate;
    }

    @Override
    public void setStartedDate(Date startedDate) {
        this.startedDate = startedDate;
    }

    @Override
    public Date getFinishedDate() {
        return finishedDate;
    }

    @Override
    public void setFinishedDate(Date finishedDate) {
        this.finishedDate = finishedDate;
    }

    @Override
    public Date getUpdatedDate() {
        return updatedDate;
    }

    @Override
    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    @Override
    public long getRecordsProcessed() {
        return recordsProcessed;
    }

    @Override
    public void setRecordsProcessed(long recordsProcessed) {
        this.recordsProcessed = recordsProcessed;
    }

    @Override
    public long getRecordsFailed() {
        return recordsFailed;
    }

    @Override
    public void setRecordsFailed(long recordsFailed) {
        this.recordsFailed = recordsFailed;
    }

    @Override
    public long getRecordsCreated() {
        return recordsCreated;
    }

    @Override
    public void setRecordsCreated(long recordsCreated) {
        this.recordsCreated = recordsCreated;
    }

    @Override
    public long getRecordsUpdated() {
        return recordsUpdated;
    }

    @Override
    public void setRecordsUpdated(long recordsUpdated) {
        this.recordsUpdated = recordsUpdated;
    }

    @Override
    public long getRecordsDeleted() {
        return recordsDeleted;
    }

    @Override
    public void setRecordsDeleted(long recordsDeleted) {
        this.recordsDeleted = recordsDeleted;
    }

    @Override
    public void setParameters(Map<String, List<String>> parameters) {
        this.parameters = parameters;
    }

    @Override
    public Map<String, List<String>> getParameters() {
        return parameters;
    }

    @Override
    public void execute() {
    }

    @Override
    public CloudStatistics monitor(String datasetId) {
        return null;
    }

}
