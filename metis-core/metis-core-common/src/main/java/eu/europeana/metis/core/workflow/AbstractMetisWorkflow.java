package eu.europeana.metis.core.workflow;

import org.springframework.plugin.core.Plugin;

import java.util.List;
import java.util.Map;

/**
 * This interface specifies the minimum o plugin should support so that it can be plugged in the Metis workflow registry
 * and can be accessible via the REST API of Metis
 * Created by ymamakis on 11/9/16.
 */
public interface AbstractMetisWorkflow extends Plugin<String> {

    /**
     * The name of the workflow. Important as this is used to select the workflow
     * @return The name of the workflow
     */
    String getName();

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
     * The business logic that the Workflow implements. This is where the connection to the Europeana Cloud DPS REST API
     * is implemented.
     */
    void execute();

    CloudStatistics monitor(String dataseId);

}
