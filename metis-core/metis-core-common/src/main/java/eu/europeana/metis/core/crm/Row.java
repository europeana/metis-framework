/*
 * Copyright 2007-2013 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */

package eu.europeana.metis.core.crm;

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
