package eu.europeana.metis.core.workflow;

import java.util.List;
import java.util.Map;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;

/**
 * Created by ymamakis on 11/15/16.
 */
@Entity
public class VoidMetisPlugin implements AbstractMetisPlugin {
    private PluginStatus pluginStatus;
    private final PluginType pluginType = PluginType.VOID;
    private int requestedOrder;
    @Transient
    private long sleepMillis = 10000;

    private long recordsProcessed;
    private long recordsFailed;
    private long recordsCreated;
    private long recordsUpdated;
    private long recordsDeleted;

    public VoidMetisPlugin() {
    }

    public VoidMetisPlugin(long sleepMillis){
        this.sleepMillis = sleepMillis;
    }

    @Override
    public PluginStatus getPluginStatus() {
        return pluginStatus;
    }

    public PluginType getPluginType() {
        return pluginType;
    }

    @Override
    public int getRequestedOrder() {
        return requestedOrder;
    }

    @Override
    public void setRequestedOrder(int requestedOrder) {
        this.requestedOrder = requestedOrder;
    }

    @Override
    public long getRecordsProcessed() {
        return 0;
    }

    @Override
    public void setRecordsProcessed() {

    }

    @Override
    public long getRecordsFailed() {
        return 0;
    }

    @Override
    public void setRecordsFailed() {

    }

    @Override
    public long getRecordsUpdated() {
        return 0;
    }

    @Override
    public void setRecordsUpdated() {

    }

    @Override
    public long getRecordsCreated() {
        return 0;
    }

    @Override
    public void setRecordsCreated() {

    }

    @Override
    public long getRecordsDeleted() {
        return 0;
    }

    @Override
    public void setRecordsDeleted() {

    }


    public void setPluginStatus(PluginStatus pluginStatus) {
        this.pluginStatus = pluginStatus;
    }

    @Override
    public void setParameters(Map<String, List<String>> parameters) {

    }

    @Override
    public Map<String, List<String>> getParameters() {
        return null;
    }

    @Override
    public void execute() {

        try {
            Thread.sleep(sleepMillis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public CloudStatistics monitor(String datasetId) {
        return null;
    }

}
