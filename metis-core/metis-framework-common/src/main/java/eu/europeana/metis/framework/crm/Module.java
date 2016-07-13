package eu.europeana.metis.framework.crm;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * A module wrapper
 * Created by ymamakis on 2/23/16.
 */

public class Module {
    /**
     * List of results
     */
    @JsonProperty(value= "row")
    private List<Row> rows;


    public List<Row> getRows() {
        return rows;
    }

    public void setRows(List<Row> rows) {
        this.rows = rows;
    }
}
