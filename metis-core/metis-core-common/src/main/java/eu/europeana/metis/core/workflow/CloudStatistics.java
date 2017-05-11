package eu.europeana.metis.core.workflow;

import java.util.List;

/**
 * Cloud statistics representation
 * Empty for now will be filled in with the integration
 * Created by ymamakis on 11/18/16.
 */
public interface CloudStatistics {
    Long getDeleted();

    Long getProcessed();

    Long getUpdated();

    Long getCreated();

    Long getFailed();

    List<String> getFailedRecords();
}
