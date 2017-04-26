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

package eu.europeana.metis.framework.crm;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A field representation in Zoho. Each field has a val (the name of the field) and content (the value of the field)
 * Created by ymamakis on 2/23/16.
 */
public class Field {

    /**
     * The name of the field
     */
    @JsonProperty(value = "val")
    private String val;
    /**
     * The value of the field
     */
    @JsonProperty(value = "content")
    private String content;


    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }


    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
