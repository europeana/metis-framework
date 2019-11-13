package eu.europeana.metis.core.dataset;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

/**
 * Dataset search model that contains all the required fields for Dataset Search functionality.
 * @author Srishti Singh (srishti.singh@europeana.eu)
 * @since 2019-11-12
 */
public class DatasetSearch {

    private String datasetId;
    private String providerName;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private Date lastExecutionDate;

    public String getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public Date getLastExecutionDate() {
        return lastExecutionDate;
    }

    public void setLastExecutionDate(Date lastExecutionDate) {
        this.lastExecutionDate = lastExecutionDate == null ? null : new Date(lastExecutionDate.getTime());
    }
}
