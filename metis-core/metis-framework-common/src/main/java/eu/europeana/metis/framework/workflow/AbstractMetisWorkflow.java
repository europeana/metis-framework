package eu.europeana.metis.framework.workflow;

import java.util.Map;

/**
 * Created by ymamakis on 11/9/16.
 */
public interface AbstractMetisWorkflow {

    void process();

    ExecutionStatistics progress();

    boolean isActive();

    boolean isFinished();

    void setParameters();

    Map<String, String> getParameters();
}
