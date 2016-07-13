package eu.europeana.metis.framework.crm;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * REpresentation of a row of results in Zoho
 * Created by ymamakis on 2/23/16.
 */

public class Row {

    /**
     * The row number
     */
    @JsonProperty(value = "no")
    private String rowNum;
    /**
     * The list of fields for each row
     */
    @JsonProperty(value="FL")
    private List<Field> fields;


    public String getRowNum() {
        return rowNum;
    }

    public void setRowNum(String rowNum) {
        this.rowNum = rowNum;
    }


    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }
}
