package eu.europeana.metis.core.workflow;

import java.util.List;
import java.util.Map;
import org.mongodb.morphia.annotations.Entity;

/**
 * This interface specifies the minimum o plugin should support so that it can be plugged in the Metis workflow registry
 * and can be accessible via the REST API of Metis
 * Created by ymamakis on 11/9/16.
 */
//@JsonTypeInfo(use = Id.CLASS,
//    include = JsonTypeInfo.As.PROPERTY,
//    property = "type")
//@JsonSubTypes({
//    @Type(value = VoidMetisPlugin.class),
//    @Type(value = VoidOaipmhHarvestPlugin.class),
//    @Type(value = VoidHTTPHarvestPlugin.class),
//    @Type(value = VoidDereferencePlugin.class)
//})
@Entity
public interface AbstractMetisPlugin{

    PluginStatus getPluginStatus();

    int getRequestedOrder();

    void setRequestedOrder(int requestedOrder);

    long getRecordsProcessed();

    void setRecordsProcessed();

    long getRecordsFailed();

    void setRecordsFailed();

    long getRecordsUpdated();

    void setRecordsUpdated();

    long getRecordsCreated();

    void setRecordsCreated();

    long getRecordsDeleted();

    void setRecordsDeleted();

    void setPluginStatus(PluginStatus pluginStatus);

    /**
     * The parameters of the workflow
     * @param parameters The parameters of the workflow
     */
    void setParameters(Map<String,List<String>> parameters);

    /**
     * Set the parameters of the workflow
     * @return The parameters of the workflow
     */
    Map<String, List<String>> getParameters();

    /**
     * The business logic that the UserWorkflow implements. This is where the connection to the Europeana Cloud DPS REST API
     * is implemented.
     */
    void execute();

    CloudStatistics monitor(String dataseId);

}
